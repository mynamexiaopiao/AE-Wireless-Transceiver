package com.aewireless;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.client.IValidationHandler;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = AeWireless.MOD_ID)
public class AeWirelessConfig {

    public static AeWirelessConfig INSTANCE;
    private static final Object lock = new Object();

    public static void init() {
        synchronized (lock) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(AeWirelessConfig.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment(value = {"If enabled, the wireless transceiver will consume energy for transmission."}, localize = true)
    public boolean isEnergy = true;

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment(value = {"Allow cross-dimensional connection"}, localize = true)
    public boolean crossDimensional = true;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0)
    public double baseEnergy = 100.0;


    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0)
    @Configurable.Comment(value = {"Maximum transmission distance between Sub and Main wireless transceivers(0 for unlimited)."}, localize = true)
    public double maxDistance = 0;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0)
    @Configurable.Comment(value = {"Sub energy consumption = Distance between Main and Sub × Energy multiplier."}, localize = true)
    public double batteryMultiplier = 1.0;

}
