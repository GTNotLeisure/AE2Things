package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.container.slot.SlotEncodedPatternInput;
import com.asdflj.ae2thing.inventory.item.PatternModifierInventory;
import com.asdflj.ae2thing.util.ItemStackNBT;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEStack;
import appeng.container.AEBaseContainer;
import appeng.container.interfaces.IVirtualSlotSource;
import appeng.container.sync.SyncRegistrar;
import appeng.container.sync.handlers.AEStackInventorySyncHandler;
import appeng.core.AELog;
import appeng.tile.inventory.IAEStackInventory;

public class ContainerPatternModifier extends AEBaseContainer implements IPatternValueContainer, IVirtualSlotSource {

    private final PatternModifierInventory it;

    @Override
    public void updateVirtualSlot(StorageName name, int slotId, IAEStack<?> aes) {
        if (this.it == null) return;
        final IAEStackInventory inv = this.it.getAEInventoryByName(name);
        if (inv == null || slotId < 0 || slotId >= inv.getSizeInventory()) return;
        if (aes == null) {
            inv.putAEStackInSlot(slotId, null);
            return;
        }
        final IAEStack<?> stored = aes.copy();
        stored.setStackSize(1);
        inv.putAEStackInSlot(slotId, stored);
    }

    @Override
    public StorageName getAEStorageName(Slot slot) {
        return null;
    }

    public final AEStackInventorySyncHandler replaceSourceSync;
    public final AEStackInventorySyncHandler replaceTargetSync;
    private static final ItemStack encodePattern = AEApi.instance()
        .definitions()
        .items()
        .encodedPattern()
        .maybeStack(1)
        .orNull();
    private static final ItemStack ultimatePattern = AEApi.instance()
        .definitions()
        .items()
        .encodedUltimatePattern()
        .maybeStack(1)
        .orNull();
    private final IInventory patterns;

    public ContainerPatternModifier(InventoryPlayer ip, ITerminalHost host) {
        super(ip, host);
        if (!(host instanceof PatternModifierInventory patternModifier)) {
            this.it = null;
            this.patterns = null;
            this.replaceSourceSync = null;
            this.replaceTargetSync = null;
            return;
        }
        this.it = patternModifier;
        this.patterns = this.it.getInventoryByName(Constants.PATTERN);
        final SyncRegistrar sync = this.syncRegistrar();
        final IAEStackInventory sourceInv = this.it.getAEInventoryByName(StorageName.CRAFTING_INPUT);
        final IAEStackInventory targetInv = this.it.getAEInventoryByName(StorageName.CRAFTING_OUTPUT);
        this.replaceSourceSync = sourceInv == null ? null : sync.aeStackInventory("replaceSource", sourceInv);
        this.replaceTargetSync = targetInv == null ? null : sync.aeStackInventory("replaceTarget", targetInv);
        if (this.patterns != null) {
            for (int i = 0; i < this.patterns.getSizeInventory(); i++) {
                int x = (i % 9) * 18 + 8;
                int y = (i / 9) * 18 + 19;
                this.addSlotToContainer(new SlotEncodedPatternInput(this.patterns, i, x, y, ip));
            }
        }
        this.lockPlayerInventorySlot(it.getInventorySlot());
        this.bindPlayerInventory(ip, 0, 125);
    }

    public IAEStack<?> getReplaceAEStack(StorageName name) {
        if (this.it == null) return null;
        final IAEStackInventory inv = this.it.getAEInventoryByName(name);
        return inv == null ? null : inv.getAEStackInSlot(0);
    }

    public void clearPattern() {
        if (this.patterns == null) return;
        int blankPattern = 0;
        for (int i = 0; i < this.patterns.getSizeInventory(); i++) {
            final ItemStack itemStack = this.patterns.getStackInSlot(i);
            if (itemStack != null) {
                blankPattern++;
                this.patterns.setInventorySlotContents(i, null);
            }
        }
        if (blankPattern <= 0) return;
        final ItemStack pattern = AEApi.instance()
            .definitions()
            .materials()
            .blankPattern()
            .maybeStack(blankPattern)
            .orNull();
        if (!getPlayerInv().addItemStackToInventory(pattern)) {
            this.dropItem(pattern);
        }
    }

    protected void dropItem(ItemStack is) {
        if (is == null || is.stackSize <= 0) return;
        ItemStack itemStack = is.copy();
        int i = itemStack.getMaxStackSize();
        while (itemStack.stackSize > 0) {
            if (i > itemStack.stackSize) {
                if (!getPlayerInv().addItemStackToInventory(itemStack.copy())) {
                    getPlayerInv().player.entityDropItem(itemStack.copy(), 0);
                }
                break;
            } else {
                itemStack.stackSize -= i;
                ItemStack item = itemStack.copy();
                item.stackSize = i;
                if (!getPlayerInv().addItemStackToInventory(item)) {
                    getPlayerInv().player.entityDropItem(item, 0);
                }
            }
        }
    }

    public void replacePattern() {
        if (this.patterns == null) return;
        final IAEStack<?> source = this.getReplaceAEStack(StorageName.CRAFTING_INPUT);
        if (source == null) return;
        final IAEStack<?> target = this.getReplaceAEStack(StorageName.CRAFTING_OUTPUT);
        final EntityPlayer player = this.getPlayerInv().player;
        int rewritten = 0;
        int skipped = 0;
        for (int i = 0; i < patterns.getSizeInventory(); i++) {
            final ItemStack stack = patterns.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ICraftingPatternItem cpi)) continue;
            try {
                final ICraftingPatternDetails details = stack.getItem() instanceof ItemFluidEncodedPattern fluidPattern
                    ? fluidPattern.getPatternForItem(stack, player.worldObj)
                    : cpi.getPatternForItem(stack, player.worldObj);
                if (details == null) {
                    skipped++;
                    continue;
                }

                final IAEStack<?>[] inputs = details.getAEInputs();
                final IAEStack<?>[] outputs = details.getAEOutputs();
                if (inputs == null || outputs == null) {
                    skipped++;
                    continue;
                }

                final IAEStack<?>[] in = this.replacePattern(inputs, source, target, details);
                final IAEStack<?>[] out = this.replacePattern(outputs, source, target, details);
                if (this.encode(details, in, out, i)) {
                    rewritten++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                skipped++;
                AELog.error(e, "[AE2Thing] Failed to rewrite pattern in slot " + i);
            }
        }
        if (skipped > 0) {
            player.addChatMessage(new ChatComponentTranslation("ae2thing.chat.pattern_modifier.skipped", skipped));
        } else if (rewritten == 0) {
            player.addChatMessage(new ChatComponentTranslation("ae2thing.chat.pattern_modifier.no_match"));
        }
    }

    protected ItemStack stampAuthor(ItemStack patternStack) {
        ItemStackNBT.setString(patternStack, "author", this.getPlayerInv().player.getCommandSenderName());
        return patternStack;
    }

    private boolean encode(ICraftingPatternDetails cpi, IAEStack<?>[] in, IAEStack<?>[] out, int slot) {
        if (encodePattern == null || ultimatePattern == null) {
            AELog.warn("[AE2Thing] Cannot rewrite pattern in slot %d: pattern item is missing", slot);
            return false;
        }
        final ItemStackNBT source = ItemStackNBT.of(cpi.getPattern());
        final boolean inputOnly = source.getBoolean("tunnel");
        if (countNonEmpty(in) == 0 || (!inputOnly && countNonEmpty(out) == 0)) {
            AELog.warn("[AE2Thing] Skip rewriting pattern in slot %d: it would become an empty pattern", slot);
            return false;
        }
        final ItemStack base = inputOnly && cpi.getPattern() != null ? new ItemStack(
            cpi.getPattern()
                .getItem(),
            1,
            cpi.getPattern()
                .getItemDamage())
            : (cpi.isCraftable() ? encodePattern : ultimatePattern);
        final ItemStack cp = base.copy();
        cp.setTagCompound(source.copy());
        ItemStackNBT.of(cp)
            .removeTag("Inputs")
            .removeTag("Outputs")
            .setTag("in", list2tagList(in))
            .setTag("out", list2tagList(out));
        this.patterns.setInventorySlotContents(slot, stampAuthor(cp));
        return true;
    }

    private NBTTagList list2tagList(IAEStack<?>[] list) {
        NBTTagList nbtTagList = new NBTTagList();
        for (final IAEStack<?> is : list) {
            final IAEStack<?> stack = Util.toPatternStack(is);
            nbtTagList.appendTag(stack == null ? new NBTTagCompound() : stack.toNBTGeneric());
        }
        return nbtTagList;
    }

    private static int countNonEmpty(IAEStack<?>[] list) {
        int count = 0;
        for (final IAEStack<?> stack : list) {
            if (stack != null) count++;
        }
        return count;
    }

    private IAEStack<?>[] replacePattern(IAEStack<?>[] list, IAEStack<?> source, IAEStack<?> target,
        ICraftingPatternDetails details) {
        final IAEStack<?>[] results = new IAEStack<?>[list.length];
        for (int i = 0; i < list.length; i++) {
            final IAEStack<?> aes = list[i];
            if (aes == null) {
                results[i] = null;
                continue;
            }
            if (!aes.isSameType(source)) {
                results[i] = aes;
                continue;
            }
            if (target == null) {
                results[i] = details.isCraftable() ? aes : null;
                continue;
            }
            if (details.isCraftable()
                && !(target.isItem() && details.isValidItemForSlot(i, target, this.getPlayerInv().player.worldObj))) {
                results[i] = aes;
                continue;
            }
            final IAEStack<?> replaced = target.copy();
            replaced.setStackSize(aes.getStackSize());
            results[i] = replaced;
        }
        return results;
    }

    @Override
    public boolean isValidContainer() {
        return true;
    }
}
