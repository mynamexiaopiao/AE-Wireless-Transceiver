package com.aewireless.gui.wireless;

import appeng.api.config.YesNo;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.network.MenuDataPacket;
import com.aewireless.network.NetworkHandler;
import com.aewireless.network.WirelessDataSyncPacket;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.WirelessData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;

public class WirelessMenu extends AEBaseMenu {
//    public static final MenuType<WirelessMenu> TYPE = MenuTypeBuilder.create((WirelessMenu::new), WirelessConnectBlockEntity.class).build("wireless");

    WirelessConnectBlockEntity blockEntity;

    @GuiSync(1)
    private String currentFrequency;

    @GuiSync(0)
    private boolean mode;

    private final ContainerData data;

    public WirelessMenu(int containerId, Inventory inventory,FriendlyByteBuf friendlyByteBuf) {
        this(containerId, inventory, ((WirelessConnectBlockEntity) inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos())) , new SimpleContainerData(1));
    }

    public WirelessMenu( int id, Inventory playerInventory, WirelessConnectBlockEntity host, ContainerData data) {
        super(ModRegister.WIRELESS_MENU.get(), id, playerInventory, host);

//        this.createPlayerInventorySlots(playerInventory);

        this.blockEntity = host;

        this.currentFrequency = host.getFrequency();
        this.mode = host.isMode();

        this.data = data;

        addDataSlots(data);
    }


    public boolean isMode() {
        return mode;
    }

    public String getFrequency() {
        return currentFrequency;
    }

    public void setFrequency(String frequency) {
        this.blockEntity.setFrequency(frequency);


        broadcastChanges();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPlayingOnServer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() == null && mc.getConnection() != null;
    }


    public void setMode(boolean mode) {
        blockEntity.setMasterMode( mode);
        broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (blockEntity != null) {
            this.mode = blockEntity.isMode();
            this.currentFrequency = blockEntity.getFrequency() ==  null ? "" : blockEntity.getFrequency();
        }
//        // 同步频道数据到客户端
//        if (getPlayerInventory().player instanceof ServerPlayer serverPlayer) {
//            Set<String> channels = new HashSet<>(WirelessData.DATA.keySet());
//            NetworkHandler.sendToClient(new WirelessDataSyncPacket(channels),
//                    PacketDistributor.PLAYER.with(() -> serverPlayer));
//        }

    }

    public boolean isOnline() {
        if (data != null){
            return data.get(0) == 1;
        }
        return false;
    }

}
