// src/main/java/com/aewireless/network/packet/SyncWirelessDataPacket.java
package com.aewireless.network.packet;

import com.aewireless.gui.wireless.WirelessScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncWirelessDataPacket(List<String> keys) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "sync_wireless_data");
    public static final Type<SyncWirelessDataPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncWirelessDataPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeInt(packet.keys.size());
                for (String s : packet.keys) {
                    buf.writeUtf(s);
                }
            }, buf -> {
                int size = buf.readInt();
                List<String> keys = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    keys.add(buf.readUtf(32767));
                }
                return new SyncWirelessDataPacket(keys);
            });

    public static void handle(SyncWirelessDataPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // 在客户端线程中处理：如果当前打开的是 WirelessScreen，则直接更新它的列表
            if (Minecraft.getInstance().screen instanceof WirelessScreen ws) {
                ws.receiveServerData(msg.keys);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
