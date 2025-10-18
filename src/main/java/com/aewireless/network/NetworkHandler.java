package com.aewireless.network;

import com.aewireless.AeWireless;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings("all")
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel CHANNEL;
    private static boolean registered = false;

    public static SimpleChannel getChannel() {
        if (CHANNEL == null) {
            CHANNEL = NetworkRegistry.ChannelBuilder.named(
                            new ResourceLocation(AeWireless.MOD_ID,"main"))
                    .serverAcceptedVersions((version) -> true)
                    .clientAcceptedVersions((version) -> true)
                    .networkProtocolVersion(() -> PROTOCOL_VERSION)
                    .simpleChannel();
        }
        return CHANNEL;
    }

    public static void register() {
        if (registered) return;

        int id = 0;
        getChannel().messageBuilder(MenuDataPacket.class, id++)
                .encoder(MenuDataPacket::encode)
                .decoder(MenuDataPacket::decode)
                .consumerMainThread(MenuDataPacket::handle)
                .add();

        getChannel().messageBuilder(WirelessDataSyncPacket.class, id++)
                .encoder(WirelessDataSyncPacket::encode)
                .decoder(WirelessDataSyncPacket::decode)
                .consumerMainThread(WirelessDataSyncPacket::handle)
                .add();

        registered = true;
    }

    public static void sendToServer(Object msg) {
        getChannel().send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToClient(Object msg, PacketDistributor.PacketTarget target) {
        getChannel().send(target, msg);
    }
}

