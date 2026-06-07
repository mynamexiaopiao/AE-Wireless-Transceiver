package com.aewireless.client.render;

import com.aewireless.block.WirelessConnectBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class WirelessLinkRenderState {
    private static boolean enabled = false;
    private static BlockPos masterPos;
    private static ResourceKey<Level> masterDim;
    private static List<WirelessConnectBlockEntity.SlaveRef> slaves = new ArrayList<>();

    private WirelessLinkRenderState() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        WirelessLinkRenderState.enabled = enabled;
        if (!enabled) {
            masterPos = null;
            masterDim = null;
            slaves = new ArrayList<>();
        }
    }

    public static void setData(BlockPos masterPos, ResourceKey<Level> masterDim,
                               List<WirelessConnectBlockEntity.SlaveRef> slaves) {
        WirelessLinkRenderState.masterPos = masterPos;
        WirelessLinkRenderState.masterDim = masterDim;
        WirelessLinkRenderState.slaves = new ArrayList<>(slaves);
    }

    public static BlockPos getMasterPos() {
        return masterPos;
    }

    public static ResourceKey<Level> getMasterDim() {
        return masterDim;
    }

    public static List<WirelessConnectBlockEntity.SlaveRef> getSlaves() {
        return slaves;
    }
}
