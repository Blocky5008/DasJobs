package de.Blocky.dasjobs.util;

import org.bukkit.ChatColor;
import java.util.List;
import java.util.stream.Collectors;

public class ChatUtil {

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorize(List<String> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream()
                .map(ChatUtil::colorize)
                .collect(Collectors.toList());
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}