// src/main/java/com/aewireless/network/packet/RequestWirelessDataPacket.java
package com.aewireless.network.packet;

import com.aewireless.AeWireless;
import com.aewireless.network.NetworkHandler;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record RequestWirelessDataPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "request_wireless_data");
    public static final Type<RequestWirelessDataPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RequestWirelessDataPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                // 无需内容
            }, buf -> new RequestWirelessDataPacket());

    public static void handle(RequestWirelessDataPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sender) {
                // 服务端收集属于该玩家队伍/玩家的频道数据，然后发送回客户端
                if (AeWireless.IS_FTB_TEAMS_LOADED) {
                    UUID teamId = WirelessTeamUtil.getNetworkOwnerUUID(sender.getUUID());
                    List<String> keys = WirelessData.getKeys().stream()
                            .filter(k -> k.uuid().equals(teamId))
                            .map(WirelessData.Key::string)
                            .toList();

                    // 使用已有的 NetworkHandler 提供的发送给指定玩家的方法（替换为项目实际实现）
                    NetworkHandler.sendToPlayer(new SyncWirelessDataPacket(keys), sender);
                } else {
                    NetworkHandler.sendToPlayer(
                            new SyncWirelessDataPacket(
                                    WirelessData.getKeys().stream()
                                            .filter(key -> key.uuid().equals(AeWireless.PUBLIC_NETWORK_UUID))
                                            .map(WirelessData.Key::string)
                                            .toList()),
                            sender);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
