package com.aewireless.mixin;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.parts.PartPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(PartPlacement.class)
public class PartPlacementMixin {
    @Inject(method = "placePart" , at = @At("TAIL") , remap = false)
    private static <T extends IPart> void s(@Nullable Player player, Level level, IPartItem<T> partItem, @Nullable CompoundTag configTag, BlockPos pos, Direction side, CallbackInfoReturnable<T> cir){
        if (!level.isClientSide){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null){
                try {
                    Method aewireless$updateWireless = blockEntity.getClass().getMethod("aewireless$updateWireless");
                    Method clearLink = blockEntity.getClass().getMethod("clearLink");
                    clearLink.invoke(blockEntity);
                    aewireless$updateWireless.invoke(blockEntity);

                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
