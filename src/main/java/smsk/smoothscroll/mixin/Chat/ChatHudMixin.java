package smsk.smoothscroll.mixin.Chat;

import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(value = ChatHud.class, priority = 1001) // i want mods to modify the chat position before, so i get to know where they put it
public class ChatHudMixin {

    @Shadow private int scrolledLines;
    @Final @Shadow private List<ChatHudLine.Visible> visibleMessages;

    @Unique private float scrollOffset;
    @Unique private float maskHeightBuffer;
    @Unique private boolean refreshing = false;
    @Unique private int scrollValBefore;
    @Unique private int savedCurrentTick;
    @Unique private Vec2f mtc = new Vec2f(0, 0); // matrix translate
    @Unique private int shownLineCount;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderH(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if (Config.cfg.chatSpeed == 0) return;
        savedCurrentTick = currentTick;

        scrollOffset = (float) (scrollOffset * Math.pow(Config.cfg.chatSpeed, SmoothSc.getLastFrameDuration()));

        scrollValBefore = scrolledLines;
        scrolledLines -= getChatScrollOffset() / getLineHeight();
        if (scrolledLines < 0) scrolledLines = 0;
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void matrixTranslateCorrector(Args args) {
        int x = (int) (float) args.get(0) - 4;
        int y = (int) (float) args.get(1);

        var newY = (float) ((mtc.y - y) * Math.pow(Config.cfg.chatOpeningSpeed, SmoothSc.getLastFrameDuration()) + y);

        args.set(1, (float) Math.round(newY));
        mtc = new Vec2f(x, newY);
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 7)
    private int mask(int m, @Local(argsOnly = true) DrawContext context, @Local float f) { // m - the y position of the chat
        if ((Config.cfg.chatSpeed == 0 && Config.cfg.chatOpeningSpeed == 0) || isChatHidden()) return (m);

        var shownLineCount = 0;
        //SmoothSc.print("1: "+visibleMessages.size());
        for(int r = 0; r + scrolledLines < visibleMessages.size() && r < getVisibleLineCount(); r++) {
            //SmoothSc.print("2: "+(savedCurrentTick - visibleMessages.get(r).addedTime()));
            if (savedCurrentTick - visibleMessages.get(r).addedTime() < 200 || isChatFocused()) shownLineCount++;
        }
        // var targetHeight = getVisibleLineCount() * getLineHeight();
        var targetHeight = shownLineCount * getLineHeight();

        // mask doesn't really match the smooth scrolling with the first few messages
        // i really don't know the root cause currently
        // cause: targetHeight matches the interpolated scroll value for some reason
        //     when the shownlinecount is equal to the size of visible messages
        //     so the mask falls behind sometimes
        // not working great workaround:
        // if (shownLineCount == visibleMessages.size()) maskHeightBuffer = targetHeight;
        // else {
        maskHeightBuffer = (float) ((maskHeightBuffer - targetHeight) * Math.pow(Config.cfg.chatOpeningSpeed, SmoothSc.getLastFrameDuration()) + targetHeight);

        var masktop = m - Math.round(maskHeightBuffer) + (int) mtc.y;
        var maskbottom = m + (int) mtc.y;

        // this makes underlined text and such correct again
        if (getChatScrollOffset() == 0 && Math.round(maskHeightBuffer) != 0) {
            if (Math.round(maskHeightBuffer) == targetHeight) {
                maskbottom += 2;
                masktop -= 2;
            } else {
                maskbottom += 2;
            }
        }
        if (FabricLoader.getInstance().getObjectShare().get("raised:chat") instanceof Integer distance) {
            masktop -= distance;
            maskbottom -= distance;
        }

        SmoothSc.scissorScaleFactor = SmoothSc.mc.getWindow().getScaleFactor() * f;
        context.enableScissor(0, masktop, context.getScaledWindowWidth(), maskbottom);
        SmoothSc.scissorScaleFactor = 0;

        return (m);
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 14)
    private int opacity(int t) {
        if (Config.cfg.chatOpeningSpeed == 0) return (t);
        return (0);
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 18)
    private int changePosY(int y) {
        if (Config.cfg.chatSpeed == 0) return (y);
        return (y - getChatDrawOffset());
    }

    @ModifyVariable(method = "render", at = @At("STORE"))
    private long demask(long a, @Local(argsOnly = true) DrawContext context) { // after the cycle
        if ((Config.cfg.chatSpeed == 0 && Config.cfg.chatOpeningSpeed == 0) || this.isChatHidden()) return (a);
        if (Config.cfg.enableMaskDebug) context.fill(-10000, -10000, 10000, 10000, ColorHelper.Argb.getArgb(50, 255, 0, 255));
        context.disableScissor();
        return (a);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderT(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if (Config.cfg.chatSpeed == 0) return;
        scrolledLines = scrollValBefore;
    }

    @ModifyVariable(method = "addVisibleMessage", at = @At("STORE"), ordinal = 0)
    private List<OrderedText> onNewMessage(List<OrderedText> ot) {
        if (refreshing) return (ot);
        scrollOffset -= ot.size() * getLineHeight();
        return (ot);
    }

    @Inject(method = "scroll", at = @At("HEAD"))
    private void scrollH(int scroll, CallbackInfo ci) {
        scrollValBefore = scrolledLines;
    }

    @Inject(method = "scroll", at = @At("TAIL"))
    private void scrollT(int scroll, CallbackInfo ci) {
        scrollOffset += (scrolledLines - scrollValBefore) * getLineHeight();
    }

    @Inject(method = "resetScroll", at = @At("HEAD"))
    private void scrollResetH(CallbackInfo ci) {
        scrollValBefore = scrolledLines;
    }

    @Inject(method = "resetScroll", at = @At("TAIL"))
    private void scrollResetT(CallbackInfo ci) {
        scrollOffset += (scrolledLines - scrollValBefore) * getLineHeight();
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;getLineHeight()I"), ordinal = 3)
    private int addLinesAbove(int i) {
        if (Config.cfg.chatSpeed == 0 && Config.cfg.chatOpeningSpeed == 0) return (i);
        return ((int) Math.ceil(Math.round(maskHeightBuffer) / (float) getLineHeight()) + (getChatScrollOffset() < 0 ? 1 : 0));
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 12)
    private int addLinesUnder(int r) {
        if (scrolledLines == 0 || Config.cfg.chatSpeed == 0 || getChatScrollOffset() <= 0) return (r);
        return (r - 1);
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void refreshH(CallbackInfo ci) {refreshing = true;}

    @Inject(method = "refresh", at = @At("TAIL"))
    private void refreshT(CallbackInfo ci) {refreshing = false;}

    @Shadow
    private int getLineHeight() {return (0);}

    @Shadow
    public double getChatScale() {return (0);}

    @Shadow
    public int getWidth() {return (0);}
    
    @Shadow
    public int getVisibleLineCount() {return (0);}

    @Shadow
    private boolean isChatHidden() {return (false);}

    @Shadow
    public boolean isChatFocused() {return (false);}

    @Unique
    private int getChatDrawOffset() {
        return Math.round(scrollOffset) - (Math.round(scrollOffset) / getLineHeight() * getLineHeight());
    }

    @Unique
    private int getChatScrollOffset() {
        return Math.round(scrollOffset);
    }
}
