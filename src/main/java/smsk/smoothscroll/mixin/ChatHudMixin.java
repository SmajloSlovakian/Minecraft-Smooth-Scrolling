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
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(ChatHud.class)
public class ChatHudMixin{
    @Shadow int scrolledLines;
    @Shadow private List<ChatHudLine.Visible> visibleMessages;
    int scrolledLinesA;
    DrawContext currContext;

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
        currContext=context;
		MinecraftClient mc=MinecraftClient.getInstance();
        SmoothSc.chatOffsetY*=Math.pow(Config.cfg.chatSpeed,mc.getLastFrameDuration());
        scrolledLinesA=scrolledLines;
        scrolledLines-=SmoothSc.chatOffsetY/getLineHeight();
        if(scrolledLines<0)scrolledLines=0;

        if(this.isChatHidden()||visibleMessages.size()<=0)return;
        int o = this.getLineHeight();
        float f = (float)this.getChatScale();
        int l = context.getScaledWindowHeight();
        int m = MathHelper.floor((float)(l - 40) / f);
        int minx = m;
        int maxx = m - (getVisibleLineCount()-1) * o;
        context.enableScissor(0, maxx - o, context.getScaledWindowWidth(), minx);
        //context.fill(0, 0, 9999, 9999, ColorHelper.Argb.getArgb(50, 255, 0, 255));
    }
    @ModifyVariable(method = "render",at = @At("STORE"))
    private long renderMid0(long a){
        if(Config.cfg.chatSpeed==0||this.isChatHidden())return(a);
        currContext.disableScissor();
        return(a);
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
    /*@ModifyArgs(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",ordinal = 0))
    private void changeBackgroundY(Args ar){
        if(Config.cfg.chatSpeed==0)return;
        int a=ar.get(1);
        int b=ar.get(3);
        ar.set(1, a-SmoothSc.chatOffsetY+(SmoothSc.chatOffsetY/getLineHeight()*getLineHeight()));
        ar.set(3, b-SmoothSc.chatOffsetY+(SmoothSc.chatOffsetY/getLineHeight()*getLineHeight()));
    }/* */

    @ModifyVariable(method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",at=@At("STORE"),ordinal = 0)
    List<OrderedText> onNewMessage(List<OrderedText> ot){
        SmoothSc.chatOffsetY-=ot.size()*getLineHeight();
        return(ot);
    }

    @Shadow private int getLineHeight(){return(0);}
    @Shadow private double getChatScale(){return(0);}
    @Shadow private int getWidth(){return(0);}
    @Shadow private int getVisibleLineCount(){return(0);}
    @Shadow private boolean isChatHidden(){return(false);}
}
