package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Booster;
import de.Blocky.dasjobs.data.JobAction;
import de.Blocky.dasjobs.data.JobReward;
import de.Blocky.dasjobs.data.PlayerJobData;
import de.Blocky.dasjobs.manager.MessageManager;
import de.Blocky.dasjobs.util.ChatUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.Sound;

public class BlockBreakListener implements Listener {

    private final DasJobs plugin;
    private final MessageManager messageManager;

    public BlockBreakListener(DasJobs plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (plugin.getBlockPlaceTracker().isPlayerPlaced(block.getLocation())) {
            plugin.getBlockPlaceTracker().removePlacedBlock(block.getLocation());
            return;
        }

        final String worldName = block.getWorld().getName();
        final Material material = block.getType();
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() != ageable.getMaximumAge()) {
                return;
            }
        } else if (block.getBlockData() instanceof Cocoa cocoa) {
            if (cocoa.getAge() != cocoa.getMaximumAge()) {
                return;
            }
        }
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
                                "&e" + String.format("%.1f", earnedXp) + "XP &8• &a" + String.format("%.2f", earnedMoney) + "$ &8• &c" + job.getName() + " &8• &bLevel " + playerData.getLevel(job.getName()) +
                                        " &8• &b" + String.format("%.0f", percentage) + "%"
                        );
                        player.sendActionBar(actionBarMessage);
                    }
                });
    }
}