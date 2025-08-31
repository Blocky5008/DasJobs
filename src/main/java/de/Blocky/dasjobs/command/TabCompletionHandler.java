package de.Blocky.dasjobs.command;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Quest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class TabCompletionHandler implements TabCompleter {

    private final DasJobs plugin;
    private static final List<String> COMMANDS = Arrays.asList(
            "hilfe", "info", "level", "reload", "booster",
            "belohnung", "setbelohnung", "removebelohnung", "resetbelohnung", "top", "quests"
    );

    public TabCompletionHandler(DasJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            addMatchingCompletions(completions, args[0], COMMANDS.toArray(new String[0]));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("level")) {
                plugin.getServer().getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            } else if (args[0].equalsIgnoreCase("booster")) {
                addMatchingCompletions(completions, args[1], "xp", "money");
            } else if (args[0].equalsIgnoreCase("belohnung") || args[0].equalsIgnoreCase("setbelohnung") ||
                    args[0].equalsIgnoreCase("removebelohnung") || args[0].equalsIgnoreCase("top") ||
                    args[0].equalsIgnoreCase("info")) {
                plugin.getJobManager().getJobs().keySet().forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("resetbelohnung")) {
                plugin.getServer().getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            } else if (args[0].equalsIgnoreCase("quests")) {
                List<String> sub = new ArrayList<>();
                sub.add("erstellen");
                if (sender.hasPermission("jobs.admin") || sender.hasPermission("jobs.*") || sender.isOp()) {
                    sub.add("zurücksetzen");
                }
                sub.forEach(s -> addMatchingCompletions(completions, args[1], s));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("level")) {
                plugin.getJobManager().getJobs().keySet().forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("booster")) {
                plugin.getJobManager().getJobs().keySet().forEach(completions::add);
                completions.add("*");
            } else if (args[0].equalsIgnoreCase("setbelohnung") && sender.hasPermission("jobs.admin")) {
                addMatchingCompletions(completions, args[2], "1", "5", "10", "20", "30", "40");
            } else if (args[0].equalsIgnoreCase("removebelohnung") && sender.hasPermission("jobs.admin")) {
                String jobName = args[1].toLowerCase();
                Set<Integer> rewardLevels = plugin.getRewardManager().getRewardLevelsForJob(jobName);
                rewardLevels.forEach(level -> completions.add(String.valueOf(level)));
            } else if (args[0].equalsIgnoreCase("resetbelohnung")) {
                plugin.getJobManager().getJobs().keySet().forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("quests") && args[1].equalsIgnoreCase("erstellen") && sender.hasPermission("jobs.admin")) {
                plugin.getJobManager().getJobs().keySet().forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("quests") && (args[1].equalsIgnoreCase("zurücksetzen") || args[1].equalsIgnoreCase("zuruecksetzen")) && (sender.hasPermission("jobs.admin") || sender.hasPermission("jobs.*") || sender.isOp())) {
                plugin.getServer().getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("level") && sender.hasPermission("jobs.admin")) {
                addMatchingCompletions(completions, args[3], "1", "10", "50", "100");
            } else if (args[0].equalsIgnoreCase("booster") && sender.hasPermission("jobs.admin")) {
                addMatchingCompletions(completions, args[3], "1.5", "2.0", "3.0", "5.0");
            } else if (args[0].equalsIgnoreCase("quests") && (args[1].equalsIgnoreCase("zurücksetzen") || args[1].equalsIgnoreCase("zuruecksetzen")) && (sender.hasPermission("jobs.admin") || sender.hasPermission("jobs.*") || sender.isOp())) {
                plugin.getJobManager().getJobs().keySet().forEach(completions::add);
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("booster") && sender.hasPermission("jobs.admin")) {
                addMatchingCompletions(completions, args[4], "5", "15", "30", "60", "120");
            } else if (args[0].equalsIgnoreCase("quests") && (args[1].equalsIgnoreCase("zurücksetzen") || args[1].equalsIgnoreCase("zuruecksetzen")) && (sender.hasPermission("jobs.admin") || sender.hasPermission("jobs.*") || sender.isOp())) {
                String jobName = args[3].toLowerCase();
                List<Quest> quests = plugin.getQuestManager().getQuestsForJob(jobName);
                Set<Integer> slots = new HashSet<>();
                for (Quest q : quests) {
                    slots.add(q.getSlot());
                }
                for (Integer s : slots) {
                    completions.add(String.valueOf(s));
                }
            }
        }
        List<String> finalCompletions = completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
        Collections.sort(finalCompletions);
        return finalCompletions;
    }

    private void addMatchingCompletions(List<String> list, String input, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(input.toLowerCase())) {
                list.add(candidate);
            }
        }
    }
}