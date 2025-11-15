package com.aewireless.network.packet;



import com.aewireless.gui.wireless.WirelessScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncWirelessDataPacket {
    private final List<String> keys;

    public SyncWirelessDataPacket(List<String> keys) {
        this.keys = new ArrayList<>(keys);
    }

    public static void encode(SyncWirelessDataPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.keys.size());
        for (String s : msg.keys) buf.writeUtf(s);
    }

    public static SyncWirelessDataPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<String> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) keys.add(buf.readUtf(32767));
        return new SyncWirelessDataPacket(keys);
    }

    public static void handle(SyncWirelessDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端线程中处理：如果当前打开的是 WirelessScreen，则直接更新它的列表
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().screen instanceof WirelessScreen ws) {
                    ws.receiveServerData(msg.keys);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
