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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    @Unique private boolean translated = false;
    @Unique private boolean refreshing = false;

    @WrapMethod(method = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V")
    private void renderWrap(Backend drawer, int windowHeight, int currentTick, boolean expanded, Operation<Void> operation) {
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
    
    @WrapOperation(method = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;forEachVisibleLine(Lnet/minecraft/client/gui/hud/ChatHud$OpacityRule;Lnet/minecraft/client/gui/hud/ChatHud$LineConsumer;)I", ordinal = 0))
    private int forVisibleLineWrap(ChatHud ch, @Coerce Object opacityRule, @Coerce Object consumer, Operation<Integer> operation, @Local Backend drawer) {
        //enMask(context, windowHeight);
        drawer.updatePose((pose) -> {
            pose.translate(0, (int) Math.floor(getDrawOffset()));
        });
        //context.getMatrices().pushMatrix();
        //context.getMatrices().translate(0, (int) Math.floor(getDrawOffset()));

        int ret = operation.call(ch, opacityRule, consumer);

        //context.getMatrices().popMatrix();
        //if (SmScCfg.enableMaskDebug)
            //context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 255, 255, 0));
        //context.disableScissor();
        return ret;
    }
    
    @Unique
    private void enMask(DrawContext context, int chatYPos) {
        int maskTop = (int) Math.round(chatYPos - smoothMaskHeight);
        int maskBottom = chatYPos;

        // this lets underlined text, diacritics and stuff overflow two pixels above or under chat
        if (smoothScrollPos == targetScrollPos && Math.round(getDrawOffset()) == 0 && Math.round(smoothMaskHeight) != 0) {
            if (Math.round(smoothMaskHeight) == targetMaskHeight) {
                maskTop -= 2;
            }
            maskBottom += 2;
        }

        SmoothSc.preciseScissor = true;
        context.enableScissor(-10, maskTop, getWidth() + 999999, maskBottom);
        SmoothSc.preciseScissor = false;
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

/*
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", ordinal = 0))
    private Matrix3x2f matrixTranslateWrap(Matrix3x2fStack matrix, float x, float y, Operation<Matrix3x2f> operation) {
        var targetVec = new Vector2f(x, y);
        if (smoothMtxTrans == null) {
            smoothMtxTrans = targetVec;
        } else {
            smoothMtxTrans = SmoothSc.vec2fAdd(SmoothSc.vec2fMul(SmoothSc.vec2fSub(smoothMtxTrans, targetVec), (float) Math.pow(SmScCfg.chatOpeningSmoothness, SmoothSc.getLastFrameDuration())), targetVec);
        }
        return operation.call(matrix, (float) Math.round(smoothMtxTrans.x()), (float) Math.round(smoothMtxTrans.y()));
    }
    

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 3)
    private int addLinesAbove(int i) {
        return (int) Math.ceil(Math.round(smoothMaskHeight) / (float) getLineHeight()) + (Math.round(getDrawOffset()) == 0 ? 0 : 1);
    }

    @WrapMethod(method = "resetScroll")
    private void resetScrollWrap(Operation<Void> operation) {
        operation.call();
        targetScrollPos = scrolledLines;
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 10)
    private int scrollbarVisibleLines(int p) {
        return p - (Math.round(getDrawOffset()) == 0 ? 0 : 1);
    }

    @ModifyVariable(method = "render" ,at = @At(value = "STORE"), ordinal = 14)
    private int scrollbarSmooth(int u, @Local(ordinal = 4) int j, @Local(ordinal = 7) int m, @Local(ordinal = 13) int t) {
        return (int) (Math.round(smoothScrollPos) * t / j - m);
    }
*/


    @Inject(method = "refresh", at = @At("HEAD"))
    private void refreshH(CallbackInfo ci) {refreshing = true;}

    @Inject(method = "refresh", at = @At("TAIL"))
    private void refreshT(CallbackInfo ci) {refreshing = false;}
    @Shadow
    private int getLineHeight() {return 0;}

    @Shadow
    public double getChatScale() {return 0;}

    @Shadow
    public int getWidth() {return 0;}
    
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
