package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Booster;
import de.Blocky.dasjobs.data.JobAction;
import de.Blocky.dasjobs.data.JobReward;
import de.Blocky.dasjobs.data.PlayerJobData;
import de.Blocky.dasjobs.data.Quest;
import de.Blocky.dasjobs.manager.MessageManager;
import de.Blocky.dasjobs.util.ChatUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.Sound;

public class EntityKillListener implements Listener {

    private final DasJobs plugin;
    private final MessageManager messageManager;

    public EntityKillListener(DasJobs plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        final Player killer;
        if (entity.getKiller() instanceof Player) {
            killer = (Player) entity.getKiller();
        } else {
            return;
        }

        final String entityTypeKey = entity.getType().name().toUpperCase();

        final String worldName = entity.getWorld().getName();

        plugin.getJobManager().getJobs().values().stream()
                .filter(job -> job.getRewards().containsKey(JobAction.KILL))
                .filter(job -> job.getRewards().get(JobAction.KILL).containsKey(entityTypeKey))
                .filter(job -> !job.getDisabledWorlds().contains(worldName))
                .forEach(job -> {
                    PlayerJobData playerData = plugin.getPlayerDataManager().getPlayerData(killer.getUniqueId());
                    JobReward reward = job.getReward(JobAction.KILL, entityTypeKey);

                    if (reward != null) {
                        double baseIncome = reward.getIncome();
                        int playerLevel = playerData.getLevel(job.getName());
                        double levelBonusMoney = job.getMoneyBonusForLevel(playerLevel);
                        double earnedMoney = baseIncome + levelBonusMoney;

                        double earnedXp = reward.getExperience();

                        earnedMoney *= plugin.getBoosterManager().getBoostMultiplier(job.getName(), Booster.BoostType.MONEY);
                        earnedXp *= plugin.getBoosterManager().getBoostMultiplier(job.getName(), Booster.BoostType.XP);

                        plugin.getEconomyManager().depositPlayer(killer, earnedMoney);
                        plugin.addTemporaryEarnedMoney(killer.getUniqueId(), earnedMoney);
                        playerData.addExperience(job.getName(), earnedXp);

                        int currentLevel = playerData.getLevel(job.getName());
                        double currentXp = playerData.getExperience(job.getName());
                        double xpNeededForNextLevel = job.getRequiredXpForLevel(currentLevel);

                        if (currentXp >= xpNeededForNextLevel) {
                            playerData.setLevel(job.getName(), currentLevel + 1);
                            playerData.setExperience(job.getName(), currentXp - xpNeededForNextLevel);
                            messageManager.sendMessage(killer, "&aGlückwunsch! Du bist in Job &e" + job.getName() + " &aauf Level &b" + (currentLevel + 1) + " &aaufgestiegen!");
                            killer.sendTitle(ChatUtil.colorize("&aLEVEL UP!"), ChatUtil.colorize("&e" + job.getName() + " Level " + (currentLevel + 1)), 10, 40, 10);
                            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        }

                        plugin.getPlayerDataManager().savePlayerData(killer.getUniqueId());

                        double percentage = (xpNeededForNextLevel > 0) ? (currentXp / xpNeededForNextLevel) * 100 : 0;
                        percentage = Math.min(percentage, 100);

                        String actionBarMessage = ChatUtil.colorize(
                                "&e" + String.format("%.1f", earnedXp) + "XP &8• &a" + String.format("%.2f", earnedMoney) + "$ &8• &c" + job.getName() + " &8• &bLevel " + playerData.getLevel(job.getName()) +
                                        " &8• &b" + String.format("%.0f", percentage) + "%"
                        );
                        killer.sendActionBar(actionBarMessage);
                    }
                });

        plugin.getQuestManager().checkQuestProgress(killer, Quest.QuestTask.KILL, entityTypeKey);
    }
}