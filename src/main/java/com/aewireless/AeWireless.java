package com.aewireless;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.network.NetworkHandler;
import com.aewireless.register.ModRegister;
import com.aewireless.register.RegisterHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.UUID;

@Mod(AeWireless.MOD_ID)
public class AeWireless {
    public static final String MOD_ID = "aewireless";

    public static final UUID PUBLIC_NETWORK_UUID = new UUID(0, 0);

    public static final boolean IS_FTB_TEAMS_LOADED = ModList.get().isLoaded("ftbteams");


    @SuppressWarnings("all")
    public AeWireless(IEventBus modEventBus, ModContainer modContainer) {

        ModRegister.BLOCKS.register(modEventBus);

        ModRegister.BLOCKS_ENTITY.register(modEventBus);

        ModRegister.ITEMS.register(modEventBus);

        ModRegister.MENU_TYPES.register(modEventBus);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON , ModConfig.CONFIG);


        modEventBus.addListener(this::registerCapabilities);
    }

    public static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModRegister.WIRELESS_TRANSCEIVER_ENTITY.get(),
                (blockEntity, side) -> blockEntity
        );
    }




}
