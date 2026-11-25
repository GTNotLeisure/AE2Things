package com.asdflj.ae2thing.api;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.localization.Messages;
import com.asdflj.ae2thing.network.SPacketCraftingDebugCardUpdate;
import com.asdflj.ae2thing.util.ItemStackNBT;

import appeng.core.localization.Localization;
import appeng.me.Grid;
import appeng.tile.networking.TileController;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CraftingDebugCardObject {

    private final ItemStackNBT data;
    private Mode currentMode;

    public enum Mode implements Localization {

        Everything(Messages.crafting_request_everything),
        Player(Messages.crafting_request_player),
        Machine(Messages.crafting_request_machine);

        private final Messages message;

        Mode(Messages message) {
            this.message = message;
        }

        @Override
        public String getUnlocalized() {
            return this.message.getUnlocalized();
        }
    }

    public CraftingDebugCardObject(ItemStack itemStack) {
        this.data = ItemStackNBT.of(itemStack);
        int mode = this.data.getByte(Constants.DEBUG_CARD_MODE);
        this.currentMode = mode >= 0 && mode < Mode.values().length ? Mode.values()[mode] : Mode.Everything;
    }

    public Mode getMode() {
        return currentMode;
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        data.setByte(Constants.DEBUG_CARD_MODE, (byte) mode.ordinal());
    }

    public Mode getNextMode() {
        return Mode.values()[(this.getMode()
            .ordinal() + 1) % Mode.values().length];
    }

    @SideOnly(Side.CLIENT)
    public static void sendMessageToPlayer(long networkID, Mode mode) {
        LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> infos = AE2ThingAPI.instance()
            .getHistory(networkID);
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (infos.isEmpty()) {
            player.addChatComponentMessage(Messages.crafting_no_history.toChat());
            return;
        }
        List<IChatComponent> text = getHistoryText(infos, mode);
        if (!text.isEmpty()) {
            for (IChatComponent message : text) {
                player.addChatComponentMessage(message);
            }
        } else {
            player.addChatComponentMessage(Messages.crafting_no_history.toChat());
        }
    }

    public void sendRecordToPlayer(Grid grid, EntityPlayer player) {
        if (grid == null) return;
        long id = AE2ThingAPI.instance()
            .getStorageMyID(grid);
        if (!player.worldObj.isRemote && player instanceof EntityPlayerMP serverPlayer) {
            AE2Thing.proxy.netHandler.sendTo(
                new SPacketCraftingDebugCardUpdate(
                    id,
                    AE2ThingAPI.instance()
                        .getHistory(grid),
                    this.getMode()),
                serverPlayer);
        } else if (player.worldObj.isRemote) {
            sendMessageToPlayer(id, this.getMode());
            AE2ThingAPI.instance()
                .saveHistory();
        }
    }

    public Grid getGrid(TileController tc) {
        try {
            return (Grid) tc.getProxy()
                .getGrid();
        } catch (Exception ignored) {}
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static List<IChatComponent> getHistoryText(LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> history,
        Mode mode) {
        List<IChatComponent> msg = new ObjectArrayList<>();
        if (history.isEmpty()) return msg;
        for (CraftingDebugHelper.CraftingInfo info : history) {
            if (mode == Mode.Player && !info.isPlayer) continue;
            if (mode == Mode.Machine && info.isPlayer) continue;
            msg.add(Messages.crafting_network_id.toChat(info.getNetworkID()));
            msg.add(Messages.crafting_request_type.toChat((info.isPlayer ? Mode.Player : Mode.Machine).toChat()));
            if (!info.isPlayer && info.pos != null) {
                msg.add(Messages.crafting_pos.toChat(info.pos.x, info.pos.y, info.pos.z, info.pos.getDimension()));
            }
            msg.add(Messages.crafting_name.toChat(info.name));
            msg.add(Messages.crafting_start_time.toChat(info.getFormatStartTime()));
            if (info.isFinish()) {
                msg.add(Messages.crafting_usage_time.toChat(info.getUsageTime()));
            }
            switch (info.getState()) {
                case FINISHED -> msg.add(Messages.crafting_state.toChat(Messages.crafting_finish.toChat()));
                case CANCELLED -> msg.add(Messages.crafting_state.toChat(Messages.crafting_cancel.toChat()));
                case RUNNING -> msg.add(Messages.crafting_state.toChat(Messages.crafting_running.toChat()));
            }
            if (!info.getErrorMessage()
                .isEmpty()) {
                msg.add(Messages.crafting_error_message.toChat(info.getErrorMessage()));
            }
            if (info.isSimulation()) {
                msg.add(Messages.crafting_simulation.toChat());
            }

            msg.add(
                Messages.crafting_stack
                    .toChat(info.itemName, ReadableNumberConverter.INSTANCE.toWideReadableForm(info.requestSize)));
            msg.add(new ChatComponentText("------------------------------------------------"));
        }
        if (!msg.isEmpty()) {
            msg.addFirst(new ChatComponentText("------------------------------------------------"));
        }
        return msg;
    }
}
