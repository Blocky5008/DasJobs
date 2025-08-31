package de.Blocky.dasjobs.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestProgress {
    private final UUID playerUUID;
    private final String questId;
    private final Map<String, Integer> progress;
    private boolean completed;
    private boolean claimed;

    public QuestProgress(UUID playerUUID, String questId) {
        this.playerUUID = playerUUID;
        this.questId = questId;
        this.progress = new HashMap<>();
        this.completed = false;
        this.claimed = false;
    }

    public QuestProgress(UUID playerUUID, String questId, Map<String, Integer> progress, boolean completed, boolean claimed) {
        this.playerUUID = playerUUID;
        this.questId = questId;
        this.progress = new HashMap<>(progress);
        this.completed = completed;
        this.claimed = claimed;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getQuestId() {
        return questId;
    }

    public Map<String, Integer> getProgress() {
        return new HashMap<>(progress);
    }

    public int getProgress(String task) {
        return progress.getOrDefault(task, 0);
    }

    public void setProgress(String task, int amount) {
        progress.put(task, amount);
    }

    public void addProgress(String task, int amount) {
        progress.put(task, getProgress(task) + amount);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }
}

