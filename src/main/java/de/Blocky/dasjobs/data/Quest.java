package de.Blocky.dasjobs.data;

import java.util.UUID;

public class Quest {
    private final String id;
    private final String jobName;
    private final int slot;
    private final String lore;
    private final String displayItem;
    private final QuestTask task;
    private final String specificTask;
    private final int amount;
    private final QuestReward reward;
    private final int rewardAmount;
    private final String rewardItem;

    public Quest(String id, String jobName, int slot, String lore, String displayItem, 
                 QuestTask task, String specificTask, int amount, QuestReward reward, int rewardAmount, String rewardItem) {
        this.id = id;
        this.jobName = jobName;
        this.slot = slot;
        this.lore = lore;
        this.displayItem = displayItem;
        this.task = task;
        this.specificTask = specificTask;
        this.amount = amount;
        this.reward = reward;
        this.rewardAmount = rewardAmount;
        this.rewardItem = rewardItem;
    }

    public String getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public int getSlot() {
        return slot;
    }

    public String getLore() {
        return lore;
    }

    public String getDisplayItem() {
        return displayItem;
    }

    public QuestTask getTask() {
        return task;
    }

    public String getSpecificTask() {
        return specificTask;
    }

    public int getAmount() {
        return amount;
    }

    public QuestReward getReward() {
        return reward;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }

    public String getRewardItem() {
        return rewardItem;
    }

    public enum QuestTask {
        BREAK, KILL, PLACE
    }

    public enum QuestReward {
        JOBXP, MONEY, ITEM
    }
}
