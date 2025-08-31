package de.Blocky.dasjobs.manager;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Quest;
import de.Blocky.dasjobs.data.QuestProgress;
import de.Blocky.dasjobs.data.Quest.QuestTask;
import de.Blocky.dasjobs.data.Quest.QuestReward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class QuestManager {
    private final DasJobs plugin;
    private final Map<String, Quest> quests;
    private final Map<UUID, Map<String, QuestProgress>> playerQuestProgress;
    private final File questDataFile;

    public QuestManager(DasJobs plugin) {
        this.plugin = plugin;
        this.quests = new HashMap<>();
        this.playerQuestProgress = new HashMap<>();
        this.questDataFile = new File(plugin.getDataFolder(), "questdata.yml");
        loadQuests();
        loadQuestProgress();
    }

    public void loadQuests() {
        quests.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection jobsSection = config.getConfigurationSection("jobs");
        
        if (jobsSection == null) return;

        for (String jobName : jobsSection.getKeys(false)) {
            ConfigurationSection jobSection = jobsSection.getConfigurationSection(jobName);
            if (jobSection == null) continue;

            ConfigurationSection questsSection = jobSection.getConfigurationSection("quests");
            if (questsSection == null) continue;

            for (String questId : questsSection.getKeys(false)) {
                ConfigurationSection questSection = questsSection.getConfigurationSection(questId);
                if (questSection == null) continue;

                try {
                    Quest quest = loadQuestFromConfig(jobName, questId, questSection);
                    quests.put(questId, quest);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load quest " + questId + " for job " + jobName, e);
                }
            }
        }
    }

    private Quest loadQuestFromConfig(String jobName, String questId, ConfigurationSection questSection) {
        int slot = questSection.getInt("slot", 1);
        String lore = questSection.getString("lore", "");
        String displayItem = questSection.getString("display-item", "STONE");
        String taskStr = questSection.getString("task", "BREAK");
        String specificTask = questSection.getString("specific-task", "");
        int amount = questSection.getInt("amount", 1);
        String rewardStr = questSection.getString("reward", "JOBXP");
        int rewardAmount = questSection.getInt("reward-amount", 1);
        String rewardItem = questSection.getString("reward-item", "");

        QuestTask task = QuestTask.valueOf(taskStr.toUpperCase());
        QuestReward reward = QuestReward.valueOf(rewardStr.toUpperCase());

        return new Quest(questId, jobName, slot, lore, displayItem, task, specificTask, amount, reward, rewardAmount, rewardItem);
    }

    public void saveQuests() {
        FileConfiguration config = plugin.getConfig();
        
        for (Quest quest : quests.values()) {
            String path = "jobs." + quest.getJobName() + ".quests." + quest.getId();
            config.set(path + ".slot", quest.getSlot());
            config.set(path + ".lore", quest.getLore());
            config.set(path + ".display-item", quest.getDisplayItem());
            config.set(path + ".task", quest.getTask().name());
            config.set(path + ".specific-task", quest.getSpecificTask());
            config.set(path + ".amount", quest.getAmount());
            config.set(path + ".reward", quest.getReward().name());
            config.set(path + ".reward-amount", quest.getRewardAmount());
            if (quest.getReward() == QuestReward.ITEM && quest.getRewardItem() != null && !quest.getRewardItem().isEmpty()) {
                config.set(path + ".reward-item", quest.getRewardItem());
            }
        }
        
        plugin.saveConfig();
    }

    public void loadQuestProgress() {
        if (!questDataFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(questDataFile);
        playerQuestProgress.clear();

        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String playerUUIDStr : playersSection.getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(playerUUIDStr);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(playerUUIDStr);
                if (playerSection == null) continue;

                Map<String, QuestProgress> playerQuests = new HashMap<>();
                ConfigurationSection questsSection = playerSection.getConfigurationSection("quests");
                if (questsSection != null) {
                    for (String questId : questsSection.getKeys(false)) {
                        ConfigurationSection questSection = questsSection.getConfigurationSection(questId);
                        if (questSection != null) {
                            QuestProgress progress = loadQuestProgressFromConfig(playerUUID, questId, questSection);
                            playerQuests.put(questId, progress);
                        }
                    }
                }
                playerQuestProgress.put(playerUUID, playerQuests);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in quest data: " + playerUUIDStr);
            }
        }
    }

    private QuestProgress loadQuestProgressFromConfig(UUID playerUUID, String questId, ConfigurationSection questSection) {
        Map<String, Integer> progress = new HashMap<>();
        ConfigurationSection progressSection = questSection.getConfigurationSection("progress");
        if (progressSection != null) {
            for (String task : progressSection.getKeys(false)) {
                progress.put(task, progressSection.getInt(task, 0));
            }
        }

        boolean completed = questSection.getBoolean("completed", false);
        boolean claimed = questSection.getBoolean("claimed", false);

        return new QuestProgress(playerUUID, questId, progress, completed, claimed);
    }

    public void saveQuestProgress() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Map<String, QuestProgress>> playerEntry : playerQuestProgress.entrySet()) {
            String playerUUIDStr = playerEntry.getKey().toString();
            Map<String, QuestProgress> playerQuests = playerEntry.getValue();

            for (QuestProgress progress : playerQuests.values()) {
                String path = "players." + playerUUIDStr + ".quests." + progress.getQuestId();
                config.set(path + ".completed", progress.isCompleted());
                config.set(path + ".claimed", progress.isClaimed());

                for (Map.Entry<String, Integer> progressEntry : progress.getProgress().entrySet()) {
                    config.set(path + ".progress." + progressEntry.getKey(), progressEntry.getValue());
                }
            }
        }

        try {
            config.save(questDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save quest progress data", e);
        }
    }

    public Quest createQuest(String jobName, String displayItem, QuestTask task, String specificTask, 
                           int amount, QuestReward reward, int rewardAmount, String rewardItem) {
        String questId = generateQuestId();
        Quest quest = new Quest(questId, jobName, 1, "", displayItem, task, specificTask, amount, reward, rewardAmount, rewardItem);
        quests.put(questId, quest);
        saveQuests();
        return quest;
    }

    private String generateQuestId() {
        return "quest_" + System.currentTimeMillis();
    }

    public void removeQuest(String questId) {
        quests.remove(questId);
        for (Map<String, QuestProgress> playerQuests : playerQuestProgress.values()) {
            playerQuests.remove(questId);
        }
        saveQuests();
        saveQuestProgress();
    }

    public Quest getQuest(String questId) {
        return quests.get(questId);
    }

    public List<Quest> getQuestsForJob(String jobName) {
        List<Quest> jobQuests = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.getJobName().equalsIgnoreCase(jobName)) {
                jobQuests.add(quest);
            }
        }
        jobQuests.sort(Comparator.comparing(Quest::getSlot));
        return jobQuests;
    }

    public QuestProgress getQuestProgress(UUID playerUUID, String questId) {
        return playerQuestProgress
                .computeIfAbsent(playerUUID, k -> new HashMap<>())
                .computeIfAbsent(questId, k -> new QuestProgress(playerUUID, questId));
    }

    public void updateQuestProgress(UUID playerUUID, String questId, String task, int amount) {
        QuestProgress progress = getQuestProgress(playerUUID, questId);
        Quest quest = getQuest(questId);
        
        if (quest == null) return;

        progress.addProgress(task, amount);

        boolean wasCompletedBefore = progress.isCompleted();
        if (!progress.isCompleted() && progress.getProgress(task) >= quest.getAmount()) {
            progress.setCompleted(true);

            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                plugin.getMessageManager().sendMessage(player, 
                        "&a&lQuest abgeschlossen! &e" + quest.getLore() + " &a- Belohnung bereit zum Einsammeln!");
            }
        }
        
        saveQuestProgress();
        plugin.getQuestMenuListener().handleProgressUpdate(playerUUID, questId);
    }

    public boolean claimQuestReward(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) return false;

        QuestProgress progress = getQuestProgress(player.getUniqueId(), questId);
        if (!progress.isCompleted() || progress.isClaimed()) return false;

        switch (quest.getReward()) {
            case JOBXP:
                plugin.getPlayerDataManager().getPlayerData(player.getUniqueId())
                        .addExperience(quest.getJobName(), quest.getRewardAmount());
                plugin.getMessageManager().sendMessage(player, 
                        "&aDu hast &e" + quest.getRewardAmount() + " XP &afür den Job &c" + quest.getJobName() + " &aerhalten!");
                break;
            case MONEY:
                plugin.getEconomy().depositPlayer(player, quest.getRewardAmount());
                plugin.getMessageManager().sendMessage(player, 
                        "&aDu hast &e" + quest.getRewardAmount() + " " + plugin.getCurrencySymbol() + " &aerhalten!");
                break;
            case ITEM:
                String itemName = quest.getRewardItem() != null && !quest.getRewardItem().isEmpty() 
                    ? quest.getRewardItem() : quest.getSpecificTask();
                Material material = Material.getMaterial(itemName);
                if (material != null) {
                    ItemStack item = new ItemStack(material, quest.getRewardAmount());
                    player.getInventory().addItem(item);
                    plugin.getMessageManager().sendMessage(player, 
                            "&aDu hast &e" + quest.getRewardAmount() + "x " + material.name() + " &aerhalten!");
                } else {
                    plugin.getMessageManager().sendMessage(player, 
                            "&cFehler: Ungültiges Item &e" + itemName + " &cfür Belohnung!");
                }
                break;
        }

        progress.setClaimed(true);
        saveQuestProgress();
        return true;
    }

    public void checkQuestProgress(Player player, QuestTask task, String specificTask) {
        for (Quest quest : quests.values()) {
            if (quest.getTask() == task && quest.getSpecificTask().equalsIgnoreCase(specificTask)) {
                updateQuestProgress(player.getUniqueId(), quest.getId(), specificTask, 1);
            }
        }
    }

    public Map<String, Quest> getAllQuests() {
        return new HashMap<>(quests);
    }

    public boolean resetQuestProgress(UUID playerUUID, String questId) {
        Map<String, QuestProgress> map = playerQuestProgress.computeIfAbsent(playerUUID, k -> new HashMap<>());
        if (map.containsKey(questId)) {
            map.remove(questId);
            saveQuestProgress();
            return true;
        } else {
            saveQuestProgress();
            return false;
        }
    }
}
