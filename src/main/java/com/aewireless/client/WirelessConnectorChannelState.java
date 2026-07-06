package com.aewireless.client;

import com.aewireless.network.NetworkHandler;
import com.aewireless.network.packet.RequestWirelessDataPacket;
import com.aewireless.network.packet.SetConnectorChannelPacket;
import com.aewireless.register.ModRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WirelessConnectorChannelState {
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final List<String> CHANNELS = new ArrayList<>();
    private static long overlayUntilMillis;
    private static long lastRequestMillis;

    private WirelessConnectorChannelState() {
    }

    public static void setChannels(List<String> channels) {
        CHANNELS.clear();
        for (String channel : channels) {
            if (channel != null && !channel.isEmpty() && !CHANNELS.contains(channel)) {
                CHANNELS.add(channel);
            }
        }
    }

    public static void clearChannels() {
        CHANNELS.clear();
        overlayUntilMillis = 0L;
        lastRequestMillis = 0L;
    }

    public static void updateChannel(String channel, boolean add) {
        if (channel == null || channel.isEmpty()) return;
        if (add) {
            if (!CHANNELS.contains(channel)) {
                CHANNELS.add(channel);
            }
        } else {
            CHANNELS.remove(channel);
        }
    }

    public static List<String> getChannels() {
        return Collections.unmodifiableList(CHANNELS);
    }

    public static boolean shouldShowOverlay() {
        return System.currentTimeMillis() < overlayUntilMillis;
    }

    public static void showOverlay() {
        overlayUntilMillis = System.currentTimeMillis() + 2500L;
    }

    public static boolean cycleSelectedChannel(Minecraft minecraft, double scrollDelta) {
        HeldConnector held = getHeldConnector(minecraft);
        if (held == null) return false;

        if (CHANNELS.isEmpty()) {
            requestChannels();
            showOverlay();
            return true;
        }

        ItemStack stack = held.stack();
        String current = getFrequency(stack);
        int index = CHANNELS.indexOf(current);
        if (index < 0) {
            index = scrollDelta > 0 ? CHANNELS.size() - 1 : 0;
        } else {
            index += scrollDelta > 0 ? -1 : 1;
            if (index < 0) index = CHANNELS.size() - 1;
            if (index >= CHANNELS.size()) index = 0;
        }

        String selected = CHANNELS.get(index);
        setLocalFrequency(minecraft, stack, selected);
        NetworkHandler.sendToServer(new SetConnectorChannelPacket(held.hand(), selected));
        showOverlay();
        return true;
    }

    public static HeldConnector getHeldConnector(Minecraft minecraft) {
        if (minecraft.player == null) return null;
        ItemStack mainHand = minecraft.player.getMainHandItem();
        if (mainHand.is(ModRegister.WIRELESS_CORER.get())) {
            return new HeldConnector(InteractionHand.MAIN_HAND, mainHand);
        }
        ItemStack offHand = minecraft.player.getOffhandItem();
        if (offHand.is(ModRegister.WIRELESS_CORER.get())) {
            return new HeldConnector(InteractionHand.OFF_HAND, offHand);
        }
        return null;
    }

    public static String getFrequency(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return "";
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(KEY_FREQUENCY)) return "";
        return tag.getString(KEY_FREQUENCY);
    }

    public static void requestChannels() {
        long now = System.currentTimeMillis();
        if (now - lastRequestMillis < 1000L) return;
        lastRequestMillis = now;
        NetworkHandler.sendToServer(new RequestWirelessDataPacket());
    }

    private static void setLocalFrequency(Minecraft minecraft, ItemStack stack, String frequency) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString(KEY_FREQUENCY, frequency);
        if (minecraft.player != null) {
            tag.putUUID(KEY_UUID, minecraft.player.getUUID());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public record HeldConnector(InteractionHand hand, ItemStack stack) {}
}
