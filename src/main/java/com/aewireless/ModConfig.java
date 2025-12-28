package com.aewireless;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {

    protected static ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec.ConfigValue<Boolean> IS_ENERGY = BUILDER
            .comment("Whether to use energy")
            .define("is_energy", false);
    public static ForgeConfigSpec.ConfigValue<Double> BASE_ENERGY = BUILDER
            .comment("Base Energy")
            .defineInRange("base_energy", 100.0,0,Integer.MAX_VALUE);

    public static ForgeConfigSpec.ConfigValue<Double> BATTERY_MULTIPLIER = BUILDER
            .comment("The battery multiplier")
            .defineInRange("battery_multiplier", 1.0, 0, Integer.MAX_VALUE);;


    public static final ForgeConfigSpec CONFIG = BUILDER.build();

    // 添加公共字段来存储当前配置值
    public static boolean isEnergy;
    public static double baseEnergy;
    public static double batteryMultiplier;

    public static void getConfig() {
        isEnergy = IS_ENERGY.get();
        baseEnergy = BASE_ENERGY.get();
        batteryMultiplier = BATTERY_MULTIPLIER.get();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        getConfig();
    }
}
