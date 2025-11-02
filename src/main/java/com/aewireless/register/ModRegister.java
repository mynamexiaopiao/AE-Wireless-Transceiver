package com.aewireless.register;

import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlock;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.gui.wireless.WirelessMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegister {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AeWireless.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCKS_ENTITY =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AeWireless.MOD_ID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AeWireless.MOD_ID);


    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, AeWireless.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<WirelessMenu>> WIRELESS_MENU =
            MENU_TYPES.register("wireless_menu", () -> IMenuTypeExtension.create(WirelessMenu::new));


    public static final DeferredBlock<Block> WIRELESS_TRANSCEIVER = registerBlock("wireless_transceiver",
            () -> new WirelessConnectBlock(Block.Properties.of().strength(3f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final Supplier<BlockEntityType<WirelessConnectBlockEntity>> WIRELESS_TRANSCEIVER_ENTITY = BLOCKS_ENTITY.register(
            "wireless_transceiver_block_entity",
            () -> BlockEntityType.Builder.of(WirelessConnectBlockEntity::new, WIRELESS_TRANSCEIVER.get()).build(null)
    );


    public static DeferredBlock<Block> registerBlock(String name, Supplier<Block> blockSupplier){
        DeferredBlock<Block> deff = BLOCKS.register(name, blockSupplier);
        ITEMS.register(name,()-> new BlockItem(deff.get(),new Item.Properties()));
        return deff;
    }
}
