package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Quest;
import de.Blocky.dasjobs.data.QuestProgress;
import de.Blocky.dasjobs.util.ChatUtil;
import de.Blocky.dasjobs.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.*;

public class QuestMenuListener implements Listener {

    private final DasJobs plugin;
    private final Map<UUID, Inventory> openQuestMenus = new HashMap<>();
    private final Map<UUID, String> currentJobView = new HashMap<>();
    private final Map<UUID, Integer> currentPage = new HashMap<>();
    private final Map<UUID, String> selectedQuest = new HashMap<>();
    private final Map<UUID, org.bukkit.boss.BossBar> bossBars = new HashMap<>();
    private static final Map<String, List<Integer>> JOB_QUEST_SLOTS = Map.of(
            "miner", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
            "holzfaeller", Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17),
            "jaeger", Arrays.asList(19, 20, 21, 22, 23, 24, 25, 26),
            "graeber", Arrays.asList(28, 29, 30, 31, 32, 33, 34, 35),
            "farmer", Arrays.asList(37, 38, 39, 40, 41, 42, 43, 44)
    );

    public QuestMenuListener(DasJobs plugin) {
        this.plugin = plugin;
    }

    public void openQuestMenu(Player player) {
        String inventoryTitle = ChatUtil.colorize("&cQuests");
        Inventory questMenu = Bukkit.createInventory(null, 54, inventoryTitle);

        openQuestMenus.put(player.getUniqueId(), questMenu);
        currentJobView.put(player.getUniqueId(), null);
        currentPage.put(player.getUniqueId(), 0);

        fillQuestMenu(questMenu, player, null, 0);
        player.openInventory(questMenu);
    }

    private void fillQuestMenu(Inventory menu, Player player, String jobName, int page) {
        menu.clear();
        addNavigationItems(menu, jobName, page);
        addJobCategoryItems(menu, player);
        addJobQuestItems(menu, player, jobName, page);
    }

    private void addNavigationItems(Inventory menu, String jobName, int page) {
        ItemStack backButton = new ItemBuilder(Material.REDSTONE)
                .setName(ChatUtil.colorize("&cZurück"))
                .setLore(ChatUtil.colorize(Collections.singletonList("&7Klicke, um zurückzugehen.")).toArray(new String[0]))
                .build();
        menu.setItem(45, backButton);

        int totalPages = 1;
        if (jobName != null) {
            List<Integer> questSlots = JOB_QUEST_SLOTS.get(jobName.toLowerCase());
            int questsPerPage = (questSlots != null ? questSlots.size() : 8);
            int totalQuests = plugin.getQuestManager().getQuestsForJob(jobName).size();
            totalPages = (int) Math.ceil(totalQuests / (double) questsPerPage);
        } else {
            int maxPages = 1;
            for (String job : Arrays.asList("miner", "holzfaeller", "jaeger", "graeber", "farmer")) {
                List<Integer> questSlots = JOB_QUEST_SLOTS.get(job.toLowerCase());
                int questsPerPage = (questSlots != null ? questSlots.size() : 8);
                int totalQuests = plugin.getQuestManager().getQuestsForJob(job).size();
                int pages = (int) Math.ceil(totalQuests / (double) questsPerPage);
                if (pages > maxPages) maxPages = pages;
            }
            totalPages = maxPages;
        }
        if (totalPages <= 0) totalPages = 1;

        boolean showPagination = totalPages > 1;

        if (showPagination && page > 0) {
            ItemStack prevButton = new ItemBuilder(Material.ARROW)
                    .setName(ChatUtil.colorize("&7Zurück"))
                    .setLore(ChatUtil.colorize(Collections.singletonList("&7Klicke, um zur vorherigen Seite zu gehen.")).toArray(new String[0]))
                    .build();
            menu.setItem(48, prevButton);
        } else {
            ItemStack placeholderPrev = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .setName(" ")
                    .build();
            menu.setItem(48, placeholderPrev);
        }

        if (showPagination && page < totalPages - 1) {
            ItemStack nextButton = new ItemBuilder(Material.ARROW)
                    .setName(ChatUtil.colorize("&7Weiter"))
                    .setLore(ChatUtil.colorize(Collections.singletonList("&7Klicke, um zur nächsten Seite zu gehen.")).toArray(new String[0]))
                    .build();
            menu.setItem(50, nextButton);
        } else {
            ItemStack placeholderNext = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .setName(" ")
                    .build();
            menu.setItem(50, placeholderNext);
        }

        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setName(ChatUtil.colorize("&cSchließen"))
                .setLore(ChatUtil.colorize(Collections.singletonList("&7Klicke, um das Menü zu schließen.")).toArray(new String[0]))
                .build();
        menu.setItem(53, closeButton);

        int[] placeholderSlots = {46, 47, 49, 51, 52};
        ItemStack placeholder = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();
        for (int slot : placeholderSlots) {
            menu.setItem(slot, placeholder);
        }
    }

    private void addJobCategoryItems(Inventory menu, Player player) {
        addJobCategoryItem(menu, 0, "miner", "Miner", "DIAMOND_PICKAXE");
        addJobCategoryItem(menu, 9, "holzfaeller", "Holzfäller", "DIAMOND_AXE");
        addJobCategoryItem(menu, 18, "jaeger", "Jäger", "DIAMOND_SWORD");
        addJobCategoryItem(menu, 27, "graeber", "Gräber", "DIAMOND_SHOVEL");
        addJobCategoryItem(menu, 36, "farmer", "Farmer", "DIAMOND_HOE");
    }

    private void addJobCategoryItem(Inventory menu, int slot, String jobName, String displayName, String materialName) {
        Material material = Material.getMaterial(materialName);
        if (material == null) material = Material.BARRIER;

        ItemStack item = new ItemBuilder(material)
                .setName(ChatUtil.colorize("&c&l" + displayName))
                .build();
        menu.setItem(slot, item);
    }

    private void addJobQuestItems(Inventory menu, Player player, String jobName, int page) {
        if (jobName == null) {
            for (String job : Arrays.asList("miner", "holzfaeller", "jaeger", "graeber", "farmer")) {
                List<Quest> jobQuests = plugin.getQuestManager().getQuestsForJob(job);
                List<Integer> questSlots = JOB_QUEST_SLOTS.get(job.toLowerCase());

                if (questSlots == null) continue;

                int questsPerPage = questSlots.size();
                int startIndex = page * questsPerPage;
                int endIndex = Math.min(startIndex + questsPerPage, jobQuests.size());

                for (int i = startIndex; i < endIndex; i++) {
                    Quest quest = jobQuests.get(i);
                    int slot = questSlots.get(i - startIndex);

                    addQuestItem(menu, slot, quest, player);
                }
            }
        } else {
            List<Quest> jobQuests = plugin.getQuestManager().getQuestsForJob(jobName);
            List<Integer> questSlots = JOB_QUEST_SLOTS.get(jobName.toLowerCase());

            if (questSlots == null) return;

            int questsPerPage = questSlots.size();
            int startIndex = page * questsPerPage;
            int endIndex = Math.min(startIndex + questsPerPage, jobQuests.size());

            for (int i = startIndex; i < endIndex; i++) {
                Quest quest = jobQuests.get(i);
                int slot = questSlots.get(i - startIndex);

                addQuestItem(menu, slot, quest, player);
            }
        }
    }

    private void addQuestItem(Inventory menu, int slot, Quest quest, Player player) {
        Material displayMaterial = Material.getMaterial(quest.getDisplayItem());
        if (displayMaterial == null) displayMaterial = Material.BARRIER;

        QuestProgress progress = plugin.getQuestManager().getQuestProgress(player.getUniqueId(), quest.getId());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatUtil.colorize(quest.getLore()));

        int currentProgress = progress.getProgress(quest.getSpecificTask());
        int requiredAmount = quest.getAmount();
        if (progress.isCompleted()) {
            currentProgress = requiredAmount;
        }
        String progressText = ChatUtil.colorize("&7Fortschritt: &b(" + currentProgress + "/" + requiredAmount + ")");
        lore.add(progressText);

        if (progress.isCompleted() && !progress.isClaimed()) {
            lore.add(ChatUtil.colorize("&aBelohnung einsammeln"));
        } else if (progress.isClaimed()) {
            lore.add(ChatUtil.colorize("&7Quest abgeschlossen"));
        }

        ItemBuilder builder = new ItemBuilder(displayMaterial)
                .setName(ChatUtil.colorize("&c" + quest.getDisplayItem().replace("_", " ")))
                .setLore(lore.toArray(new String[0]));

        String sel = selectedQuest.get(player.getUniqueId());
        if (sel != null && sel.equals(quest.getId())) {
            builder.addGlow();
        }
        ItemStack item = builder.build();
        menu.setItem(slot, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!openQuestMenus.containsKey(player.getUniqueId()) ||
                !openQuestMenus.get(player.getUniqueId()).equals(event.getInventory())) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.0F);

        String currentJob = currentJobView.get(player.getUniqueId());
        int currentPageNum = currentPage.get(player.getUniqueId());

        switch (event.getRawSlot()) {
            case 53:
                player.closeInventory();
                plugin.getMessageManager().sendMessage(player, "&7Questmenü geschlossen.");
                return;
            case 45:
                if (currentJob != null) {
                    currentJobView.put(player.getUniqueId(), null);
                    currentPage.put(player.getUniqueId(), 0);
                    fillQuestMenu(event.getInventory(), player, null, 0);
                } else {
                    player.closeInventory();
                    plugin.getJobMenuListener().openJobMenu(player);
                }
                return;
            case 48:
                if (currentPageNum > 0) {
                    currentPage.put(player.getUniqueId(), currentPageNum - 1);
                    fillQuestMenu(event.getInventory(), player, currentJob, currentPageNum - 1);
                }
                return;
            case 50:
                if (currentJob != null) {
                    List<Integer> questSlots = JOB_QUEST_SLOTS.get(currentJob.toLowerCase());
                    int questsPerPage = (questSlots != null ? questSlots.size() : 8);
                    int totalQuests = plugin.getQuestManager().getQuestsForJob(currentJob).size();
                    int totalPages = (int) Math.ceil(totalQuests / (double) questsPerPage);
                    if (currentPageNum < totalPages - 1) {
                        currentPage.put(player.getUniqueId(), currentPageNum + 1);
                        fillQuestMenu(event.getInventory(), player, currentJob, currentPageNum + 1);
                    }
                } else {
                    int maxPages = 1;
                    for (String job : Arrays.asList("miner", "holzfaeller", "jaeger", "graeber", "farmer")) {
                        List<Integer> questSlots = JOB_QUEST_SLOTS.get(job.toLowerCase());
                        int questsPerPage = (questSlots != null ? questSlots.size() : 8);
                        int totalQuests = plugin.getQuestManager().getQuestsForJob(job).size();
                        int pages = (int) Math.ceil(totalQuests / (double) questsPerPage);
                        if (pages > maxPages) maxPages = pages;
                    }
                    int totalPages = maxPages;
                    if (currentPageNum < totalPages - 1) {
                        currentPage.put(player.getUniqueId(), currentPageNum + 1);
                        fillQuestMenu(event.getInventory(), player, null, currentPageNum + 1);
                    }
                }
                return;
        }

        if (currentJob == null) {
            String clickedJob = null;
            switch (event.getRawSlot()) {
                case 0: clickedJob = "miner"; break;
                case 9: clickedJob = "holzfaeller"; break;
                case 18: clickedJob = "jaeger"; break;
                case 27: clickedJob = "graeber"; break;
                case 36: clickedJob = "farmer"; break;
            }
            if (clickedJob != null) {
                currentJobView.put(player.getUniqueId(), clickedJob);
                currentPage.put(player.getUniqueId(), 0);
                fillQuestMenu(event.getInventory(), player, clickedJob, 0);
                return;
            }
        }

        if (currentJob == null) {
            for (String job : Arrays.asList("miner", "holzfaeller", "jaeger", "graeber", "farmer")) {
                List<Integer> questSlots = JOB_QUEST_SLOTS.get(job.toLowerCase());
                if (questSlots != null && questSlots.contains(event.getRawSlot())) {
                    List<Quest> jobQuests = plugin.getQuestManager().getQuestsForJob(job);
                    int questIndex = questSlots.indexOf(event.getRawSlot()) + (currentPageNum * questSlots.size());

                    if (questIndex < jobQuests.size()) {
                        Quest quest = jobQuests.get(questIndex);
                        QuestProgress progress = plugin.getQuestManager().getQuestProgress(player.getUniqueId(), quest.getId());

                        if (progress.isCompleted() && !progress.isClaimed()) {
                            if (plugin.getQuestManager().claimQuestReward(player, quest.getId())) {
                                fillQuestMenu(event.getInventory(), player, null, currentPageNum);
                                if (quest.getId().equals(selectedQuest.get(player.getUniqueId()))) {
                                    showCompletionAndHide(player);
                                }
                            }
                        } else {
                            handleSelectionToggle(player, quest);
                            fillQuestMenu(event.getInventory(), player, null, currentPageNum);
                        }
                    }
                    return;
                }
            }
        } else {
            List<Integer> questSlots = JOB_QUEST_SLOTS.get(currentJob.toLowerCase());
            if (questSlots != null && questSlots.contains(event.getRawSlot())) {
                List<Quest> jobQuests = plugin.getQuestManager().getQuestsForJob(currentJob);
                int questIndex = questSlots.indexOf(event.getRawSlot()) + (currentPageNum * questSlots.size());

                if (questIndex < jobQuests.size()) {
                    Quest quest = jobQuests.get(questIndex);
                    QuestProgress progress = plugin.getQuestManager().getQuestProgress(player.getUniqueId(), quest.getId());

                    if (progress.isCompleted() && !progress.isClaimed()) {
                        if (plugin.getQuestManager().claimQuestReward(player, quest.getId())) {
                            fillQuestMenu(event.getInventory(), player, currentJob, currentPageNum);
                            if (quest.getId().equals(selectedQuest.get(player.getUniqueId()))) {
                                showCompletionAndHide(player);
                            }
                        }
                    } else {
                        handleSelectionToggle(player, quest);
                        fillQuestMenu(event.getInventory(), player, currentJob, currentPageNum);
                    }
                }
            }
        }
    }

    private void handleSelectionToggle(Player player, Quest quest) {
        UUID uuid = player.getUniqueId();
        String currently = selectedQuest.get(uuid);
        if (currently != null && currently.equals(quest.getId())) {
            selectedQuest.remove(uuid);
            hideBossBar(player);
        } else {
            selectedQuest.put(uuid, quest.getId());
            showOrUpdateBossBar(player, quest);
        }
    }

    private void showOrUpdateBossBar(Player player, Quest quest) {
        UUID uuid = player.getUniqueId();
        QuestProgress progress = plugin.getQuestManager().getQuestProgress(uuid, quest.getId());
        int current = Math.min(progress.getProgress(quest.getSpecificTask()), quest.getAmount());
        int required = quest.getAmount();

        String rewardText;
        switch (quest.getReward()) {
            case MONEY:
                rewardText = quest.getRewardAmount() + plugin.getCurrencySymbol();
                break;
            case JOBXP:
                rewardText = quest.getRewardAmount() + " XP";
                break;
            case ITEM:
                String itemName = (quest.getRewardItem() != null && !quest.getRewardItem().isEmpty()) ? quest.getRewardItem() : quest.getSpecificTask();
                rewardText = quest.getRewardAmount() + "x " + itemName;
                break;
            default:
                rewardText = String.valueOf(quest.getRewardAmount());
        }

        String title = ChatUtil.colorize("&7Fortschritt: &b(" + current + "/" + required + ") &7- &b" + rewardText);
        double progressFraction = required <= 0 ? 0.0 : Math.min(1.0, (double) current / (double) required);

        BossBar bar = bossBars.get(uuid);
        if (bar == null) {
            bar = Bukkit.createBossBar(title, BarColor.BLUE, BarStyle.SOLID);
            bossBars.put(uuid, bar);
            bar.addPlayer(player);
        } else {
            bar.setTitle(title);
        }
        bar.setProgress(progressFraction);
        bar.setVisible(true);
        if (progress.isCompleted()) {
            showCompletionAndHide(player);
        }
    }

    private void showCompletionAndHide(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.get(uuid);
        if (bar == null) return;
        bar.setTitle(ChatUtil.colorize("&aQuest abgeschlossen"));
        bar.setColor(BarColor.GREEN);
        Bukkit.getScheduler().runTaskLater(plugin, () -> hideBossBar(player), 60L);
    }

    private void hideBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
        }
    }

    public void handleProgressUpdate(UUID playerUUID, String questId) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        String selected = selectedQuest.get(playerUUID);
        if (selected != null && selected.equals(questId)) {
            Quest quest = plugin.getQuestManager().getQuest(questId);
            if (quest != null) {
                showOrUpdateBossBar(player, quest);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        openQuestMenus.remove(player.getUniqueId());
        currentJobView.remove(player.getUniqueId());
        currentPage.remove(player.getUniqueId());
    }
}
