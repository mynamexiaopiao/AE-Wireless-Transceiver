package com.aewireless.network.packet;

import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSlaveListPacket {
    private final BlockPos masterPos;
    private final ResourceKey<net.minecraft.world.level.Level> masterDim;

    public RequestSlaveListPacket(BlockPos masterPos, ResourceKey<net.minecraft.world.level.Level> masterDim) {
        this.masterPos = masterPos;
        this.masterDim = masterDim;
    }

    public static void encode(RequestSlaveListPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.masterPos);
        buf.writeResourceLocation(msg.masterDim.location());
    }

    public static RequestSlaveListPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation dimId = buf.readResourceLocation();
        ResourceKey<net.minecraft.world.level.Level> dim = ResourceKey.create(Registries.DIMENSION, dimId);
        return new RequestSlaveListPacket(pos, dim);
    }

    public static void handle(RequestSlaveListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
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
        context.setPacketHandled(true);
    }
}
