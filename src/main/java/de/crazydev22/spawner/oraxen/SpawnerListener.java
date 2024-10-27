package de.crazydev22.mythicSpawner.oraxen;

import de.crazydev22.mythicSpawner.MythicSpawner;
import de.crazydev22.mythicSpawner.cache.Position;
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockBreakEvent;
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockPlaceEvent;
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockBreakEvent;
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockPlaceEvent;
import lombok.extern.java.Log;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

@Log
public record SpawnerListener(SpawnerFactory factory, MythicSpawner plugin) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        onBlockPlace(event.getBlock(), event.getItemInHand());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNoteBlockPlace(OraxenNoteBlockPlaceEvent event) {
        onBlockPlace(event.getBlock(), event.getItemInHand());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStringBlockPlace(OraxenStringBlockPlaceEvent event) {
        onBlockPlace(event.getBlock(), event.getItemInHand());
    }

    private void onBlockPlace(Block block, ItemStack itemStack) {
        if (!factory.isNotImplementedIn(itemStack))
            return;
        Spawner spawner = (Spawner) factory.getMechanic(itemStack);
        if (spawner == null)
            return;

        var opt = plugin.getCacheManager().getWorld(block.getWorld().getUID());
        if (opt.isEmpty()) {
            log.warning("Failed to get world for block " + block);
            return;
        }
        var data = spawner.toData(block);
        opt.get().setData(Position.fromBlock(data.getBlock()), data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        onBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach(this::onBlockBreak);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNoteBlockBreak(OraxenNoteBlockBreakEvent event) {
        onBlockBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStringBlockBreak(OraxenStringBlockBreakEvent event) {
        onBlockBreak(event.getBlock());
    }

    private void onBlockBreak(Block block) {
        plugin.getCacheManager().getWorld(block.getWorld().getUID())
                .ifPresent(cache -> cache.removeData(Position.fromBlock(block), true));
    }
}
