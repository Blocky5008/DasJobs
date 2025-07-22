package de.Blocky.dasjobs.data;

public class Booster {

    public enum BoostType {
        XP, MONEY
    }

    private final String jobName;
    private final BoostType boostType;
    private final double multiplier;
    private final long expiryTime;
    private final String starterPlayerName;

    public Booster(String jobName, BoostType boostType, double multiplier, long durationMinutes, String starterPlayerName) {
        this.jobName = jobName;
        this.boostType = boostType;
        this.multiplier = multiplier;
        this.expiryTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        this.starterPlayerName = starterPlayerName;
    }

    public String getJobName() {
        return jobName;
    }

    public BoostType getBoostType() {
        return boostType;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public String getStarterPlayerName() {
        return starterPlayerName;
    }

    public long getRemainingMinutes() {
        long remainingMillis = expiryTime - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return 0;
        }
        return (long) Math.ceil((double) remainingMillis / (60 * 1000L));
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%.2f;%d;%s", jobName, boostType.name(), multiplier, expiryTime, starterPlayerName);
    }

    public static Booster fromString(String boosterString) {
        String[] parts = boosterString.split(";");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Ungültiger Booster-String-Format. Erwartet 5 Teile, aber hat " + parts.length + ": " + boosterString);
        }
        String jobName = parts[0];
        BoostType boostType = BoostType.valueOf(parts[1]);
        double multiplier = Double.parseDouble(parts[2]);
        long expiryTime = Long.parseLong(parts[3]);
        String starterPlayerName = parts[4];

        Booster booster = new Booster(jobName, boostType, multiplier, 0, starterPlayerName);
        try {
            java.lang.reflect.Field field = Booster.class.getDeclaredField("expiryTime");
            field.setAccessible(true);
            field.set(booster, expiryTime);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler beim Setzen des expiryTime Feldes im Booster.", e);
        }
        return booster;
    }
}