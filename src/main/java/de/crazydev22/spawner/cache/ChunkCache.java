package de.crazydev22.spawner.cache;

import de.crazydev22.spawner.oraxen.SpawnerData;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Log
public record ChunkCache(@NotNull ChunkPosition position, @NotNull Map<Position, SpawnerData> map) {

    ChunkCache(@NotNull ChunkPosition position) {
        this(position, new HashMap<>());
    }

    @Override
    @Unmodifiable
    public Map<Position, SpawnerData> map() {
        return Collections.unmodifiableMap(map);
    }

    @Nullable
    public SpawnerData get(@NotNull Position position) {
        if (!this.position.equals(position.getChunkPosition()))
            throw new IllegalArgumentException("Position " + position + " is not in chunk " + this.position);

        return map.get(position);
    }

    public void set(@NonNull Position position, @NonNull SpawnerData value) {
        if (!this.position.equals(position.getChunkPosition()))
            throw new IllegalArgumentException("Position " + position + " is not in chunk " + this.position);
        if (map.containsKey(position))
            throw new IllegalArgumentException("Position " + position + " is already set");
        map.put(position, value);
    }

    public void remove(@NonNull Position position, boolean delete) {
        if (!this.position.equals(position.getChunkPosition()))
            throw new IllegalArgumentException("Position " + position + " is not in chunk " + this.position);
        var removed = map.remove(position);
        if (removed == null || !delete) return;
        removed.delete();
    }

    public void unload() {
        map.values().forEach(data -> {
            try {
                data.serialize(false);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to save spawner data at " + data.getBlock().getLocation(), e);
            }
        });
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
