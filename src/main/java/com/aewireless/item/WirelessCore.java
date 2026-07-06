package com.aewireless.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WirelessCore extends Item {
    public static final String KEY_DESTROY_MODE = "destroyMode";
    private static final int DESTROY_MODE_MODEL_DATA = 1;

    public WirelessCore(Properties arg) {
        super(arg);
    }

    public static boolean isDestroyMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().getBoolean(KEY_DESTROY_MODE);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = (BlockHitResult) player.pick(5.0, 0.0F, false);

        if (hitResult.getType() != HitResult.Type.MISS || !player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            boolean destroyMode = !isDestroyMode(stack);
            setDestroyMode(stack, destroyMode);
            player.displayClientMessage(Component.translatable(
                    destroyMode ? "tooltip.aewireless_connect.mode_destroy" : "tooltip.aewireless_connect.mode_connect"
            ), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void setDestroyMode(ItemStack stack, boolean destroyMode) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putBoolean(KEY_DESTROY_MODE, destroyMode);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (destroyMode) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(DESTROY_MODE_MODEL_DATA));
        } else {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, list, tooltipFlag);

        list.add(Component.translatable("tooltip.aewireless_connect.2"));
        list.add(Component.translatable(isDestroyMode(stack)
                ? "tooltip.aewireless_connect.mode_destroy"
                : "tooltip.aewireless_connect.mode_connect"));

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("frequency")) {
                String frequency = tag.getString("frequency");
                list.add(Component.translatable("aewireless.tooltip.channel_name", frequency));
            }
        }
    }
}
