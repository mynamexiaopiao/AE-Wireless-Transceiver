package com.aewireless.mixin;


import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AETextField.class)
public class AETextFieldMixin {
    @Mutable
    @Shadow  private static Blitter BLITTER;
    boolean isNew = false;


    @Inject(method = "renderWidget" , at = @At("HEAD"))
    private void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        if (isNew){
            BLITTER = Blitter.texture("guis/new_text.png",128,128);

        }
    }


    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
