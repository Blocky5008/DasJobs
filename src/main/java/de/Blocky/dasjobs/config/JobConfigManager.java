package de.Blocky.dasjobs.config;

import de.Blocky.dasjobs.DasJobs;
import de.Blocky.dasjobs.data.Job;
import de.Blocky.dasjobs.data.JobAction;
import de.Blocky.dasjobs.data.JobReward;
import de.Blocky.dasjobs.util.ChatUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JobConfigManager {

    private final DasJobs plugin;

    public JobConfigManager(DasJobs plugin) {
        this.plugin = plugin;
    }

    public void setupJobsConfig() {
        FileConfiguration config = plugin.getConfig();

        config.addDefault("prefix", "&c&lDasJobs &8>> ");
        config.addDefault("currency-symbol", "$");
        config.addDefault("minutes-for-summary", 1);


        if (!config.contains("jobs.#_HEADER")) {
            config.set("jobs.#_HEADER",
                    " # jobs: Hier können die verschiedenen Berufe konfiguriert werden.\n" +
                            " # Jeder Beruf hat seine eigenen Einstellungen für Level-Progression und Belohnungen.\n" +
                            " # --------------------------------------------------------------------------------\n" +
                            " # XP_FORMULA_EXPLANATION: Die Formel zur Berechnung der benötigten XP für das nächste Level ist:\n" +
                            " # RequiredXP = level-xp-coefficient-a * (CurrentLevel^2) + level-xp-constant-b\n" +
                            " # Beispiel: Wenn das aktuelle Level 1 ist, benötigt man XP = A * (1^2) + B\n" +
                            " # --------------------------------------------------------------------------------\n" +
                            " # REWARDS: Hier können die Belohnungen für jede Aktion (z.B. BREAK, KILL) und jedes Objekt/Entity konfiguriert werden.\n" +
                            " # Beispiel: rewards.Break.STONE.income: 1.0, rewards.Break.STONE.experience: 1.0\n" +
                            " #           rewards.Kill.ZOMBIE.income: 5.0, rewards.Kill.ZOMBIE.experience: 3.0\n" +
                            " # Achte auf die korrekte Schreibweise der Minecraft-Materialien und Entity-Typen (Großbuchstaben und Underscores für Leerzeichen).\n" +
                            " # Du kannst hier eigene Belohnungen hinzufügen oder bestehende ändern/entfernen.\n" +
                            " # --------------------------------------------------------------------------------\n" +
                            " # QUESTS: Hier können Quests für jeden Job konfiguriert werden.\n" +
                            " # Beispiel: quests.quest_1.slot: 1, quests.quest_1.lore: \"Sammle 10 Diamanten\", quests.quest_1.display-item: \"DIAMOND\"\n" +
                            " #          quests.quest_1.task: \"BREAK\", quests.quest_1.specific-task: \"DIAMOND_ORE\", quests.quest_1.amount: 10\n" +
                            " #          quests.quest_1.reward: \"JOBXP\", quests.quest_1.reward-amount: 100\n" +
                            " # --------------------------------------------------------------------------------");
        }

        List<String> defaultJobNames = Arrays.asList("Miner", "Holzfaeller", "Jaeger", "Graeber", "Farmer");

        for (String jobName : defaultJobNames) {
            String configPath = "jobs." + jobName;
            createDefaultJobConfig(config, configPath, jobName);
        }

        plugin.saveConfig();
    }

    private void createDefaultJobConfig(FileConfiguration config, String basePath, String jobName) {
        config.addDefault(basePath + ".level-xp-max", 40);
        config.addDefault(basePath + ".level-xp-coefficient-a", 1.0);
        config.addDefault(basePath + ".level-xp-constant-b", 10.0);
        config.addDefault(basePath + ".level-money-increase", 0.05);

        if (jobName.equalsIgnoreCase("Miner")) {
            config.addDefault(basePath + ".menu-icon-material", "DIAMOND_PICKAXE");
        } else if (jobName.equalsIgnoreCase("Holzfaeller")) {
            config.addDefault(basePath + ".menu-icon-material", "DIAMOND_AXE");
        } else if (jobName.equalsIgnoreCase("Jaeger")) {
            config.addDefault(basePath + ".menu-icon-material", "DIAMOND_SWORD");
        } else if (jobName.equalsIgnoreCase("Graeber")) {
            config.addDefault(basePath + ".menu-icon-material", "DIAMOND_SHOVEL");
        } else if (jobName.equalsIgnoreCase("Farmer")) {
            config.addDefault(basePath + ".menu-icon-material", "DIAMOND_HOE");
        }

        config.addDefault(basePath + ".disabled-world", List.of("world"));

        if (jobName.equalsIgnoreCase("Miner")) {
            config.addDefault(basePath + ".display-name", "&6Miner");
            config.addDefault(basePath + ".description", Arrays.asList(
                    "&7Sei ein fähiger Miner!",
                    "&7Baue Erze und Blöcke ab, um Geld und XP zu verdienen.",
                    "",
                    "&8>> Verdiene Geld mit dem Abbau von Gesteinen"
            ));
            config.addDefault(basePath + ".level-xp-max", 40);
            config.addDefault(basePath + ".level-xp-coefficient-a", 2000.0);
            config.addDefault(basePath + ".level-xp-constant-b", 500.0);
            config.addDefault(basePath + ".level-money-increase", 0.5);
            config.addDefault(basePath + ".disabled-world", List.of("world_nether", "world_the_end"));
        } else if (jobName.equalsIgnoreCase("Holzfaeller")) {
            config.addDefault(basePath + ".display-name", "&2Holzfäller");
            config.addDefault(basePath + ".description", Arrays.asList(
                    "&7Werde ein geschickter Holzfäller!",
                    "&7Fälle Bäume und ernte Holz für Profit und Erfahrung.",
                    "",
                    "&8>> Verdiene Geld mit dem Fällen von Bäumen"
            ));
            config.addDefault(basePath + ".level-xp-max", 40);
            config.addDefault(basePath + ".level-xp-coefficient-a", 1800.0);
            config.addDefault(basePath + ".level-xp-constant-b", 350.0);
            config.addDefault(basePath + ".level-money-increase", 0.8);
            config.addDefault(basePath + ".disabled-world", List.of("world_the_end"));
        } else if (jobName.equalsIgnoreCase("Jaeger")) {
            config.addDefault(basePath + ".display-name", "&4Jäger");
            config.addDefault(basePath + ".description", Arrays.asList(
                    "&7Jage und besiege gefährliche Kreaturen!",
                    "&7Erhalte Belohnungen für jeden erfolgreichen Abschuss.",
                    "",
                    "&8>> Verdiene Geld mit dem Töten von Mobs"
            ));
            config.addDefault(basePath + ".level-xp-max", 40);
            config.addDefault(basePath + ".level-xp-coefficient-a", 1000.0);
            config.addDefault(basePath + ".level-xp-constant-b", 300.0);
            config.addDefault(basePath + ".level-money-increase", 0.5);
            config.addDefault(basePath + ".disabled-world", List.of("world_nether"));
        } else if (jobName.equalsIgnoreCase("Graeber")) {
            config.addDefault(basePath + ".display-name", "&8Gräber");
            config.addDefault(basePath + ".description", Arrays.asList(
                    "&7Grabe dich durch die Erde!",
                    "&7Sammle Rohstoffe wie Erde, Sand und Kies.",
                    "",
                    "&8>> Verdiene Geld mit Terraformen"
            ));
            config.addDefault(basePath + ".level-xp-max", 40);
            config.addDefault(basePath + ".level-xp-coefficient-a", 2000.0);
            config.addDefault(basePath + ".level-xp-constant-b", 500.0);
            config.addDefault(basePath + ".level-money-increase", 0.3);
            config.addDefault(basePath + ".disabled-world", List.of("world_the_end"));
        } else if (jobName.equalsIgnoreCase("Farmer")) {
            config.addDefault(basePath + ".display-name", "&aFarmer");
            config.addDefault(basePath + ".description", Arrays.asList(
                    "&7Bestelle deine Felder und ernte!",
                    "&7Baue Pflanzen an und ernte sie für Geld und Erfahrung.",
                    "",
                    "&8>> Verdiene Geld mit Pflanzen und Früchten"
            ));
            config.addDefault(basePath + ".level-xp-max", 40);
            config.addDefault(basePath + ".level-xp-coefficient-a", 1600.0);
            config.addDefault(basePath + ".level-xp-constant-b", 250.0);
            config.addDefault(basePath + ".level-money-increase", 0.8);
            config.addDefault(basePath + ".disabled-world", List.of("world_nether", "world_the_end"));
        }

        Map<JobAction, Map<String, JobReward>> defaultRewards = getDefaultRewardsForJob(jobName);
        for (Map.Entry<JobAction, Map<String, JobReward>> actionEntry : defaultRewards.entrySet()) {
            JobAction action = actionEntry.getKey();
            Map<String, JobReward> rewardsMap = actionEntry.getValue();

            for (Map.Entry<String, JobReward> rewardEntry : rewardsMap.entrySet()) {
                String itemKey = rewardEntry.getKey();
                JobReward reward = rewardEntry.getValue();
                config.addDefault(basePath + ".rewards." + action.name() + "." + itemKey + ".income", reward.getIncome());
                config.addDefault(basePath + ".rewards." + action.name() + "." + itemKey + ".experience", reward.getExperience());
            }
        }

        addDefaultQuestsForJob(config, basePath, jobName);
    }

    private Map<JobAction, Map<String, JobReward>> getDefaultRewardsForJob(String jobName) {
        Map<JobAction, Map<String, JobReward>> rewards = new HashMap<>();
        Map<String, JobReward> breakRewards = new HashMap<>();
        Map<String, JobReward> killRewards = new HashMap<>();

        if (jobName.equalsIgnoreCase("Miner")) {
            breakRewards.put("ANDESITE", new JobReward(1.0, 1.0));
            breakRewards.put("DEEPSLATE", new JobReward(1.0, 1.0));
            breakRewards.put("GRANITE", new JobReward(1.0, 1.0));
            breakRewards.put("DIORITE", new JobReward(1.0, 1.0));
            breakRewards.put("SANDSTONE", new JobReward(1.0, 1.0));
            breakRewards.put("CHISELED_SANDSTONE", new JobReward(1.0, 1.0));
            breakRewards.put("CUT_SANDSTONE", new JobReward(1.0, 1.0));
            breakRewards.put("COAL_ORE", new JobReward(5.0, 1.0));
            breakRewards.put("DEEPSLATE_COAL_ORE", new JobReward(10.0, 1.0));
            breakRewards.put("REDSTONE_ORE", new JobReward(10.0, 1.0));
            breakRewards.put("DEEPSLATE_REDSTONE_ORE", new JobReward(20.0, 1.0));
            breakRewards.put("COPPER_ORE", new JobReward(5.0, 1.0));
            breakRewards.put("DEEPSLATE_COPPER_ORE", new JobReward(10.0, 1.0));
            breakRewards.put("IRON_ORE", new JobReward(8.0, 1.0));
            breakRewards.put("DEEPSLATE_IRON_ORE", new JobReward(16.0, 1.0));
            breakRewards.put("GOLD_ORE", new JobReward(15.0, 1.0));
            breakRewards.put("DEEPSLATE_GOLD_ORE", new JobReward(30.0, 1.0));
            breakRewards.put("LAPIS_ORE", new JobReward(15.0, 1.0));
            breakRewards.put("DEEPSLATE_LAPIS_ORE", new JobReward(30.0, 1.0));
            breakRewards.put("DIAMOND_ORE", new JobReward(30.0, 1.0));
            breakRewards.put("DEEPSLATE_DIAMOND_ORE", new JobReward(60.0, 1.0));
            breakRewards.put("EMERALD_ORE", new JobReward(50.0, 1.0));
            breakRewards.put("DEEPSLATE_EMERALD_ORE", new JobReward(150.0, 1.0));
            breakRewards.put("NETHER_QUARTZ_ORE", new JobReward(5.0, 1.0));
            breakRewards.put("OBSIDIAN", new JobReward(30.0, 1.0));
            breakRewards.put("TUFF", new JobReward(1.0, 1.0));
            breakRewards.put("SPAWNER", new JobReward(1000.0, 1.0));
            rewards.put(JobAction.BREAK, breakRewards);
        } else if (jobName.equalsIgnoreCase("Holzfaeller")) {
            breakRewards.put("OAK_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("SPRUCE_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("BIRCH_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("JUNGLE_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("ACACIA_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("DARK_OAK_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_OAK_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_SPRUCE_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_BIRCH_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_JUNGLE_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_ACACIA_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_DARK_OAK_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_PALE_OAK_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("CHERRY_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_CHERRY_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("CRIMSON_STEM", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_CRIMSON_STEM", new JobReward(7.0, 1.0));
            breakRewards.put("WARPED_STEM", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_WARPED_STEM", new JobReward(7.0, 1.0));
            breakRewards.put("MANGROVE_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("STRIPPED_MANGROVE_LOG", new JobReward(7.0, 1.0));
            breakRewards.put("PALE_OAK_LOG", new JobReward(7.0, 1.0));
            rewards.put(JobAction.BREAK, breakRewards);
        } else if (jobName.equalsIgnoreCase("Jaeger")) {
            killRewards.put("PLAYER", new JobReward(50.0, 10.0));
            killRewards.put("CREEPER", new JobReward(5.0, 3.0));
            killRewards.put("ZOMBIE", new JobReward(5.0, 3.0));
            killRewards.put("SKELETON", new JobReward(5.0, 3.0));
            killRewards.put("SPIDER", new JobReward(5.0, 3.0));
            killRewards.put("WITCH", new JobReward(5.0, 3.0));
            killRewards.put("ZOMBIFIED_PIGLIN", new JobReward(5.0, 3.0));
            killRewards.put("WITHER_SKELETON", new JobReward(5.0, 3.0));
            killRewards.put("WARDEN", new JobReward(200.0, 20.0));
            killRewards.put("VINDICATOR", new JobReward(8.0, 4.0));
            killRewards.put("VEX", new JobReward(5.0, 3.0));
            killRewards.put("STRAY", new JobReward(5.0, 3.0));
            killRewards.put("SLIME", new JobReward(5.0, 3.0));
            killRewards.put("SILVERFISH", new JobReward(5.0, 3.0));
            killRewards.put("SHULKER", new JobReward(8.0, 5.0));
            killRewards.put("RAVAGER", new JobReward(50.0, 10.0));
            killRewards.put("PILLAGER", new JobReward(8.0, 5.0));
            killRewards.put("PIGLIN_BRUTE", new JobReward(8.0, 5.0));
            killRewards.put("PIGLIN", new JobReward(8.0, 5.0));
            killRewards.put("HOGLIN", new JobReward(8.0, 5.0));
            killRewards.put("BLAZE", new JobReward(8.0, 5.0));
            killRewards.put("GHAST", new JobReward(10.0, 6.0));
            killRewards.put("MAGMA_CUBE", new JobReward(5.0, 3.0));
            killRewards.put("IRON_GOLEM", new JobReward(20.0, 8.0));
            killRewards.put("GUARDIAN", new JobReward(10.0, 6.0));
            killRewards.put("EVOKER", new JobReward(30.0, 15.0));
            killRewards.put("ENDERMAN", new JobReward(4.0, 3.0));
            killRewards.put("ENDERMITE", new JobReward(5.0, 3.0));
            killRewards.put("CAVE_SPIDER", new JobReward(6.0, 4.0));
            killRewards.put("BREEZE", new JobReward(30.0, 15.0));
            killRewards.put("BOGGED", new JobReward(30.0, 15.0));
            rewards.put(JobAction.KILL, killRewards);
        } else if (jobName.equalsIgnoreCase("Graeber")) {
            breakRewards.put("RED_SAND", new JobReward(1.0, 1.0));
            breakRewards.put("COARSE_DIRT", new JobReward(1.0, 1.0));
            breakRewards.put("DIRT", new JobReward(0.5, 1.0));
            breakRewards.put("GRASS_BLOCK", new JobReward(3.0, 1.0));
            breakRewards.put("GRAVEL", new JobReward(1.0, 1.0));
            breakRewards.put("SAND", new JobReward(1.0, 1.0));
            breakRewards.put("CLAY", new JobReward(1.0, 1.0));
            breakRewards.put("MUD", new JobReward(1.0, 1.0));
            breakRewards.put("MOSS_BLOCK", new JobReward(1.0, 1.0));
            breakRewards.put("SNOW_BLOCK", new JobReward(0.5, 1.0));
            rewards.put(JobAction.BREAK, breakRewards);
        } else if (jobName.equalsIgnoreCase("Farmer")) {
            breakRewards.put("CHORUS_PLANT", new JobReward(3.0, 1.0));
            breakRewards.put("CHORUS_FLOWER", new JobReward(3.0, 1.0));
            breakRewards.put("BEETROOTS", new JobReward(3.0, 1.0));
            breakRewards.put("WHEAT", new JobReward(3.0, 1.0));
            breakRewards.put("CARROTS", new JobReward(1.0, 1.0));
            breakRewards.put("POTATOES", new JobReward(1.0, 1.0));
            breakRewards.put("PUMPKIN", new JobReward(1.0, 1.0));
            breakRewards.put("MELON", new JobReward(1.0, 1.0));
            breakRewards.put("SUGAR_CANE", new JobReward(2.0, 1.0));
            breakRewards.put("COCOA_BEANS", new JobReward(4.0, 1.0));
            breakRewards.put("LILY_PAD", new JobReward(2.0, 1.0));
            breakRewards.put("DANDELION", new JobReward(2.0, 1.0));
            breakRewards.put("POPPY", new JobReward(2.0, 1.0));
            breakRewards.put("BLUE_ORCHID", new JobReward(2.0, 1.0));
            breakRewards.put("ALLIUM", new JobReward(2.0, 1.0));
            breakRewards.put("AZURE_BLUET", new JobReward(2.0, 1.0));
            breakRewards.put("RED_TULIP", new JobReward(2.0, 1.0));
            breakRewards.put("ORANGE_TULIP", new JobReward(2.0, 1.0));
            breakRewards.put("WHITE_TULIP", new JobReward(2.0, 1.0));
            breakRewards.put("PINK_TULIP", new JobReward(2.0, 1.0));
            breakRewards.put("OXEYE_DAISY", new JobReward(2.0, 1.0));
            breakRewards.put("BROWN_MUSHROOM", new JobReward(1.0, 1.0));
            breakRewards.put("RED_MUSHROOM", new JobReward(1.0, 1.0));
            breakRewards.put("VINE", new JobReward(1.0, 1.0));
            breakRewards.put("CACTUS", new JobReward(1.0, 1.0));
            breakRewards.put("NETHER_WART", new JobReward(1.0, 1.0));
            breakRewards.put("BAMBOO", new JobReward(0.5, 1.0));
            breakRewards.put("HAY_BLOCK", new JobReward(1.0, 1.0));
            rewards.put(JobAction.BREAK, breakRewards);
        }

        return rewards;
    }

    public Job loadJobFromConfig(String jobName) {
        FileConfiguration config = plugin.getConfig();
        String basePath = "jobs." + jobName;

        if (!config.contains(basePath)) {
            plugin.getLogger().warning("Job-Konfiguration nicht gefunden in config.yml (möglicherweise manuell entfernt oder falsches Casing): " + jobName + ". Es werden keine Jobdaten geladen.");
            return null;
        }

        String displayName = ChatUtil.colorize(config.getString(basePath + ".display-name", "&r" + jobName));
        String menuIconMaterial = config.getString(basePath + ".menu-icon-material", "BARRIER").toUpperCase();
        List<String> description = ChatUtil.colorize(config.getStringList(basePath + ".description"));

        int levelXpMax = config.getInt(basePath + ".level-xp-max");
        double levelXpCoefficientA = config.getDouble(basePath + ".level-xp-coefficient-a");
        double levelXpConstantB = config.getDouble(basePath + ".level-xp-constant-b");
        double levelMoneyIncrease = config.getDouble(basePath + ".level-money-increase");
        List<String> disabledWorlds = config.getStringList(basePath + ".disabled-world");

        Map<JobAction, Map<String, JobReward>> rewards = new HashMap<>();
        ConfigurationSection rewardsSection = config.getConfigurationSection(basePath + ".rewards");

        if (rewardsSection != null) {
            for (String actionKey : rewardsSection.getKeys(false)) {
                try {
                    JobAction action = JobAction.valueOf(actionKey.toUpperCase());
                    Map<String, JobReward> actionRewards = new HashMap<>();
                    ConfigurationSection actionSection = rewardsSection.getConfigurationSection(actionKey);
                    if (actionSection != null) {
                        for (String itemKey : actionSection.getKeys(false)) {
                            if (actionSection.contains(itemKey + ".income") && actionSection.contains(itemKey + ".experience")) {
                                double income = actionSection.getDouble(itemKey + ".income");
                                double experience = actionSection.getDouble(itemKey + ".experience");
                                actionRewards.put(itemKey.toUpperCase(), new JobReward(income, experience));
                            } else {
                                plugin.getLogger().warning("Unvollständige Belohnungsdefinition für Job " + jobName + ", Aktion " + actionKey + ", Item " + itemKey + ". Überspringe.");
                            }
                        }
                    }
                    rewards.put(action, actionRewards);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Unbekannte Job-Aktion in config.yml für Job " + jobName + ": " + actionKey + ". Überspringe diese Aktion. Fehler: " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("Keine Belohnungen für Job " + jobName + " in config.yml gefunden. Der Job wird ohne Belohnungen geladen.");
        }

        return new Job(jobName.toLowerCase(), displayName, menuIconMaterial, description,
                levelXpMax, levelXpCoefficientA, levelXpConstantB, levelMoneyIncrease, disabledWorlds, rewards);
    }

    public Set<String> getJobNamesFromConfig() {
        ConfigurationSection jobsSection = plugin.getConfig().getConfigurationSection("jobs");
        if (jobsSection == null) {
            plugin.getLogger().info("jobs section not found in config.yml. Returning empty job names set.");
            return new HashMap<String, Job>().keySet();
        }
        Set<String> actualJobNames = new java.util.HashSet<>();
        for (String key : jobsSection.getKeys(false)) {
            if (!key.startsWith("#")) {
                actualJobNames.add(key);
            }
        }
        plugin.getLogger().info("Found " + actualJobNames.size() + " actual job names: " + actualJobNames.toString());
        return actualJobNames;
    }

    private void addDefaultQuestsForJob(FileConfiguration config, String basePath, String jobName) {
        if (jobName.equalsIgnoreCase("Miner")) {
            config.addDefault(basePath + ".quests.quest_1.slot", 1);
            config.addDefault(basePath + ".quests.quest_1.lore", "Sammle 50 Kohle");
            config.addDefault(basePath + ".quests.quest_1.display-item", "COAL_ORE");
            config.addDefault(basePath + ".quests.quest_1.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_1.specific-task", "COAL_ORE");
            config.addDefault(basePath + ".quests.quest_1.amount", 50);
            config.addDefault(basePath + ".quests.quest_1.reward", "JOBXP");
            config.addDefault(basePath + ".quests.quest_1.reward-amount", 100);

            config.addDefault(basePath + ".quests.quest_2.slot", 2);
            config.addDefault(basePath + ".quests.quest_2.lore", "Sammle 25 Eisenerz");
            config.addDefault(basePath + ".quests.quest_2.display-item", "IRON_ORE");
            config.addDefault(basePath + ".quests.quest_2.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_2.specific-task", "IRON_ORE");
            config.addDefault(basePath + ".quests.quest_2.amount", 25);
            config.addDefault(basePath + ".quests.quest_2.reward", "MONEY");
            config.addDefault(basePath + ".quests.quest_2.reward-amount", 500);
        } else if (jobName.equalsIgnoreCase("Holzfaeller")) {
            config.addDefault(basePath + ".quests.quest_1.slot", 1);
            config.addDefault(basePath + ".quests.quest_1.lore", "Fälle 100 Eichenstämme");
            config.addDefault(basePath + ".quests.quest_1.display-item", "OAK_LOG");
            config.addDefault(basePath + ".quests.quest_1.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_1.specific-task", "OAK_LOG");
            config.addDefault(basePath + ".quests.quest_1.amount", 100);
            config.addDefault(basePath + ".quests.quest_1.reward", "JOBXP");
            config.addDefault(basePath + ".quests.quest_1.reward-amount", 150);

            config.addDefault(basePath + ".quests.quest_2.slot", 2);
            config.addDefault(basePath + ".quests.quest_2.lore", "Fälle 50 Fichtenstämme");
            config.addDefault(basePath + ".quests.quest_2.display-item", "SPRUCE_LOG");
            config.addDefault(basePath + ".quests.quest_2.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_2.specific-task", "SPRUCE_LOG");
            config.addDefault(basePath + ".quests.quest_2.amount", 50);
            config.addDefault(basePath + ".quests.quest_2.reward", "MONEY");
            config.addDefault(basePath + ".quests.quest_2.reward-amount", 300);
        } else if (jobName.equalsIgnoreCase("Jaeger")) {
            config.addDefault(basePath + ".quests.quest_1.slot", 1);
            config.addDefault(basePath + ".quests.quest_1.lore", "Töte 20 Zombies");
            config.addDefault(basePath + ".quests.quest_1.display-item", "ZOMBIE_HEAD");
            config.addDefault(basePath + ".quests.quest_1.task", "KILL");
            config.addDefault(basePath + ".quests.quest_1.specific-task", "ZOMBIE");
            config.addDefault(basePath + ".quests.quest_1.amount", 20);
            config.addDefault(basePath + ".quests.quest_1.reward", "JOBXP");
            config.addDefault(basePath + ".quests.quest_1.reward-amount", 200);

            config.addDefault(basePath + ".quests.quest_2.slot", 2);
            config.addDefault(basePath + ".quests.quest_2.lore", "Töte 15 Skelette");
            config.addDefault(basePath + ".quests.quest_2.display-item", "SKELETON_HEAD");
            config.addDefault(basePath + ".quests.quest_2.task", "KILL");
            config.addDefault(basePath + ".quests.quest_2.specific-task", "SKELETON");
            config.addDefault(basePath + ".quests.quest_2.amount", 15);
            config.addDefault(basePath + ".quests.quest_2.reward", "MONEY");
            config.addDefault(basePath + ".quests.quest_2.reward-amount", 400);
        } else if (jobName.equalsIgnoreCase("Graeber")) {
            config.addDefault(basePath + ".quests.quest_1.slot", 1);
            config.addDefault(basePath + ".quests.quest_1.lore", "Grabe 200 Erde");
            config.addDefault(basePath + ".quests.quest_1.display-item", "DIRT");
            config.addDefault(basePath + ".quests.quest_1.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_1.specific-task", "DIRT");
            config.addDefault(basePath + ".quests.quest_1.amount", 200);
            config.addDefault(basePath + ".quests.quest_1.reward", "JOBXP");
            config.addDefault(basePath + ".quests.quest_1.reward-amount", 100);

            config.addDefault(basePath + ".quests.quest_2.slot", 2);
            config.addDefault(basePath + ".quests.quest_2.lore", "Grabe 100 Sand");
            config.addDefault(basePath + ".quests.quest_2.display-item", "SAND");
            config.addDefault(basePath + ".quests.quest_2.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_2.specific-task", "SAND");
            config.addDefault(basePath + ".quests.quest_2.amount", 100);
            config.addDefault(basePath + ".quests.quest_2.reward", "MONEY");
            config.addDefault(basePath + ".quests.quest_2.reward-amount", 250);
        } else if (jobName.equalsIgnoreCase("Farmer")) {
            config.addDefault(basePath + ".quests.quest_1.slot", 1);
            config.addDefault(basePath + ".quests.quest_1.lore", "Ernte 50 Weizen");
            config.addDefault(basePath + ".quests.quest_1.display-item", "WHEAT");
            config.addDefault(basePath + ".quests.quest_1.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_1.specific-task", "WHEAT");
            config.addDefault(basePath + ".quests.quest_1.amount", 50);
            config.addDefault(basePath + ".quests.quest_1.reward", "JOBXP");
            config.addDefault(basePath + ".quests.quest_1.reward-amount", 120);

            config.addDefault(basePath + ".quests.quest_2.slot", 2);
            config.addDefault(basePath + ".quests.quest_2.lore", "Ernte 30 Karotten");
            config.addDefault(basePath + ".quests.quest_2.display-item", "CARROT");
            config.addDefault(basePath + ".quests.quest_2.task", "BREAK");
            config.addDefault(basePath + ".quests.quest_2.specific-task", "CARROT");
            config.addDefault(basePath + ".quests.quest_2.amount", 30);
            config.addDefault(basePath + ".quests.quest_2.reward", "MONEY");
            config.addDefault(basePath + ".quests.quest_2.reward-amount", 350);
        }
    }
}