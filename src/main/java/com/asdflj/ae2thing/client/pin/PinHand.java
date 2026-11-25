package com.asdflj.ae2thing.client.pin;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.pin.PinIdentity;

import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.modules.NEI;

public record PinHand(ItemStack real, ItemStack ghost) {

    public static PinHand read() {
        ItemStack real = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
        ItemStack ghost = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI)
            ? NEI.instance.getDraggingPhantomItem()
            : null;
        return new PinHand(real, ghost);
    }

    public ItemStack effective() {
        return ghost == null ? real : ghost;
    }

    public boolean isEmpty() {
        return real == null && ghost == null;
    }

    public IAEStack<?> pinKey(int button) {
        ItemStack hand = effective();
        if (hand == null) return null;
        if (button == 0) return PinIdentity.fromInventoryItem(hand.copy());
        if (button != 1) return null;
        for (IAEStackType<?> type : AEStackTypeRegistry.getAllTypes()) {
            if (!type.isContainerItemForType(hand)) continue;
            IAEStack<?> content = type.getStackFromContainerItem(hand.copy());
            return content != null && content.getStackSize() > 0 ? content : null;
        }
        return null;
    }
}
