package de.crazydev22.spawner.cache;

import com.jeff_media.customblockdata.CustomBlockData;
import de.crazydev22.spawner.MythicSpawner;
import de.crazydev22.spawner.oraxen.SpawnerData;
import lombok.extern.java.Log;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@Log
public record WorldCache(@NotNull UUID world, @NotNull Map<ChunkPosition, ChunkCache> map) {

    public WorldCache(@NotNull UUID world) {
        this(world, new HashMap<>());
    }

    @Override
    @Unmodifiable
    public Map<ChunkPosition, ChunkCache> map() {
        return Collections.unmodifiableMap(map);
    }

    @NotNull
    public Optional<ChunkCache> getChunk(@NotNull ChunkPosition position) {
        return Optional.ofNullable(map.get(position));
    }

    @NotNull
    public Optional<SpawnerData> getData(@NotNull Position position) {
        return getChunk(position.getChunkPosition())
                .map(chunk -> chunk.get(position));
    }

    public void setData(@NotNull Position position, @NotNull SpawnerData value) {
        getOrCreateChunk(position.getChunkPosition())
                .set(position, value);
    }

    public void removeData(@NotNull Position position, boolean delete) {
        getChunk(position.getChunkPosition())
                .ifPresent(chunk -> {
                    chunk.remove(position, delete);
                    if (chunk.isEmpty()) {
                        map.remove(chunk.position());
                    }
                });
    }

    @NotNull
    public ChunkCache getOrCreateChunk(@NotNull ChunkPosition position) {
        return map.computeIfAbsent(position, ChunkCache::new);
    }

    public void loadChunk(Chunk chunk) {
        if (!world.equals(chunk.getWorld().getUID()))
            throw new IllegalArgumentException("Chunk " + chunk + " is not in world " + world);

        ChunkPosition position = new ChunkPosition(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        if (map.containsKey(position))
            throw new IllegalArgumentException("Chunk " + chunk + " is already loaded");

        map.computeIfAbsent(position, p -> {
            var blocks = CustomBlockData.getBlocksWithCustomData(MythicSpawner.getInstance(), chunk);
            if (blocks.isEmpty())
                return null;

            var map = new HashMap<Position, SpawnerData>();
            var burst = MythicSpawner.burst.burst(blocks.size());

            blocks.forEach(block -> burst.queue(() -> {
                try {
                    var data = SpawnerData.deserialize(block);
                    map.put(Position.fromBlock(block), data);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failed to load spawner data at " + block.getLocation(), e);
                }
            }));

            burst.complete();

            return new ChunkCache(p, map);
        });
    }

    public void unloadChunk(Chunk chunk) {
        if (!world.equals(chunk.getWorld().getUID()))
            throw new IllegalArgumentException("Chunk " + chunk + " is not in world " + world);

        ChunkPosition position = new ChunkPosition(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        if (!map.containsKey(position))
            return;

        map.remove(position).unload();
    }

    public void unload() {
        map.values().forEach(ChunkCache::unload);
    }
}
