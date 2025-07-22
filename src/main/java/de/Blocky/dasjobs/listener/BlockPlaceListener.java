package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final DasJobs plugin;

    public BlockPlaceListener(DasJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer() != null) {
            plugin.getBlockPlaceTracker().addPlacedBlock(event.getBlockPlaced().getLocation());
        }
    }
}