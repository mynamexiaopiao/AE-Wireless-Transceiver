package com.aewireless;

import com.aewireless.network.NetworkHandler;
import com.aewireless.register.ModRegister;
import com.aewireless.register.RegisterHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.util.UUID;

@Mod(AeWireless.MOD_ID)
public class AeWireless {
    public static final String MOD_ID = "aewireless";

    public static final UUID PUBLIC_NETWORK_UUID = new UUID(0, 0);

    public static final boolean IS_FTB_TEAMS_LOADED = ModList.get().isLoaded("ftbteams");

    @SuppressWarnings("all")
    public AeWireless() throws IOException {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegister.BLOCKS.register(modEventBus);

        ModRegister.BLOCKS_ENTITY.register(modEventBus);

        ModRegister.ITEMS.register(modEventBus);

        ModRegister.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);



        AeWirelessConfig.init();

        if (FMLEnvironment.dist == Dist.CLIENT){
            new ModPack().load();
        }

//        // 注册配置屏幕
//        ModLoadingContext.get().registerExtensionPoint(
//                ConfigScreenHandler.ConfigScreenFactory.class,
//                () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new ConfigScreen(parent))
//        );
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
