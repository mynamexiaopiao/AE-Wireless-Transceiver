package com.aewireless;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;

public final class ModConfig {
    public static final ModConfigSpec SPEC;

    private static final BooleanValue IS_ENERGY;
    private static final BooleanValue CROSS_DIMENSIONAL;
    private static final BooleanValue SHIFT_AUTO_CONNECT;
    private static final BooleanValue SHIFT_SCROLL_CHANNEL_SWITCH;
    private static final DoubleValue BASE_ENERGY;
    private static final DoubleValue MAX_DISTANCE;
    private static final DoubleValue BATTERY_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        IS_ENERGY = builder
                .comment("If enabled, the wireless transceiver will consume energy for transmission.")
                .translation("config.aewireless.option.isEnergy")
                .define("isEnergy", true);

        CROSS_DIMENSIONAL = builder
                .comment("Allow cross-dimensional connection.")
                .translation("config.aewireless.option.crossDimensional")
                .define("crossDimensional", true);

        SHIFT_AUTO_CONNECT = builder
                .comment("Enable auto-connect when placing blocks while sneaking.")
                .translation("config.aewireless.option.shiftAutoConnect")
                .define("shiftAutoConnect", true);

        SHIFT_SCROLL_CHANNEL_SWITCH = builder
                .comment("Enable switching Wireless Connector channels with sneak and mouse wheel.")
                .translation("config.aewireless.option.shiftScrollChannelSwitch")
                .define("shiftScrollChannelSwitch", false);

        BASE_ENERGY = builder
                .translation("config.aewireless.option.baseEnergy")
                .defineInRange("baseEnergy", 100.0, 0.0, Double.MAX_VALUE);

        MAX_DISTANCE = builder
                .comment("Maximum transmission distance between Sub and Main wireless transceivers (0 for unlimited).")
                .translation("config.aewireless.option.maxDistance")
                .defineInRange("maxDistance", 0.0, 0.0, Double.MAX_VALUE);

        BATTERY_MULTIPLIER = builder
                .comment("Sub energy consumption = Distance between Main and Sub x Energy multiplier.")
                .translation("config.aewireless.option.batteryMultiplier")
                .defineInRange("batteryMultiplier", 1.0, 0.0, Double.MAX_VALUE);

        SPEC = builder.build();
    }

    private ModConfig() {
    }

    public static boolean isEnergy() {
        return IS_ENERGY.get();
    }

    public static boolean crossDimensional() {
        return CROSS_DIMENSIONAL.get();
    }

    public static boolean shiftAutoConnect() {
        return SHIFT_AUTO_CONNECT.get();
    }

    public static boolean shiftScrollChannelSwitch() {
        return SHIFT_SCROLL_CHANNEL_SWITCH.get();
    }

    public static double baseEnergy() {
        return BASE_ENERGY.get();
    }

    public static double maxDistance() {
        return MAX_DISTANCE.get();
    }

    public static double batteryMultiplier() {
        return BATTERY_MULTIPLIER.get();
    }
}
