package com.aewireless.network.packet;

import com.aewireless.AeWireless;
import com.aewireless.register.ModRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetConnectorChannelPacket(InteractionHand hand, String frequency) implements CustomPacketPayload {
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AeWireless.MOD_ID, "set_connector_channel");
    public static final Type<SetConnectorChannelPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SetConnectorChannelPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeEnum(packet.hand());
                buf.writeUtf(packet.frequency() == null ? "" : packet.frequency());
            }, buf -> new SetConnectorChannelPacket(buf.readEnum(InteractionHand.class), buf.readUtf(32767)));

    public static void handle(SetConnectorChannelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;
            if (packet.frequency() == null || packet.frequency().isEmpty()) return;

            ItemStack stack = sender.getItemInHand(packet.hand());
            if (!stack.is(ModRegister.WIRELESS_CORER.get())) return;

            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            tag.putString(KEY_FREQUENCY, packet.frequency());
            tag.putUUID(KEY_UUID, sender.getUUID());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
