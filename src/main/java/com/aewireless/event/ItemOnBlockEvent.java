package com.aewireless.event;

import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.compat.gtceu.GTCeuPacketUtil;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.WirelessTeamUtil;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemOnBlockEvent {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock arg) {

        if (arg.getLevel().isClientSide) return;

        ItemStack itemStack = arg.getItemStack();
        if (itemStack.is(ModRegister.WIRELESS_CORER.get())){
            BlockPos clickedPos = arg.getPos();
            Player player = arg.getEntity();
            InteractionHand hand = arg.getHand();
            Level level = arg.getLevel();
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            ItemStack itemInHand = player.getItemInHand(hand);
            Direction clickedFace = arg.getFace();
                arg.setCanceled(true);
                if (player.isShiftKeyDown() && blockEntity instanceof WirelessConnectBlockEntity entity){
                    if (entity.getPlacerId() != null){
                        if (AeWireless.IS_FTB_TEAMS_LOADED){
                            if (!WirelessTeamUtil.getNetworkOwnerUUID(
                                    player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(entity.getPlacerId()))){
                                player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen" ,
                                        WirelessTeamUtil.getNetworkOwnerName(entity.getServerLevel() ,entity.getPlacerId())), true);
                                arg.setCancellationResult(InteractionResult.SUCCESS);
                            }
                        }
                    }

                    if (entity.isMode()){
                        String frequency = entity.getFrequency();
                        CompoundTag orCreateTag = itemInHand.getOrCreateTag();
                        orCreateTag.putString("frequency", frequency);
                        orCreateTag.putUUID("uuid", player.getUUID());

                        arg.setCancellationResult(InteractionResult.SUCCESS);
                    }else {
                        player.displayClientMessage(Component.translatable("tooltip.aewireless_connect.1") , true);
                        arg.setCancellationResult(InteractionResult.SUCCESS);

                    }
                }else if (GTCeuPacketUtil.castIfInstance(blockEntity , GTCeuPacketUtil.MetaMachineBlockEntity) != null){
                    Object o = GTCeuPacketUtil.castIfInstance(blockEntity, GTCeuPacketUtil.MetaMachineBlockEntity);

                    Class<?> aClass = o.getClass();
                    try {
                        Object invoke = aClass.getMethod("getMetaMachine").invoke(o);
                        if (invoke instanceof IInWorldGridNodeHost){

                            arg.setCancellationResult(getInteractionResult(itemInHand, blockEntity , clickedFace));
                        }
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }

                else if (blockEntity instanceof IInWorldGridNodeHost){
                    arg.setCancellationResult(getInteractionResult(itemInHand, blockEntity , clickedFace));
                }



        }else if (itemStack.is(ModRegister.WIRELESS_DESTROY.get())){
            BlockPos clickedPos = arg.getPos();
            Player player = arg.getEntity();
            InteractionHand hand = arg.getHand();
            Level level = arg.getLevel();
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
                arg.setCanceled(true);
                CompoundTag persistentData = blockEntity.getPersistentData();
                if (persistentData.contains("uuid") || persistentData.contains("frequency") || persistentData.contains("direction")){
                    String frequency = persistentData.getString("frequency");
                    UUID uuid = persistentData.getUUID("uuid");

                    if (!WirelessTeamUtil.getNetworkOwnerUUID(
                            player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(uuid))){
                        player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen" ,
                                WirelessTeamUtil.getNetworkOwnerName((ServerLevel) level ,uuid)), true);
                        arg.setCancellationResult(InteractionResult.SUCCESS);

                    }else {
                        persistentData.remove("uuid");
                        persistentData.remove("frequency");
                        int direction = persistentData.getInt("direction");
                        WirelessBlockManage.removeBlockPos(clickedPos );
                        persistentData.remove("direction");

                        arg.setCancellationResult(InteractionResult.SUCCESS);

                    }


            }

            arg.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static @NotNull InteractionResult getInteractionResult(ItemStack itemInHand, BlockEntity blockEntity , Direction direction) {
        CompoundTag orCreateTag = itemInHand.getOrCreateTag();
        if (orCreateTag.contains("frequency") && orCreateTag.contains("uuid") ) {
            CompoundTag updateTag = blockEntity.getPersistentData();

            updateTag.putString("frequency", orCreateTag.getString("frequency"));

            updateTag.putUUID("uuid", orCreateTag.getUUID("uuid"));

            updateTag.putInt("direction" , direction.ordinal());

            WirelessBlockManage.addBlockPos(
                    new WirelessBlockManage.PosAndDirection(
                            blockEntity.getBlockPos(),
                            direction
                    ),
                    null
            );


            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }
}
