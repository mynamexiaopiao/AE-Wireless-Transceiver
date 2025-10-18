package com.aewireless.gui;

import com.aewireless.AeWireless;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderButton extends Button {
    public RenderButton(int i, int j, int k, int l, Component arg, OnPress arg2, CreateNarration arg3) {
        super(i, j, k, l, arg, arg2, arg3);
    }

    @SuppressWarnings("all")
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(AeWireless.MOD_ID,"textures/widgets.png");

    @Override
    protected void renderWidget(GuiGraphics arg, int j, int k, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        arg.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        arg.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 4, 4, 200, 14, 0, getTextureY());
        arg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.getFGColor();
        this.renderString(arg, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    private int getTextureY() {
        int i = 0;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 1;
        }

        return i *14;
    }
}
