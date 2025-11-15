package com.aewireless.network;

import com.aewireless.AeWireless;
import com.aewireless.network.packet.*;
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

        // 注册客户端 -> 服务端的请求包
        getChannel().messageBuilder(RequestWirelessDataPacket.class, id++)
                .encoder(RequestWirelessDataPacket::encode)
                .decoder(RequestWirelessDataPacket::decode)
                .consumerMainThread(RequestWirelessDataPacket::handle)
                .add();

        // 注册服务端 -> 客户端的同步包
        getChannel().messageBuilder(SyncWirelessDataPacket.class, id++)
                .encoder(SyncWirelessDataPacket::encode)
                .decoder(SyncWirelessDataPacket::decode)
                .consumerMainThread(SyncWirelessDataPacket::handle)
                .add();



        // 添加新的数据更新包注册
        getChannel().messageBuilder(WirelessDataUpdatePacket.class, id++)
                .encoder(WirelessDataUpdatePacket::encode)
                .decoder(WirelessDataUpdatePacket::decode)
                .consumerMainThread(WirelessDataUpdatePacket::handle)
                .add();

        registered = true;
    }
    public static void sendToServer(Object msg) {
        getChannel().send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToClient(Object msg, PacketDistributor.PacketTarget target) {
        getChannel().send(target, msg);
    }

    public static void sendToPlayer(Object msg, net.minecraft.server.level.ServerPlayer player) {
        getChannel().send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}

