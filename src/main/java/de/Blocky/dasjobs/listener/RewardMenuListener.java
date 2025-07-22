package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Job;
import de.Blocky.dasjobs.data.PlayerJobData;
import de.Blocky.dasjobs.util.ChatUtil;
import de.Blocky.dasjobs.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class RewardMenuListener implements Listener {

    private final DasJobs plugin;

    private final Map<UUID, String> openRewardMenus = new HashMap<>();
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

    public RewardMenuListener(DasJobs plugin) {
        this.plugin = plugin;
    }

    public void openRewardMenu(Player player, Job job) {
        String inventoryTitle = ChatUtil.colorize("&c" + job.getName() + " Belohnungen");
        Inventory rewardInventory = Bukkit.createInventory(null, 54, inventoryTitle);

        openRewardMenus.put(player.getUniqueId(), job.getName());

        ItemStack placeholderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        for (int slot : PLACEHOLDER_SLOTS) {
            rewardInventory.setItem(slot, placeholderItem);
        }
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setName("&cSchließen")
                .setLore("&7Klicke, um das Menü zu schließen.")
                .build();
        rewardInventory.setItem(CLOSE_BUTTON_SLOT, closeButton);
        PlayerJobData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        List<Integer> availableContentSlots = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (!PLACEHOLDER_SLOTS.contains(i) && i != CLOSE_BUTTON_SLOT) {
                availableContentSlots.add(i);
            }
        }
        Collections.sort(availableContentSlots);

        int currentContentSlotIndex = 0;
        Map<Integer, ItemStack> configuredRewards = plugin.getRewardManager().getJobRewards(job.getName());

        List<Integer> sortedLevels = new ArrayList<>(configuredRewards.keySet());
        Collections.sort(sortedLevels);

        for (int level : sortedLevels) {
            ItemStack originalRewardItem = configuredRewards.get(level);
            if (originalRewardItem == null) {
                plugin.getLogger().warning("No original reward item found for job " + job.getName() + " level " + level);
                continue;
            }

            String rewardId = job.getName() + "_level_" + level;

            ItemBuilder itemBuilder;
            List<String> displayLore = new ArrayList<>();
            String hiddenLevelIdLore = "&0Level_ID:" + level;
            itemBuilder = new ItemBuilder(originalRewardItem.clone());

            if (playerData.getLevel(job.getName()) >= level) {
                if (!playerData.hasClaimedReward(job.getName(), rewardId)) {
                    displayLore.add("");
                    displayLore.add("&aZum Abholen bereit");
                    itemBuilder.addEnchantment(Enchantment.UNBREAKING, 1);
                    itemBuilder.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                } else {
                    displayLore.add("");
                    displayLore.add("&cBereits abgeholt!");
                }
            } else {
                displayLore.add("");
                displayLore.add("&cBenötigtes Level: " + level);
            }

            displayLore.add(hiddenLevelIdLore);
            itemBuilder.setLore(displayLore.toArray(new String[0]));

            if (currentContentSlotIndex < availableContentSlots.size()) {
                rewardInventory.setItem(availableContentSlots.get(currentContentSlotIndex), itemBuilder.build());
                currentContentSlotIndex++;
            } else {
                plugin.getLogger().warning("Not enough slots in reward menu for all rewards for job " + job.getName());
                break;
            }
        }

        player.openInventory(rewardInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getHolder() == null && event.getView().getTitle().contains("Belohnungen")) {
            if (!openRewardMenus.containsKey(player.getUniqueId())) {
                return;
            }

            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            if (event.getRawSlot() == CLOSE_BUTTON_SLOT) {
                player.closeInventory();
                plugin.getMessageManager().sendMessage(player, "&7Belohnungsmenü geschlossen.");
                return;
            }

            if (PLACEHOLDER_SLOTS.contains(event.getRawSlot())) {
                return;
            }

            String jobName = openRewardMenus.get(player.getUniqueId());
            Job job = plugin.getJobManager().getJob(jobName).orElse(null);

            if (job == null) {
                plugin.getMessageManager().sendMessage(player, "&cFehler: Jobdaten konnten nicht gefunden werden.");
                player.closeInventory();
                return;
            }

            PlayerJobData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

            int rewardLevel = getLevelFromRewardItem(clickedItem);
            if (rewardLevel == -1) {
                plugin.getLogger().log(Level.WARNING, "Clicked item in reward menu does not contain a valid reward level ID in its lore. Item: " + (clickedItem.hasItemMeta() ? clickedItem.getItemMeta().getDisplayName() : clickedItem.getType().name()));
                return;
            }

            String rewardId = job.getName() + "_level_" + rewardLevel;

            if (playerData.getLevel(jobName) >= rewardLevel) {
                if (!playerData.hasClaimedReward(job.getName(), rewardId)) {
                    Optional<ItemStack> actualRewardOpt = plugin.getRewardManager().getRewardItem(job.getName(), rewardLevel);

                    if (actualRewardOpt.isPresent()) {
                        ItemStack actualReward = actualRewardOpt.get();

                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(actualReward.clone());
                            playerData.addClaimedReward(job.getName(), rewardId);
                            plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

                            plugin.getMessageManager().sendMessage(player, "&aDu hast die Belohnung für Level &b" + rewardLevel + " &ades Berufs &b" + jobName + " &aerfolgreich abgeholt!");
                            updateRewardMenuItem(event.getInventory(), event.getRawSlot(), jobName, rewardLevel, true, actualReward);
                        } else {
                            plugin.getMessageManager().sendMessage(player, "&cDein Inventar ist voll! Mache Platz, um die Belohnung abzuholen.");
                        }
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Reward item not found in config for job " + jobName + ", level " + rewardLevel + ". This should not happen if saved correctly.");
                        plugin.getMessageManager().sendMessage(player, "&cFehler: Belohnungsitem konnte nicht gefunden werden. Bitte kontaktiere einen Admin.");
                    }
                } else {
                    plugin.getMessageManager().sendMessage(player, "&cDu hast diese Belohnung bereits abgeholt.");
                }
            } else {
                plugin.getMessageManager().sendMessage(player, "&cDu hast das erforderliche Level für diese Belohnung noch nicht erreicht (Benötigt Level " + rewardLevel + ").");
            }
        }
    }

    private int getLevelFromRewardItem(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            for (String line : lore) {
                String strippedLine = ChatUtil.stripColor(line);
                if (strippedLine.startsWith("Level_ID:")) {
                    try {
                        return Integer.parseInt(strippedLine.substring("Level_ID:".length()));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to parse level from hidden lore: " + line, e);
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    private void updateRewardMenuItem(Inventory inventory, int slot, String jobName, int rewardLevel, boolean claimed, ItemStack originalRewardItem) {
        ItemBuilder itemBuilder;
        List<String> displayLore = new ArrayList<>();
        String hiddenLevelIdLore = "&0Level_ID:" + rewardLevel;
        itemBuilder = new ItemBuilder(originalRewardItem.clone());

        if (claimed) {
            displayLore.add("");
            displayLore.add("&cBereits abgeholt!");
        } else {
            displayLore.add("");
            displayLore.add("&aZum Abholen bereit");
            itemBuilder.addEnchantment(Enchantment.UNBREAKING, 1);
            itemBuilder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        displayLore.add(hiddenLevelIdLore);
        itemBuilder.setLore(displayLore.toArray(new String[0]));
        inventory.setItem(slot, itemBuilder.build());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        openRewardMenus.remove(player.getUniqueId());
    }
}