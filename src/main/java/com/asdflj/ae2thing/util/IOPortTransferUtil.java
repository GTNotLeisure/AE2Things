package com.asdflj.ae2thing.util;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IItemList;
import appeng.util.IterationCounter;

public final class IOPortTransferUtil {

    private IOPortTransferUtil() {}

    public static long scaleBudget(long amount, int multiplier, int amountPerUnit) {
        if (amount <= 0) return 0;
        final long limit = Long.MAX_VALUE / Math.max(1, amountPerUnit);
        final int rate = Math.max(1, multiplier);
        return amount > limit / rate ? limit : amount * rate;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IItemList<?> snapshot(IMEInventory<?> source) {
        // Extraction can clean zero-sized entries from the backing list during saveChanges.
        // Never keep an iterator over that live list while transferring its contents.
        return ((IMEInventory) source).getAvailableItems(
            source.getStackType()
                .createList(),
            IterationCounter.fetchNewId());
    }
}
