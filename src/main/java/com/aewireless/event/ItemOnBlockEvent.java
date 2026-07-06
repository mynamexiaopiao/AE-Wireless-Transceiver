package com.aewireless.event;

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.compat.gtceu.GTCeuPacketUtil;
import com.aewireless.item.WirelessCore;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.WirelessTeamUtil;
import com.aewireless.wireless.block.link.WirelessBlockLinkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@EventBusSubscriber
public class ItemOnBlockEvent {
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_DIRECTION = "direction";

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;

        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(ModRegister.WIRELESS_CORER.get())) {
            handleConnector(event);
        }
    }

    private static void handleConnector(PlayerInteractEvent.RightClickBlock event) {
        BlockPos clickedPos = event.getPos();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        Level level = event.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        if (blockEntity == null) {
            return;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        Direction clickedFace = event.getFace();

        event.setCanceled(true);

        if (WirelessCore.isDestroyMode(itemInHand)) {
            event.setCancellationResult(clearWirelessData(player, level, blockEntity));
            return;
        }

        if (player.isShiftKeyDown() && blockEntity instanceof WirelessConnectBlockEntity entity) {
            if (entity.getPlacerId() != null) {
                if (AeWireless.IS_FTB_TEAMS_LOADED) {
                    if (!WirelessTeamUtil.getNetworkOwnerUUID(
                            player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(entity.getPlacerId()))) {
                        player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen",
                                WirelessTeamUtil.getNetworkOwnerName(entity.getServerLevel(), entity.getPlacerId())), true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                }
            }

            if (entity.isMode()) {
                String frequency = entity.getFrequency();
                CustomData customData = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                tag.putString(KEY_FREQUENCY, frequency);
                tag.putUUID(KEY_UUID, player.getUUID());
                itemInHand.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                player.displayClientMessage(Component.translatable("aewireless.tooltip.bind_channel_success", frequency), true);
            } else {
                player.displayClientMessage(Component.translatable("tooltip.aewireless_connect.1"), true);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (blockEntity instanceof WirelessConnectBlockEntity) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (handleGtceuHost(event, itemInHand, blockEntity, clickedFace)) {
            return;
        }

        if (blockEntity instanceof CableBusBlockEntity cableBus && isBareCable(cableBus)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else
        if (blockEntity instanceof IInWorldGridNodeHost) {
            event.setCancellationResult(getInteractionResult(itemInHand, blockEntity, clickedFace));
        }
    }

    private static InteractionResult clearWirelessData(Player player, Level level, BlockEntity blockEntity) {
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (persistentData.contains(KEY_UUID) || persistentData.contains(KEY_FREQUENCY) || persistentData.contains(KEY_DIRECTION)) {
            UUID uuid = persistentData.getUUID(KEY_UUID);

            if (!WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(uuid))) {
                player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen",
                        WirelessTeamUtil.getNetworkOwnerName((ServerLevel) level, uuid)), true);
            } else {
                persistentData.remove(KEY_UUID);
                persistentData.remove(KEY_FREQUENCY);
                persistentData.remove(KEY_DIRECTION);

                WirelessBlockLinkManager.clear(blockEntity);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean handleGtceuHost(PlayerInteractEvent.RightClickBlock event, ItemStack itemInHand, BlockEntity blockEntity, Direction clickedFace) {
        Object gtceuBlockEntity = GTCeuPacketUtil.castIfInstance(blockEntity, GTCeuPacketUtil.MetaMachineBlockEntity);
        if (gtceuBlockEntity == null) {
            return false;
        }

        Class<?> clazz = gtceuBlockEntity.getClass();
        try {
            Object invoke = clazz.getMethod("getMetaMachine").invoke(gtceuBlockEntity);
            if (invoke instanceof IInWorldGridNodeHost) {
                event.setCancellationResult(getInteractionResult(itemInHand, blockEntity, clickedFace));
            }
            return true;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull InteractionResult getInteractionResult(ItemStack itemInHand, BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof WirelessConnectBlockEntity) {
            return InteractionResult.SUCCESS;
        }

        CustomData customData = itemInHand.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(KEY_FREQUENCY) && tag.contains(KEY_UUID)) {
                CompoundTag updateTag = blockEntity.getPersistentData();

                updateTag.putString(KEY_FREQUENCY, tag.getString(KEY_FREQUENCY));
                updateTag.putUUID(KEY_UUID, tag.getUUID(KEY_UUID));
                updateTag.putInt(KEY_DIRECTION, direction.ordinal());

                blockEntity.setChanged();
                WirelessBlockLinkManager.clear(blockEntity);
                WirelessBlockLinkManager.updateWireless(blockEntity);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isBareCable(CableBusBlockEntity cableBus) {
        for (var dir : Direction.values()) {
            if (cableBus.getPart(dir) != null) return false;
        }
        return true;
    }
}
