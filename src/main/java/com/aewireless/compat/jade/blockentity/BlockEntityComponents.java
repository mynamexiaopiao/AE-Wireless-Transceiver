package com.aewireless.compat.jade.blockentity;

import com.aewireless.AeWireless;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class BlockEntityComponents implements IBlockComponentProvider {

    private final ResourceLocation uid = ResourceLocation.fromNamespaceAndPath(AeWireless.MOD_ID, "block_entity_tooltip");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();

        if (serverData.contains("frequency") && serverData.contains("uuid") && serverData.contains("direction")) {
            String frequency = serverData.getString("frequency");
            String uuid = serverData.getString("uuid");
            int direction = serverData.getInt("direction");
            Direction value = Direction.values()[direction];
            Level level = blockAccessor.getBlockEntity().getLevel();

            if (serverData.contains("wirelessConnected")) {
                boolean connected = serverData.getBoolean("wirelessConnected");
                iTooltip.add(Component.translatable(
                                connected ? "aewireless.jade.tooltip.wireless_connected" : "aewireless.jade.tooltip.wireless_disconnected")
                        .withStyle(connected ? ChatFormatting.GREEN : ChatFormatting.RED));
            }

            iTooltip.add(Component.translatable("aewireless.tooltip.channel_name" , frequency).withStyle(ChatFormatting.BLUE));

            iTooltip.add(Component.translatable("aewireless.tooltip.owner" ,uuid));

            iTooltip.add(Component.translatable("aewireless.tooltip.direction" ,value.toString()));

        }

    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }
}
