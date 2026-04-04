package com.aewireless.network.packet;

import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestSlaveListPacket(BlockPos masterPos, ResourceKey<net.minecraft.world.level.Level> masterDim)
        implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "request_slave_list");
    public static final Type<RequestSlaveListPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, RequestSlaveListPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeBlockPos(packet.masterPos);
                buf.writeResourceLocation(packet.masterDim.location());
            }, buf -> {
                BlockPos pos = buf.readBlockPos();
                ResourceLocation dimId = buf.readResourceLocation();
                ResourceKey<net.minecraft.world.level.Level> dim = ResourceKey.create(Registries.DIMENSION, dimId);
                return new RequestSlaveListPacket(pos, dim);
            });

    public static void handle(RequestSlaveListPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ServerLevel level = player.getServer().getLevel(msg.masterDim);
            if (level == null) return;
            var be = level.getBlockEntity(msg.masterPos);
            if (!(be instanceof WirelessConnectBlockEntity master)) return;
            if (!master.isMode()) return;
            NetworkHandler.sendToPlayer(
                    new SyncSlaveListPacket(msg.masterPos, msg.masterDim, master.getSlaveRefsSnapshot()),
                    player
            );
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
