package smsk.smoothscroll.mixin.Chat;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHud.Backend;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

/*
 * Priority
 * >1000: bedrockify needs to move the matrix translate first, so i can smooth it out
 */
@Mixin(value = ChatHud.class, priority = 1001) // i want mods to modify the chat position before, so i get to know where they put it
public class ChatHudMixin {

    @Shadow private int scrolledLines;
    @Final @Shadow private List<ChatHudLine.Visible> visibleMessages;

    @Unique private float smoothScrollPos = scrolledLines;
    @Unique private float targetScrollPos = scrolledLines;
    @Unique private float smoothMaskHeight = 0;
    @Unique private float targetMaskHeight = 0;
    @Unique private Vector2f smoothMtxTrans = null;
    @Unique private boolean refreshing = false;
    @Unique private static ChatHudMixin lastThis;

    @WrapMethod(method = "render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V")
    private void renderWrap(Backend drawer, int windowHeight, int currentTick, boolean expanded, Operation<Void> operation) {
        lastThis = this;
        smoothScrollPos = (smoothScrollPos - targetScrollPos) * (float) Math.pow(SmScCfg.chatSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        // snap on less than half a pixel difference
        if (Math.abs(smoothScrollPos - targetScrollPos) < 1f / getLineHeight() / 2)
            smoothScrollPos = targetScrollPos;
        
        scrolledLines = (int) Math.floor(smoothScrollPos);
        
        // mask height
        var shownLineCount = 0;
        for(int r = 0; r + scrolledLines < visibleMessages.size() && r < getVisibleLineCount(); r++) {
            if (currentTick - visibleMessages.get(r).addedTime() < 200 || expanded) shownLineCount++;
        }

        targetMaskHeight = shownLineCount * getLineHeight();
        smoothMaskHeight = (smoothMaskHeight - targetMaskHeight) * (float) Math.pow(SmScCfg.chatOpeningSmoothness, SmoothSc.getLastFrameDuration()) + targetMaskHeight;

        operation.call(drawer, windowHeight, currentTick, expanded);
    }

    //lambda$render$0 (Consumer<Matrix3x2f>)
    @WrapOperation(method = "method_75801", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2f;translate(FF)Lorg/joml/Matrix3x2f;"), remap = false)
    private static Matrix3x2f matrixTranslateWrap(Matrix3x2f matrix, float x, float y, Operation<Matrix3x2f> operation) {
        var targetVec = new Vector2f(x, y);
        if (lastThis.smoothMtxTrans == null) {
            lastThis.smoothMtxTrans = targetVec;
        } else {
            lastThis.smoothMtxTrans = SmoothSc.vec2fAdd(SmoothSc.vec2fMul(SmoothSc.vec2fSub(lastThis.smoothMtxTrans, targetVec), (float) Math.pow(SmScCfg.chatOpeningSmoothness, SmoothSc.getLastFrameDuration())), targetVec);
        }
        return operation.call(matrix, (float) Math.round(lastThis.smoothMtxTrans.x()), (float) Math.round(lastThis.smoothMtxTrans.y()));
    }
    
    @WrapOperation(method = "render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;forEachVisibleLine(Lnet/minecraft/client/gui/hud/ChatHud$OpacityRule;Lnet/minecraft/client/gui/hud/ChatHud$LineConsumer;)I"))
    private int forVisibleLineWrap(ChatHud ch, @Coerce Object opacityRule, @Coerce Object consumer, Operation<Integer> operation, @Local(argsOnly = true) Backend drawer, @Local(ordinal = 4) int chatYPos) {
        var transformationAccess = new TransformationAccess(drawer);
        enMask(transformationAccess, chatYPos);
        drawer.updatePose((pose) -> {
            pose.translate(0, (int) Math.floor(getDrawOffset()));
        });
        //context.getMatrices().pushMatrix();
        //context.getMatrices().translate(0, (int) Math.floor(getDrawOffset()));

        int ret = operation.call(ch, opacityRule, consumer);

        //context.getMatrices().popMatrix();
        drawer.updatePose((pose) -> {
            pose.translate(0, -(int) Math.floor(getDrawOffset()));
        });
        deMask(transformationAccess);
        return ret;
    }
    
    @Unique
    private void enMask(TransformationAccess transformationAccess, int chatYPos) {
        int maskTop = (int) Math.round(chatYPos - smoothMaskHeight);
        int maskBottom = chatYPos;

        // this lets underlined text, diacritics and stuff overflow two pixels above or under chat
        if (smoothScrollPos == targetScrollPos && Math.round(getDrawOffset()) == 0 && Math.round(smoothMaskHeight) != 0) {
            if (Math.round(smoothMaskHeight) == targetMaskHeight) {
                maskTop -= 2;
            }
            maskBottom += 2;
        }

        // this only affects text and the other only affects everything else... wtf mojank?
        var a = transformationAccess.getTransformation();
        if (a != null) {
            transformationAccess.setTransformation(a.withScissor(-10, getWidth() + 999999, maskTop, maskBottom));
        }

        var context = transformationAccess.getContext();
        if (context != null) {
            //if (SmScCfg.enableMaskDebug)
                //context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 255, 255, 0));
            context.enableScissor(-10, maskTop, getWidth() + 999999, maskBottom);
        }
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
    
    @WrapMethod(method = "scroll")
    private void scrollWrap(int amount, Operation<Void> operation) {
        // target + mousescrollamount * lineamount
        var newTarget = targetScrollPos + (amount / 7f) * (SmScCfg.chatAmount != 0 ? SmScCfg.chatAmount / getLineHeight() : 7);
        scrolledLines = (int) Math.ceil(newTarget);


        // we'll only use the clamp from the call
        operation.call(0);

        if (newTarget > scrolledLines) {
            newTarget = scrolledLines;
        }
        if (newTarget < 0) {
            newTarget = 0;
        }

        targetScrollPos = (float) newTarget;
    }
    
    @ModifyVariable(method = "forEachVisibleLine", at = @At(value = "STORE"), ordinal = 0)
    private float opacity(float p) {
        if (SmScCfg.chatOpeningSmoothness == 0) return p;
        return 1;
    }

    @ModifyVariable(method = "addVisibleMessage", at = @At("STORE"), ordinal = 0)
    private List<OrderedText> onNewMessage(List<OrderedText> ot) {
        if (refreshing) return ot;
        smoothScrollPos += ot.size();
        return ot;
    }

    @ModifyVariable(method = "forEachVisibleLine", at = @At("STORE"), ordinal = 0)
    private int addLinesAbove(int i) {
        return (int) Math.ceil(Math.round(smoothMaskHeight) / (float) getLineHeight()) + (Math.round(getDrawOffset()) == 0 ? 0 : 1);
    }
    
    @WrapMethod(method = "resetScroll")
    private void resetScrollWrap(Operation<Void> operation) {
        operation.call();
        targetScrollPos = scrolledLines;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V", at = @At(value = "STORE"), ordinal = 9)
    private int scrollbarVisibleLines(int p) {
        return p - (Math.round(getDrawOffset()) == 0 ? 0 : 1);
    }

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V" ,at = @At(value = "STORE"), ordinal = 12)
    private int scrollbarSmooth(int scrollbarY, @Local(ordinal = 2) int j, @Local(ordinal = 4) int m, @Local(ordinal = 11) int t) {
        return (int) (Math.round(smoothScrollPos) * t / j - m);
    }

    public class TransformationAccess {
        HudAccessor h;
        InteractableAccessor i;
        ForwarderAccessor f;
        TransformationAccess(Object obj) {
            if (obj instanceof InteractableAccessor cast) {
                i = cast;
                return;
            }
            if (obj instanceof HudAccessor cast) {
                h = cast;
                return;
            }
            if (obj instanceof ForwarderAccessor cast) {
                f = cast;
                return;
            }
        }
        public DrawnTextConsumer.Transformation getTransformation() {
            if (i != null)
                return i.getTransformation();
            if (h != null)
                return h.getTransformation();
            if (f != null)
                return f.getDrawer().getTransformation();
            return null;
        }
        public void setTransformation(DrawnTextConsumer.Transformation transformation) {
            if (i != null) {
                i.setTransformation(transformation);
                return;
            }
            if (h != null){
                h.setTransformation(transformation);
                return;
            }
            if (f != null){
                f.getDrawer().setTransformation(transformation);
                return;
            }
        }
        public DrawContext getContext() {
            if (i != null)
                return i.getContext();
            if (h != null)
                return h.getContext();
            return null;
        }
    }

    @Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$Hud")
    public interface HudAccessor {
        @Accessor("transformation")
        DrawnTextConsumer.Transformation getTransformation();
        @Accessor("transformation")
        void setTransformation(DrawnTextConsumer.Transformation transformation);
        @Accessor("context")
        DrawContext getContext();
    }

    @Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$Interactable")
    public interface InteractableAccessor {
        @Accessor("transformation")
        DrawnTextConsumer.Transformation getTransformation();
        @Accessor("transformation")
        void setTransformation(DrawnTextConsumer.Transformation transformation);
        @Accessor("context")
        DrawContext getContext();
    }

    @Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$Forwarder")
    public interface ForwarderAccessor {
        @Accessor("drawer")
        DrawnTextConsumer getDrawer();
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void refreshH(CallbackInfo ci) {refreshing = true;}

    @Inject(method = "refresh", at = @At("TAIL"))
    private void refreshT(CallbackInfo ci) {refreshing = false;}
    @Shadow
    private int getLineHeight() {return 0;}

    @Shadow
    private double getChatScale() {return 0;}

    @Shadow
    private int getWidth() {return 0;}
    
    @Shadow
    public int getVisibleLineCount() {return 0;}

    @Shadow
    private boolean isChatHidden() {return false;}

    @Shadow
    public boolean isChatFocused() {return false;}

    @Unique
    private float getDrawOffset() {
        return -(scrolledLines - smoothScrollPos) * getLineHeight();
    }
}
