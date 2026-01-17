package com.aewireless;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber
public class ModConfig {

    protected static ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static ModConfigSpec.ConfigValue<Boolean> IS_ENERGY = BUILDER
            .comment("Whether to use energy")
            .define("is_energy", true);
    public static ModConfigSpec.ConfigValue<Double> BASE_ENERGY = BUILDER
            .comment("Master Transceiver Energy")
            .defineInRange("base_energy", 100.0,0,Integer.MAX_VALUE);

    public static ModConfigSpec.ConfigValue<Double> BATTERY_MULTIPLIER = BUILDER
            .comment("The battery multiplier")
            .defineInRange("battery_multiplier", 1.0, 0, Integer.MAX_VALUE);;

    public static final ModConfigSpec CONFIG = BUILDER.build();

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
