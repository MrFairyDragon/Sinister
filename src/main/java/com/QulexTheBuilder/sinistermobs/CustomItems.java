package com.QulexTheBuilder.sinistermobs;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public abstract class CustomItems {

    public static void updateItemList() {
        Main.getPlugin().mobs.customItems.clear();
        for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("items"))) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            Main.getPlugin().mobs.customItems.put(file.getName().toLowerCase().substring(0, file.getName().length()-4), createItem(yml));
        }
    }

    public static ItemStack createItem(YamlConfiguration yml) {
        ItemStack item = new ItemStack(Material.valueOf(Objects.requireNonNull(yml.getString("Type")).toUpperCase()), 1);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setUnbreakable(yml.getBoolean("Unbreakable"));
        meta.setDisplayName(yml.getString("Name"));
        meta.setCustomModelData(yml.getInt("Model"));
        meta.setLore(yml.getStringList("Lore"));
        for(String str : yml.getStringList("Attributes")) {
            String[] stringArray = str.split(",");
            meta.addAttributeModifier(Attribute.valueOf(stringArray[0].toUpperCase()), new AttributeModifier(UUID.randomUUID(), stringArray[0].toLowerCase(), Integer.parseInt(stringArray[2]), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(stringArray[1])));
        }
        for(String str : yml.getStringList("Enchantments")) {
            String[] strings = str.split(",");
            NamespacedKey key = NamespacedKey.minecraft(strings[0].trim().toLowerCase());
            meta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(key)), Integer.parseInt(strings[1].trim()), true);
        }
        for(String str : yml.getStringList("persistentData")) {
            String[] strings = str.trim().split(",");
            meta.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), strings[0].trim()), PersistentDataType.STRING, strings[1].trim());
        }
        if(meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(Integer.parseInt(Objects.requireNonNull(yml.getString(
                    "Color")), 16)));
        }
        item.setItemMeta(meta);
        return item;
    }

}
