package de.Blocky.dasjobs.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerJobData {
    private final UUID playerUUID;
    private final Map<String, Integer> jobLevels;
    private final Map<String, Double> jobExperiences;
    private final Map<String, List<String>> claimedRewards;
    private String activeJobName;

    public PlayerJobData(UUID playerUUID) {
        this(playerUUID, new HashMap<>(), new HashMap<>(), new HashMap<>(), null);
    }

    public PlayerJobData(UUID playerUUID, Map<String, Integer> jobLevels, Map<String, Double> jobExperiences,
                         Map<String, List<String>> claimedRewards) {
        this(playerUUID, jobLevels, jobExperiences, claimedRewards, null);
    }

    public PlayerJobData(UUID playerUUID, Map<String, Integer> jobLevels, Map<String, Double> jobExperiences,
                         Map<String, List<String>> claimedRewards, String activeJobName) {
        this.playerUUID = playerUUID;
        this.jobLevels = new HashMap<>(jobLevels);
        this.jobExperiences = new HashMap<>(jobExperiences);
        this.claimedRewards = new HashMap<>(claimedRewards);
        this.activeJobName = activeJobName;
    }
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Map<String, Integer> getJobLevels() {
        return Collections.unmodifiableMap(jobLevels);
    }

    public Map<String, Double> getJobExperiences() {
        return Collections.unmodifiableMap(jobExperiences);
    }

    public Map<String, List<String>> getClaimedRewards() {
        Map<String, List<String>> copy = new HashMap<>();
        claimedRewards.forEach((job, rewardsList) -> copy.put(job, Collections.unmodifiableList(rewardsList)));
        return Collections.unmodifiableMap(copy);
    }

    public int getLevel(String jobName) {
        return jobLevels.getOrDefault(jobName.toLowerCase(), 0);
    }

    public double getExperience(String jobName) {
        return jobExperiences.getOrDefault(jobName.toLowerCase(), 0.0);
    }

    public void setLevel(String jobName, int level) {
        jobLevels.put(jobName.toLowerCase(), level);
    }

    public void setExperience(String jobName, double experience) {
        jobExperiences.put(jobName.toLowerCase(), experience);
    }

    public void addExperience(String jobName, double amount) {
        jobExperiences.merge(jobName.toLowerCase(), amount, Double::sum);
    }

    public void addClaimedReward(String jobName, String rewardId) {
        claimedRewards.computeIfAbsent(jobName.toLowerCase(), k -> new java.util.ArrayList<>()).add(rewardId);
    }

    public boolean hasClaimedReward(String jobName, String rewardId) {
        List<String> rewards = claimedRewards.get(jobName.toLowerCase());
        return rewards != null && rewards.contains(rewardId);
    }

    public void clearClaimedRewardsForJob(String jobName) {
        claimedRewards.remove(jobName.toLowerCase());
    }

    public boolean hasActiveJob() {
        return this.activeJobName != null && !this.activeJobName.isEmpty();
    }

    public String getActiveJobName() {
        return activeJobName;
    }

    public void setActiveJob(String jobName) {
        this.activeJobName = jobName;
    }

    public boolean hasJob(String jobName) {
        return jobLevels.containsKey(jobName.toLowerCase()) || jobExperiences.containsKey(jobName.toLowerCase());
    }
}