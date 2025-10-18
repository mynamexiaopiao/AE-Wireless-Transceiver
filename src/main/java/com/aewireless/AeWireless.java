package com.aewireless;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.network.NetworkHandler;
import com.aewireless.register.ModRegister;
import com.aewireless.register.RegisterHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(AeWireless.MOD_ID)
public class AeWireless {
    public static final String MOD_ID = "aewireless";

    @SuppressWarnings("all")
    public AeWireless(IEventBus modEventBus, ModContainer modContainer) {

        ModRegister.BLOCKS.register(modEventBus);

        ModRegister.BLOCKS_ENTITY.register(modEventBus);

        ModRegister.ITEMS.register(modEventBus);

        ModRegister.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(RegisterHandler::reg);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModRegister.WIRELESS_TRANSCEIVER_ENTITY.get(),
                (blockEntity, side) -> blockEntity
        );
    }

    public static boolean isPlayingOnServer() {
        if (FMLEnvironment.dist.isClient()) {
            try {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                return mc.getConnection() != null && mc.getSingleplayerServer() == null;
            } catch (Throwable t) {
                return false;
            }
        }
        return false;
    }


}
