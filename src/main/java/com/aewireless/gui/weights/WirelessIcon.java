package com.aewireless.gui.weights;

import appeng.client.gui.style.Blitter;
import com.aewireless.AeWireless;
import net.minecraft.resources.ResourceLocation;

public enum WirelessIcon {
    WIRELESS_BUTTON_BACKGROUND(0, 0,64,64);

    public final int x;
    public final int y;
    public final int width;
    public final int height;


    @SuppressWarnings("all")
    public static final ResourceLocation TEXTURE = new ResourceLocation(AeWireless.MOD_ID, "textures/guis/states.png");
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    WirelessIcon(int x, int y) {
        this(x, y, 16, 16);
    }

    WirelessIcon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT)
                .src(x, y, width, height);
    }
}
