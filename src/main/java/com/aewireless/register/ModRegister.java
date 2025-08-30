package com.aewireless.register;

import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlock;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.gui.wireless.WirelessMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModRegister {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AeWireless.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCKS_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AeWireless.MOD_ID);
    private static final List<Runnable> BLOCK_ENTITY_SETUP = new ArrayList<>();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AeWireless.MOD_ID);


    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AeWireless.MOD_ID);

    public static final RegistryObject<Block> WIRELESS_TRANSCEIVER = registerBlock("wireless_transceiver",
            () -> new WirelessConnectBlock(Block.Properties.of()));

    public static final RegistryObject<BlockEntityType<WirelessConnectBlockEntity>> WIRELESS_TRANSCEIVER_ENTITY = BLOCKS_ENTITY.register(
            "wireless_transceiver_block_entity",
            () -> BlockEntityType.Builder.of(WirelessConnectBlockEntity::new, WIRELESS_TRANSCEIVER.get()).build(null)
    );

//    public static final RegistryObject<BlockEntityType<WirelessConnectBlockEntity>> WIRELESS_TRANSCEIVER_ENTITY = reg(
//            "wireless_block_entity" , ModRegister.WIRELESS_TRANSCEIVER  , WirelessConnectBlockEntity::new  , WirelessConnectBlockEntity.class);


    public static final RegistryObject<MenuType<WirelessMenu>> WIRELESS_MENU =
            MENU_TYPES.register("wireless_menu", () -> WirelessMenu.TYPE);


    private static <C extends AEBaseMenu, I> RegistryObject<MenuType<C>> reg(
            String id, MenuTypeBuilder.MenuFactory<C, I> factory, Class<I> host) {

        return MENU_TYPES.register(id,
                () -> MenuTypeBuilder.create(factory, host).build(id));
    }

//    private static <T extends AEBaseBlockEntity> RegistryObject<BlockEntityType<T>> reg(
//            String id,
//            RegistryObject<? extends AEBaseEntityBlock<?>> block,
//            BlockEntityType.BlockEntitySupplier<T> factory,
//            Class<T> blockEntityClass
//    ) {
//        return BLOCKS_ENTITY.register(id, () -> {
//            var blk = block.get();
//            var type = BlockEntityType.Builder.of(factory, blk).build(null);
//
//            BLOCK_ENTITY_SETUP.add(() -> blk.setBlockEntity(
//                    (Class) blockEntityClass, (BlockEntityType) type, null, null
//            ));
//
//            return type;
//        });
//    }

//    public static void setupBlockEntityTypes() {
//        for (var runnable : BLOCK_ENTITY_SETUP) {
//            runnable.run();
//        }
//    }
//
//    public static List<? extends BlockEntityType<?>> getEntities() {
//        return BLOCKS_ENTITY.getEntries().stream().map(RegistryObject::get).toList();
//    }

    public static RegistryObject<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        RegistryObject<Block> block = BLOCKS.register(name, blockSupplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
