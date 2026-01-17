package com.aewireless.mixin;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.OpenGuideButton;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.menu.AEBaseMenu;
import com.aewireless.gui.weights.RenderOpenGuideButton;
import com.aewireless.gui.wireless.WirelessScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(AEBaseScreen.class)
public abstract class AEBaseScreenMixin<T extends AEBaseMenu> extends AbstractContainerScreen<T> {

    @Shadow protected abstract void openHelp();

    @Shadow @Final private VerticalButtonBar verticalToolbar;

    public AEBaseScreenMixin(T arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }
    @Inject(at = @At("HEAD"), method = "addToLeftToolbar" , cancellable = true ,remap = false)
    public  <B extends Button> void add(B button, CallbackInfoReturnable<B> cir){
        if ((AEBaseScreen)(Object)this instanceof WirelessScreen && button instanceof OpenGuideButton){
            OpenGuideButton button1 = new RenderOpenGuideButton((s)->openHelp());
            verticalToolbar.add(button1);
            try {
                Class<? extends VerticalButtonBar> aClass = verticalToolbar.getClass();

                Method isRenderingBackground = aClass.getMethod("isRenderingBackground" ,boolean.class);

                isRenderingBackground.invoke(verticalToolbar,false);


            }catch (Exception e){
                throw new RuntimeException("rendering background false failed");
            }
            cir.setReturnValue((B) button1);
        }
    }



}
