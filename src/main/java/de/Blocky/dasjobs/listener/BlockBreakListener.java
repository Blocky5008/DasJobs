package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Booster;
import de.Blocky.dasjobs.data.JobAction;
import de.Blocky.dasjobs.data.JobReward;
import de.Blocky.dasjobs.data.PlayerJobData;
import de.Blocky.dasjobs.data.Quest;
import de.Blocky.dasjobs.manager.MessageManager;
import de.Blocky.dasjobs.util.ChatUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockBreakListener implements Listener {

    private final DasJobs plugin;
    private final MessageManager messageManager;
    private final Map<UUID, Integer> infinityAxeBreakCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> infinityPickaxeBreakCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> infinityShovelBreakCounter = new ConcurrentHashMap<>();
    private BukkitRunnable counterResetTask;

    public BlockBreakListener(DasJobs plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        startCounterResetTask();
    }

    private void startCounterResetTask() {
        if (counterResetTask != null) {
            counterResetTask.cancel();
        }
        counterResetTask = new BukkitRunnable() {
            @Override
            public void run() {
                infinityAxeBreakCounter.clear();
                infinityPickaxeBreakCounter.clear();
                infinityShovelBreakCounter.clear();
            }
        };
        counterResetTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopCounterResetTask() {
        if (counterResetTask != null) {
            counterResetTask.cancel();
            counterResetTask = null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (plugin.getBlockPlaceTracker().isPlayerPlaced(block.getLocation())) {
            plugin.getBlockPlaceTracker().removePlacedBlock(block.getLocation());
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (isInfinityAxe(itemInHand)) {
            int currentBreaks = infinityAxeBreakCounter.getOrDefault(player.getUniqueId(), 0);
            int maxAxeBreaks = plugin.getConfig().getInt("infinity-axe-max-blocks", 12);
            if (currentBreaks >= maxAxeBreaks) {
                return;
            }
            infinityAxeBreakCounter.put(player.getUniqueId(), currentBreaks + 1);
        }

        if (isInfinityPickaxe(itemInHand)) {
            int currentBreaks = infinityPickaxeBreakCounter.getOrDefault(player.getUniqueId(), 0);
            int maxPickaxeBreaks = plugin.getConfig().getInt("infinity-pickaxe-max-blocks", 4);
            if (currentBreaks >= maxPickaxeBreaks) {
                return;
            }
            infinityPickaxeBreakCounter.put(player.getUniqueId(), currentBreaks + 1);
        }

        if (isInfinityShovel(itemInHand)) {
            int currentBreaks = infinityShovelBreakCounter.getOrDefault(player.getUniqueId(), 0);
            int maxShovelBreaks = plugin.getConfig().getInt("infinity-shovel-max-blocks", 2);
            if (currentBreaks >= maxShovelBreaks) {
                return;
            }
            infinityShovelBreakCounter.put(player.getUniqueId(), currentBreaks + 1);
        }

        if (!block.isPreferredTool(itemInHand)) {
            return;
        }

        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() != ageable.getMaximumAge()) {
                return;
            }
        } else if (block.getBlockData() instanceof Cocoa cocoa) {
            if (cocoa.getAge() != cocoa.getMaximumAge()) {
                return;
            }
        }

        final String worldName = block.getWorld().getName();
        final Material material = block.getType();
        final String effectiveBlockKey = material.name().toUpperCase();

        plugin.getJobManager().getJobs().values().stream()
                .filter(job -> job.getRewards().containsKey(JobAction.BREAK))
                .filter(job -> job.getRewards().get(JobAction.BREAK).containsKey(effectiveBlockKey))
                .filter(job -> !job.getDisabledWorlds().contains(worldName))
                .forEach(job -> {
                    PlayerJobData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                    JobReward reward = job.getReward(JobAction.BREAK, effectiveBlockKey);

                    if (reward != null) {
                        double baseIncome = reward.getIncome();
                        int playerLevel = playerData.getLevel(job.getName());
                        double levelBonusMoney = job.getMoneyBonusForLevel(playerLevel);
                        double earnedMoney = baseIncome + levelBonusMoney;

                        double earnedXp = reward.getExperience();

                        earnedMoney *= plugin.getBoosterManager().getBoostMultiplier(job.getName(), Booster.BoostType.MONEY);
                        earnedXp *= plugin.getBoosterManager().getBoostMultiplier(job.getName(), Booster.BoostType.XP);

                        plugin.getEconomyManager().depositPlayer(player, earnedMoney);
                        plugin.addTemporaryEarnedMoney(player.getUniqueId(), earnedMoney);
                        playerData.addExperience(job.getName(), earnedXp);

                        int currentLevel = playerData.getLevel(job.getName());
                        double currentXp = playerData.getExperience(job.getName());
                        double xpNeededForNextLevel = job.getRequiredXpForLevel(currentLevel);

                        if (currentXp >= xpNeededForNextLevel) {
                            playerData.setLevel(job.getName(), currentLevel + 1);
                            playerData.setExperience(job.getName(), currentXp - xpNeededForNextLevel);
                            messageManager.sendMessage(player, "&7Glückwunsch! Du bist in Job &a" + job.getName() + " &7auf Level &a" + (currentLevel + 1) + " &7aufgestiegen!");
                            player.sendTitle(ChatUtil.colorize("&aLEVEL UP!"), ChatUtil.colorize("&e" + job.getName() + " Level " + (currentLevel + 1)), 10, 40, 10);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        }

                        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());

                        double percentage = (xpNeededForNextLevel > 0) ? (currentXp / xpNeededForNextLevel) * 100 : 0;
                        percentage = Math.min(percentage, 100);

                        String actionBarMessage = ChatUtil.colorize(
                                "&e" + String.format("%.1f", earnedXp) + "XP &8• &a" + String.format("%.2f", earnedMoney) + plugin.getCurrencySymbol() + " &8• &c" + job.getName() + " &8• &bLevel " + playerData.getLevel(job.getName()) +
                                        " &8• &b" + String.format("%.0f", percentage) + "%"
                        );
                        player.sendActionBar(actionBarMessage);
                    }
                });

        plugin.getQuestManager().checkQuestProgress(player, Quest.QuestTask.BREAK, effectiveBlockKey);
    }

    private boolean isInfinityAxe(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        String materialName = item.getType().name();
        return materialName.contains("_AXE") && item.containsEnchantment(Enchantment.INFINITY);
    }

    private boolean isInfinityPickaxe(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        String materialName = item.getType().name();
        return materialName.contains("_PICKAXE") && item.containsEnchantment(Enchantment.INFINITY);
    }

    private boolean isInfinityShovel(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        String materialName = item.getType().name();
        return materialName.contains("_SHOVEL") && item.containsEnchantment(Enchantment.INFINITY);
    }
}
