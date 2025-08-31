package de.Blocky.dasjobs.command;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Booster;
import de.Blocky.dasjobs.data.Job;
import de.Blocky.dasjobs.data.PlayerJobData;
import de.Blocky.dasjobs.data.Quest;
import de.Blocky.dasjobs.listener.JobMenuListener;
import de.Blocky.dasjobs.manager.MessageManager;
import de.Blocky.dasjobs.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.Optional;
import java.util.Map;
import java.util.List;

public class JobsCommand implements CommandExecutor {

    private final DasJobs plugin;
    private final MessageManager messageManager;
    private final JobMenuListener jobMenuListener;

    public JobsCommand(DasJobs plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.jobMenuListener = plugin.getJobMenuListener();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0) {
            if (player == null) {
                messageManager.sendMessage(sender, "&cDiesen Befehl können nur Spieler ausführen.");
                return true;
            }
            jobMenuListener.openJobMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "level":
                handleLevelCommand(sender, args);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            case "booster":
                handleBoosterCommand(sender, args);
                break;
            case "info":
                if (player == null) {
                    messageManager.sendMessage(sender, "&cDiesen Befehl können nur Spieler ausführen.");
                    break;
                }
                messageManager.sendMessage(sender, "&cInformationen über deine Jobs:");
                plugin.getJobManager().getJobs().values().forEach(job -> {
                    PlayerJobData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                    int level = playerData.getLevel(job.getName());
                    double xp = playerData.getExperience(job.getName());

                    double xpNeeded = job.getRequiredXpForLevel(level);
                    String progress = String.format("%.2f/%.2f XP", xp, xpNeeded);

                    messageManager.sendMessage(sender, "&c- &c" + job.getDisplayName() + ": &aLevel " + level + " &7(" + progress + ")");
                });
                break;
            case "quests":
                if (player == null) {
                    messageManager.sendMessage(sender, "&cDiesen Befehl können nur Spieler ausführen.");
                } else {
                    handleQuestsCommand(player, args);
                }
                break;
            case "belohnung":
                if (player == null) {
                    messageManager.sendMessage(sender, "&cDiesen Befehl können nur Spieler ausführen.");
                } else {
                    handleRewardMenuCommand(player, args);
                }
                break;
            case "setbelohnung":
                if (player == null) {
                    messageManager.sendMessage(sender, "&cDiesen Befehl können nur Spieler ausführen.");
                } else {
                    handleSetRewardCommand(player, args);
                }
                break;
            case "removebelohnung":
                handleRemoveRewardCommand(sender, args);
                break;
            case "resetbelohnung":
                handleResetClaimedRewardsCommand(sender, args);
                break;
            case "top":
                if (player == null) {
                    messageManager.sendMessage(sender, "&cDiesen Befehl können nur Spieler ausführen.");
                } else {
                    handleTopCommand(player, args);
                }
                break;
            case "hilfe":
                sendHelpMessage(sender);
                break;
            default:
                messageManager.sendMessage(sender, "&cUnbekannter Befehl. Nutze /jobs hilfe für Hilfe.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        messageManager.sendMessage(sender, "&c--- Job Hilfe ---");
        messageManager.sendMessage(sender, "&e/jobs &7- Öffnet das Jobmenü.");
        messageManager.sendMessage(sender, "&e/jobs hilfe &7- Zeigt diese Hilfe an.");
        messageManager.sendMessage(sender, "&e/jobs info &7- Zeigt Informationen über deine Jobs an."); // Updated description
        messageManager.sendMessage(sender, "&e/jobs quests &7- Öffnet das Questmenü.");
        messageManager.sendMessage(sender, "&e/jobs belohnung <JobName> &7- Öffnet das Belohnungsmenü für einen Job.");
        messageManager.sendMessage(sender, "&e/jobs top <JobName> &7- Zeigt die Top-Spieler für einen Job an.");
        messageManager.sendMessage(sender, "&a/jobs level <spieler> <jobname> [level] &7- Zeigt/Setzt das Job-Level eines Spielers. (Admin)");
        messageManager.sendMessage(sender, "&a/jobs setBelohnung <Jobname> <Level> &7- Setzt ein Belohnungsitem. (Admin)");
        messageManager.sendMessage(sender, "&a/jobs removeBelohnung <Jobname> <Level> &7- Löscht ein Belohnungsitem. (Admin)");
        messageManager.sendMessage(sender, "&a/jobs resetBelohnung <Spieler> <Jobname> &7- Setzt abgeholte Belohnungen zurück. (Admin)");
        messageManager.sendMessage(sender, "&a/jobs reload &7- Lädt alle Plugin-Dateien neu. (Admin)");
        messageManager.sendMessage(sender, "&a/jobs booster <xp|money> <jobname|*> <multiplier> <time_in_minutes> &7- Startet einen Job-Booster. (Admin)");
        messageManager.sendMessage(sender, "&a/jobs quests erstellen <item> <task> <specific> <amount> <reward> <rewardAmount> &7- Erstellt eine neue Quest. (Admin)");
    }

    private void handleLevelCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jobs.admin")) {
            messageManager.sendMessage(sender, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        if (args.length < 3) {
            messageManager.sendMessage(sender, "&cNutzung: /jobs level <spieler> <jobname> [level]");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            messageManager.sendMessage(sender, "&cSpieler '" + args[1] + "' nicht gefunden oder offline.");
            return;
        }

        String jobName = args[2].toLowerCase();
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobName);

        if (optionalJob.isEmpty()) {
            messageManager.sendMessage(sender, "&cJob '" + jobName + "' nicht gefunden.");
            return;
        }
        Job job = optionalJob.get();

        UUID targetUUID = targetPlayer.getUniqueId();
        PlayerJobData playerData = plugin.getPlayerDataManager().getPlayerData(targetUUID);

        if (args.length == 3) {
            int currentLevel = playerData.getLevel(job.getName());
            messageManager.sendMessage(sender, "&7Das aktuelle Level von &a" + targetPlayer.getName() + " &7im Job &a" + job.getDisplayName() + " &7ist: &a" + currentLevel);
        } else if (args.length == 4) {
            try {
                int newLevel = Integer.parseInt(args[3]);
                if (newLevel < 0) {
                    messageManager.sendMessage(sender, "&cDas Level muss eine positive Zahl sein.");
                    return;
                }

                playerData.setLevel(job.getName(), newLevel);
                playerData.setExperience(job.getName(), 0);
                plugin.getPlayerDataManager().savePlayerData(targetUUID);

                messageManager.sendMessage(sender, "&7Level von &a" + targetPlayer.getName() + " &7im Job &a" + job.getDisplayName() + " &7auf &a" + newLevel + " &7gesetzt.");
                messageManager.sendMessage(targetPlayer, "&7Dein Level im Job &a" + job.getDisplayName() + " &7wurde auf &a" + newLevel + " &7gesetzt!");

            } catch (NumberFormatException e) {
                messageManager.sendMessage(sender, "&cUngültiges Level.");
            }
        } else {
            messageManager.sendMessage(sender, "&cNutzung: /jobs level <spieler> <jobname> [level]");
        }
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("jobs.admin")) {
            messageManager.sendMessage(sender, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        plugin.reloadPluginFiles();
        messageManager.sendMessage(sender, "&aDasJobs Konfigurationsdateien wurden neu geladen.");
    }

    private void handleBoosterCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jobs.admin")) {
            messageManager.sendMessage(sender, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        if (args.length < 5) {
            messageManager.sendMessage(sender, "&cNutzung: /jobs booster <xp|money> <jobname|*> <multiplier> <time_in_minutes>");
            return;
        }

        Booster.BoostType boostType;
        try {
            boostType = Booster.BoostType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            messageManager.sendMessage(sender, "&cUngültiger Boost-Typ. Nutze 'xp' oder 'money'.");
            return;
        }

        String jobName = args[2].toLowerCase();
        if (!jobName.equals("*")) {
            if (plugin.getJobManager().getJob(jobName).isEmpty()) {
                messageManager.sendMessage(sender, "&cJob '" + jobName + "' nicht gefunden. Nutze '*' für alle Jobs.");
                return;
            }
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[3]);
            if (multiplier <= 0) {
                messageManager.sendMessage(sender, "&cDer Multiplikator muss größer als 0 sein.");
                return;
            }
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "&cUngültiger Multiplikator.");
            return;
        }

        long durationMinutes;
        try {
            durationMinutes = Long.parseLong(args[4]);
            if (durationMinutes <= 0) {
                messageManager.sendMessage(sender, "&cDie Zeit in Minuten muss größer als 0 sein.");
                return;
            }
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "Error: Invalid number format.");
            return;
        }

        String starterName = sender.getName();

        Booster newBooster = new Booster(jobName, boostType, multiplier, durationMinutes, starterName);
        plugin.getBoosterManager().addBooster(newBooster);

        String boostTypeName = (boostType == Booster.BoostType.XP) ? "XP" : "Geld";
        String titleLine1 = ChatUtil.colorize("&c&lJob Booster aktiviert!");
        String titleLine2 = ChatUtil.colorize("&7" + boostTypeName + " ist nun aktiv für " + durationMinutes + " Minuten");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendTitle(titleLine1, titleLine2, 10, 70, 20);
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }

        Bukkit.broadcastMessage(plugin.getPrefix() + ChatUtil.colorize("&7Der Spieler &a" + sender.getName() + " &7hat einen &a" + String.format("%.2f", multiplier) + " &7" + boostTypeName + " Booster für &a" + durationMinutes + " Minuten &7gezündet."));
    }

    private void handleRewardMenuCommand(Player player, String[] args) {
        if (args.length < 2) {
            messageManager.sendMessage(player, "&cNutzung: /jobs belohnung <Jobname>");
            return;
        }

        String jobName = args[1].toLowerCase();
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobName);

        if (optionalJob.isEmpty()) {
            messageManager.sendMessage(player, "&cJob '" + args[1] + "' nicht gefunden.");
            return;
        }
        Job job = optionalJob.get();

        plugin.getRewardMenuListener().openRewardMenu(player, job);
    }

    private void handleSetRewardCommand(Player player, String[] args) {
        if (!player.hasPermission("jobs.admin")) {
            messageManager.sendMessage(player, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        if (args.length < 3) {
            messageManager.sendMessage(player, "&cNutzung: /jobs setBelohnung <Jobname> <Level>");
            return;
        }

        String jobName = args[1].toLowerCase();
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobName);

        if (optionalJob.isEmpty()) {
            messageManager.sendMessage(player, "&cJob '" + args[1] + "' nicht gefunden.");
            return;
        }
        Job job = optionalJob.get();

        int level;
        try {
            level = Integer.parseInt(args[2]);
            if (level <= 0) {
                messageManager.sendMessage(player, "&cDas Level muss eine positive Ganzzahl sein.");
                return;
            }
        } catch (NumberFormatException e) {
            messageManager.sendMessage(player, "&cUngültiges Level. Bitte gib eine Zahl ein.");
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType().isAir()) {
            messageManager.sendMessage(player, "&cDu musst ein Item in der Hand halten, um es als Belohnung zu setzen.");
            return;
        }

        plugin.getRewardManager().setRewardItem(job.getName(), level, itemInHand.clone());
        messageManager.sendMessage(player, "&aBelohnung für Job &e" + job.getDisplayName() + " &aauf Level &b" + level + " &aerfolgreich gesetzt.");
    }

    private void handleRemoveRewardCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jobs.admin")) {
            messageManager.sendMessage(sender, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        if (args.length < 3) {
            messageManager.sendMessage(sender, "&cNutzung: /jobs removeBelohnung <Jobname> <Level>");
            return;
        }

        String jobName = args[1].toLowerCase();
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobName);

        if (optionalJob.isEmpty()) {
            messageManager.sendMessage(sender, "&cJob '" + args[1] + "' nicht gefunden.");
            return;
        }
        Job job = optionalJob.get();

        int level;
        try {
            level = Integer.parseInt(args[2]);
            if (level <= 0) {
                messageManager.sendMessage(sender, "&cDas Level muss eine positive Ganzzahl sein.");
                return;
            }
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "&cUngültiges Level. Bitte gib eine Zahl ein.");
            return;
        }

        if (plugin.getRewardManager().removeRewardItem(job.getName(), level)) {
            messageManager.sendMessage(sender, "&7Belohnung für Job &a" + job.getDisplayName() + " &7auf Level &a" + level + " &7erfolgreich entfernt.");
        } else {
            messageManager.sendMessage(sender, "&cEs wurde keine Belohnung für Job &a" + job.getDisplayName() + " &cauf Level &a" + level + " &cgefunden.");
        }
    }

    private void handleResetClaimedRewardsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("jobs.admin")) {
            messageManager.sendMessage(sender, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        if (args.length < 3) {
            messageManager.sendMessage(sender, "&cNutzung: /jobs resetBelohnung <Spieler> <Jobname>");
            return;
        }

        String targetPlayerName = args[1];
        OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);

        if (targetOfflinePlayer == null || (!targetOfflinePlayer.hasPlayedBefore() && !targetOfflinePlayer.isOnline())) {
            messageManager.sendMessage(sender, "&cSpieler '" + targetPlayerName + "' wurde nicht gefunden oder hat noch nie gespielt.");
            return;
        }

        String jobName = args[2].toLowerCase();
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobName);

        if (optionalJob.isEmpty()) {
            messageManager.sendMessage(sender, "&cJob '" + jobName + "' nicht gefunden.");
            return;
        }
        Job job = optionalJob.get();

        plugin.getPlayerDataManager().resetClaimedRewards(targetOfflinePlayer.getUniqueId(), job.getName());
        messageManager.sendMessage(sender, "&7Abgeholte Belohnungen für Spieler &a" + targetPlayerName + " &7im Job &a" + job.getDisplayName() + " &7erfolgreich zurückgesetzt.");

        if (targetOfflinePlayer.isOnline()) {
            plugin.getMessageManager().sendMessage(targetOfflinePlayer.getPlayer(), "&7Deine abgeholten Belohnungen im Job &a" + job.getDisplayName() + " &7wurden zurückgesetzt!");
        }
    }

    private void handleTopCommand(Player player, String[] args) {
        if (args.length < 2) {
            messageManager.sendMessage(player, "&cNutzung: /jobs top <Jobname>");
            return;
        }

        String jobName = args[1].toLowerCase();
        Optional<Job> optionalJob = plugin.getJobManager().getJob(jobName);

        if (optionalJob.isEmpty()) {
            messageManager.sendMessage(player, "&cJob '" + args[1] + "' nicht gefunden.");
            return;
        }
        Job job = optionalJob.get();

        messageManager.sendMessage(player, "&c--- TOP 10 Spieler für &4" + job.getDisplayName() + " &c---");

        List<Map.Entry<UUID, Integer>> topPlayers = plugin.getPlayerDataManager().getTopPlayersForJob(job.getName(), 10);

        if (topPlayers.isEmpty()) {
            messageManager.sendMessage(player, "&cEs gibt noch keine Spieler in diesem Job oder keine Daten vorhanden.");
            return;
        }

        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<UUID, Integer> entry = topPlayers.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unbekannter Spieler";
            int level = entry.getValue();
            messageManager.sendMessage(player, "&b" + (i + 1) + ". Platz  &c" + playerName + " &7mit &aLevel &a" + level);
        }

        PlayerJobData senderData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (senderData.hasJob(job.getName())) {
            int senderLevel = senderData.getLevel(job.getName());
            messageManager.sendMessage(player, "&7Dein Level in &a" + job.getDisplayName() + " &7ist: &a" + senderLevel);
        } else {
            messageManager.sendMessage(player, "&7Du hast noch keine Erfahrung in diesem Job gesammelt.");
        }
    }

    private void handleQuestsCommand(Player player, String[] args) {
        if (args.length == 1) {
            plugin.getQuestMenuListener().openQuestMenu(player);
        } else if (args.length >= 2 && args[1].equalsIgnoreCase("erstellen")) {
            handleCreateQuestCommand(player, args);
        } else if (args.length >= 2 && args[1].equalsIgnoreCase("zurücksetzen")) {
            handleResetQuestCommand(player, args);
        } else if (args.length >= 2 && args[1].equalsIgnoreCase("zuruecksetzen")) {
            // ASCII fallback
            handleResetQuestCommand(player, args);
        } else {
            messageManager.sendMessage(player, "&cNutzung: /jobs quests [erstellen|zurücksetzen]");
        }
    }

    private void handleResetQuestCommand(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("jobs.admin") || sender.hasPermission("jobs.*") || sender.isOp())) {
            messageManager.sendMessage(sender, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }
        if (args.length < 5) {
            messageManager.sendMessage(sender, "&cNutzung: /jobs quests zurücksetzen <Spieler> <Jobname> <Slot>");
            return;
        }
        String targetName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            messageManager.sendMessage(sender, "&cSpieler '" + targetName + "' nicht gefunden.");
            return;
        }
        String jobName = args[3].toLowerCase();
        Optional<Job> jobOpt = plugin.getJobManager().getJob(jobName);
        if (jobOpt.isEmpty()) {
            messageManager.sendMessage(sender, "&cJob '" + jobName + "' nicht gefunden.");
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            messageManager.sendMessage(sender, "&cUngültiger Slot. Bitte Zahl angeben.");
            return;
        }
        List<Quest> quests = plugin.getQuestManager().getQuestsForJob(jobName);
        Quest targetQuest = null;
        for (Quest q : quests) {
            if (q.getSlot() == slot) { targetQuest = q; break; }
        }
        if (targetQuest == null) {
            messageManager.sendMessage(sender, "&cKeine Quest im Job '" + jobName + "' mit Slot " + slot + " gefunden.");
            return;
        }
        boolean existed = plugin.getQuestManager().resetQuestProgress(target.getUniqueId(), targetQuest.getId());
        if (existed) {
            messageManager.sendMessage(sender, "&aQuest zurückgesetzt für &e" + targetName + " &7- Job &c" + jobOpt.get().getDisplayName() + " &7Slot &b" + slot + ".");
        } else {
            messageManager.sendMessage(sender, "&aQuest war bereits zurückgesetzt oder ohne Fortschritt für &e" + targetName + "&7. Dennoch gespeichert.");
        }
        if (target.isOnline()) {
            plugin.getMessageManager().sendMessage(target.getPlayer(), "&7Deine Quest im Job &a" + jobOpt.get().getDisplayName() + " &7(Slot &b" + slot + "&7) wurde zurückgesetzt.");
        }
    }

    private void handleCreateQuestCommand(Player player, String[] args) {
        if (!player.hasPermission("jobs.admin")) {
            messageManager.sendMessage(player, "&cDu hast keine Berechtigung für diesen Befehl.");
            return;
        }

        if (args.length < 8) {
            messageManager.sendMessage(player, "&cNutzung: /jobs quests erstellen <jobname> <item> <task> <specific> <amount> <reward> <rewardAmount> [rewardItem]");
            return;
        }

        String jobName = args[2].toLowerCase();
        String displayItem = args[3];
        String taskStr = args[4].toUpperCase();
        String specificTask = args[5];
        int amount;
        String rewardStr = args[6].toUpperCase();
        int rewardAmount;
        String rewardItem = "";

        try {
            amount = Integer.parseInt(args[5]);
            rewardAmount = Integer.parseInt(args[7]);
        } catch (NumberFormatException e) {
            messageManager.sendMessage(player, "&cUngültige Zahl für amount oder rewardAmount.");
            return;
        }

        if (args.length > 8) {
            rewardItem = args[8];
        }

        if (plugin.getJobManager().getJob(jobName).isEmpty()) {
            messageManager.sendMessage(player, "&cJob '" + jobName + "' nicht gefunden.");
            return;
        }

        Quest.QuestTask task;
        try {
            task = Quest.QuestTask.valueOf(taskStr);
        } catch (IllegalArgumentException e) {
            messageManager.sendMessage(player, "&cUngültige Aufgabe. Nutze BREAK, KILL oder PLACE.");
            return;
        }

        Quest.QuestReward reward;
        try {
            reward = Quest.QuestReward.valueOf(rewardStr);
        } catch (IllegalArgumentException e) {
            messageManager.sendMessage(player, "&cUngültige Belohnung. Nutze JOBXP, MONEY oder ITEM.");
            return;
        }

        Quest quest = plugin.getQuestManager().createQuest(jobName, displayItem, task, specificTask, amount, reward, rewardAmount, rewardItem);
        messageManager.sendMessage(player, "&aQuest erfolgreich erstellt mit ID: &e" + quest.getId());
    }
}