package de.Blocky.dasjobs.config;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerDataManager {

    private final DasJobs plugin;
    private final File playerdataFile;
    private YamlConfiguration playerDataConfig;
    private final Map<UUID, PlayerJobData> playerJobDataMap = new HashMap<>();

    public PlayerDataManager(DasJobs plugin) {
        this.plugin = plugin;
        this.playerdataFile = new File(plugin.getDataFolder() + File.separator + "playerdata", "playerdata.yml");
        loadPlayerDataFile();
    }

    private void loadPlayerDataFile() {
        if (!playerdataFile.exists()) {
            try {
                playerdataFile.getParentFile().mkdirs();
                playerdataFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[DasJobs] Konnte playerdata.yml nicht erstellen: " + e.getMessage());
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerdataFile);
        Bukkit.getLogger().info("[DasJobs] playerdata.yml geladen.");
    }

    public PlayerJobData getPlayerData(UUID uuid) {
        return playerJobDataMap.computeIfAbsent(uuid, this::loadPlayerJobData);
    }

    private PlayerJobData loadPlayerJobData(UUID uuid) {
        ConfigurationSection playerSection = playerDataConfig.getConfigurationSection(uuid.toString());
        if (playerSection == null) {
            return new PlayerJobData(uuid);
        }

        Map<String, Integer> jobLevels = new HashMap<>();
        Map<String, Double> jobExperiences = new HashMap<>();
        Map<String, List<String>> claimedRewards = new HashMap<>();

        ConfigurationSection levelsSection = playerSection.getConfigurationSection("levels");
        if (levelsSection != null) {
            levelsSection.getKeys(false).forEach(jobName -> jobLevels.put(jobName, levelsSection.getInt(jobName)));
        }

        ConfigurationSection experiencesSection = playerSection.getConfigurationSection("experiences");
        if (experiencesSection != null) {
            experiencesSection.getKeys(false).forEach(jobName -> jobExperiences.put(jobName, experiencesSection.getDouble(jobName)));
        }

        ConfigurationSection claimedRewardsSection = playerSection.getConfigurationSection("claimed_rewards");
        if (claimedRewardsSection != null) {
            claimedRewardsSection.getKeys(false).forEach(jobName -> {
                claimedRewards.put(jobName, claimedRewardsSection.getStringList(jobName));
            });
        }

        String activeJob = playerSection.getString("active_job");
        PlayerJobData data = new PlayerJobData(uuid, jobLevels, jobExperiences, claimedRewards);

        return data;
    }

    public void savePlayerData(UUID uuid) {
        PlayerJobData data = playerJobDataMap.get(uuid);
        if (data == null) {
            plugin.getLogger().warning("Attempted to save player data for UUID " + uuid + " but data was not in memory map.");
            return;
        }

        String uuidString = uuid.toString();
        playerDataConfig.set(uuidString + ".levels", null);
        playerDataConfig.set(uuidString + ".experiences", null);
        playerDataConfig.set(uuidString + ".claimed_rewards", null);
        playerDataConfig.createSection(uuidString + ".levels", data.getJobLevels());
        playerDataConfig.createSection(uuidString + ".experiences", data.getJobExperiences());
        playerDataConfig.set(uuidString + ".active_job", data.getActiveJobName());
        ConfigurationSection claimedRewardsSection = playerDataConfig.createSection(uuidString + ".claimed_rewards");
        data.getClaimedRewards().forEach((jobName, claimedList) -> {
            claimedRewardsSection.set(jobName, claimedList);
        });

        try {
            playerDataConfig.save(playerdataFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[DasJobs] Konnte Spielerdaten für " + uuid.toString() + " nicht speichern: " + e.getMessage());
        }
    }

    public void saveAllPlayerData() {
        Bukkit.getLogger().info("[DasJobs] Speichere alle Spielerdaten...");
        for (UUID uuid : new ArrayList<>(playerJobDataMap.keySet())) {
            savePlayerData(uuid);
        }
        Bukkit.getLogger().info("[DasJobs] Alle Spielerdaten gespeichert.");
    }


    public void resetClaimedRewards(UUID playerUUID, String jobName) {
        PlayerJobData playerData = getPlayerData(playerUUID);
        playerData.clearClaimedRewardsForJob(jobName);
        savePlayerData(playerUUID);
    }

    public List<Map.Entry<UUID, Integer>> getTopPlayersForJob(String jobName, int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();

        if (playerDataConfig.getKeys(false).isEmpty()) {
            return topPlayers;
        }

        for (String uuidString : playerDataConfig.getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(uuidString);
                ConfigurationSection playerSection = playerDataConfig.getConfigurationSection(uuidString);
                if (playerSection != null) {
                    ConfigurationSection levelsSection = playerSection.getConfigurationSection("levels");
                    if (levelsSection != null && levelsSection.contains(jobName)) {
                        int level = levelsSection.getInt(jobName);
                        topPlayers.add(Map.entry(playerUUID, level));
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID found in playerdata.yml: " + uuidString + " - " + e.getMessage());
            }
        }
        topPlayers.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return topPlayers.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}