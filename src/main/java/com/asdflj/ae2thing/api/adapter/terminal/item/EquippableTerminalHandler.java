package com.asdflj.ae2thing.api.adapter.terminal.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;
import com.asdflj.ae2thing.util.BaublesUtil;

import appeng.util.Platform;

public class EquippableTerminalHandler implements ITerminalHandler {

    private final Class<? extends Item> itemType;
    private final GuiType gui;

    public EquippableTerminalHandler(Class<? extends Item> itemType, GuiType gui) {
        this.itemType = itemType;
        this.gui = gui;
    }

    public Class<? extends Item> getItemType() {
        return this.itemType;
    }

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        if (item == null || !itemType.isInstance(item.getItem())) return;
        if (items.getData()
            .hasKey(Constants.SLOT)) {
            if (!AE2ThingConfig.terminalBaubles || !Mods.BAUBLES.isModLoaded()) return;
            int slot = items.getData()
                .getInteger(Constants.SLOT);
            IInventory baubles = BaublesUtil.getBaublesInv(player);
            if (baubles == null || slot < 0 || slot >= baubles.getSizeInventory()) return;
            int ref = PlayerInvType.encode(slot, PlayerInvType.PLAYER_BAUBLES);
            if (Platform.isSameItem(baubles.getStackInSlot(slot), item)) {
                InventoryHandler.openItemGui(player, player.worldObj, ref, gui);
            }
            return;
        }
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            if (Platform.isSameItemPrecise(player.inventory.getStackInSlot(i), item)) {
                InventoryHandler.openItemGui(player, player.worldObj, i, gui);
                return;
            }
        }
    }
}
