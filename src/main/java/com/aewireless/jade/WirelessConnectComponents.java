package com.aewireless.jade;

import com.aewireless.AeWireless;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum WirelessConnectComponents implements IBlockComponentProvider {
    TOOLTIP("wireless_tooltip") {
        @Override
        protected void add(BlockAccessor accessor, ITooltip tooltip, IPluginConfig config, CompoundTag data) {
            if (data.contains("channel_name")) {
                String frequency = data.getString("channel_name");
                tooltip.add(Component.translatable("aewireless.tooltip.channel_name", frequency));
            }

            if (data.contains("masterMode")) {
                boolean masterMode = data.getBoolean("masterMode");
                String string = Component.translatable("aewireless.jade.tooltip.mastermode").getString();
                String string1 = Component.translatable("aewireless.jade.tooltip.mode").getString();
                tooltip.add(Component.translatable("aewireless.tooltip.master", masterMode ? string : string1));
            }

            if (data.contains("masterMode") && !data.getBoolean("masterMode") && data.contains("masterPos")) {
                BlockPos pos = BlockPos.of(data.getLong("masterPos"));
                String dim = data.contains("masterDim") ? data.getString("masterDim") : "";
                String customName = data.contains("customName") ? data.getString("customName") : null;
                if (customName != null) {
                    String string = Component.translatable("aewireless.jade.tooltip.masternode").getString();
                    tooltip.add(Component.literal(string +": " + customName + "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
                } else {
                    String string = Component.translatable("aewireless.jade.tooltip.masterpos").getString();
                    tooltip.add(Component.literal(string+": (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
                }
                if (!dim.isEmpty()) {
                    String string = Component.translatable("aewireless.jade.tooltip.dimension").getString();
                    tooltip.add(Component.literal(string+": " + dim));
                }
            }

            if (data.contains("networkUsable")) {
                boolean usable = data.getBoolean("networkUsable");
                String string = Component.translatable("aewireless.jade.tooltip.deviceon").getString();
                String string1 = Component.translatable("aewireless.jade.tooltip.deviceoff").getString();
                tooltip.add(Component.literal((usable ? string : string1)));
            }

            if (data.contains("usedChannels") && data.contains("maxChannels")) {
                int usedChannels = data.getInt("usedChannels");
                int maxChannels = data.getInt("maxChannels");
                // 参考AE2的显示方式
                if (maxChannels <= 0) {
                    // 无限频道或未设置
                    tooltip.add(Component.translatable("aewireless.tooltip.channels", usedChannels));
                } else {
                    // 显示 "已使用/最大"
                    tooltip.add(Component.translatable("aewireless.tooltip.channels_of", usedChannels, maxChannels));
                }
            }
            if (data.contains("ownerName")) {
                String ownerName = data.getString("ownerName");
                tooltip.add(Component.translatable("aewireless.tooltip.owner", ownerName));
            } else if (data.contains("placerId")) {
                // 有placerId但没有名称，显示UUID
                java.util.UUID placerId = data.getUUID("placerId");
                tooltip.add(Component.translatable("aewireless.tooltip.owner", placerId.toString().substring(0, 8) + "..."));
            } else {
                // 没有所有者信息（公共收发器）
                tooltip.add(Component.translatable("aewireless.tooltip.owner.public"));
            }
        }
    };


    private final ResourceLocation uid;

    WirelessConnectComponents(String path){
        this.uid = ResourceLocation.fromNamespaceAndPath(AeWireless.MOD_ID, path);
    }


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor accessor, IPluginConfig iPluginConfig) {
        CompoundTag data = accessor.getServerData();
        add(accessor, iTooltip, iPluginConfig, data);
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }

    protected abstract void add(BlockAccessor accessor, ITooltip tooltip, IPluginConfig config, CompoundTag data);
}
