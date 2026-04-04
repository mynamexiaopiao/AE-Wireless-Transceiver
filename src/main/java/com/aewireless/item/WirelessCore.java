package com.aewireless.item;

import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class WirelessCore extends Item {
    public WirelessCore(Properties arg) {
        super(arg);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, list, tooltipFlag);

        list.add(Component.translatable("tooltip.aewireless_connect.2"));

        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();  // 或使用 customData.getUnsafe() 如果不修改
            if (tag.contains("frequency")) {
                String frequency = tag.getString("frequency");
                list.add(Component.translatable("aewireless.tooltip.channel_name", frequency));

            }
        }
    }


}
