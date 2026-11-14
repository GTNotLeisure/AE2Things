package com.asdflj.ae2thing.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.network.CPacketInventoryActionExtend;
import com.asdflj.ae2thing.util.BlockPos;
import com.gtnewhorizon.gtnhlib.event.PickBlockEvent;

import appeng.util.item.AEItemStack;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeybindLoader implements Runnable {

    public static KeyBinding openBackpackTerminal;
    public static KeyBinding openDualInterfaceTerminal;
    public static KeyBinding openTerminalMenu;

    @Override
    public void run() {
        openBackpackTerminal = new KeyBinding(
            AE2Thing.MODID + ".key.open_backpack_terminal",
            Keyboard.CHAR_NONE,
            "itemGroup." + AE2Thing.MODID);
        ClientRegistry.registerKeyBinding(openBackpackTerminal);
        openDualInterfaceTerminal = new KeyBinding(
            AE2Thing.MODID + ".key.open_dual_interface_terminal",
            Keyboard.CHAR_NONE,
            "itemGroup." + AE2Thing.MODID);
        ClientRegistry.registerKeyBinding(openDualInterfaceTerminal);
        openTerminalMenu = new KeyBinding(
            AE2Thing.MODID + ".key.open_terminal",
            Keyboard.CHAR_NONE,
            "itemGroup." + AE2Thing.MODID);
        ClientRegistry.registerKeyBinding(openTerminalMenu);
        FMLCommonHandler.instance()
            .bus()
            .register(this);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP p = mc.thePlayer;
        if (mc.currentScreen != null || p == null || mc.theWorld == null) return;

        if (!(event instanceof InputEvent.KeyInputEvent) && !(event instanceof InputEvent.MouseInputEvent)) return;
        if (p.openContainer == null) {
            return;
        }
        if (openTerminalMenu.isPressed()) {
            AE2ThingAPI.instance()
                .openTerminalMenu();
        }
        if (openBackpackTerminal.isPressed()) {
            AE2ThingAPI.instance()
                .openBackpackTerminal();
        }
        if (openDualInterfaceTerminal.isPressed()) {
            AE2ThingAPI.instance()
                .openDualinterfaceTerminal();
        }
    }

    @SubscribeEvent
    public void onPickBlock(PickBlockEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null || mc.theWorld == null
            || mc.currentScreen != null
            || !mc.inGameHasFocus
            || player.capabilities.isCreativeMode) return;

        ItemStack held = player.inventory.getCurrentItem();
        final ItemStack target;
        final int mode;
        if (held != null && held.stackSize > 0) {
            target = held.copy();
            mode = CPacketInventoryActionExtend.REFILL_HELD_STACK;
        } else {
            target = getTargetBlock(mc);
            mode = CPacketInventoryActionExtend.PICK_INTO_EMPTY_SLOT;
        }
        if (target == null || target.getItem() == null) return;

        ItemStack sample = target.copy();
        sample.stackSize = 1;

        AE2Thing.proxy.netHandler.sendToServer(
            new CPacketInventoryActionExtend(
                InventoryActionExtend.REQUEST_ITEM,
                player.inventory.currentItem,
                mode,
                AEItemStack.create(sample)));

        event.setCanceled(true);
    }

    private static ItemStack getTargetBlock(Minecraft mc) {
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return null;
        ItemStack picked = new BlockPos(hit, mc.theWorld).getPickBlock(hit, mc.theWorld, mc.thePlayer);

        if (picked == null || picked.getItem() == null || picked.stackSize <= 0) return null;

        return picked;
    }
}
