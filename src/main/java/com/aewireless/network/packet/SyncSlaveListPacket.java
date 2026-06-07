package com.aewireless.network.packet;

import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.client.render.WirelessLinkRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncSlaveListPacket {
    private final BlockPos masterPos;
    private final ResourceKey<net.minecraft.world.level.Level> masterDim;
    private final List<WirelessConnectBlockEntity.SlaveRef> slaves;

    public SyncSlaveListPacket(BlockPos masterPos, ResourceKey<net.minecraft.world.level.Level> masterDim,
                               List<WirelessConnectBlockEntity.SlaveRef> slaves) {
        this.masterPos = masterPos;
        this.masterDim = masterDim;
        this.slaves = new ArrayList<>(slaves);
    }

    public static void encode(SyncSlaveListPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.masterPos);
        buf.writeResourceLocation(msg.masterDim.location());
        buf.writeInt(msg.slaves.size());
        for (var ref : msg.slaves) {
            buf.writeResourceLocation(ref.dimension().location());
            buf.writeBlockPos(ref.pos());
        }
    }

    public static SyncSlaveListPacket decode(FriendlyByteBuf buf) {
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
    }

    public static void handle(SyncSlaveListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> WirelessLinkRenderState.setData(msg.masterPos, msg.masterDim, msg.slaves));
        context.setPacketHandled(true);
    }
}
