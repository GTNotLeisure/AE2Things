package com.asdflj.ae2thing.util.target;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import org.jetbrains.annotations.Nullable;

/**
 * copy of the chunk lookups from {@link appeng.util.Platform}
 * TODO : remove later
 */
@Deprecated
public final class LoadedTiles {

    private LoadedTiles() {}

    /** Looks up an already loaded chunk without loading it. */
    public static @Nullable Chunk getLoadedChunk(World world, int x, int z) {
        if (world == null) return null;
        final int chunkX = x >> 4;
        final int chunkZ = z >> 4;
        final IChunkProvider provider = world.getChunkProvider();
        if (provider == null || !provider.chunkExists(chunkX, chunkZ)) return null;
        return world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    /** Returns the current tile without loading its chunk. */
    public static @Nullable TileEntity getLoadedTileEntity(World world, int x, int y, int z) {
        if (world == null || !world.blockExists(x, y, z)) return null;
        final TileEntity tile = world.getTileEntity(x, y, z);
        return tile == null || tile.isInvalid() ? null : tile;
    }
}
