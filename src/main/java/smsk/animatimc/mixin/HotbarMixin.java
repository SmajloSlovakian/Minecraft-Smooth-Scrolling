package smsk.animatimc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerInventory;
import smsk.animatimc.AnimatiMC;

@Mixin(InGameHud.class)
public class HotbarMixin {

	private static float selslotvisual=0;
	private static float speed=5f;

	@ModifyArg(method="renderHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",ordinal = 1),index = 1)
	private int selectedSlotX(int x){
		MinecraftClient mc=MinecraftClient.getInstance();
		PlayerInventory inv=mc.player.getInventory();
		
		AnimatiMC.print(inv.selectedSlot+" "+AnimatiMC.hotbarRollover*9);

		selslotvisual=(float)((selslotvisual-(inv.selectedSlot-AnimatiMC.hotbarRollover*9))/Math.pow(speed,mc.getLastFrameDuration())+(inv.selectedSlot-AnimatiMC.hotbarRollover*9));
		if(selslotvisual*20<-10){
			selslotvisual+=9;
			AnimatiMC.hotbarRollover-=1;
		}else if(selslotvisual*20>20*9-10){
			selslotvisual-=9;
			AnimatiMC.hotbarRollover+=1;
		}
		if(inv.selectedSlot+0.1>selslotvisual && inv.selectedSlot<selslotvisual) selslotvisual=inv.selectedSlot;
		if(inv.selectedSlot-0.1<selslotvisual && inv.selectedSlot>selslotvisual) selslotvisual=inv.selectedSlot;

		x-=inv.selectedSlot*20;
		x+=selslotvisual*20;
		return(x);
	}
}
