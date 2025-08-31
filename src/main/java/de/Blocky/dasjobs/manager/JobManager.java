package de.Blocky.dasjobs.manager;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Job;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class JobManager {

    private final DasJobs plugin;
    private final Map<String, Job> jobs = new HashMap<>();

    public JobManager(DasJobs plugin) {
        this.plugin = plugin;
    }

    public void loadJobsFromConfig() {
        jobs.clear();

        for (String jobName : plugin.getJobConfigManager().getJobNamesFromConfig()) {
            try {
                Job job = plugin.getJobConfigManager().loadJobFromConfig(jobName);
                if (job != null) {
                    jobs.put(job.getName(), job);
                    plugin.getLogger().info("Job geladen: " + job.getName() + " (aus config.yml)");
                } else {
                    plugin.getLogger().warning("Konnte Job '" + jobName + "' nicht aus config.yml laden.");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden des Jobs '" + jobName + "' aus config.yml: " + e.getMessage(), e);
            }
        }
        plugin.getLogger().info(jobs.size() + " Jobs erfolgreich aus config.yml geladen.");
    }

    public Optional<Job> getJob(String name) {
        return Optional.ofNullable(jobs.get(name.toLowerCase()));
    }

    public Map<String, Job> getJobs() {
        return Collections.unmodifiableMap(jobs);
    }

    public void openRewardMenu(Player player, Job job) {
        if (plugin.getRewardMenuListener() != null) {
            plugin.getRewardMenuListener().openRewardMenu(player, job);
        } else {
            plugin.getLogger().severe("RewardMenuListener not initialized! Cannot open reward menu for " + player.getName());
            plugin.getMessageManager().sendMessage(player, "&cEin interner Fehler ist aufgetreten. Belohnungsmenü konnte nicht geöffnet werden.");
        }
    }
}