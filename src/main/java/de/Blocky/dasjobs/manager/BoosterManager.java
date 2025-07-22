package de.Blocky.dasjobs.manager;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Booster;
import de.Blocky.dasjobs.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class BoosterManager {

    private final DasJobs plugin;
    private final File boosterFile;
    private YamlConfiguration boosterConfig;
    private final CopyOnWriteArrayList<Booster> activeBoosters = new CopyOnWriteArrayList<>();

    public BoosterManager(DasJobs plugin) {
        this.plugin = plugin;
        this.boosterFile = new File(plugin.getDataFolder() + File.separator + "data", "boosters.yml");
        if (!boosterFile.getParentFile().exists()) {
            boosterFile.getParentFile().mkdirs();
        }
        loadBoosters();
        startBoosterCleanupTask();
    }

    public void addBooster(Booster booster) {
        activeBoosters.add(booster);
        saveBoosters();
        plugin.getLogger().info("Neuer Booster aktiviert: " + booster.getJobName() + " " + booster.getBoostType() + " x" + booster.getMultiplier() + " für " + booster.getRemainingMinutes() + " Minuten.");
    }

    public double getBoostMultiplier(String jobName, Booster.BoostType type) {
        double multiplier = 1.0;
        long currentTime = System.currentTimeMillis();
        for (Booster booster : activeBoosters) {
            if (booster.getExpiryTime() > currentTime) {
                if (booster.getBoostType() == type &&
                        (booster.getJobName().equalsIgnoreCase("*") || booster.getJobName().equalsIgnoreCase(jobName))) {
                    multiplier *= booster.getMultiplier();
                }
            }
        }
        return multiplier;
    }

    public void loadBoosters() {
        if (!boosterFile.exists()) {
            try {
                boosterFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Konnte boosters.yml nicht erstellen: " + e.getMessage());
                return;
            }
        }
        boosterConfig = YamlConfiguration.loadConfiguration(boosterFile);

        activeBoosters.clear();
        List<?> boosterList = boosterConfig.getList("boosters", new ArrayList<>());

        long currentTime = System.currentTimeMillis();

        for (Object obj : boosterList) {
            if (obj instanceof String boosterString) {
                try {
                    Booster booster = Booster.fromString(boosterString);
                    if (booster.getExpiryTime() > currentTime) {
                        activeBoosters.add(booster);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Ungültiger Booster-Eintrag in boosters.yml: " + boosterString + " - " + e.getMessage());
                }
            }
        }
        plugin.getLogger().info(ChatUtil.colorize(plugin.getPrefix() + "&a" + activeBoosters.size() + " aktive Booster geladen."));
    }

    public void saveBoosters() {
        List<String> boosterStrings = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        activeBoosters.removeIf(booster -> booster.getExpiryTime() <= currentTime);

        for (Booster booster : activeBoosters) {
            boosterStrings.add(booster.toString());
        }
        boosterConfig.set("boosters", boosterStrings);
        try {
            boosterConfig.save(boosterFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Konnte boosters.yml nicht speichern: " + e.getMessage());
        }
    }

    private void startBoosterCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            List<Booster> expiredBoosters = new ArrayList<>();

            for (Booster booster : activeBoosters) {
                if (booster.getExpiryTime() <= currentTime) {
                    expiredBoosters.add(booster);
                }
            }

            for (Booster expiredBooster : expiredBoosters) {
                activeBoosters.remove(expiredBooster);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String starterName = expiredBooster.getStarterPlayerName() != null ? expiredBooster.getStarterPlayerName() : "Ein Administrator";
                    Bukkit.broadcastMessage(plugin.getPrefix() + ChatUtil.colorize("&7Der Booster von &c" + starterName + " &7ist abgelaufen."));
                });
                plugin.getLogger().info("Booster abgelaufen: " + expiredBooster.getJobName() + " " + expiredBooster.getBoostType() + " gestartet von " + expiredBooster.getStarterPlayerName());
            }
            saveBoosters();

        }, 20 * 5, 20 * 5);
    }
}