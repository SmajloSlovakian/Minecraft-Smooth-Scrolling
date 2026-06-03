package io.github.smajloslovakian.smoothscroll.mixin.client.Chat;

import java.util.List;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.ActiveTextCollector.Parameters;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.ChatGraphicsAccess;
import net.minecraft.client.gui.components.ChatComponent.DisplayMode;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.TransformationAccess;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

/*
 * Priority
 * >1000: bedrockify needs to move the chatBottom first, so i can smooth it out
 */
@Mixin(value = ChatComponent.class, priority = 1001)
public class ChatComponentMixin {

    private final String renderMethodSignature = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V";

    @Shadow private int chatScrollbarPos;
    @Final @Shadow private List<GuiMessage.Line> trimmedMessages;

    // everything in lines for better float precision sync
    @Unique private double smoothScrollPos = chatScrollbarPos;
    @Unique private double targetScrollPos = chatScrollbarPos;
    @Unique private double smoothMaskHeight = 0;
    @Unique private double targetMaskHeight = 0;
    @Unique private double smoothChatBottom = -69696969;

    @Unique private boolean refreshing = false;

    @Unique private double epsilon = 1d / 10;

    // this has to be an inject, so that modifyvariable on parameters affects us
    @Inject(method = renderMethodSignature, at = @At("HEAD"))
    private void renderWrap(ChatGraphicsAccess graphics, int screenHeight, int ticks, DisplayMode displayMode, CallbackInfo ci) {
        smoothScrollPos = (smoothScrollPos - targetScrollPos) * Math.pow(SmScCfg.chatSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        // ~~snap on less than half a pixel difference~~
        // snap when there is a low enough scroll offset
        if (Math.abs(smoothScrollPos - targetScrollPos) * getLineHeight() < epsilon)
            smoothScrollPos = targetScrollPos;
        
        chatScrollbarPos = (int) Math.floor(smoothScrollPos);
        
        // mask height
        var shownLineCount = 0;
        for(int r = 0; r < trimmedMessages.size() && r < getLinesPerPage(); r++) {
            if (ticks - trimmedMessages.get(r).addedTime() < 200 || displayMode.foreground) shownLineCount++;
        }
        
        targetMaskHeight = shownLineCount;
        smoothMaskHeight = (smoothMaskHeight - targetMaskHeight) * Math.pow(SmScCfg.chatOpeningSmoothness, SmoothSc.getLastFrameDuration()) + targetMaskHeight;

        if (Math.abs((smoothMaskHeight - targetMaskHeight) * getLineHeight()) < epsilon) {
            smoothMaskHeight = targetMaskHeight;
        }
    }

    @ModifyVariable(method = renderMethodSignature, at = @At("STORE"), name = "chatBottom")
    int modifyYPos(int chatBottom, @Local(argsOnly = true) ChatGraphicsAccess graphics) {
        if (smoothChatBottom == -69696969) {
            smoothChatBottom = chatBottom / (double) getLineHeight();
        }
        smoothChatBottom = (smoothChatBottom - chatBottom / (double) getLineHeight()) * Math.pow(SmScCfg.chatOpeningSmoothness, SmoothSc.getLastFrameDuration()) + chatBottom / (double) getLineHeight();
        //return (int) Math.round(smoothChatBottom * getLineHeight());
        graphics.updatePose((pose) -> {
            pose.translate(0, (float) smoothChatBottom * getLineHeight());
        });
        return 0;
    }
    
    @WrapOperation(method = renderMethodSignature, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(Lnet/minecraft/client/gui/components/ChatComponent$AlphaCalculator;Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"))
    private int forVisibleLineWrap(ChatComponent ch, @Coerce Object alphaCalculator, @Coerce Object lineConsumer, Operation<Integer> operation, @Local(argsOnly = true) ChatGraphicsAccess graphics, @Local(name = "chatBottom") int chatBottom) {
        if (graphics instanceof TransformationAccess transformationAccess) {
            enMask(transformationAccess, chatBottom);
        }
        graphics.updatePose((pose) -> {
            pose.translate(0, (float) getDrawOffset());
        });
        //context.getMatrices().pushMatrix();
        //context.getMatrices().translate(0, (int) Math.floor(getDrawOffset()));

        int ret = operation.call(ch, alphaCalculator, lineConsumer);

        //context.getMatrices().popMatrix();
        graphics.updatePose((pose) -> {
            pose.translate(0, -(float) getDrawOffset());
        });
        if (graphics instanceof TransformationAccess transformationAccess) {
            deMask(transformationAccess);
        }
        return ret;
    }
    
    @Unique
    private void enMask(TransformationAccess transformationAccess, int chatBottom) {
        int maskTop = (int) Math.round((/*smoothChatBottom*/ - smoothMaskHeight) * getLineHeight());
        int maskBottom = 0;//(int) Math.round(smoothChatBottom * getLineHeight());

        // this lets underlined text, diacritics and stuff overflow two pixels above or under chat
        if (smoothScrollPos == targetScrollPos && getDrawOffset() == 0 && smoothMaskHeight != 0) {
            if (Math.abs(smoothMaskHeight - targetMaskHeight) * getLineHeight() < epsilon) {
                maskTop -= 2;
            }
            maskBottom += 2;
        }

        // this only affects text and the other only affects everything else... wtf mojank?
        var a = transformationAccess.getTransformation();
        if (a != null) {
            transformationAccess.setTransformation(a.withScissor(-10, getWidth() + 1000, maskTop, maskBottom));
        }

        var context = transformationAccess.getContext();
        if (context != null) {
            context.enableScissor(-10, maskTop, getWidth() + 1000, maskBottom);
        }
        if (SmScCfg.enableMaskDebug)
            context.fill(-10000, -10000, context.guiWidth(), context.guiHeight(), ARGB.color(50, 255, 255, 0));
    }
    @Unique
    private void deMask(TransformationAccess transformationAccess) {
        var a = transformationAccess.getTransformation();
        if (a != null) {
            transformationAccess.setTransformation(a.withScissor(null));
        }

        var context = transformationAccess.getContext();
        if (context != null) {
            context.disableScissor();
        }

    }
    
    @WrapMethod(method = "scrollChat")
    private void scrollWrap(int dir, Operation<Void> operation) {
        // target + mousescrollamount * lineamount
        var newTarget = targetScrollPos + (dir / 7f) * (SmScCfg.chatAmount != 0 ? SmScCfg.chatAmount / getLineHeight() : 7);
        chatScrollbarPos = (int) Math.ceil(newTarget);


        // we'll only use the clamp from the call
        operation.call(0);

        if (newTarget > chatScrollbarPos) {
            newTarget = chatScrollbarPos;
        }
        if (newTarget < 0) {
            newTarget = 0;
        }

        targetScrollPos = newTarget;
    }
    
    @ModifyVariable(method = "forEachLine", at = @At(value = "STORE"), name = "alpha")
    private float opacity(float alpha) {
        if (SmScCfg.chatOpeningSmoothness == 0) return alpha;
        return 1;
    }

    @ModifyVariable(method = "addMessageToDisplayQueue", at = @At("STORE"), name = "lines")
    private List<FormattedCharSequence> onNewMessage(List<FormattedCharSequence> lines) {
        if (refreshing) return lines;
        smoothScrollPos += lines.size();
        return lines;
    }

    @ModifyVariable(method = "forEachLine", at = @At("STORE"), name = "perPage")
    private int addLinesAbove(int perPage) {
        return (int) Math.ceil(smoothMaskHeight + getDrawOffset() / getLineHeight());// + (getDrawOffset() == 0 ? 0 : 1);
    }
    
    @WrapMethod(method = "resetChatScroll")
    private void resetScrollWrap(Operation<Void> operation) {
        operation.call();
        targetScrollPos = chatScrollbarPos;
    }

    @ModifyVariable(method = renderMethodSignature, at = @At(value = "STORE"), name = "count")
    private int scrollbarVisibleLines(int count) {
        return count - (getDrawOffset() == 0 ? 0 : 1);
    }

    @ModifyVariable(method = renderMethodSignature, at = @At(value = "STORE"), name = "y")
    private int scrollbarSmooth(int y, @Local(name = "total") int total, @Local(name = "chatBottom") int chatBottom, @Local(name = "chatHeight") int chatHeight) {
        return (int) Math.round(smoothScrollPos * chatHeight / total - chatBottom);
    }

    @Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess")
    public static class DrawingBackgroundGraphicsAccessMixin implements TransformationAccess {
        @Shadow @Final GuiGraphicsExtractor graphics;
        @Shadow ActiveTextCollector.Parameters parameters;

        @Override
        public Parameters getTransformation() {
            return parameters;
        }

        @Override
        public void setTransformation(Parameters transformation) {
            parameters = transformation;
        }

        @Override
        public GuiGraphicsExtractor getContext() {
            return graphics;
        }
    }

    @Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess")
    public static class DrawingFocusedGraphicsAccessMixin implements TransformationAccess {
        @Shadow @Final GuiGraphicsExtractor graphics;
        @Shadow ActiveTextCollector.Parameters parameters;
        
        @Override
        public Parameters getTransformation() {
            return parameters;
        }

        @Override
        public void setTransformation(Parameters transformation) {
            parameters = transformation;
        }

        @Override
        public GuiGraphicsExtractor getContext() {
            return graphics;
        }
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("HEAD"))
    private void refreshH(CallbackInfo ci) {refreshing = true;}

    @Inject(method = "refreshTrimmedMessages", at = @At("TAIL"))
    private void refreshT(CallbackInfo ci) {refreshing = false;}
    @Shadow
    private int getLineHeight() {return 0;}

    @Shadow
    private double getScale() {return 0;}

    @Shadow
    private int getWidth() {return 0;}
    
    @Shadow
    public int getLinesPerPage() {return 0;}

    @Shadow
    public boolean isChatFocused() {return false;}

    @Unique
    private double getDrawOffset() {
        return -(chatScrollbarPos - smoothScrollPos) * getLineHeight();
    }
}
