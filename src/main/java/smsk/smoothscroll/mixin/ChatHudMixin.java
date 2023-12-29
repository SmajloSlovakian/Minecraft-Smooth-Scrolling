package smsk.smoothscroll.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(ChatHud.class)
public class ChatHudMixin{
    @Shadow int scrolledLines;
    @Shadow private List<ChatHudLine.Visible> visibleMessages;
    boolean refreshing=false;
    int scrolledLinesA;
    DrawContext currContext;

    @Inject(method="scroll",at=@At("HEAD"))
    public void scrollH(int scroll, CallbackInfo ci){
        scrolledLinesA=scrolledLines;
    }
    @Inject(method="scroll",at=@At("TAIL"))
    public void scrollT(int scroll, CallbackInfo ci){
        SmoothSc.chatOffsetY+=(scrolledLines-scrolledLinesA)*getLineHeight();
    }
    @Inject(method="resetScroll",at=@At("HEAD"))
    public void scrollResetH(CallbackInfo ci){
        scrolledLinesA=scrolledLines;
    }
    @Inject(method="resetScroll",at=@At("TAIL"))
    public void scrollResetT(CallbackInfo ci){
        SmoothSc.chatOffsetY+=(scrolledLines-scrolledLinesA)*getLineHeight();
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
    }
    @ModifyVariable(method = "render",at = @At("STORE"),ordinal = 7)
    private int mask(int m){
        if(Config.cfg.chatSpeed==0||this.isChatHidden())return(m);
        int miny = m;
        int maxy = m - (getVisibleLineCount()-1) * getLineHeight();
        currContext.enableScissor(0, maxy - getLineHeight(), currContext.getScaledWindowWidth(), miny);
        return(m);
    }
    @ModifyVariable(method = "render",at = @At("STORE"))
    private long demask(long a){
        if(Config.cfg.chatSpeed==0||this.isChatHidden())return(a);
        currContext.disableScissor();
        return(a);
    }
    @Inject(method = "render",at=@At("TAIL"))
    public void renderT(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci){
        if(Config.cfg.chatSpeed==0)return;
        scrolledLines=scrolledLinesA;
    }

    @ModifyVariable(method="render",at=@At(value="STORE"),ordinal=18)
    private int changePosY(int y){
        if(Config.cfg.chatSpeed==0)return(y);
        return(y-SmoothSc.chatOffsetY+(SmoothSc.chatOffsetY/getLineHeight()*getLineHeight()));
    }

    @ModifyVariable(method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",at=@At("STORE"),ordinal = 0)
    List<OrderedText> onNewMessage(List<OrderedText> ot){
        if(refreshing)return(ot);
        SmoothSc.chatOffsetY-=ot.size()*getLineHeight();
        return(ot);
    }

    @ModifyVariable(method="render",at=@At(value="STORE"),ordinal=3)
    private int addLinesAbove(int i){
        if(Config.cfg.chatSpeed==0||SmoothSc.chatOffsetY>=0)return(i);
        return(i+1);
    }
    @ModifyVariable(method="render",at=@At(value="STORE"),ordinal=12)
    private int addLinesUnder(int r){
        if(Config.cfg.chatSpeed==0||SmoothSc.chatOffsetY<=0)return(r);
        return(r-1);
    }

    @Inject(method = "refresh",at = @At("HEAD"))
    private void refreshH(CallbackInfo ci){refreshing=true;}

    @Inject(method = "refresh",at = @At("TAIL"))
    private void refreshT(CallbackInfo ci){refreshing=false;}

    @Shadow private int getLineHeight(){return(0);}
    @Shadow private double getChatScale(){return(0);}
    @Shadow private int getWidth(){return(0);}
    @Shadow private int getVisibleLineCount(){return(0);}
    @Shadow private boolean isChatHidden(){return(false);}
    @Shadow private boolean isChatFocused(){return(false);}
}
