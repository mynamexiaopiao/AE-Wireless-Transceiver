package com.aewireless.compat.jade.blockentity;

import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

public class BlockEntityComponents implements IBlockComponentProvider {

    private final ResourceLocation uid = new ResourceLocation(AeWireless.MOD_ID, "block_entity_tooltip");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();

        if (serverData.contains("frequency") && serverData.contains("uuid")) {
            String frequency = serverData.getString("frequency");
            String uuid = serverData.getString("uuid");
            Level level = blockAccessor.getBlockEntity().getLevel();

            iTooltip.add(Component.translatable("aewireless.tooltip.channel_name" , frequency));

            iTooltip.add(Component.translatable("aewireless.tooltip.owner" ,uuid));

        }

    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }
}
