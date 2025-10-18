package com.aewireless;

import com.aewireless.network.NetworkHandler;
import com.aewireless.register.ModRegister;
import com.aewireless.register.RegisterHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AeWireless.MOD_ID)
public class AeWireless {
    public static final String MOD_ID = "aewireless";

    @SuppressWarnings("all")
    public AeWireless() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegister.BLOCKS.register(modEventBus);

        ModRegister.BLOCKS_ENTITY.register(modEventBus);

        ModRegister.ITEMS.register(modEventBus);

        ModRegister.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.aewireless.ModConfig.CONFIG);
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        RegisterHandler.INSTANCE.init();
    }
}
