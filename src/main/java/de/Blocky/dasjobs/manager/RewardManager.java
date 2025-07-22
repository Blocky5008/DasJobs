package de.Blocky.dasjobs.manager;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.util.ItemSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class RewardManager {

    private final DasJobs plugin;
    private final File rewardFile;
    private YamlConfiguration rewardConfig;
    private final Map<String, Map<Integer, ItemStack>> jobRewards = new HashMap<>();

    public RewardManager(DasJobs plugin) {
        this.plugin = plugin;
        this.rewardFile = new File(plugin.getDataFolder(), "rewards.yml");
        loadRewards();
    }

    public void loadRewards() {
        if (!rewardFile.exists()) {
            plugin.getLogger().info("rewards.yml not found, attempting to create it.");
            try {
                if (rewardFile.getParentFile() != null && !rewardFile.getParentFile().exists()) {
                    rewardFile.getParentFile().mkdirs();
                }
                rewardFile.createNewFile();
                plugin.getLogger().info("rewards.yml created successfully.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create rewards.yml: " + e.getMessage(), e);
            }
        }
        rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
        jobRewards.clear();

        if (rewardConfig.isConfigurationSection("rewards")) {
            for (String jobName : rewardConfig.getConfigurationSection("rewards").getKeys(false)) {
                Map<Integer, ItemStack> levelRewards = new HashMap<>();
                if (rewardConfig.isConfigurationSection("rewards." + jobName)) {
                    for (String levelKey : rewardConfig.getConfigurationSection("rewards." + jobName).getKeys(false)) {
                        try {
                            int level = Integer.parseInt(levelKey);
                            String itemString = rewardConfig.getString("rewards." + jobName + "." + levelKey);
                            if (itemString != null && !itemString.isEmpty()) {
                                Optional<ItemStack> item = ItemSerializer.deserialize(itemString);
                                item.ifPresent(itemStack -> levelRewards.put(level, itemStack));
                            }
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid level key in rewards.yml for job " + jobName + ": " + levelKey);
                        }
                    }
                }
                jobRewards.put(jobName.toLowerCase(), levelRewards);
            }
        }
        plugin.getLogger().info(jobRewards.size() + " job reward configurations loaded.");
    }
    public void saveRewards() {
        rewardConfig = new YamlConfiguration();

        for (Map.Entry<String, Map<Integer, ItemStack>> jobEntry : jobRewards.entrySet()) {
            String jobName = jobEntry.getKey();
            for (Map.Entry<Integer, ItemStack> levelEntry : jobEntry.getValue().entrySet()) {
                int level = levelEntry.getKey();
                ItemStack item = levelEntry.getValue();
                String itemString = ItemSerializer.serialize(item);
                if (itemString != null && !itemString.isEmpty()) {
                    rewardConfig.set("rewards." + jobName + "." + level, itemString);
                } else {
                    plugin.getLogger().warning("Failed to serialize item for job " + jobName + " at level " + level + ". Item will not be saved.");
                }
            }
        }

        try {
            if (rewardFile.getParentFile() != null && !rewardFile.getParentFile().exists()) {
                rewardFile.getParentFile().mkdirs();
            }
            rewardConfig.save(rewardFile);
            plugin.getLogger().info("rewards.yml saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save rewards.yml: " + e.getMessage(), e);
        }
    }

    public void setRewardItem(String jobName, int level, ItemStack item) {
        jobName = jobName.toLowerCase();
        jobRewards.computeIfAbsent(jobName, k -> new HashMap<>()).put(level, item);
        saveRewards();
    }

    public boolean removeRewardItem(String jobName, int level) {
        jobName = jobName.toLowerCase();
        Map<Integer, ItemStack> levelRewards = jobRewards.get(jobName);
        if (levelRewards != null && levelRewards.containsKey(level)) {
            levelRewards.remove(level);
            if (levelRewards.isEmpty()) {
                jobRewards.remove(jobName);
            }
            saveRewards();
            return true;
        }
        return false;
    }
    public Optional<ItemStack> getRewardItem(String jobName, int level) {
        jobName = jobName.toLowerCase();
        return Optional.ofNullable(jobRewards.getOrDefault(jobName, new HashMap<>()).get(level));
    }

    public Map<Integer, ItemStack> getJobRewards(String jobName) {
        jobName = jobName.toLowerCase();
        return new HashMap<>(jobRewards.getOrDefault(jobName, new HashMap<>()));
    }

    public Set<Integer> getRewardLevelsForJob(String jobName) {
        jobName = jobName.toLowerCase();
        Map<Integer, ItemStack> rewards = jobRewards.get(jobName);
        if (rewards != null) {
            return Collections.unmodifiableSet(rewards.keySet());
        }
        return Collections.emptySet();
    }
}