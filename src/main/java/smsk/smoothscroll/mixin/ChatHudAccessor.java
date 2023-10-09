package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.hud.ChatHud;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {
    @Invoker("getLineHeight")
    public int lineHeight();
}
