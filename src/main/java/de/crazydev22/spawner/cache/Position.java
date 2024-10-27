package de.crazydev22.spawner.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private UUID world = null;
    private int x = 0;
    private int y = 0;
    private int z = 0;

    @NotNull
    public static Position fromBlock(@NotNull org.bukkit.block.Block block) {
        return new Position(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    @NotNull
    public static Position fromLocation(@NotNull Location location) {
        return new Position(location.getWorld() != null ? location.getWorld().getUID() : null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @NotNull
    public Location toLocation() {
        World world = Bukkit.getWorld(this.world);
        if (world == null) throw new IllegalArgumentException("World not found: " + this.world);
        return new Location(world, this.x, this.y, this.z);
    }

    @NotNull
    public ChunkPosition getChunkPosition() {
        return new ChunkPosition(this.world, this.x >> 4, this.z >> 4);
    }
}