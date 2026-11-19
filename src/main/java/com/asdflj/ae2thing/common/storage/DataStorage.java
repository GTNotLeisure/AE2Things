package com.asdflj.ae2thing.common.storage;

import java.util.Comparator;
import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEItemStackType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class DataStorage implements IDataStorage {

    private static final Comparator<IAEStack<?>> BY_SIZE_DESC = Comparator
        .<IAEStack<?>>comparingLong(IAEStack::getStackSize)
        .reversed();

    private IItemList<IAEItemStack> items;
    private IItemList<IAEFluidStack> fluids;
    private IItemList<IAEStack<?>> others;
    private final UUID uuid;
    private final IAEStackType<?> stackType;
    private long storedCount = -1;
    private long storedTypes = -1;
    private long revision;
    private ObjectList<IAEStack<?>> pageCache;
    private long pageCacheRevision = -1;
    private int pageCacheLimit;

    public DataStorage(UUID uuid, IAEStackType<?> stackType) {
        this.uuid = uuid;
        this.stackType = stackType;
    }

    @Override
    public IAEStackType<?> getStackType() {
        return this.stackType;
    }

    public long getStoredCount() {
        if (this.storedCount < 0) this.recount();
        return this.storedCount;
    }

    public long getStoredTypes() {
        if (this.storedTypes < 0) this.recount();
        return this.storedTypes;
    }

    public void addStored(final long count, final long types) {
        this.revision++;
        if (this.storedCount >= 0) this.storedCount += count;
        if (this.storedTypes >= 0) this.storedTypes += types;
    }

    public ObjectList<IAEStack<?>> getSortedContents(int limit) {
        if (this.pageCache != null && this.pageCacheRevision == this.revision && this.pageCacheLimit >= limit) {
            return this.pageCache;
        }
        final ObjectList<IAEStack<?>> sorted;
        if (this.getStoredTypes() > limit) {
            sorted = topK(this.list(), limit);
        } else {
            final ObjectArrayList<IAEStack<?>> all = new ObjectArrayList<>();
            for (final IAEStack<?> stack : this.list()) {
                if (stack.getStackSize() > 0) all.add(stack);
            }
            all.sort(BY_SIZE_DESC);
            sorted = all;
        }
        this.pageCache = sorted;
        this.pageCacheRevision = this.revision;
        this.pageCacheLimit = limit;
        return sorted;
    }

    private static ObjectList<IAEStack<?>> topK(Iterable<? extends IAEStack<?>> source, int limit) {
        final ObjectArrayList<IAEStack<?>> heap = new ObjectArrayList<>(limit + 1);
        for (final IAEStack<?> stack : source) {
            final long size = stack.getStackSize();
            if (size <= 0) continue;
            if (heap.size() < limit) {
                heap.add(stack);
                siftUp(heap, heap.size() - 1);
            } else if (size > heap.getFirst()
                .getStackSize()) {
                    heap.set(0, stack);
                    siftDown(heap);
                }
        }
        heap.sort(BY_SIZE_DESC);
        return heap;
    }

    private static void siftUp(ObjectList<IAEStack<?>> heap, int index) {
        final IAEStack<?> stack = heap.get(index);
        final long size = stack.getStackSize();
        while (index > 0) {
            final int parent = (index - 1) >> 1;
            final IAEStack<?> parentStack = heap.get(parent);
            if (parentStack.getStackSize() <= size) break;
            heap.set(index, parentStack);
            index = parent;
        }
        heap.set(index, stack);
    }

    private static void siftDown(ObjectList<IAEStack<?>> heap) {
        final IAEStack<?> root = heap.getFirst();
        final long size = root.getStackSize();
        final int count = heap.size();
        int index = 0;
        while (true) {
            final int left = index * 2 + 1;
            if (left >= count) break;
            final int right = left + 1;
            int min = left;
            if (right < count && heap.get(right)
                .getStackSize()
                < heap.get(left)
                    .getStackSize())
                min = right;
            final IAEStack<?> child = heap.get(min);
            if (child.getStackSize() >= size) break;
            heap.set(index, child);
            index = min;
        }
        heap.set(index, root);
    }

    public Iterable<? extends IAEStack<?>> list() {
        if (this.getStackType() == AEItemStackType.ITEM_STACK_TYPE) {
            return this.getItems();
        } else if (this.getStackType() == AEFluidStackType.FLUID_STACK_TYPE) {
            return this.getFluids();
        } else {
            return this.getOthers();
        }
    }

    private void recount() {
        final Iterable<? extends IAEStack<?>> list = this.list();
        long count = 0;
        long types = 0;
        for (final IAEStack<?> stack : list) {
            if (stack.getStackSize() <= 0) continue;
            count += stack.getStackSize();
            types++;
        }
        this.storedCount = count;
        this.storedTypes = types;
    }

    @Override
    public IItemList<IAEItemStack> getItems() {
        if (this.items == null) {
            this.items = AEApi.instance()
                .storage()
                .createPrimitiveItemList();
        }
        return items;
    }

    @Override
    public IItemList<IAEFluidStack> getFluids() {
        if (this.fluids == null) {
            this.fluids = AEApi.instance()
                .storage()
                .createFluidList();
        }
        return fluids;
    }

    @Override
    public IItemList<IAEStack<?>> getOthers() {
        if (this.others == null) {
            this.others = AEApi.instance()
                .storage()
                .createAEStackList();
        }
        return others;
    }

    @Override
    public boolean isEmpty() {
        if (this.getStackType() == AEItemStackType.ITEM_STACK_TYPE) {
            return this.getItems()
                .isEmpty();
        } else if (this.getStackType() == AEFluidStackType.FLUID_STACK_TYPE) {
            return this.getFluids()
                .isEmpty();
        } else {
            return this.getOthers()
                .isEmpty();
        }
    }

    @Override
    public String getUUID() {
        return this.uuid.toString();
    }

    @Override
    public UUID getRawUUID() {
        return this.uuid;
    }

    public static DataStorage readFromNBT(UUID uuid, NBTTagList data) {
        return readFromNBT(uuid, data, AEItemStackType.ITEM_STACK_TYPE);
    }

    public static DataStorage readFromNBT(UUID uuid, NBTTagList data, IAEStackType<?> stackType) {
        DataStorage storage = new DataStorage(uuid, stackType);
        storage.readFromNBT(data);
        return storage;
    }

    @Override
    public void readFromNBT(NBTTagList data) {
        if (this.getStackType() == AEItemStackType.ITEM_STACK_TYPE) {
            for (final IAEItemStack ais : this.readList(data)) {
                this.getItems()
                    .add(ais);
            }
        } else if (this.getStackType() == AEFluidStackType.FLUID_STACK_TYPE) {
            for (final IAEFluidStack ais : this.readFluidList(data)) {
                this.getFluids()
                    .add(ais);
            }
        } else {
            for (final IAEStack<?> as : this.readOtherList(data)) {
                this.getOthers()
                    .add(as);
            }
        }
        this.storedCount = -1;
        this.storedTypes = -1;
        this.revision++;
    }

    private IItemList<IAEStack<?>> readOtherList(final NBTTagList tag) {
        final IItemList<IAEStack<?>> out = AEApi.instance()
            .storage()
            .createAEStackList();
        if (tag == null) {
            return out;
        }

        final IAEStackType<?> type = this.getStackType();
        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEStack<?> ais = type.loadStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }

    private IItemList<IAEFluidStack> readFluidList(final NBTTagList tag) {
        final IItemList<IAEFluidStack> out = AEApi.instance()
            .storage()
            .createFluidList();
        if (tag == null) {
            return out;
        }
        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEFluidStack ais = AEFluidStack.loadFluidStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }
        return out;
    }

    private IItemList<IAEItemStack> readList(final NBTTagList tag) {
        final IItemList<IAEItemStack> out = AEApi.instance()
            .storage()
            .createItemList();

        if (tag == null) {
            return out;
        }

        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEItemStack ais = AEItemStack.loadItemStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }

    @Override
    public NBTBase writeToNBT() {
        if (this.getStackType() == AEItemStackType.ITEM_STACK_TYPE) {
            return writeList(this.getItems());
        } else if (this.getStackType() == AEFluidStackType.FLUID_STACK_TYPE) {
            return writeFluidList(this.getFluids());
        } else {
            return writeOtherList(this.getOthers());
        }
    }

    private NBTTagList writeOtherList(final IItemList<IAEStack<?>> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEStack<?> ais : myList) {
            if (ais.getStackSize() > 0) {
                out.appendTag(this.writeOther(ais));
            }
        }

        return out;
    }

    private NBTBase writeOther(IAEStack<?> other) {
        final NBTTagCompound out = new NBTTagCompound();

        if (other != null) {
            other.writeToNBT(out);
        }

        return out;
    }

    private NBTTagList writeFluidList(final IItemList<IAEFluidStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEFluidStack ais : myList) {
            if (ais.getStackSize() > 0) {
                out.appendTag(this.writeFluid(ais));
            }
        }

        return out;
    }

    private NBTBase writeFluid(IAEFluidStack fluid) {
        final NBTTagCompound out = new NBTTagCompound();

        if (fluid != null) {
            fluid.writeToNBT(out);
        }
        return out;
    }

    private NBTTagList writeList(final IItemList<IAEItemStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEItemStack ais : myList) {
            if (ais.getStackSize() > 0) {
                out.appendTag(this.writeItem(ais));
            }
        }

        return out;
    }

    private NBTTagCompound writeItem(final IAEItemStack item) {
        final NBTTagCompound out = new NBTTagCompound();

        if (item != null) {
            item.writeToNBT(out);
        }

        return out;
    }

    public long revision() {
        return this.revision;
    }
}
