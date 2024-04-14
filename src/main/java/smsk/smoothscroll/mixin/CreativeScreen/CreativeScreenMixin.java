package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(CreativeInventoryScreen.class)
public class CreativeScreenMixin {

    @Shadow
    private static ItemGroup selectedTab;

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    void setSelectedTabT(ItemGroup group, CallbackInfo ci) {
        SmoothSc.creativeScreenScrollOffset = 0;
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmoothSc.creativeScreenScrollOffset == 0 || Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeSH == null) return;

        int x0 = Math.round(context.getScaledWindowWidth() / 2f) - 90;
        int y0 = context.getScaledWindowHeight() / 2 - 51;
        int x1 = 162;
        int y1 = 90;
        int x2 = 8;
        int y2 = 17;

        // context.drawText(SmoothSc.mc.textRenderer, mouseX + " - " + mouseY, 10, 10, ColorHelper.Argb.getArgb(255, 0, 255, 255), true);
        // context.fill(0, 0, 1920, 1080, ColorHelper.Argb.getArgb(50, 255, 128, 0));
        context.enableScissor(x0, y0 + 1, x0 + x1, y0 + y1 - 1);
        context.drawTexture(new Identifier("textures/gui/container/creative_inventory/tab_" + selectedTab.getTexture()),
                x0, y0 + (SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18), x2, y2, x1, y1);
        context.drawTexture(new Identifier("textures/gui/container/creative_inventory/tab_" + selectedTab.getTexture()),
                x0, (int) (y0 + (SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18) - y1 * Math.signum(SmoothSc.creativeScreenScrollOffset)),
                x2, y2, x1, y1);

        if (Config.cfg.enableMaskDebug)
            SmoothSc.unmodifiedFill(context, -100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 255, 255, 0));
        
        context.disableScissor();
    }
}
