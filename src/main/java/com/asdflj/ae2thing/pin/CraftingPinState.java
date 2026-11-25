package com.asdflj.ae2thing.pin;

import java.util.Arrays;
import java.util.List;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.container.sync.SyncCodec;
import appeng.container.sync.SyncCodecs;
import appeng.items.contents.PinList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class CraftingPinState {

    public static final int WORDS = (PinList.CRAFTING_SLOTS + Long.SIZE - 1) / Long.SIZE;
    public static final SyncCodec<long[]> CODEC = SyncCodecs
        .of("ae2thing:craftingPins", (buf, value) -> { for (long word : copy(value)) buf.writeLong(word); }, buf -> {
            long[] value = new long[WORDS];
            for (int i = 0; i < WORDS; i++) value[i] = buf.readLong();
            return copy(value);
        }, CraftingPinState::copy, Arrays::equals);

    private CraftingPinState() {}

    public static List<IAEStack<?>> pendingOutputs(Iterable<? extends ICraftingCPU> cpus) {
        List<IAEStack<?>> result = new ObjectArrayList<>();
        for (ICraftingCPU cpu : cpus) {
            if (!cpu.isBusy()) continue;
            IAEStack<?> output = cpu.getFinalMultiOutput();
            if (output != null) result.add(output.copy());
        }
        return result;
    }

    public static boolean isPending(IAEStack<?> key, List<IAEStack<?>> outputs) {
        if (key == null) return false;
        for (IAEStack<?> output : outputs) {
            if (output != null && key.isSameType(output)) return true;
        }
        return false;
    }

    public static boolean isPending(long[] bits, int index) {
        return index >= 0 && index < PinList.CRAFTING_SLOTS
            && bits != null
            && index / Long.SIZE < bits.length
            && (bits[index / Long.SIZE] & (1L << (index % Long.SIZE))) != 0;
    }

    public static long[] copy(long[] bits) {
        long[] result = bits == null ? new long[WORDS] : Arrays.copyOf(bits, WORDS);
        int remainder = PinList.CRAFTING_SLOTS % Long.SIZE;
        if (remainder != 0) result[WORDS - 1] &= (1L << remainder) - 1;
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean canCraft(IAEStack<?> key, IMEMonitor<?> monitor) {
        if (key == null || monitor == null) return false;
        IAEStack<?> authoritative = (IAEStack<?>) ((IMEMonitor) monitor).getStorageList()
            .findPrecise(key);
        return authoritative != null && authoritative.isCraftable();
    }
}
