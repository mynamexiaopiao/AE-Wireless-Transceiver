package com.aewireless.event;

import appeng.util.InteractionUtil;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID)
public class WrenchEvent {

    @SubscribeEvent
    public static void onPlayerUseBlockEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getUseBlock() == Event.Result.DENY) {
            return;
        }
        var player = event.getEntity();
        var level = event.getLevel();
        var hand = event.getHand();
        var hit = event.getHitVec();

        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchDisassemble(stack)) {
            BlockEntity be = level.getBlockEntity(hit.getBlockPos());
            if (be instanceof WirelessConnectBlockEntity te) {
                var pos = hit.getBlockPos();
                BlockState state = level.getBlockState(pos);
                var block = state.getBlock();

                if (!level.isClientSide) {
                    var drops = Block.getDrops(state, (net.minecraft.server.level.ServerLevel) level, pos, te, player, stack);
                    for (var item : drops) {
                        player.getInventory().placeItemBackInInventory(item);
                    }
                }

                level.playSound(player, hit.getBlockPos(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F);

//                block.playerWillDestroy(level, hit.getBlockPos(), state, player);
                level.removeBlock(hit.getBlockPos(), false);
                block.destroy(level, hit.getBlockPos(), state);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            }
        }
    }
}
