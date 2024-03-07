package smsk.smoothscroll.mixin.Chat;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.font.TextRenderer;
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

    @Shadow int scrolledLines;
    @Shadow List<ChatHudLine.Visible> visibleMessages;

    int scrollOffset;
    int upperMaskBuffer;
    boolean refreshing = false;
    int scrollValBefore;
    float lFDBuffer;
    DrawContext currentContext;
    Vec2f mtc = new Vec2f(0, 0); // matrix translate

    @Inject(method = "scroll", at = @At("HEAD"))
    public void scrollH(int scroll, CallbackInfo ci) {
        scrollValBefore = scrolledLines;
    }

    @Inject(method = "scroll", at = @At("TAIL"))
    public void scrollT(int scroll, CallbackInfo ci) {
        scrollOffset += (scrolledLines - scrollValBefore) * getLineHeight();
    }

    @Inject(method = "resetScroll", at = @At("HEAD"))
    public void scrollResetH(CallbackInfo ci) {
        scrollValBefore = scrolledLines;
    }

    @Inject(method = "resetScroll", at = @At("TAIL"))
    public void scrollResetT(CallbackInfo ci) {
        scrollOffset += (scrolledLines - scrollValBefore) * getLineHeight();
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void renderH(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (Config.cfg.chatSpeed == 0) return;
        currentContext = context;

        lFDBuffer += SmoothSc.mc.getLastFrameDuration();
        var a = scrollOffset;
        scrollOffset = (int) Math.round(((double) scrollOffset) * Math.pow(Config.cfg.chatSpeed, lFDBuffer));
        if (scrollOffset != a || scrollOffset == 0) lFDBuffer = 0;

        scrollValBefore = scrolledLines;
        scrolledLines -= scrollOffset / getLineHeight();
        if (scrolledLines < 0) scrolledLines = 0;
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void matrixTranslateCorrector(Args args) {
        int x = (int) (float) args.get(0) - 4;
        int y = (int) (float) args.get(1);
        y = (int) ((mtc.y - y) * Math.pow(Config.cfg.chatSpeed, SmoothSc.mc.getLastFrameDuration())) + y;
        args.set(1, (float) y);
        mtc = new Vec2f(x, y);
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 7)
    private int mask(int m) { // m - the height of the chat
        if (Config.cfg.chatSpeed == 0 || this.isChatHidden()) return (m);
        int miny = m;
        int maxy = m - (getVisibleLineCount() - 1) * getLineHeight();
        var masktop = maxy - getLineHeight() + (int) mtc.y;
        var maskbottom = miny + (int) mtc.y;

        currentContext.enableScissor(0, masktop, currentContext.getScaledWindowWidth(), maskbottom);
        return (m);
    }

    @ModifyVariable(method = "render", at = @At("STORE"))
    private long demask(long a) {
        if (Config.cfg.chatSpeed == 0 || this.isChatHidden()) return (a);
        if (Config.cfg.enableMaskDebug) currentContext.fill(-100, -100, currentContext.getScaledWindowWidth(), currentContext.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 255, 0, 255));
        currentContext.disableScissor();
        return (a);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderT(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (Config.cfg.chatSpeed == 0) return;
        scrolledLines = scrollValBefore;
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 18)
    private int changePosY(int y) {
        if (Config.cfg.chatSpeed == 0) return (y);
        return (y - scrollOffset + (scrollOffset / getLineHeight() * getLineHeight()));
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("STORE"), ordinal = 0)
    List<OrderedText> onNewMessage(List<OrderedText> ot) {
        if (refreshing) return (ot);
        scrollOffset -= ot.size() * getLineHeight();
        return (ot);
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 3)
    private int addLinesAbove(int i) {
        if (Config.cfg.chatSpeed == 0 || scrollOffset >= 0) return (i);
        return (i + 1);
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 12)
    private int addLinesUnder(int r) {
        if (Config.cfg.chatSpeed == 0 || scrollOffset <= 0) return (r);
        return (r - 1);
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void refreshH(CallbackInfo ci) {refreshing = true;}

    @Inject(method = "refresh", at = @At("TAIL"))
    private void refreshT(CallbackInfo ci) {refreshing = false;}

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I", ordinal = 0))
    private int unmodifiedShadowedText(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        return (SmoothSc.unmodifiedShadowedText(drawContext, textRenderer, text, x, y, color));
    }
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void unmodifiedFill(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        SmoothSc.unmodifiedFill(drawContext, x1, y1, x2, y2, color);
    }

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
    private boolean isChatFocused() {return (false);}
}
