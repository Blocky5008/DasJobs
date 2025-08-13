package de.Blocky.dasjobs.listener;

import de.Blocky.dasjobs.DasJobs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final DasJobs plugin;

    public PlayerJoinQuitListener(DasJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Hier ist keine Änderung erforderlich.
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Korrektur: Die Daten werden gespeichert, aber nicht sofort wieder entfernt.
        // Die removePlayerData-Methode sollte nur verwendet werden, wenn die Daten wirklich gelöscht werden sollen.
        plugin.getPlayerDataManager().savePlayerData(event.getPlayer().getUniqueId());

        // Die folgende Zeile ist der Fehler und wurde entfernt:
        // plugin.getPlayerDataManager().removePlayerData(event.getPlayer().getUniqueId());
    }
}