package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerInventory;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(InGameHud.class)
public class HotbarMixin {

	private static float selslotvisual=0;

	@ModifyArg(method="renderHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1),index = 1,require=0) private int selectedSlotX201(int x){return(selectedSlotXcommon(x));}
	@ModifyArg(method="renderHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",ordinal = 1),index = 1,require=0) private int selectedSlotX202(int x){return(selectedSlotXcommon(x));}
	private int selectedSlotXcommon(int x){
		if(Config.cfg.hotbarSpeed==0)return(x);
		MinecraftClient mc=MinecraftClient.getInstance();
		PlayerInventory inv=mc.player.getInventory();

		selslotvisual=(float)((selslotvisual-(inv.selectedSlot-SmoothSc.hotbarRollover*9))*Math.pow(Config.cfg.hotbarSpeed,mc.getLastFrameDuration())+(inv.selectedSlot-SmoothSc.hotbarRollover*9));
		if(selslotvisual*20<-10){
			selslotvisual+=9;
			SmoothSc.hotbarRollover-=1;
		}else if(selslotvisual*20>20*9-10){
			selslotvisual-=9;
			SmoothSc.hotbarRollover+=1;
		}

		if(inv.selectedSlot+0.05>selslotvisual && inv.selectedSlot<selslotvisual) selslotvisual=inv.selectedSlot;
		if(inv.selectedSlot-0.05<selslotvisual && inv.selectedSlot>selslotvisual) selslotvisual=inv.selectedSlot;

		x-=inv.selectedSlot*20;
		x+=selslotvisual*20;
		return(x);
	}
}
