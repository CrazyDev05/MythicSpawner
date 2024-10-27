package de.crazydev22.spawner;

import art.arcane.multiburst.MultiBurst;
import com.jeff_media.customblockdata.CustomBlockData;
import de.crazydev22.spawner.cache.CacheManager;
import de.crazydev22.spawner.cache.ChunkCache;
import de.crazydev22.spawner.cache.WorldCache;
import de.crazydev22.spawner.oraxen.SpawnerData;
import de.crazydev22.spawner.oraxen.SpawnerFactory;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenNativeMechanicsRegisteredEvent;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class MythicSpawner extends JavaPlugin implements Listener {
    @Getter
    private static MythicSpawner instance;
    @Getter
    private final CacheManager cacheManager = new CacheManager(new HashMap<>());

    public static MultiBurst burst;

    public MythicSpawner() {
        instance = this;
    }

    @Override
    public void onEnable() {
        burst = new MultiBurst("MythicSpawner", 1);

        CustomBlockData.registerListener(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::tick, 0, 2);
        Bukkit.getWorlds().forEach(cacheManager::loadWorld);
    }

    @Override
    public void onDisable() {
        cacheManager.unload();
        burst.close();
    }

    private void tick() {
        cacheManager.map()
                .values()
                .stream()
                .map(WorldCache::map)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(ChunkCache::map)
                .map(Map::values)
                .flatMap(Collection::stream)
                .filter(SpawnerData::shouldTick)
                .forEach(data -> {
                    try {
                        data.tick();
                    } catch (Throwable e) {
                        getLogger().log(Level.SEVERE, "Failed to tick spawner at " + data.getBlock().getLocation(), e);
                    }

                    try {
                        data.serialize(false);
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Failed to save spawner data at " + data.getBlock().getLocation(), e);
                    }
                });
    }

    @EventHandler
    private void onMechanicRegister(OraxenNativeMechanicsRegisteredEvent event) {
        getLogger().info("Registering oraxen mechanic");
        MechanicsManager.registerMechanicFactory("spawner", new SpawnerFactory(this), true);
        OraxenItems.loadItems();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk())
            return;

        cacheManager.loadChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onChunkUnload(ChunkUnloadEvent event) {
        cacheManager.unloadChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onWorldUnload(WorldUnloadEvent event) {
        cacheManager.unloadWorld(event.getWorld());
    }
}
