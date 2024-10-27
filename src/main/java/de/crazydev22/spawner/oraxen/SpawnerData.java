package de.crazydev22.spawner.oraxen;

import com.jeff_media.customblockdata.CustomBlockData;
import de.crazydev22.spawner.MythicSpawner;
import io.lumine.mythic.bukkit.MythicBukkit;
import lombok.*;
import lombok.extern.java.Log;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Random;

@Log
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class SpawnerData {
    private static final NamespacedKey KEY = new NamespacedKey(MythicSpawner.getInstance(), "spawner");
    private static final Random RANDOM = new Random();

    private final Block block;

    private @NonNull String mobType = "";
    private int playerRange = 16;
    private int minDelay = 200;
    private int maxDelay = 800;
    private int range = 4;
    private int maxEntities = 6;
    private int spawnCount = 4;
    private double minLevel = 1.0;
    private double maxLevel = 1.0;
    private int nextTick = 0;
    private boolean dirty = true;

    @Contract("_ -> this")
    public SpawnerData setMobType(@NonNull String mobType) {
        this.mobType = mobType;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setPlayerRange(int playerRange) {
        this.playerRange = playerRange;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setMinDelay(int minDelay) {
        if (minDelay != this.minDelay)
            setNextTick(nextDelay(minDelay, maxDelay));
        this.minDelay = minDelay;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setMaxDelay(int maxDelay) {
        if (maxDelay != this.maxDelay)
            setNextTick(nextDelay(minDelay, maxDelay));
        this.maxDelay = maxDelay;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setRange(int range) {
        this.range = range;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setMaxEntities(int maxEntities) {
        this.maxEntities = maxEntities;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
        dirty = true;
        return this;
    }

    @Contract(" -> this")
    public SpawnerData skipTick() {
        return setNextTick(nextDelay(minDelay, maxDelay));
    }

    @Contract("_ -> this")
    public SpawnerData setNextTick(int nextTick) {
        this.nextTick = Bukkit.getCurrentTick() + nextTick;
        dirty = true;
        return this;
    }

    public boolean shouldTick() {
        return nextTick <= Bukkit.getCurrentTick() || dirty;
    }

    public void tick() {
        if (nextTick > Bukkit.getCurrentTick())
            return;
        if (block.getWorld().getNearbyPlayers(block.getLocation(), playerRange, player -> player.getGameMode() != GameMode.SPECTATOR).isEmpty()) {
            setNextTick(10);
            return;
        }

        setNextTick(nextDelay(minDelay, maxDelay));
        var diameter = getRange() * 2 + 1;
        BoundingBox box = new BoundingBox(0, 0, 0, diameter, 4, diameter)
                .shift(block.getLocation().subtract(getRange() + 1, 0, getRange() + 1));

        var api = MythicBukkit.inst().getMobManager();
        var mobs = api.getActiveMobs(mob -> {
            var pos = mob.getLocation();
            return box.contains(pos.getX(), pos.getY(), pos.getZ());
        });
        mobs.removeIf(mob -> !mob.getMobType().equals(mobType));
        if (mobs.size() >= getMaxEntities())
            return;

        int count = Math.min(getSpawnCount(), getMaxEntities() - mobs.size());
        while (count-- > 0) {
            var loc = pickLocation(box, block.getWorld());
            var block = loc.getBlock()
                    .getRelative(BlockFace.UP);
            if (block.getType().isCollidable())
                continue;
            block = block.getRelative(BlockFace.UP);
            if (block.getType().isCollidable())
                continue;

            api.spawnMob(mobType, loc, random(minLevel, maxLevel));
        }
    }

    private static Location pickLocation(BoundingBox box, World world) {
        double x = random(box.getMinX(), box.getMaxX());
        double y = random(box.getMinY(), box.getMaxY());
        double z = random(box.getMinZ(), box.getMaxZ());
        float yaw = (float) random(0, 360);
        float pitch = (float) random(0, 360);
        return new Location(world, ((int) x) + 0.5, (int) y, ((int) z) + 0.5, yaw, pitch);
    }

    private static double random(double min, double max) {
        if (min == max)
            return min;
        return RANDOM.nextDouble() * (max - min) + min;
    }

    private static int nextDelay(int minDelay, int maxDelay) {
        if (minDelay == maxDelay)
            return minDelay;
        return RANDOM.nextInt(maxDelay - minDelay) + minDelay;
    }

    @Nullable
    public static SpawnerData deserialize(@NotNull Block block) throws IOException {
        var data = new CustomBlockData(block, MythicSpawner.getInstance());
        var bytes = data.get(KEY, PersistentDataType.BYTE_ARRAY);
        if (bytes == null)
            return null;

        var in = new ByteArrayInputStream(bytes);
        try (var din = new DataInputStream(in)) {
            return new SpawnerData(block).deserialize(din);
        }
    }

    public void serialize(boolean force) throws IOException {
        if (!force && !isDirty())
            return;

        var data = new CustomBlockData(block, MythicSpawner.getInstance());
        var out = new ByteArrayOutputStream();
        try (var dos = new DataOutputStream(out)) {
            serialize(dos);
        }
        data.set(KEY, PersistentDataType.BYTE_ARRAY, out.toByteArray());
    }

    public void delete() {
        var data = new CustomBlockData(block, MythicSpawner.getInstance());
        data.remove(KEY);
        dirty = false;
    }

    @Contract("_ -> this")
    public SpawnerData deserialize(@NonNull DataInputStream din) throws IOException {
        mobType = din.readUTF();
        playerRange = din.readInt();
        minDelay = din.readInt();
        maxDelay = din.readInt();
        range = din.readInt();
        maxEntities = din.readInt();
        spawnCount = din.readInt();
        minLevel = din.readDouble();
        maxLevel = din.readDouble();
        nextTick = Bukkit.getCurrentTick() + din.readInt();
        dirty = false;
        return this;
    }


    public void serialize(@NonNull DataOutputStream dos) throws IOException {
        dos.writeUTF(getMobType());
        dos.writeInt(getPlayerRange());
        dos.writeInt(getMinDelay());
        dos.writeInt(getMaxDelay());
        dos.writeInt(getRange());
        dos.writeInt(getMaxEntities());
        dos.writeInt(getSpawnCount());
        dos.writeDouble(getMinLevel());
        dos.writeDouble(getMaxLevel());
        dos.writeInt(Math.max(0, getNextTick() - Bukkit.getCurrentTick()));
        dirty = false;
    }
}
