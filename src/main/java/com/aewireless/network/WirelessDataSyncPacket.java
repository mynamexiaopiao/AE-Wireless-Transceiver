package com.aewireless.network;


import com.aewireless.wireless.WirelessData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class WirelessDataSyncPacket {
    private Set<String> channels;
    private UUID uuid;

    public WirelessDataSyncPacket(Set<String> channels , UUID uuid) {
        this.uuid = uuid;
        this.channels = channels;
    }

    public void encode(FriendlyByteBuf buf ) {
        buf.writeInt(channels.size());
        for (String channel : channels) {
            buf.writeUtf(channel);
        }
        buf.writeUUID(uuid);
    }

    public static WirelessDataSyncPacket decode(FriendlyByteBuf buf  ) {
        int size = buf.readInt();
        Set<String> channels = new HashSet<>();
        for (int i = 0; i < size; i++) {
            channels.add(buf.readUtf());
        }
        UUID uuid = buf.readUUID();
        return new WirelessDataSyncPacket(channels , uuid);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            WirelessData.clearData();
            for (String channel : channels) {
                WirelessData.addData(channel, uuid,null);
            }
        });
        ctx.setPacketHandled(true);
    }
}
