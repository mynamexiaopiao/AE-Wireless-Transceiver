package com.aewireless.gui.weights;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RenderLinkToggleButton extends Button {
    private boolean enabled;

    public RenderLinkToggleButton(int x, int y, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (!this.visible) return;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        WirelessIcon.WIRELESS_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY(), 16, 16).blit(guiGraphics);

        if (enabled) {
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + 17, getY(), 0xFF00AA00);
            guiGraphics.fill(getX() - 1, getY() + 16, getX() + 17, getY() + 17, 0xFF00AA00);
            guiGraphics.fill(getX() - 1, getY(), getX(), getY() + 16, 0xFF00AA00);
            guiGraphics.fill(getX() + 16, getY(), getX() + 17, getY() + 16, 0xFF00AA00);
        }

        WirelessIcon.WIRELESS_LINK_RENDER.getBlitter().dest(getX(), getY(), 16, 16).blit(guiGraphics);

        RenderSystem.enableDepthTest();
    }
}
