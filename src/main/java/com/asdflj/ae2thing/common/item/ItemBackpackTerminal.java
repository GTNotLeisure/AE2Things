package com.asdflj.ae2thing.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.MagnetObject;
import com.asdflj.ae2thing.client.localization.Tooltips;
import com.asdflj.ae2thing.common.AE2ThingConfig;
import com.asdflj.ae2thing.common.storage.CellInventory;
import com.asdflj.ae2thing.common.storage.CellInventoryHandler;
import com.asdflj.ae2thing.common.storage.IStorageItemCell;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.gui.PlayerInvType;
import com.asdflj.ae2thing.inventory.item.BackpackTerminalInventory;
import com.asdflj.ae2thing.inventory.item.IItemInventory;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStackType;
import appeng.tile.inventory.IAEStackInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStackType;
import baubles.api.BaubleType;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;

@Optional.Interface(modid = "Baubles|Expanded", iface = "baubles.api.expanded.IBaubleExpanded")
public class ItemBackpackTerminal extends BaseItem
    implements IItemInventory, IStorageItemCell, IItemInventoryHandler, IBaubleExpanded {

    public ItemBackpackTerminal() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_BACKPACK_TERMINAL);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_BACKPACK_TERMINAL)
                .toString());
    }

    @Override
    public ItemBackpackTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_BACKPACK_TERMINAL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean hasEffect(ItemStack itemStack, int pass) {
        return !(new MagnetObject(itemStack)).isOff();
    }

    private Tooltips getMagnetMode(MagnetObject object) {
        return switch (object.getMode()) {
            case Inv -> Tooltips.magnet_inv;
            case Backpack -> Tooltips.magnet_backpack;
            default -> Tooltips.magnet_off;
        };
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player) {
        if (player.isSneaking()) {
            MagnetObject object = new MagnetObject(item);
            object.setNextMode();
            if (Platform.isClient()) {
                player.addChatMessage(getMagnetMode(object).toChat());
            }
        } else {
            InventoryHandler.openGui(
                player,
                w,
                new BlockPos(player.inventory.currentItem, 0, 0),
                ForgeDirection.UNKNOWN,
                this.guiGuiType());
        }
        return item;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return AE2ThingConfig.terminalBaubles ? BaubleType.UNIVERSAL : null;
    }

    @Override
    public String[] getBaubleTypes(ItemStack stack) {
        return AE2ThingConfig.terminalBaubles ? new String[] { BaubleExpandedSlots.universalType } : new String[0];
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase player) {
        return AE2ThingConfig.terminalBaubles;
    }

    @Override
    public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if (AE2ThingConfig.terminalBaubles) onUpdate(stack, player.worldObj, player, -1, false);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean hotbar) {
        if (!entity.isSneaking() && entity.ticksExisted % 10 == 0) {
            MagnetObject object = new MagnetObject(stack);
            if (!object.isOff()) {
                object.doMagnet(world, entity);
            }
        }
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return getInventoryAt(stack, world, x, y, z, player);
    }

    @Override
    public Object getInventoryAt(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        if (PlayerInvType.typeOf(x) == PlayerInvType.PLAYER_BAUBLES && !AE2ThingConfig.terminalBaubles) return null;
        if (stack.getItem() instanceof IItemInventoryHandler iih) {
            try {
                return new BackpackTerminalInventory(
                    stack,
                    PlayerInvType.typeOf(x) == PlayerInvType.PLAYER_INV ? PlayerInvType.slotOf(x) : x,
                    player,
                    iih.getInventoryHandler(stack, null, player));
            } catch (AppEngException ignored) {}
        }
        return null;
    }

    private GuiType guiGuiType() {
        return GuiType.BACKPACK_TERMINAL;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getItem() == null
            || AE2ThingAPI.instance()
                .isBlacklistedInStorage(requestedAddition.getItem());
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return null;
    }

    @Override
    public IAEStackInventory getConfigAEInventory(ItemStack is) {
        return null;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return null;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {

    }

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> toolTip,
        boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, toolTip, displayMoreInfo);
        MagnetObject magnetObject = new MagnetObject(stack);
        toolTip.add(Tooltips.magnet_current_mode.getLocal(getMagnetMode(magnetObject).getLocal()));
        if (isShiftKeyDown()) {
            toolTip.add(Tooltips.backpack_terminal_desc.getLocal());
        } else {
            toolTip.add(Tooltips.shift_for_more.getLocal());
        }
    }

    @Override
    public IMEInventoryHandler<IAEItemStack> getInventoryHandler(ItemStack o, ISaveProvider container,
        EntityPlayer player) throws AppEngException {
        return new CellInventoryHandler(new CellInventory(o, container, player));
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return AEItemStackType.ITEM_STACK_TYPE;
    }
}
