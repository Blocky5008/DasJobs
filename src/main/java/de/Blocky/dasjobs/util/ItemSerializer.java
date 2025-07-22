package de.Blocky.dasjobs.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;

public class ItemSerializer {

    public static String serialize(ItemStack item) {
        if (item == null) {
            return "";
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize ItemStack: " + e.getMessage(), e);
            return "";
        }
    }

    public static Optional<ItemStack> deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            return Optional.ofNullable((ItemStack) dataInput.readObject());

        } catch (ClassNotFoundException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize ItemStack: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
}
