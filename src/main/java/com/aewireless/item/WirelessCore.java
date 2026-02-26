package com.aewireless.item;

import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.wireless.WirelessTeamUtil;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class WirelessCore extends Item {
    public WirelessCore(Properties arg) {
        super(arg);
    }

    @Override
    public InteractionResult useOn(UseOnContext arg) {


        return super.useOn(arg);
    }


    @Override
    public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
        super.appendHoverText(arg, arg2, list, arg3);

        CompoundTag tag = arg.getTag();
        if (tag != null && tag.contains("frequency")) {
            String frequency = tag.getString("frequency");
            list.add(Component.translatable("aewireless.tooltip.channel_name" , frequency));
        }
    }
}
