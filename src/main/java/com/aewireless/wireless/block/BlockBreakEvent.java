package com.aewireless.wireless.block;


import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlockBreakEvent {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        LevelAccessor level = event.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(event.getPos());

        if (blockEntity != null) {
            if (blockEntity.getPersistentData().contains("frequency") && blockEntity.getPersistentData().contains("uuid") && blockEntity.getPersistentData().contains("direction")) {
                WirelessBlockManage.removeBlockPos(event.getPos() );
            }
        }
    }
}
