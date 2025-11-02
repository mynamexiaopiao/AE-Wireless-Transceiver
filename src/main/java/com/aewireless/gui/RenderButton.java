package com.aewireless.gui;

import appeng.core.AppEng;
import com.aewireless.AeWireless;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderButton extends Button {
    public RenderButton(int i, int j, int k, int l, Component arg, OnPress arg2, CreateNarration arg3) {
        super(i, j, k, l, arg, arg2, arg3);
    }

    public static final ResourceLocation BUTTON =AeWireless.makeId("button");
    public static final ResourceLocation BUTTON_HIGHLIGHT = AeWireless.makeId("button_highlighted");
    protected static final WidgetSprites SPRITES = new WidgetSprites(BUTTON,BUTTON,BUTTON_HIGHLIGHT);


    @Override
    protected void renderWidget(GuiGraphics arg, int j, int k, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        arg.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        arg.blitSprite(SPRITES.get(this.active, this.isHovered()), this.getX(), this.getY(), this.getWidth(), this.getHeight());


        arg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.getFGColor();
        this.renderString(arg, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

}
