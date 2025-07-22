package de.Blocky.dasjobs.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager {
    private final Economy economy;

    public EconomyManager(Economy economy) {
        this.economy = economy;
    }

    public boolean depositPlayer(Player player, double amount) {
        if (economy != null) {
            return economy.depositPlayer(player, amount).transactionSuccess();
        }
        return false;
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (economy != null) {
            return economy.has(player, amount);
        }
        return false;
    }

    public double getBalance(OfflinePlayer player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }
}