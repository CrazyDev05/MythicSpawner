package de.crazydev22.mythicSpawner.oraxen;

import de.crazydev22.mythicSpawner.MythicSpawner;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import org.bukkit.configuration.ConfigurationSection;

public class SpawnerFactory extends MechanicFactory {
    public SpawnerFactory(MythicSpawner plugin) {
        super("spawner");
        MechanicsManager.registerListeners(plugin, "spawner", new SpawnerListener(this, plugin));
    }

    @Override
    public Spawner parse(ConfigurationSection section) {
        Spawner spawner = new Spawner(this, section);
        addToImplemented(spawner);
        return spawner;
    }
}
