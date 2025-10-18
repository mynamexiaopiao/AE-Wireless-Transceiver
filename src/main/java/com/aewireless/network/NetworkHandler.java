package com.aewireless.network;

import com.aewireless.AeWireless;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                MenuDataPacket.TYPE,
                MenuDataPacket.STREAM_CODEC,
                MenuDataPacket::handle
        );

        registrar.playToClient(
                WirelessDataSyncPacket.ID,
                WirelessDataSyncPacket.STREAM_CODEC,
                WirelessDataSyncPacket::handle
        );
    }

    public static void sendToServer(MenuDataPacket msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static void sendToClient(WirelessDataSyncPacket msg, ServerPlayer player) {
        PacketDistributor.sendToPlayer( player,msg);
    }
}
