package com.aewireless.item;

import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class WirelessDestroy extends Item {
    public WirelessDestroy(Properties arg) {
        super(arg);
    }

    @Override
    public InteractionResult useOn(UseOnContext arg) {
        if (!arg.getLevel().isClientSide){
            BlockPos clickedPos = arg.getClickedPos();
            Player player = arg.getPlayer();
            InteractionHand hand = arg.getHand();
            Level level = arg.getLevel();
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            CompoundTag persistentData = blockEntity.getPersistentData();
            if (persistentData.contains("uuid") || persistentData.contains("frequency")) {
                String frequency = persistentData.getString("frequency");
                UUID uuid = persistentData.getUUID("uuid");

                if (!WirelessTeamUtil.getNetworkOwnerUUID(
                        player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(uuid))){
                    player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen" ,
                            WirelessTeamUtil.getNetworkOwnerName((ServerLevel) level ,uuid)), true);
                    return InteractionResult.CONSUME;
                }else {
                    persistentData.remove("uuid");
                    persistentData.remove("frequency");
                    return  InteractionResult.SUCCESS;
                }

            }
        }

        return super.useOn(arg);
    }


}
