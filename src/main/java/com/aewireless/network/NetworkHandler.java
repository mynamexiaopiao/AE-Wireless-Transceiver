package com.aewireless.network;

import com.aewireless.network.packet.MenuDataPacket;
import com.aewireless.network.packet.RequestWirelessDataPacket;
import com.aewireless.network.packet.SyncWirelessDataPacket;
import com.aewireless.network.packet.WirelessDataUpdatePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
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



        registrar.playToServer(
                RequestWirelessDataPacket.TYPE,
                RequestWirelessDataPacket.STREAM_CODEC,
                RequestWirelessDataPacket::handle
        );

        registrar.playToClient(
                SyncWirelessDataPacket.TYPE,
                SyncWirelessDataPacket.STREAM_CODEC,
                SyncWirelessDataPacket::handle
        );

        registrar.playToClient(
                WirelessDataUpdatePacket.TYPE,
                WirelessDataUpdatePacket.STREAM_CODEC,
                WirelessDataUpdatePacket::handle
        );


    }

    public static void sendToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }


    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer sender) {
        PacketDistributor.sendToPlayer(sender,packet);
    }
}
