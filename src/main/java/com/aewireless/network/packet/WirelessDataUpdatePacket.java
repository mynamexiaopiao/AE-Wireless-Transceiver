// WirelessDataUpdatePacket.java
package com.aewireless.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;
import java.util.function.Supplier;

public class WirelessDataUpdatePacket {
    private final String data;
    private final boolean isAdd;

    public WirelessDataUpdatePacket(String data, boolean isAdd) {
        this.data = data;
        this.isAdd = isAdd;
    }

    public static void encode(WirelessDataUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.data);
        buf.writeBoolean(msg.isAdd);
    }

    public static WirelessDataUpdatePacket decode(FriendlyByteBuf buf) {
        return new WirelessDataUpdatePacket(buf.readUtf(32767), buf.readBoolean());
    }

    public static void handle(WirelessDataUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().screen instanceof com.aewireless.gui.wireless.WirelessScreen screen) {
                    screen.receiveServerDataIncremental(msg.data, msg.isAdd);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
