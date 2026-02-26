package com.aewireless.event;

import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.WirelessTeamUtil;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemOnBlockEvent {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock arg) {
        ItemStack itemStack = arg.getItemStack();
        if (itemStack.is(ModRegister.WIRELESS_CORER.get())){
            if (!arg.getLevel().isClientSide){
                arg.setCanceled(true);

                BlockPos clickedPos = arg.getPos();
                Player player = arg.getEntity();
                InteractionHand hand = arg.getHand();
                Level level = arg.getLevel();
                BlockEntity blockEntity = level.getBlockEntity(clickedPos);
                ItemStack itemInHand = player.getItemInHand(hand);

                if (player.isShiftKeyDown() && blockEntity instanceof WirelessConnectBlockEntity entity){
                    if (entity.getPlacerId() != null){
                        if (AeWireless.IS_FTB_TEAMS_LOADED){
                            if (!WirelessTeamUtil.getNetworkOwnerUUID(
                                    player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(entity.getPlacerId()))){
                                player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen" ,
                                        WirelessTeamUtil.getNetworkOwnerName(entity.getServerLevel() ,entity.getPlacerId())), true);
                                arg.setCancellationResult( InteractionResult.SUCCESS);
                                return;
                            }
                        }
                    }

                    if (entity.isMode()){
                        String frequency = entity.getFrequency();
                        CompoundTag orCreateTag = itemInHand.getOrCreateTag();
                        orCreateTag.putString("frequency", frequency);
                        orCreateTag.putUUID("uuid", player.getUUID());

                        arg.setCancellationResult( InteractionResult.SUCCESS);

                    }else {
                        player.displayClientMessage(Component.translatable("") , true);
                        arg.setCancellationResult( InteractionResult.SUCCESS);
                    }
                }else if (blockEntity instanceof IInWorldGridNodeHost){
                    CompoundTag orCreateTag = itemInHand.getOrCreateTag();
                    if (orCreateTag.contains("frequency") && orCreateTag.contains("uuid")) {
                        CompoundTag updateTag = blockEntity.getPersistentData();

                        updateTag.putString("frequency", orCreateTag.getString("frequency"));

                        updateTag.putUUID("uuid", orCreateTag.getUUID("uuid"));


                        if (!WirelessBlockManage.getBlockPosList().containsKey(blockEntity.getBlockPos())){
                            WirelessBlockManage.addBlockPos(blockEntity.getBlockPos() , null);
                        }
                        arg.setCancellationResult( InteractionResult.SUCCESS);

                    }

                }
            }
            arg.setCancellationResult( InteractionResult.SUCCESS);

        }
    }
}
