// WirelessDataUpdatePacket.java
package com.aewireless.network.packet;

import com.aewireless.gui.wireless.WirelessScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WirelessDataUpdatePacket(String data, boolean isAdd) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "wireless_data_update");
    public static final Type<WirelessDataUpdatePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, WirelessDataUpdatePacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeUtf(packet.data);
                buf.writeBoolean(packet.isAdd);
            }, buf -> new WirelessDataUpdatePacket(buf.readUtf(32767), buf.readBoolean()));

    public static void handle(WirelessDataUpdatePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WirelessScreen screen) {
                screen.receiveServerDataIncremental(msg.data, msg.isAdd);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
