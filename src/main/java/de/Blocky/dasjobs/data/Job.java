package de.Blocky.dasjobs.data;

import java.util.List;
import java.util.Map;

public class Job {
    private final String name;
    private final String displayName;
    private final String iconMaterial;
    private final List<String> description;
    private final int levelXpMax;
    private final double levelXpCoefficientA;
    private final double levelXpConstantB;
    private final double levelMoneyIncrease;
    private final List<String> disabledWorlds;
    private final Map<JobAction, Map<String, JobReward>> rewards;

    public Job(String name, String displayName, String iconMaterial, List<String> description,
               int levelXpMax, double levelXpCoefficientA, double levelXpConstantB,
               double levelMoneyIncrease, List<String> disabledWorlds, Map<JobAction, Map<String, JobReward>> rewards) {
        this.name = name;
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
        this.description = description;
        this.levelXpMax = levelXpMax;
        this.levelXpCoefficientA = levelXpCoefficientA;
        this.levelXpConstantB = levelXpConstantB;
        this.levelMoneyIncrease = levelMoneyIncrease;
        this.disabledWorlds = disabledWorlds;
        this.rewards = rewards;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public List<String> getDescription() {
        return description;
    }

    public int getLevelXpMax() {
        return levelXpMax;
    }

    public double getLevelXpCoefficientA() {
        return levelXpCoefficientA;
    }

    public double getLevelXpConstantB() {
        return levelXpConstantB;
    }

    public double getLevelMoneyIncrease() {
        return levelMoneyIncrease;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public Map<JobAction, Map<String, JobReward>> getRewards() {
        return rewards;
    }

    public JobReward getReward(JobAction action, String materialOrEntity) {
        if (rewards.containsKey(action)) {
            return rewards.get(action).get(materialOrEntity.toUpperCase());
        }
        return null;
    }
    public double getRequiredXpForLevel(int currentLevel) {
        int levelForFormula = Math.max(0, currentLevel);

        if (levelForFormula >= levelXpMax) {
            levelForFormula = levelXpMax - 1;
            if (levelForFormula < 0) levelForFormula = 0;
        }

        return levelXpCoefficientA * Math.pow(levelForFormula, 2) + levelXpConstantB;
    }

    public double getMoneyBonusForLevel(int level) {
        if (level <= 0) return 0.0;
        return levelMoneyIncrease * (level - 1);
    }
}