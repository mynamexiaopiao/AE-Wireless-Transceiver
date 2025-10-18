package com.aewireless;


import appeng.client.gui.style.Blitter;
import net.minecraft.resources.ResourceLocation;

public enum ModIcon {

    ADD(0, 0),
    SUBTRACT(16,0),
    CHANGE(32,0);

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    @SuppressWarnings("all")

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AeWireless.MOD_ID, "textures/guis/nicons.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    ModIcon(int x, int y) {
        this(x, y, 16, 16);
    }

    ModIcon(int x, int y, int width, int height) {
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

