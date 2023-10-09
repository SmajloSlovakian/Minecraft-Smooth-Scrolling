package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dzwdz.chat_heads.ChatHeads;
import smsk.smoothscroll.SmoothSc;

@Pseudo
@Mixin(ChatHeads.class)
public class ChatHeadsMixin {
    @ModifyVariable(method = "renderChatHead",at = @At("HEAD"),ordinal = 1,remap = false)
    private static int changeChatHeadsY(int y){
        return(y-SmoothSc.chatOffsetY+(SmoothSc.chatOffsetY/((ChatHudAccessor)SmoothSc.mc.inGameHud.getChatHud()).lineHeight()*((ChatHudAccessor)SmoothSc.mc.inGameHud.getChatHud()).lineHeight()));
    }
}
