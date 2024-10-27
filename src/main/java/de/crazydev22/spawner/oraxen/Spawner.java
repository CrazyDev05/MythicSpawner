package de.crazydev22.spawner.oraxen;

import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

@Getter
@EqualsAndHashCode(callSuper = false)
public class Spawner extends Mechanic {
    private final String mobType;
    private final int playerRange;
    private final int minDelay;
    private final int maxDelay;
    private final int range;
    private final int maxEntities;
    private final int spawnCount;
    private final double minLevel;
    private final double maxLevel;

    protected Spawner(MechanicFactory factory, ConfigurationSection section) {
        super(factory, section);
        mobType = section.getString("mobType");
        playerRange = section.getInt("playerRange", 16);
        minDelay = section.getInt("minDelay", 200);
        maxDelay = section.getInt("maxDelay", 800);
        range = section.getInt("range", 4);
        maxEntities = section.getInt("maxEntities", 6);
        spawnCount = section.getInt("spawnCount", 4);
        minLevel = section.getDouble("minLevel", 1.0D);
        maxLevel = section.getDouble("maxLevel", 1.0D);
    }

    public SpawnerData toData(Block block) {
        return new SpawnerData(block)
                .setMobType(mobType)
                .setPlayerRange(playerRange)
                .setMinDelay(minDelay)
                .setMaxDelay(maxDelay)
                .setRange(range)
                .setMaxEntities(maxEntities)
                .setSpawnCount(spawnCount)
                .skipTick();
    }
}
