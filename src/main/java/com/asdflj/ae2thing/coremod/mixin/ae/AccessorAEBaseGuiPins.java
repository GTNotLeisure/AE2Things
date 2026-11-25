package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.slots.VirtualMESlot;

@SuppressWarnings("UnusedMixin")
@Mixin(AEBaseGui.class)
public interface AccessorAEBaseGuiPins {

    @Accessor(value = "virtualSlots", remap = false)
    List<VirtualMESlot> ae2t$getVirtualSlots();
}
