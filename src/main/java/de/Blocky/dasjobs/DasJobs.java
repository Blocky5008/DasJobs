package de.Blocky.dasjobs;

import de.Blocky.dasjobs.command.JobsCommand;
import de.Blocky.dasjobs.command.TabCompletionHandler;
import de.Blocky.dasjobs.config.JobConfigManager;
import de.Blocky.dasjobs.config.PlayerDataManager;
import de.Blocky.dasjobs.listener.BlockBreakListener;
import de.Blocky.dasjobs.listener.BlockPlaceListener;
import de.Blocky.dasjobs.listener.EntityKillListener;
import de.Blocky.dasjobs.listener.PlayerJoinQuitListener;
import de.Blocky.dasjobs.listener.RewardMenuListener;
import de.Blocky.dasjobs.listener.JobMenuListener;
import de.Blocky.dasjobs.listener.QuestMenuListener;
import de.Blocky.dasjobs.manager.BoosterManager;
import de.Blocky.dasjobs.manager.EconomyManager;
import de.Blocky.dasjobs.manager.JobManager;
import de.Blocky.dasjobs.manager.MessageManager;
import de.Blocky.dasjobs.manager.QuestManager;
import de.Blocky.dasjobs.manager.RewardManager;
import de.Blocky.dasjobs.tracker.BlockPlaceTracker;
import de.Blocky.dasjobs.util.ChatUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class DasJobs extends JavaPlugin {

    private static DasJobs instance;
    private Economy economy = null;
    private JobConfigManager jobConfigManager;
    private PlayerDataManager playerDataManager;
    private JobManager jobManager;
    private EconomyManager economyManager;
    private MessageManager messageManager;
    private BlockPlaceTracker blockPlaceTracker;
    private BoosterManager boosterManager;
    private RewardManager rewardManager;
    private QuestManager questManager;
    private RewardMenuListener rewardMenuListener;
    private JobMenuListener jobMenuListener;
    private QuestMenuListener questMenuListener;
    private BlockBreakListener blockBreakListener;
    private BukkitTask leaderboardUpdateTask;

    private String prefix;
    private String currencySymbol;
    private int minutesForSummary;

    private Map<UUID, Double> temporaryEarnedMoney = new HashMap<>();
    private BukkitTask summaryTask;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getLogger().info(ChatUtil.colorize("&a[DasJobs] Plugin wird aktiviert..."));
        setupManagers();

        if (!setupEconomy()) {
            Bukkit.getLogger().severe(ChatUtil.colorize("&c[DasJobs] Konnte Economy nicht laden! Deaktiviere Plugin."));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economyManager = new EconomyManager(economy);

        jobConfigManager.setupJobsConfig();
        jobManager.loadJobsFromConfig();

        boosterManager.loadBoosters();
        rewardManager.loadRewards();
        questManager.loadQuests();

        registerListeners();
        registerCommands();

        startMoneySummaryTask();
        startLeaderboardUpdateTask();

        Bukkit.getLogger().info(ChatUtil.colorize("&a[DasJobs] Plugin erfolgreich aktiviert!"));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatUtil.colorize("&c[DasJobs] Plugin wird deaktiviert..."));

        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }
        if (blockPlaceTracker != null) {
            blockPlaceTracker.savePlacedBlocks();
        }
        if (boosterManager != null) {
            boosterManager.saveBoosters();
        }
        if (rewardManager != null) {
            rewardManager.saveRewards();
        }
        if (questManager != null) {
            questManager.saveQuestProgress();
        }
        if (blockBreakListener != null) {
            blockBreakListener.stopCounterResetTask();
        }

        if (summaryTask != null) {
            summaryTask.cancel();
        }
        if (leaderboardUpdateTask != null) {
            leaderboardUpdateTask.cancel();
            getLogger().info("[DasJobs] Leaderboard-Update-Task gestoppt.");
        }

        Bukkit.getLogger().info(ChatUtil.colorize("&c[DasJobs] Plugin erfolgreich deaktiviert!"));
    }

    private void setupManagers() {
        jobConfigManager = new JobConfigManager(this);
        playerDataManager = new PlayerDataManager(this);
        messageManager = new MessageManager(this);
        blockPlaceTracker = new BlockPlaceTracker(this);
        boosterManager = new BoosterManager(this);
        rewardManager = new RewardManager(this);
        questManager = new QuestManager(this);
        jobManager = new JobManager(this);
        rewardMenuListener = new RewardMenuListener(this);
        jobMenuListener = new JobMenuListener(this);
        questMenuListener = new QuestMenuListener(this);
        blockBreakListener = new BlockBreakListener(this);

        setupConfig();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(blockBreakListener, this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityKillListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(rewardMenuListener, this);
        getServer().getPluginManager().registerEvents(jobMenuListener, this);
        getServer().getPluginManager().registerEvents(questMenuListener, this);
    }

    private void registerCommands() {
        getCommand("jobs").setExecutor(new JobsCommand(this));
        getCommand("jobs").setTabCompleter(new TabCompletionHandler(this));
    }

    public void reloadPluginFiles() {
        getLogger().info(ChatUtil.colorize("&e[DasJobs] Befehl /jobs reload empfangen. Starte Neuladen..."));

        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }
        if (blockPlaceTracker != null) {
            blockPlaceTracker.savePlacedBlocks();
        }
        if (boosterManager != null) {
            boosterManager.saveBoosters();
        }
        if (rewardManager != null) {
            rewardManager.saveRewards();
        }

        if (blockBreakListener != null) {
            HandlerList.unregisterAll(blockBreakListener);
            blockBreakListener.stopCounterResetTask();
            blockBreakListener = new BlockBreakListener(this);
            getServer().getPluginManager().registerEvents(blockBreakListener, this);
        }

        reloadConfig();
        getLogger().info(ChatUtil.colorize("&a[DasJobs] config.yml neu geladen."));

        this.prefix = ChatUtil.colorize(getConfig().getString("prefix", "&c&lDasJobs &8>> "));
        this.currencySymbol = getConfig().getString("currency-symbol", "$");
        this.minutesForSummary = getConfig().getInt("minutes-for-summary", 1);
        if (summaryTask != null) {
            summaryTask.cancel();
        }
        startMoneySummaryTask();
        if (leaderboardUpdateTask != null) {
            leaderboardUpdateTask.cancel();
        }
        startLeaderboardUpdateTask();

        jobConfigManager.setupJobsConfig();
        jobManager.loadJobsFromConfig();
        getLogger().info(ChatUtil.colorize("&a[DasJobs] Job-Konfigurationen neu geladen."));
        boosterManager.loadBoosters();
        getLogger().info(ChatUtil.colorize("&a[DasJobs] Booster-Konfigurationen neu geladen."));
        rewardManager.loadRewards();
        getLogger().info(ChatUtil.colorize("&a[DasJobs] Belohnungs-Konfigurationen neu geladen."));
        questManager.loadQuests();
        getLogger().info(ChatUtil.colorize("&a[DasJobs] Quest-Konfigurationen neu geladen."));


        getLogger().info(ChatUtil.colorize("&a[DasJobs] Alle Konfigurationsdateien und Jobs erfolgreich neu geladen!"));
    }

    private void setupConfig() {
        saveDefaultConfig();

        File playerdataFolder = new File(getDataFolder(), "playerdata");
        if (!playerdataFolder.exists()) {
            playerdataFolder.mkdirs();
        }
        File playerdataFile = new File(playerdataFolder, "playerdata.yml");
        if (!playerdataFile.exists()) {
            try {
                playerdataFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, ChatUtil.colorize("&c[DasJobs] Konnte playerdata.yml nicht erstellen: " + e.getMessage()));
            }
        }

        this.prefix = ChatUtil.colorize(getConfig().getString("prefix", "&c&lDasJobs &8>> "));
        this.currencySymbol = getConfig().getString("currency-symbol", "$");
        this.minutesForSummary = getConfig().getInt("minutes-for-summary", 1);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().severe(ChatUtil.colorize("&c[DasJobs] Vault wurde nicht gefunden!"));
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().severe(ChatUtil.colorize("&c[DasJobs] Kein Economy-Provider für Vault gefunden!"));
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void startMoneySummaryTask() {
        long ticks = (long) minutesForSummary * 60 * 20;
        if (ticks <= 0) {
            Bukkit.getLogger().warning(ChatUtil.colorize("&e[DasJobs] 'minutes-for-summary' ist 0 oder weniger. Zusammenfassungs-Task wird nicht gestartet."));
            return;
        }

        if (summaryTask != null) {
            summaryTask.cancel();
        }

        summaryTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            Map<UUID, Double> currentEarnedMoney = new HashMap<>(temporaryEarnedMoney);
            temporaryEarnedMoney.clear();

            for (Map.Entry<UUID, Double> entry : currentEarnedMoney.entrySet()) {
                UUID playerUUID = entry.getKey();
                Double earned = entry.getValue();

                if (earned > 0) {
                    Player p = Bukkit.getPlayer(playerUUID);
                    if (p != null) {
                        messageManager.sendMessage(p, "&7Du hast einen Lohn von &a" + String.format("%.2f", earned) + "&a" + currencySymbol + " &7erhalten.");
                    }
                }
            }
        }, ticks, ticks);
    }
    private void startLeaderboardUpdateTask() {
        long repeatTicks = 20L * 60L;
        leaderboardUpdateTask = Bukkit.getScheduler().runTaskTimer(this,
                () -> jobMenuListener.updateLeaderboardItems(),
                repeatTicks,
                repeatTicks);
        getLogger().info(ChatUtil.colorize("&a[DasJobs] Leaderboard-Update-Task gestartet (alle " + (repeatTicks / 20) + " Sekunden)."));
    }

    public void addTemporaryEarnedMoney(UUID playerUUID, double amount) {
        temporaryEarnedMoney.merge(playerUUID, amount, Double::sum);
    }

    public static DasJobs getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public JobConfigManager getJobConfigManager() {
        return jobConfigManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BlockPlaceTracker getBlockPlaceTracker() {
        return blockPlaceTracker;
    }

    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public RewardMenuListener getRewardMenuListener() {
        return rewardMenuListener;
    }

    public JobMenuListener getJobMenuListener() {
        return jobMenuListener;
    }

    public QuestMenuListener getQuestMenuListener() {
        return questMenuListener;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }
}
