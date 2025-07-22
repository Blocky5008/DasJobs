package de.Blocky.dasjobs.data;

public class JobReward {
    private final double income;
    private final double experience;

    public JobReward(double income, double experience) {
        this.income = income;
        this.experience = experience;
    }

    public double getIncome() {
        return income;
    }

    public double getExperience() {
        return experience;
    }
}