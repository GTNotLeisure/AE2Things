package com.asdflj.ae2thing.common.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public final class SyncedServerConfig {

    public static final Codec<Boolean> BOOLEAN = new Codec<>(
        NBT.TAG_BYTE,
        NBTTagCompound::getBoolean,
        NBTTagCompound::setBoolean);
    public static final Codec<Integer> INTEGER = new Codec<>(
        NBT.TAG_INT,
        NBTTagCompound::getInteger,
        NBTTagCompound::setInteger);
    public static final Codec<String> STRING = new Codec<>(
        NBT.TAG_STRING,
        NBTTagCompound::getString,
        NBTTagCompound::setString);

    private final BooleanSupplier isServer;
    private final Map<String, Consumer<NBTTagCompound>> writers = new LinkedHashMap<>();
    private volatile NBTTagCompound clientValues = new NBTTagCompound();

    public SyncedServerConfig(BooleanSupplier isServer) {
        this.isServer = isServer;
    }

    public <T> Supplier<T> register(String key, Codec<T> codec, T clientFallback, Supplier<T> serverValue) {
        if (writers.putIfAbsent(
            key,
            tag -> codec.write()
                .write(tag, key, serverValue.get()))
            != null) {
            throw new IllegalArgumentException("Duplicate server config key: " + key);
        }
        return () -> {
            if (isServer.getAsBoolean()) return serverValue.get();
            NBTTagCompound values = clientValues;
            return values.hasKey(key, codec.tagType()) ? codec.read()
                .apply(values, key) : clientFallback;
        };
    }

    public NBTTagCompound writeServerValues() {
        NBTTagCompound tag = new NBTTagCompound();
        writers.values()
            .forEach(writer -> writer.accept(tag));
        return tag;
    }

    public void apply(NBTTagCompound tag) {
        clientValues = tag == null ? new NBTTagCompound() : (NBTTagCompound) tag.copy();
    }

    public void clear() {
        clientValues = new NBTTagCompound();
    }

    @FunctionalInterface
    public interface TagWriter<T> {

        void write(NBTTagCompound tag, String key, T value);
    }

    public record Codec<T> (int tagType, BiFunction<NBTTagCompound, String, T> read, TagWriter<T> write) {}
}
