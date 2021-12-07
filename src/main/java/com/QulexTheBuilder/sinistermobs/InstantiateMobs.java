package com.QulexTheBuilder.sinistermobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

public class InstantiateMobs {

    public Map<String, ItemStack> customItems = new HashMap<>();
    public Map<UUID, BossBar> bossMobs = new HashMap<>(); //Data needs to be saved on server shutdown #Notyetimplemented
    public Map<UUID, List<BukkitTask>> taskIDs = new HashMap<>(); //Data needs to be saved on server shutdown #Notyetimplemented
    private MobEvents mobevent = new MobEvents();

    InstantiateMobs() {
        createDirectory("mobs");
        createDirectory("items");
    }

    private void createDirectory(String name) {
        File mobs = new File(Main.getPlugin().getDataFolder().getAbsolutePath()+File.separator+name);
        final boolean mkdir = mobs.mkdir();
        System.out.println("Folder was created is " + mkdir);
    }

    public static File[] filesInDirectory(String name) {
        File file = new File(Main.getPlugin().getDataFolder().getAbsolutePath()+File.separator+name);
        if(file.isDirectory()) {
            return file.listFiles();
        }
        System.out.println(ChatColor.RED+"This is not a directory");
        return null;
    }

    public void innit() {
        UpdateItemList();
    }

    public void UpdateItemList() {
        customItems.clear();
        for(File file : Objects.requireNonNull(InstantiateMobs.filesInDirectory("items"))) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            customItems.put(file.getName().toLowerCase().substring(0, file.getName().length()-4), CreateItem(yml));
        }
    }

    public ItemStack CreateItem(YamlConfiguration yml) {
        ItemStack item = new ItemStack(Material.valueOf(Objects.requireNonNull(yml.getString("Type")).toUpperCase()), 1);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setUnbreakable(yml.getBoolean("Unbreakable"));
        meta.setDisplayName(yml.getString("Name"));
        meta.setCustomModelData(yml.getInt("Model"));
        meta.setLore(yml.getStringList("Lore"));
        for(String str : yml.getStringList("Enchantments")) {
            String[] strings = str.split(",");
            NamespacedKey key = NamespacedKey.minecraft(strings[0].trim().toLowerCase());
            meta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(key)), Integer.parseInt(strings[1].trim()), true);
        }
        if(meta instanceof LeatherArmorMeta) {
            List<Integer> rgb = yml.getIntegerList("Color");
            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2)));
        }
        item.setItemMeta(meta);
        return item;
    }

    public Entity spawnMob(String name, Location location) {
        for(File file : Objects.requireNonNull(InstantiateMobs.filesInDirectory("mobs"))) {
            if(file.getName().equalsIgnoreCase(name+".yml")) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                org.bukkit.entity.Entity entity = Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.valueOf(Objects.requireNonNull(yml.getString("type")).toUpperCase()));
                changeEntity(entity, file, yml);
                return entity;
            }
        }
        return null;
    }

    public void changeMob(String name, Entity entity) {
        for(File file : Objects.requireNonNull(InstantiateMobs.filesInDirectory("mobs"))) {
            if(file.getName().equalsIgnoreCase(name+".yml")) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                changeEntity(entity, file, yml);
            }
        }
    }

    private void changeEntity(Entity entity, File file, YamlConfiguration yml) {
        entity.setCustomName(yml.getString("name"));
        entity.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), "customMonster"), PersistentDataType.STRING, file.getName());
        if(entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            if(yml.isString("bossbar.title") && yml.isString("bossbar.color") && yml.isString("bossbar.style")) {
                BossBar bossbar = Bukkit.createBossBar(yml.getString("bossbar.title"), BarColor.valueOf(yml.getString("bossbar.color").toUpperCase()), BarStyle.valueOf(yml.getString("bossbar.style").toUpperCase()));
                bossbar.setVisible(true);
                bossMobs.put(entity.getUniqueId(), bossbar);
            }
            for(String str : yml.getStringList("equipment")) {
                String[] strings = str.split(",");
                ItemStack item;
                if(Main.getPlugin().mobs.customItems.containsKey(strings[0].trim().toLowerCase())) {
                    item = Main.getPlugin().mobs.customItems.get(strings[0].trim().toLowerCase());
                } else {
                    item = new ItemStack(Material.valueOf(strings[0].trim().toUpperCase()), 1);
                }
                Objects.requireNonNull(livingEntity.getEquipment()).setItem(EquipmentSlot.valueOf(strings[1].trim().toUpperCase()), item);
            }
            for(String str : yml.getStringList("persistentData")) {
                String[] strings = str.trim().split(",");
                entity.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), strings[0].trim()), PersistentDataType.STRING, strings[1].trim());
            }
            if(entity instanceof Ageable) {
                Ageable ageableEntity = (Ageable) entity;
                boolean baby = yml.getBoolean("isBaby");
                if(baby) {
                    ageableEntity.setBaby();
                } else {
                    ageableEntity.setAdult();
                }
            }
            AttributeInstance health = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            AttributeInstance damage = livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            AttributeInstance armor = livingEntity.getAttribute(Attribute.GENERIC_ARMOR);
            AttributeInstance speed = livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            AttributeInstance rangeAgro = livingEntity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
            if(entity instanceof Zombie) {
                AttributeInstance zombieReinforcements = livingEntity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);
                zombieReinforcements.setBaseValue(yml.getDouble("zombieReinforcements"));
            }
            Objects.requireNonNull(health).setBaseValue(yml.getDouble("health"));
            livingEntity.setHealth(yml.getDouble("health"));
            if(entity instanceof Monster) {
                Objects.requireNonNull(damage).setBaseValue(yml.getDouble("damage"));
            }
            Objects.requireNonNull(armor).setBaseValue(yml.getDouble("defense"));
            Objects.requireNonNull(speed).setBaseValue(yml.getDouble("speed"));
            Objects.requireNonNull(rangeAgro).setBaseValue(yml.getDouble("agroRange"));
            mobevent.checkCondition(yml, entity, "onSpawn");
            activateTriggerAbilities(entity, yml);
        }
    }

    public void activateTriggerAbilities(Entity entity, YamlConfiguration yml) {
        List<BukkitTask> taskList = new ArrayList<>();
        for(String str : yml.getStringList("abilities")) {
            Pair<String[], String[]> args = mobevent.separateStrings(str, 5);
            if(args.first[2].equalsIgnoreCase("onTrigger")) {
                Map<String, String> parameters = mobevent.calcArguments(args.second[2]);
                Long counter = 100L;
                Long delay = 0L;
                if(parameters.containsKey("ticks")) {
                    counter = Long.parseLong(parameters.get("ticks"));
                }
                if(parameters.containsKey("delay")) {
                    delay = Long.parseLong(parameters.get("delay"));
                }
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        mobevent.doAbility(entity, args);
                    }
                }.runTaskTimer(Main.getPlugin(), delay, counter);
                taskList.add(task);
            }
        }
        taskIDs.put(entity.getUniqueId(), taskList);
    }
}
