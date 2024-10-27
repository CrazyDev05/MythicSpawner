package de.crazydev22.spawner.oraxen;

import de.crazydev22.spawner.MythicSpawner;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureBreakEvent;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurniturePlaceEvent;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurniturePlace(OraxenFurniturePlaceEvent event) {
        onBlockPlace(event.getBaseEntity().getLocation().getBlock(), event.getItemInHand());
    }

    private void onBlockPlace(Block block, ItemStack itemStack) {
        if (factory.isNotImplementedIn(OraxenItems.getIdByItem(itemStack)))
            return;
        Spawner spawner = (Spawner) factory.getMechanic(itemStack);
        if (spawner == null || plugin.getCacheManager().hasData(block))
            return;

        plugin.getCacheManager().setData(spawner.toData(block));
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnitureBreak(OraxenFurnitureBreakEvent event) {
        onBlockBreak(event.getBaseEntity().getLocation().getBlock());
    }

    private void onBlockBreak(Block block) {
        plugin.getCacheManager().removeData(block);
    }
}
