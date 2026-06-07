package com.aewireless.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WirelessCore extends Item {
    public static final String KEY_DESTROY_MODE = "destroyMode";
    private static final String KEY_CUSTOM_MODEL_DATA = "CustomModelData";
    private static final int DESTROY_MODE_MODEL_DATA = 1;

    public WirelessCore(Properties arg) {
        super(arg);
    }

    public static boolean isDestroyMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_DESTROY_MODE);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
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
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(KEY_DESTROY_MODE, destroyMode);
        if (destroyMode) {
            tag.putInt(KEY_CUSTOM_MODEL_DATA, DESTROY_MODE_MODEL_DATA);
        } else {
            tag.remove(KEY_CUSTOM_MODEL_DATA);
        }
    }


    @Override
    public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
        super.appendHoverText(arg, arg2, list, arg3);

        list.add(Component.translatable("tooltip.aewireless_connect.2"));
        list.add(Component.translatable(isDestroyMode(arg)
                ? "tooltip.aewireless_connect.mode_destroy"
                : "tooltip.aewireless_connect.mode_connect"));

        CompoundTag tag = arg.getTag();
        if (tag != null && tag.contains("frequency")) {
            String frequency = tag.getString("frequency");
            list.add(Component.translatable("aewireless.tooltip.channel_name" , frequency));
        }
    }
}
