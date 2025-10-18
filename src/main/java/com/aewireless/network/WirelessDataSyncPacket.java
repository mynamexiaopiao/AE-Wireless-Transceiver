package com.aewireless.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.aewireless.wireless.WirelessData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

// 使用 CustomPacketPayload 接口替代旧的 Packet 类
public record WirelessDataSyncPacket(Set<String> channels) implements CustomPacketPayload {

    public static final Type<WirelessDataSyncPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("aewireless", "wireless_data_sync"));


    public static final StreamCodec<FriendlyByteBuf, WirelessDataSyncPacket> STREAM_CODEC = StreamCodec.of(
            WirelessDataSyncPacket::encode,
            WirelessDataSyncPacket::decode
    );


    private static void encode(FriendlyByteBuf buf, WirelessDataSyncPacket packet) {
        buf.writeInt(packet.channels.size());
        for (String channel : packet.channels) {
            buf.writeUtf(channel);
        }
    }

    private static WirelessDataSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<String> channels = new HashSet<>();
        for (int i = 0; i < size; i++) {
            channels.add(buf.readUtf());
        }
        return new WirelessDataSyncPacket(channels);
    }

    public static void handle(final WirelessDataSyncPacket payload, final IPayloadContext context) {
        // 在游戏线程中执行
        context.enqueueWork(() -> {
            WirelessData.DATA.clear();
            for (String channel : payload.channels()) {
                WirelessData.DATA.put(channel, null);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}