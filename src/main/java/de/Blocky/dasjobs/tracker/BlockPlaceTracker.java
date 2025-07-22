package de.Blocky.dasjobs.tracker;

import de.Blocky.dasjobs.DasJobs;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class BlockPlaceTracker {

    private final DasJobs plugin;
    private final File blockPlacedFile;
    private YamlConfiguration blockPlacedConfig;
    private final Set<String> placedBlocksLocations = new HashSet<>();

    public BlockPlaceTracker(DasJobs plugin) {
        this.plugin = plugin;
        this.blockPlacedFile = new File(plugin.getDataFolder() + File.separator + "playerdata", "block-placed.yml");
        loadPlacedBlocks();
    }

    private void loadPlacedBlocks() {
        if (!blockPlacedFile.exists()) {
            try {
                blockPlacedFile.getParentFile().mkdirs();
                blockPlacedFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Konnte block-placed.yml nicht erstellen: " + e.getMessage());
            }
        }
        blockPlacedConfig = YamlConfiguration.loadConfiguration(blockPlacedFile);

        List<String> loadedLocations = blockPlacedConfig.getStringList("placed-blocks");
        placedBlocksLocations.addAll(loadedLocations);
        plugin.getLogger().info("[DasJobs] " + placedBlocksLocations.size() + " platzierte Blöcke geladen.");
    }

    public void savePlacedBlocks() {
        blockPlacedConfig.set("placed-blocks", new ArrayList<>(placedBlocksLocations));
        try {
            blockPlacedConfig.save(blockPlacedFile);
            plugin.getLogger().info("[DasJobs] " + placedBlocksLocations.size() + " platzierte Blöcke gespeichert.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Konnte block-placed.yml nicht speichern: " + e.getMessage());
        }
    }

    public void addPlacedBlock(Location location) {
        placedBlocksLocations.add(locationToString(location));
    }

    public boolean isPlayerPlaced(Location location) {
        return placedBlocksLocations.contains(locationToString(location));
    }

    public void removePlacedBlock(Location location) {
        placedBlocksLocations.remove(locationToString(location));
    }

    private String locationToString(Location location) {
        return String.format("%s;%d;%d;%d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }
}