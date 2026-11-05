package com.asdflj.ae2thing.api.adapter.terminal.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.ItemWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;
import com.asdflj.ae2thing.util.BaublesUtil;

import appeng.util.Platform;

public class DualInterfaceTerminalHandler implements ITerminalHandler {

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        if (item != null && item.getItem() instanceof ItemWirelessDualInterfaceTerminal) {
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (Platform.isSameItemPrecise(stack, item)) {
                    InventoryHandler.openItemGui(
                        player,
                        player.worldObj,
                        PlayerInvType.encode(i, PlayerInvType.PLAYER_INV),
                        GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL);
                    return;
                }
            }
            if (!Mods.BAUBLES.isModLoaded() || !Config.dualTerminalCanStoredInBaubles) return;
            IInventory handler = BaublesUtil.getBaublesInv(player);
            if (handler == null) return;
            for (int i = 0; i < handler.getSizeInventory(); i++) {
                ItemStack is = handler.getStackInSlot(i);
                if (BaublesUtil.isSameItemPrecise(is, item, i, items)) {
                    InventoryHandler.openItemGui(
                        player,
                        player.worldObj,
                        PlayerInvType.encode(i, PlayerInvType.PLAYER_BAUBLES),
                        GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL);
                    return;
                }
            }
        }
    }
}
