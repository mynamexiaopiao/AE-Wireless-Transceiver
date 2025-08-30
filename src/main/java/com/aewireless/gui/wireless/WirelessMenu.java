package com.aewireless.gui.wireless;

import appeng.api.config.YesNo;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import com.aewireless.block.WirelessConnectBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class WirelessMenu extends AEBaseMenu {
    public static final MenuType<WirelessMenu> TYPE = MenuTypeBuilder.create(WirelessMenu::new, WirelessConnectBlockEntity.class).build("wireless");

    WirelessConnectBlockEntity blockEntity;

    @GuiSync(1)
    private String currentFrequency;

    @GuiSync(0)
    private boolean mode;

    public WirelessMenu( int id, Inventory playerInventory, WirelessConnectBlockEntity host) {
        super(TYPE, id, playerInventory, host);

        this.createPlayerInventorySlots(playerInventory);

        this.blockEntity = host;

        this.currentFrequency = host.getFrequency();
        this.mode = host.isMode();

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

    public void setMode(boolean mode) {
        blockEntity.setMasterMode( mode);
        broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (blockEntity != null) {
            this.mode = blockEntity.isMode();
            this.currentFrequency = blockEntity.getFrequency();
        }
    }


}
