package com.aewireless.mixin;

import com.aewireless.wireless.block.LevelManage;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "destroy" , at = @At("HEAD"))
    public void s(LevelAccessor levelAccessor, BlockPos blockPos, BlockState arg3, CallbackInfo ci){
        if (!levelAccessor.isClientSide()){
            BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
            if (blockEntity!= null){
                CompoundTag persistentData = blockEntity.getPersistentData();
                if (persistentData.contains("uuid") || persistentData.contains("frequency") || persistentData.contains("direction")){
                    LevelManage.removeBlockEntity(blockPos);
                    WirelessBlockManage.removeBlockPos(blockPos);
                }
            }
        }
    }
}
