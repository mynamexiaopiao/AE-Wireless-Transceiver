package com.aewireless.event;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.aewireless.AeWireless;
import com.aewireless.ModConfig;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.block.link.JoinWorldWireless;
import com.aewireless.wireless.block.link.WirelessBlockLinkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@EventBusSubscriber
public class ShiftPlaceAutoConnectEvent {
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_DIRECTION = "direction";

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (!ModConfig.shiftAutoConnect()) return;
        if (!player.isShiftKeyDown()) return;

        Level level = event.getLevel() instanceof Level l ? l : null;
        if (level == null || level.isClientSide) return;

        ItemStack connector = ItemStack.EMPTY;

        if (ModList.get().isLoaded("curios")) {
            ICuriosItemHandler curiosItemHandler = CuriosApi.getCuriosInventory(player).orElse(null);
            if (curiosItemHandler != null) {
                int slotsCount = curiosItemHandler.getSlots();

                for (int i = 0; i < slotsCount; i++) {
                    ItemStack stackInSlot = curiosItemHandler.getEquippedCurios().getStackInSlot(i);
                    if (!stackInSlot.isEmpty() && stackInSlot.is(ModRegister.WIRELESS_CORER.get())) {
                        connector = stackInSlot;
                    }
                }
            }
        }
        if (connector.isEmpty()) {
            connector = findWirelessConnector(player);
        }

        if (connector.isEmpty()) return;

        CustomData customData = connector.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : null;
        if (tag == null || !tag.contains(KEY_FREQUENCY) || !tag.contains(KEY_UUID)) return;

        String frequency = tag.getString(KEY_FREQUENCY);
        if (frequency == null || frequency.isEmpty()) return;

        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        if (blockEntity instanceof WirelessConnectBlockEntity) return;
        if (blockEntity instanceof CableBusBlockEntity cableBus && isBareCable(cableBus)) return;

        IInWorldGridNodeHost host = getNodeHost(blockEntity);
        if (host == null) return;

        Direction direction = getPlacementDirection( player).getOpposite();

        CompoundTag persistent = blockEntity.getPersistentData();
        persistent.remove(KEY_FREQUENCY);
        persistent.remove(KEY_UUID);
        persistent.remove(KEY_DIRECTION);
        WirelessBlockLinkManager.clear(blockEntity);

        persistent.putString(KEY_FREQUENCY, frequency);
        persistent.putUUID(KEY_UUID, tag.getUUID(KEY_UUID));
        persistent.putInt(KEY_DIRECTION, direction.ordinal());

        blockEntity.setChanged();
        JoinWorldWireless.add(level , pos);

    }

    private static ItemStack findWirelessConnector(Player player) {
        var inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(ModRegister.WIRELESS_CORER.get())) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                CompoundTag tag = customData != null ? customData.copyTag() : null;
                if (tag != null && tag.contains(KEY_FREQUENCY) && tag.contains(KEY_UUID)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isBareCable(CableBusBlockEntity cableBus) {
        for (var dir : Direction.values()) {
            if (cableBus.getPart(dir) != null) return false;
        }
        return true;
    }

    private static IInWorldGridNodeHost getNodeHost(BlockEntity blockEntity) {
        if (blockEntity instanceof IInWorldGridNodeHost host) return host;
        Level level = blockEntity.getLevel();
        if (level == null) return null;
        return GridHelper.getNodeHost(level, blockEntity.getBlockPos());
    }

    public static Direction getPlacementDirection(Player player) {
        // 获取玩家视角向量
        Vec3 look = player.getLookAngle();

        double x = look.x;
        double y = look.y;
        double z = look.z;

        // 取绝对值用于比较主方向
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        // 判断哪个分量最大（主朝向）
        if (absY > absX && absY > absZ) {
            // 上下
            return y > 0 ? Direction.UP : Direction.DOWN;
        } else if (absX > absZ) {
            // 东西
            return x > 0 ? Direction.EAST : Direction.WEST;
        } else {
            // 南北
            return z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

}
