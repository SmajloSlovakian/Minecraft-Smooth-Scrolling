package smsk.smoothscroll.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(value = ChatHud.class, priority = 1001) // i want mods to modify the chat position before, so i get to know where they put it
public class ChatHudMixin{
    @Shadow int scrolledLines;
    @Shadow private List<ChatHudLine.Visible> visibleMessages;
    boolean refreshing=false;
    int scrolledLinesA;
    DrawContext currContext;
    Vec2f mtc=new Vec2f(0, 0); // matrix translate
    int upperMaskBuffer;

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
        SmoothSc.chatOffsetY*=Math.pow(Config.cfg.chatSpeed,SmoothSc.mc.getLastFrameDuration());
        scrolledLinesA=scrolledLines;
        scrolledLines-=SmoothSc.chatOffsetY/getLineHeight();
        if(scrolledLines<0)scrolledLines=0;
    }
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void matrixTranslateCorrector(Args args){
        int x=(int)(float)args.get(0)-4;
        int y=(int)(float)args.get(1);
        y=(int)((mtc.y-y)*Math.pow(Config.cfg.chatSpeed,SmoothSc.mc.getLastFrameDuration()))+y;
        args.set(1, (float)y);
        mtc=new Vec2f(x,y);
    }
    @ModifyVariable(method = "render",at = @At("STORE"),ordinal = 7)
    private int mask(int m){ // m - the height of the chat
        if(Config.cfg.chatSpeed==0||this.isChatHidden())return(m);
        int miny = m;
        int maxy = m - (getVisibleLineCount()-1) * getLineHeight();
        var masktop = maxy - getLineHeight()+(int)mtc.y;
        var maskbottom = miny+(int)mtc.y;

        currContext.enableScissor(0, masktop, currContext.getScaledWindowWidth(), maskbottom);
        return(m);
    }
    @ModifyVariable(method = "render",at = @At("STORE"))
    private long demask(long a){
        if(Config.cfg.chatSpeed==0||this.isChatHidden())return(a);
        if(Config.cfg.enableMaskDebug)currContext.fill(-100,-100,currContext.getScaledWindowWidth(),currContext.getScaledWindowHeight(),ColorHelper.Argb.getArgb(50, 255, 0, 255));
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
