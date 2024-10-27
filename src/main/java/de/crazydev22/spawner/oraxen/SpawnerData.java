package de.crazydev22.mythicSpawner.oraxen;

import com.jeff_media.customblockdata.CustomBlockData;
import de.crazydev22.mythicSpawner.MythicSpawner;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Random;

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
        this.minDelay = minDelay;
        dirty = true;
        return this;
    }

    @Contract("_ -> this")
    public SpawnerData setMaxDelay(int maxDelay) {
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

    @Contract("_ -> this")
    public SpawnerData setNextTick(int nextTick) {
        this.nextTick = nextTick;
        dirty = true;
        return this;
    }

    public boolean shouldTick() {
        return nextTick <= Bukkit.getCurrentTick() || dirty;
    }

    public void tick() {
        if (nextTick > Bukkit.getCurrentTick())
            return;
        setNextTick(Bukkit.getCurrentTick() + nextDelay(minDelay, maxDelay));

        //TODO spawn
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
        dos.writeInt(Math.max(0, getNextTick() - Bukkit.getCurrentTick()));
        dirty = false;
    }
}
