package com.aewireless.network;


import com.aewireless.wireless.WirelessData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class WirelessDataSyncPacket {
    private Set<String> channels;

    public WirelessDataSyncPacket(Set<String> channels) {
        this.channels = channels;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(channels.size());
        for (String channel : channels) {
            buf.writeUtf(channel);
        }
    }

    public static WirelessDataSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<String> channels = new HashSet<>();
        for (int i = 0; i < size; i++) {
            channels.add(buf.readUtf());
        }
        return new WirelessDataSyncPacket(channels);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            WirelessData.DATA.clear();
            for (String channel : channels) {
                WirelessData.DATA.put(channel, null);
            }
        });
        ctx.setPacketHandled(true);
    }
}
