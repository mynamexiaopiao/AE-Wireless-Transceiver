package com.aewireless.network;

import com.aewireless.gui.wireless.WirelessMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MenuDataPacket(String frequency, Boolean mode) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "menu_data");

    public static final Type<MenuDataPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MenuDataPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeUtf(packet.frequency == null ? "" : packet.frequency);
                buf.writeBoolean(packet.mode);
            }, buf -> {
                String freq = buf.readUtf();
                Boolean mode = buf.readBoolean();
                return new MenuDataPacket(
                        freq.isEmpty() ? "" : freq,
                        mode
                );
            });


    public static void handle(MenuDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu instanceof WirelessMenu wirelessMenu) {
                    // 更新频率（如果提供了有效频率）
                    if (packet.frequency() != null) {
                        wirelessMenu.setFrequency(packet.frequency());
                    }

                    // 更新模式
                    wirelessMenu.setMode(packet.mode());
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
