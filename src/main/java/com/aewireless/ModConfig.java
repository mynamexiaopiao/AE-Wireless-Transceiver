package com.aewireless;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber
public class ModConfig {

    protected static ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    protected static ModConfigSpec.ConfigValue<String> HighlightColor = BUILDER
            .comment("the background color（ARGB！）")
            .define("highlight_color", "0x3300FF00");

    protected static ModConfigSpec.ConfigValue<String> HighlightBorderColor = BUILDER
            .comment("the border color（ARGB！）")
            .define("highlight_border_color", "0xFF006600");

    public static final ModConfigSpec CONFIG = BUILDER.build();

    public static String highlightColor;
    public static String highlightBorderColor;

    public static void getConfig() {
        highlightColor = HighlightColor.get();
        highlightBorderColor = HighlightBorderColor.get();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        getConfig();
    }
}
