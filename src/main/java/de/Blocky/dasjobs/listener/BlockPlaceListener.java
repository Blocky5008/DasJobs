package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Quest;
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
            if (event.isCancelled()) {
                return;
            }
            String blockType = event.getBlockPlaced().getType().name();
            plugin.getQuestManager().checkQuestProgress(event.getPlayer(), Quest.QuestTask.PLACE, blockType);
            plugin.getBlockPlaceTracker().addPlacedBlock(event.getBlockPlaced().getLocation());
        }
    }
}
