package com.vcwdfca.appbot.ae;

import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import vazkii.botania.api.mana.IManaReceiver;

public class ManaExternalInventory implements IMEInventory<AEManaStack> {

    private final IManaReceiver receiver;

    public ManaExternalInventory(IManaReceiver receiver) {
        this.receiver = receiver;
    }

    @Nullable
    public static ManaExternalInventory of(@Nullable TileEntity tile) {
        return tile instanceof IManaReceiver receiver ? new ManaExternalInventory(receiver) : null;
    }

    public int getStored() {
        return this.receiver.getCurrentMana();
    }

    @Override
    public AEManaStack injectItems(AEManaStack input, Actionable mode, BaseActionSource src) {
        if (input == null || input.getStackSize() <= 0) {
            return null;
        }
        final int amount = (int) Math.min(input.getStackSize(), Integer.MAX_VALUE);
        final int inserted = ManaHelper.insert(this.receiver, amount, mode);
        if (inserted >= amount) {
            return null;
        }
        final AEManaStack rest = input.copy();
        rest.setStackSize(input.getStackSize() - inserted);
        return rest;
    }

    @Override
    public AEManaStack extractItems(AEManaStack request, Actionable mode, BaseActionSource src) {
        if (request == null || request.getStackSize() <= 0) {
            return null;
        }
        final int amount = (int) Math.min(request.getStackSize(), Integer.MAX_VALUE);
        final int taken = ManaHelper.extract(this.receiver, amount, mode);
        if (taken <= 0) {
            return null;
        }
        final AEManaStack out = request.copy();
        out.setStackSize(taken);
        return out;
    }

    @Override
    public IItemList<AEManaStack> getAvailableItems(IItemList<AEManaStack> out, int iteration) {
        final int mana = this.receiver.getCurrentMana();
        if (mana > 0) {
            out.addStorage(new AEManaStack(mana));
        }
        return out;
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return AEManaStackType.MANA_STACK_TYPE;
    }
}
