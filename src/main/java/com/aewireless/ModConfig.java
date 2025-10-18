package com.aewireless;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {

    protected static ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

//    protected static ForgeConfigSpec.BooleanValue SLOT_VALUE = BUILDER
//            .comment("")
//            .define("highLight_style", false);

    protected static ForgeConfigSpec.ConfigValue<String> HighlightColor = BUILDER
            .comment("the background color（ARGB！）")
            .define("highlight_color","0x3300FF00");

    protected static ForgeConfigSpec.ConfigValue<String> HighlightBorderColor = BUILDER
            .comment("the border color（ARGB！）")
            .define("highlight_border_color","0xFF006600");

    public static final ForgeConfigSpec CONFIG = BUILDER.build();

    public static boolean highlightStyle;

    public static String highlightColor;
    public static String highlightBorderColor;

    public static void getConfig() {
//        highlightStyle = SLOT_VALUE.get();

        highlightColor = HighlightColor.get();
        highlightBorderColor = HighlightBorderColor.get();

    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        getConfig();
    }
}
