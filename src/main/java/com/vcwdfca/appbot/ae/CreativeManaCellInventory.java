package com.vcwdfca.appbot.ae;

import org.jetbrains.annotations.NotNull;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;

public class CreativeManaCellInventory implements IMEInventoryHandler<AEManaStack> {

    private static final long DISPLAY_AMOUNT = 1L << 53;

    public CreativeManaCellInventory() {}

    @Override
    public AEManaStack injectItems(AEManaStack input, Actionable mode, BaseActionSource src) {
        return null;
    }

    @Override
    public AEManaStack extractItems(AEManaStack request, Actionable mode, BaseActionSource src) {
        return request == null ? null : request.copy();
    }

    @Override
    public IItemList<AEManaStack> getAvailableItems(IItemList<AEManaStack> out, int iteration) {
        out.addStorage(new AEManaStack(DISPLAY_AMOUNT));
        return out;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(AEManaStack input) {
        return true;
    }

    @Override
    public boolean canAccept(AEManaStack input) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

    @Override
    public @NotNull IAEStackType<?> getStackType() {
        return AEManaStackType.MANA_STACK_TYPE;
    }
}
