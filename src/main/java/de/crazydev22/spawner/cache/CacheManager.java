package de.crazydev22.spawner.cache;

import de.crazydev22.spawner.oraxen.SpawnerData;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record CacheManager(Map<UUID, WorldCache> map) {

    @Override
    @Unmodifiable
    public Map<UUID, WorldCache> map() {
        return Collections.unmodifiableMap(map);
    }

    @NotNull
    public Optional<WorldCache> getWorld(@NotNull UUID world) {
        return Optional.ofNullable(map.get(world));
    }

    public void setData(@NotNull SpawnerData value) {
        var block = value.getBlock();
        var worldCache = map.computeIfAbsent(block.getWorld().getUID(), WorldCache::new);
        worldCache.setData(Position.fromBlock(block), value);
    }

    public boolean hasData(@NotNull Block block) {
        if (!map.containsKey(block.getWorld().getUID())) return false;
        var worldCache = map.get(block.getWorld().getUID());
        return worldCache.getData(Position.fromBlock(block)).isPresent();
    }

    public void removeData(@NotNull Block block) {
        if (!map.containsKey(block.getWorld().getUID())) return;
        var worldCache = map.get(block.getWorld().getUID());
        worldCache.removeData(Position.fromBlock(block), true);
    }

    public void loadChunk(@NotNull Chunk chunk) {
        map.computeIfAbsent(chunk.getWorld().getUID(), WorldCache::new)
                .loadChunk(chunk);
    }

    public void unloadChunk(@NotNull Chunk chunk) {
        var world = chunk.getWorld().getUID();
        var worldCache = map.get(world);
        if (worldCache == null) return;
        worldCache.unloadChunk(chunk);
    }

    public void loadWorld(@NotNull World world) {
        var worldCache = map.computeIfAbsent(world.getUID(), WorldCache::new);
        for (Chunk chunk : world.getLoadedChunks()) {
            worldCache.loadChunk(chunk);
        }
    }

    public void unloadWorld(@NotNull World world) {
        var removed = map.remove(world.getUID());
        if (removed == null) return;
        removed.unload();
    }

    public void unload() {
        map.values().forEach(WorldCache::unload);
        map.clear();
    }
}
