package com.aewireless.network.packet;

import com.aewireless.register.ModRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetConnectorChannelPacket {
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";

    private final InteractionHand hand;
    private final String frequency;

    public SetConnectorChannelPacket(InteractionHand hand, String frequency) {
        this.hand = hand;
        this.frequency = frequency;
    }

    public static void encode(SetConnectorChannelPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeUtf(msg.frequency);
    }

    public static SetConnectorChannelPacket decode(FriendlyByteBuf buf) {
        return new SetConnectorChannelPacket(buf.readEnum(InteractionHand.class), buf.readUtf(32767));
    }

    public static void handle(SetConnectorChannelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var sender = ctx.get().getSender();
            if (sender == null || msg.frequency == null || msg.frequency.isEmpty()) return;

            ItemStack stack = sender.getItemInHand(msg.hand);
            if (!stack.is(ModRegister.WIRELESS_CORER.get())) return;

            CompoundTag tag = stack.getOrCreateTag();
            tag.putString(KEY_FREQUENCY, msg.frequency);
            tag.putUUID(KEY_UUID, sender.getUUID());
        });
        ctx.get().setPacketHandled(true);
    }
}
