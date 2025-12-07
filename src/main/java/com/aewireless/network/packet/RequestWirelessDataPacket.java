package com.aewireless.network.packet;

import com.aewireless.AeWireless;
import com.aewireless.network.NetworkHandler;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public  class RequestWirelessDataPacket {

    public static void encode(RequestWirelessDataPacket msg, FriendlyByteBuf buf) {
        // 无需内容
    }

    public static RequestWirelessDataPacket decode(FriendlyByteBuf buf) {
        return new RequestWirelessDataPacket();
    }

    public static void handle(RequestWirelessDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var sender = ctx.get().getSender();
            if (sender == null) return; // 必须来自服务端（玩家）
            // 服务端收集属于该玩家队伍/玩家的频道数据，然后发送回客户端
            if (AeWireless.IS_FTB_TEAMS_LOADED){
                UUID teamId = WirelessTeamUtil.getNetworkOwnerUUID(sender.getUUID());
                List<String> keys = WirelessData.getKeys().stream()
                        .filter(k -> k.uuid().equals(teamId))
                        .map(WirelessData.Key::string)
                        .toList();

                // 使用已有的 NetworkHandler 提供的发送给指定玩家的方法（替换为项目实际实现）
                NetworkHandler.sendToPlayer(new SyncWirelessDataPacket(keys), sender);
            }else {
                NetworkHandler.sendToPlayer(
                        new SyncWirelessDataPacket(
                                WirelessData.getKeys().stream().filter(key -> key.uuid().equals(AeWireless.PUBLIC_NETWORK_UUID)).map(WirelessData.Key::string).toList()), sender);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}