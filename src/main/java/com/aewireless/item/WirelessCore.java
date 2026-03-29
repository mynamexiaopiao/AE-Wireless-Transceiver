package com.aewireless.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WirelessCore extends Item {
    public WirelessCore(Properties arg) {
        super(arg);
    }




    @Override
    public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
        super.appendHoverText(arg, arg2, list, arg3);

        list.add(Component.translatable("tooltip.aewireless_connect.2"));

        CompoundTag tag = arg.getTag();
        if (tag != null && tag.contains("frequency")) {
            String frequency = tag.getString("frequency");
            list.add(Component.translatable("aewireless.tooltip.channel_name" , frequency));
        }
    }
}
