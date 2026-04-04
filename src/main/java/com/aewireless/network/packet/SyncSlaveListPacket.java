package com.aewireless.network.packet;

import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.client.render.WirelessLinkRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncSlaveListPacket(BlockPos masterPos,
                                  ResourceKey<net.minecraft.world.level.Level> masterDim,
                                  List<WirelessConnectBlockEntity.SlaveRef> slaves)
        implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "sync_slave_list");
    public static final Type<SyncSlaveListPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncSlaveListPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeBlockPos(packet.masterPos);
                buf.writeResourceLocation(packet.masterDim.location());
                buf.writeInt(packet.slaves.size());
                for (var ref : packet.slaves) {
                    buf.writeResourceLocation(ref.dimension().location());
                    buf.writeBlockPos(ref.pos());
                }
            }, buf -> {
                BlockPos masterPos = buf.readBlockPos();
                ResourceKey<net.minecraft.world.level.Level> masterDim =
                        ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
                int size = buf.readInt();
                List<WirelessConnectBlockEntity.SlaveRef> refs = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ResourceLocation dimId = buf.readResourceLocation();
                    BlockPos pos = buf.readBlockPos();
                    refs.add(new WirelessConnectBlockEntity.SlaveRef(ResourceKey.create(Registries.DIMENSION, dimId), pos));
                }
                return new SyncSlaveListPacket(masterPos, masterDim, refs);
            });

    public static void handle(SyncSlaveListPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> WirelessLinkRenderState.setData(msg.masterPos, msg.masterDim, msg.slaves));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
