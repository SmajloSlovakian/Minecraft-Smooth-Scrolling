package smsk.smoothscroll.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.OrderedText;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(ChatHud.class)
public class ChatHudMixin{
    @Shadow int scrolledLines;
    int scrolledLinesA;

    @Inject(method="scroll",at=@At("HEAD"))
    public void scrollH(int scroll, CallbackInfo ci){
        scrolledLinesA=scrolledLines;
    }
    @Inject(method="scroll",at=@At("TAIL"))
    public void scrollT(int scroll, CallbackInfo ci){
        SmoothSc.chatOffsetY+=(scrolledLines-scrolledLinesA)*9;
    }

    @Inject(method = "render",at=@At("HEAD"))
    public void renderH(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci){
        if(Config.cfg.chatSpeed==0)return;
		MinecraftClient mc=MinecraftClient.getInstance();
        SmoothSc.chatOffsetY*=Math.pow(Config.cfg.chatSpeed,mc.getLastFrameDuration());
        scrolledLinesA=scrolledLines;
        scrolledLines-=SmoothSc.chatOffsetY/getLineHeight();
        if(scrolledLines<0)scrolledLines=0;
    }
    @Inject(method = "render",at=@At("TAIL"))
    public void renderT(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci){
        if(Config.cfg.chatSpeed==0)return;
        scrolledLines=scrolledLinesA;
    }

    @ModifyArg(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I",ordinal=0),index=3)
    private int changeTextPosY(int y){
        if(Config.cfg.chatSpeed==0)return(y);
        return(y-SmoothSc.chatOffsetY+(SmoothSc.chatOffsetY/getLineHeight()*getLineHeight()));
    }

    @ModifyVariable(method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",at=@At("STORE"),ordinal = 0)
    List<OrderedText> onNewMessage(List<OrderedText> ot){
        SmoothSc.chatOffsetY-=ot.size()*getLineHeight();
        return(ot);
    }

    @Shadow private int getLineHeight(){return(0);}
}
