package de.Blocky.dasjobs.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack.clone(); // Clone to avoid modifying the original
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        meta.setDisplayName(ChatUtil.colorize(name));
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatUtil.colorize(line));
        }
        meta.setLore(coloredLore);
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        List<String> currentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        for (String line : lore) {
            currentLore.add(ChatUtil.colorize(line));
        }
        meta.setLore(currentLore);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        if (item.containsEnchantment(enchantment)) {
            item.removeEnchantment(enchantment);
        }
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder setDurability(short durability) {
        item.setDurability(durability);
        return this;
    }

    public ItemBuilder setSkullOwner(String ownerName) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(ownerName);
        }
        return this;
    }

    public ItemBuilder setLeatherArmorColor(Color color) {
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        return this;
    }

    public ItemBuilder setPotionType(PotionType potionType) {
        if (meta instanceof PotionMeta) {
            ((PotionMeta) meta).setBasePotionType(potionType);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}