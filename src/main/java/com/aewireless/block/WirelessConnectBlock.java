package com.aewireless.block;

import com.aewireless.register.ModRegister;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WirelessConnectBlock extends Block implements EntityBlock {
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    public WirelessConnectBlock(Properties arg) {
        super(arg);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CONNECTED, false));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WirelessConnectBlockEntity te) {
                // 设置放置者信息，用于队伍隔离
                te.setPlacerId(player.getUUID(), player.getName().getString());
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public List<ItemStack> getDrops(BlockState arg, LootParams.Builder arg2) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(ModRegister.WIRELESS_TRANSCEIVER.get().asItem().getDefaultInstance());
        return drops ;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos arg, BlockState arg2) {
        return new WirelessConnectBlockEntity(arg, arg2);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WirelessConnectBlockEntity te) {
                te.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :(lvl, pos, st, be) -> ((WirelessConnectBlockEntity)be).serverTick(lvl, pos, st);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide){
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof WirelessConnectBlockEntity wirelessConnectBlockEntity) {
                //判断打开界面玩家团队和放置者团队是否一致
                if (WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID()).equals(WirelessTeamUtil.getNetworkOwnerUUID(wirelessConnectBlockEntity.getPlacerId()))){
                    NetworkHooks.openScreen((ServerPlayer) player, wirelessConnectBlockEntity , pos);
                    return InteractionResult.SUCCESS;
                }else {
                    player.displayClientMessage(Component.translatable("aewireless.tooltip.failopen" ,
                            WirelessTeamUtil.getNetworkOwnerName(wirelessConnectBlockEntity.getServerLevel() ,wirelessConnectBlockEntity.getPlacerId())), true);
                    return InteractionResult.CONSUME;
                }



            }else {
                throw  new IllegalStateException("our container provider is missing");
            }
        }

        return InteractionResult.SUCCESS;
    }
}
