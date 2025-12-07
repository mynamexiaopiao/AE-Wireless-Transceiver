package com.aewireless.mixin;

import appeng.client.Point;
import appeng.client.gui.widgets.VerticalButtonBar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VerticalButtonBar.class)
public class VerticalButtonBarMixin {
    private boolean is = true;

    @Inject(method = "drawBackgroundLayer" , at = @At("HEAD"), cancellable = true , remap = false)
    private void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse, CallbackInfo ci) {
        if (!is) ci.cancel();
    }

    @Unique
    public void isRenderingBackground(boolean is) {
        this.is = is;
    }
}
