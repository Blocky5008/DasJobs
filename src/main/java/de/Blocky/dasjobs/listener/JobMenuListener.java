package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Job;
import de.Blocky.dasjobs.data.JobAction;
import de.Blocky.dasjobs.data.JobReward;
import de.Blocky.dasjobs.data.PlayerJobData;
import de.Blocky.dasjobs.util.ChatUtil;
import de.Blocky.dasjobs.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class JobMenuListener implements Listener {

    private final DasJobs plugin;
    private final Map<UUID, Inventory> openJobMenus = new HashMap<>();

    private static final Integer[] PLACEHOLDER_SLOTS_ARRAY = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 49, 50, 51, 52
    };
    private static final List<Integer> PLACEHOLDER_SLOTS = Collections.unmodifiableList(Arrays.asList(PLACEHOLDER_SLOTS_ARRAY));

    private static final int CLOSE_BUTTON_SLOT = 53;

    public JobMenuListener(DasJobs plugin) {
        this.plugin = plugin;
    }

    public void openJobMenu(Player player) {
        String inventoryTitle = ChatUtil.colorize("&cJobmenü");
        Inventory jobMenu = Bukkit.createInventory(null, 54, inventoryTitle);

        openJobMenus.put(player.getUniqueId(), jobMenu);

        ItemStack placeholderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        for (int slot : PLACEHOLDER_SLOTS) {
            jobMenu.setItem(slot, placeholderItem);
        }

        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setName(ChatUtil.colorize("&cSchließen"))
                .setLore(ChatUtil.colorize(Collections.singletonList("&7Klicke, um das Menü zu schließen.")).toArray(new String[0]))
                .build();
        jobMenu.setItem(CLOSE_BUTTON_SLOT, closeButton);

        addJobItem(jobMenu, 11, "miner", "Miner", player);
        addJobItem(jobMenu, 12, "holzfaeller", "Holzfäller", player);
        addJobItem(jobMenu, 13, "jaeger", "Jäger", player);
        addJobItem(jobMenu, 14, "graeber", "Gräber", player);
        addJobItem(jobMenu, 15, "farmer", "Farmer", player);

        addRewardMenuItem(jobMenu, 20, "miner");
        addRewardMenuItem(jobMenu, 21, "holzfaeller");
        addRewardMenuItem(jobMenu, 22, "jaeger");
        addRewardMenuItem(jobMenu, 23, "graeber");
        addRewardMenuItem(jobMenu, 24, "farmer");

        addTopListItem(jobMenu, 29, "miner");
        addTopListItem(jobMenu, 30, "holzfaeller");
        addTopListItem(jobMenu, 31, "jaeger");
        addTopListItem(jobMenu, 32, "graeber");
        addTopListItem(jobMenu, 33, "farmer");

        player.openInventory(jobMenu);
    }

    private void addJobItem(Inventory menu, int slot, String jobCanonicalName, String jobDisplayName, Player player) {
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobCanonicalName);
        Job job = optionalJob.orElse(null);

        List<String> lore = new ArrayList<>();

        if (job != null) {
            lore.addAll(ChatUtil.colorize(job.getDescription()));
            lore.add(ChatUtil.colorize(" "));

            boolean hasRewardsConfigured = false;

            if (job.getRewards().containsKey(JobAction.BREAK)) {
                Map<String, JobReward> breakRewards = job.getRewards().get(JobAction.BREAK);
                if (breakRewards != null && !breakRewards.isEmpty()) {
                    lore.add(ChatUtil.colorize("&cAbbaubare Blöcke:"));
                    breakRewards.forEach((materialName, reward) -> {
                        lore.add(ChatUtil.colorize("&8 - &7" + materialName + ": &e+" + String.format("%.2f", reward.getExperience()) + " XP &8/ &a+" + String.format("%.2f", reward.getIncome()) + " " + plugin.getCurrencySymbol()));
                    });
                    hasRewardsConfigured = true;
                }
            }

            if (job.getRewards().containsKey(JobAction.KILL)) {
                Map<String, JobReward> killRewards = job.getRewards().get(JobAction.KILL);
                if (killRewards != null && !killRewards.isEmpty()) {
                    lore.add(ChatUtil.colorize("&cTötbare Mobs:"));
                    killRewards.forEach((entityType, reward) -> {
                        lore.add(ChatUtil.colorize("&8 - &7" + entityType + ": &e+" + String.format("%.2f", reward.getExperience()) + " XP &8/ &a+" + String.format("%.2f", reward.getIncome()) + " " + plugin.getCurrencySymbol()));
                    });
                    hasRewardsConfigured = true;
                }
            }

            if (!hasRewardsConfigured) {
                lore.add(ChatUtil.colorize("&7Keine spezifischen Aktionen konfiguriert."));
            }

            lore.add(ChatUtil.colorize(" "));

            PlayerJobData playerJobData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerJobData != null) {
                int level = playerJobData.getLevel(jobCanonicalName);
                double xp = playerJobData.getExperience(jobCanonicalName);
                double requiredXp = job.getRequiredXpForLevel(level);

                lore.add(ChatUtil.colorize("&7Dein Level: &e" + level));
                lore.add(ChatUtil.colorize("&7Deine Erfahrung: &f" + String.format("%.2f", xp) + " &8/ &f" + String.format("%.2f", requiredXp) + " XP"));

                if (level >= job.getLevelXpMax()) {
                    lore.add(ChatUtil.colorize("&aDu hast das Max-XP Level für diesen Job erreicht!"));
                } else if (level == 0 && xp == 0.0) {
                    lore.add(ChatUtil.colorize("&7Du hast noch keine Erfahrung in diesem Job gesammelt."));
                } else {
                }
                lore.add(ChatUtil.colorize(" "));
                lore.add(ChatUtil.colorize("&7Klicke für detaillierte Statistiken."));
            } else {
                lore.add(ChatUtil.colorize("&7Jobdaten nicht verfügbar."));
            }

        } else {
            plugin.getLogger().warning("Could not find job configuration for job: " + jobCanonicalName);
            lore.add(ChatUtil.colorize("&cJob-Beschreibung nicht verfügbar."));
            lore.add(ChatUtil.colorize("&cBitte kontaktiere einen Administrator."));
        }

        // KORRIGIERT: Konvertiere den String-Wert in ein Material-Objekt
        String materialName = (job != null) ? job.getMenuIconMaterial() : "BARRIER";
        Material material = Material.getMaterial(materialName);

        // Füge einen Fallback hinzu, falls das Material ungültig ist
        if (material == null) {
            plugin.getLogger().warning("Ungültiges Material '" + materialName + "' für Job '" + jobCanonicalName + "'. Verwende BARRIER als Standard.");
            material = Material.BARRIER;
        }

        ItemStack jobItem = new ItemBuilder(material)
                .setName(ChatUtil.colorize("&c" + jobDisplayName + " Job"))
                .setLore(lore.toArray(new String[0]))
                .build();
        menu.setItem(slot, jobItem);
    }

    private void addRewardMenuItem(Inventory menu, int slot, String jobCanonicalName) {
        Job job = plugin.getJobManager().getJob(jobCanonicalName).orElse(null);
        String displayName = "Job nicht gefunden";
        if (job != null) {
            displayName = job.getDisplayName();
        }

        ItemStack rewardItem = new ItemBuilder(Material.ENCHANTED_BOOK)
                .setName(ChatUtil.colorize("&eBelohnungen"))
                .setLore(ChatUtil.colorize(Collections.singletonList(
                        "&7Klicke, um das Belohnungsmenü für &c" + displayName + " &7zu öffnen."
                )).toArray(new String[0]))
                .build();
        menu.setItem(slot, rewardItem);
    }

    private void addTopListItem(Inventory menu, int slot, String jobCanonicalName) {
        Job job = plugin.getJobManager().getJob(jobCanonicalName).orElse(null);
        String displayName = "Job nicht gefunden";
        if (job != null) {
            displayName = job.getDisplayName();
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.colorize("&7Klicke, um die Top 10 Spieler für &c" + displayName + " &7zu sehen."));
        lore.add(ChatUtil.colorize(" "));
        lore.add(ChatUtil.colorize("&bTopliste:"));

        List<Map.Entry<UUID, Integer>> topPlayers = plugin.getPlayerDataManager().getTopPlayersForJob(jobCanonicalName, 10);

        if (topPlayers != null && !topPlayers.isEmpty()) {
            for (int i = 0; i < topPlayers.size(); i++) {
                Map.Entry<UUID, Integer> entry = topPlayers.get(i);
                String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                if (playerName == null) {
                    playerName = "Unbekannt";
                }
                lore.add(ChatUtil.colorize("&b" + (i + 1) + ". Platz: &c" + playerName + " &7mit &a" + entry.getValue() + " Level"));
            }
        } else {
            lore.add(ChatUtil.colorize("&7Keine Top Spieler verfügbar."));
        }

        ItemStack topListItem = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .setName(ChatUtil.colorize("&bTOP 10 Spieler:"))
                .setLore(lore.toArray(new String[0]))
                .build();
        menu.setItem(slot, topListItem);
    }

    public void updateLeaderboardItems() {
        for (UUID playerId : new ArrayList<>(openJobMenus.keySet())) {
            Player player = Bukkit.getPlayer(playerId);
            Inventory menu = openJobMenus.get(playerId);

            if (player != null && player.isOnline() && menu != null &&
                    player.getOpenInventory().getTopInventory().equals(menu) &&
                    player.getOpenInventory().getTitle().equals(ChatUtil.colorize("&cJobmenü"))) {

                addTopListItem(menu, 29, "miner");
                addTopListItem(menu, 30, "holzfaeller");
                addTopListItem(menu, 31, "jaeger");
                addTopListItem(menu, 32, "graeber");
                addTopListItem(menu, 33, "farmer");

                addJobItem(menu, 11, "miner", "Miner", player);
                addJobItem(menu, 12, "holzfaeller", "Holzfäller", player);
                addJobItem(menu, 13, "jaeger", "Jäger", player);
                addJobItem(menu, 14, "graeber", "Gräber", player);
                addJobItem(menu, 15, "farmer", "Farmer", player);

            } else {
                openJobMenus.remove(playerId);
            }
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (openJobMenus.containsKey(player.getUniqueId()) && openJobMenus.get(player.getUniqueId()).equals(event.getInventory())) {

            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.0F);

            if (event.getRawSlot() == CLOSE_BUTTON_SLOT) {
                player.closeInventory();
                plugin.getMessageManager().sendMessage(player, "&7Jobmenü geschlossen.");
                return;
            }

            if (PLACEHOLDER_SLOTS.contains(event.getRawSlot())) {
                return;
            }

            String clickedJobName = null;
            switch (event.getRawSlot()) {
                case 11: clickedJobName = "miner"; break;
                case 12: clickedJobName = "holzfaeller"; break;
                case 13: clickedJobName = "jaeger"; break;
                case 14: clickedJobName = "graeber"; break;
                case 15: clickedJobName = "farmer"; break;
            }
            if (clickedJobName != null) {
                player.closeInventory();
                plugin.getCommand("jobs").execute(player, "jobs", new String[]{"info", clickedJobName});
                return;
            }
            String rewardJobName = null;
            switch (event.getRawSlot()) {
                case 20: rewardJobName = "miner"; break;
                case 21: rewardJobName = "holzfaeller"; break;
                case 22: rewardJobName = "jaeger"; break;
                case 23: rewardJobName = "graeber"; break;
                case 24: rewardJobName = "farmer"; break;
            }
            if (rewardJobName != null) {
                Optional<Job> optionalJob = plugin.getJobManager().getJob(rewardJobName);
                if (optionalJob.isPresent()) {
                    player.closeInventory();
                    plugin.getRewardMenuListener().openRewardMenu(player, optionalJob.get());
                } else {
                    plugin.getMessageManager().sendMessage(player, "&cJob '" + rewardJobName + "' nicht gefunden.");
                }
                return;
            }

            String topListJobName = null;
            switch (event.getRawSlot()) {
                case 29: topListJobName = "miner"; break;
                case 30: topListJobName = "holzfaeller"; break;
                case 31: topListJobName = "jaeger"; break;
                case 32: topListJobName = "graeber"; break;
                case 33: topListJobName = "farmer"; break;
            }
            if (topListJobName != null) {
                player.closeInventory();
                plugin.getCommand("jobs").execute(player, "jobs", new String[]{"top", topListJobName});
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        openJobMenus.remove(player.getUniqueId());
    }
}