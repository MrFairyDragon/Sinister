package com.AgarthaMC.Sinister;

import com.AgarthaMC.Sinister.mobs.CustomMobSpawnerHandler;
import com.AgarthaMC.Sinister.mobs.EntityList;
import com.AgarthaMC.Sinister.mobs.InstantiateMobs;
import com.AgarthaMC.Sinister.items.CustomItems;
import com.AgarthaMC.Sinister.serverEvents.potionRainEvent;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.swing.plaf.synth.Region;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor, TabExecutor {

    /*private Boolean structure(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getName().equalsIgnoreCase("Sinister")) {
            switch(args[0].toUpperCase()) {
                case "MOB":
                    switch(args[1].toUpperCase()) {
                        case "SPAWN":
                            if(!player.hasPermission("spawnMob") || !player.isOp()) {
                                return true;
                            }
                            if(args.length == 1) {
                                sender.sendMessage("You need to specify a mob");
                                return true;
                            } else if(args.length == 2) {
                                Main.getPlugin().mobs.spawnMob(args[1], player.getLocation());
                                return true;
                            }
                        case "CHANGESPAWNER":
                            if(!player.hasPermission("changeSpawner") || !player.isOp()) {
                                return true;
                            }
                            if(args.length == 1) {
                                sender.sendMessage("You need to specify a custom mob");
                                return true;
                            } else if(args.length == 2) {
                                Block block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
                                if(block.getType().equals(Material.SPAWNER)) {
                                    BlockState blockState = block.getState();
                                    for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
                                        if(file.getName().equalsIgnoreCase(args[1] + ".yml")) {
                                            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                                            EntityType type = EntityType.valueOf(Objects.requireNonNull(yml.getString("type")).toUpperCase());
                                            if(blockState instanceof CreatureSpawner) {
                                                CreatureSpawner spawnerData = (CreatureSpawner) blockState;
                                                spawnerData.setData(block.getState().getData());
                                                spawnerData.setSpawnedType(type);
                                                blockState.update();
                                                CustomMobSpawnerHandler.addSpawnerLocationToList(blockState.getLocation(), args[1]);
                                                return true;
                                            }
                                        }
                                    }
                                }
                                return true;
                            }
                        case "KILLTASKS":
                            if(!player.hasPermission("killTasks") || !player.isOp()) {
                                return true;
                            }
                            EntityList.taskIDs.clear();
                    }
                case "ITEM":
                    break;
                case "EVENT":
                    break;
            }
        }
    } */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if(command.getName().equalsIgnoreCase("Sinister")) {
            switch(args[0].toUpperCase()) {
                //region Mobs
                case "SPAWNMOB":
                    if(!player.hasPermission("spawnMob") || !player.isOp()) {
                        return true;
                    }

                    switch(args.length)
                    {
                        case 1:
                        sender.sendMessage("You need to specify a mob");
                        return true;

                        case 2:
                        Main.getPlugin().mobs.spawnMob(args[1], player.getLocation());
                        return true;

                        default:
                        return true;
                    }
                case "CHANGESPAWNER":
                    if(!player.hasPermission("changeSpawner") || !player.isOp()) {
                        return true;
                    }
                    if(args.length == 1) {
                        sender.sendMessage("You need to specify a custom mob");
                        return true;
                    } else if(args.length == 2) {
                        Block block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
                        if(block.getType().equals(Material.SPAWNER)) {
                            BlockState blockState = block.getState();
                            for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
                                if(file.getName().equalsIgnoreCase(args[1] + ".yml")) {
                                    YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                                    EntityType type = EntityType.valueOf(Objects.requireNonNull(yml.getString("type")).toUpperCase());
                                    if(blockState instanceof CreatureSpawner) {
                                        CreatureSpawner spawnerData = (CreatureSpawner) blockState;
                                        spawnerData.setData(block.getState().getData());
                                        spawnerData.setSpawnedType(type);
                                        blockState.update();
                                        CustomMobSpawnerHandler.addSpawnerLocationToList(blockState.getLocation(), args[1]);
                                        return true;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                    //endregion
                //region Items
                case "SPAWNITEM":
                    if(!player.hasPermission("spawnItem") || !player.isOp()) {
                        return true;
                    }
                    if(args.length == 1) {
                        sender.sendMessage("You need to specify a Item");
                        return true;
                    } else if(args.length == 2) {
                        spawnItem(args[1], player);
                        return true;
                    }
                case "RELOAD":
                    if(!player.hasPermission("reload") || !player.isOp()) {
                        return true;
                    }
                    CustomItems.updateItemList();
                    player.sendMessage("You have reloaded stuff");
                    return true;
                //endregion
                //region Mobs2
                case "KILLTASKS":
                    if(!player.hasPermission("killTasks") || !player.isOp()) {
                        return true;
                    }
                    EntityList.taskIDs.clear();
                default:
                    return true;
                //endregion
            }
        } else if(command.getName().equalsIgnoreCase("PotionEvent")) {
            //region ServerEvents
            if(args.length == 0) {
                player.sendMessage("You will need to specify a parameter");
                return true;
            }
            switch(args[0].toUpperCase()) {
                case "START":
                    if(!player.hasPermission("start") || !player.isOp()) {
                        return true;
                    }
                    potionRainEvent.startPotionRain();
                    break;
                case "STOP":
                    if(!player.hasPermission("stop") || !player.isOp()) {
                        return true;
                    }
                    potionRainEvent.isPotionRaining = false;
                    break;
                case "INTENSITY":
                    if(!player.hasPermission("intensity") || !player.isOp()) {
                        return true;
                    }
                    if(args.length != 2 || isNotInt(args[1])) {
                        player.sendMessage("Setting Intensity to default value");
                        potionRainEvent.intensity = 1;
                    } else {
                        potionRainEvent.intensity = Integer.parseInt(args[1]);
                        player.sendMessage("Setting Intensity to " + potionRainEvent.intensity);
                    }
                    break;
                case "POWER":
                    if(!player.hasPermission("power") || !player.isOp()) {
                        return true;
                    }
                    if(args.length != 2 || isNotInt(args[1])) {
                        player.sendMessage("Setting power to default value");
                        potionRainEvent.power = 2;
                    } else {
                        potionRainEvent.power = Integer.parseInt(args[1]);
                        player.sendMessage("Setting power to " + potionRainEvent.power);
                    }
                    break;
                case "DURATION":
                    if(!player.hasPermission("duration") || !player.isOp()) {
                        return true;
                    }
                    if(args.length != 2 || isNotInt(args[1])) {
                        player.sendMessage("Setting duration to default value");
                        potionRainEvent.duration = 20 * 20;
                    } else {
                        potionRainEvent.duration = Integer.parseInt(args[1]);
                        player.sendMessage("Setting duration to " + potionRainEvent.duration);
                    }
                    break;
                case "RANGE":
                    if(!player.hasPermission("range") || !player.isOp()) {
                        return true;
                    }
                    if(args.length != 2 || isNotInt(args[1])) {
                        player.sendMessage("Setting range to default value");
                        potionRainEvent.range = 10;
                    } else {
                        potionRainEvent.range = Integer.parseInt(args[1]);
                        player.sendMessage("Setting range to " + potionRainEvent.range);
                    }
                    break;
                case "TOGGLEPOTIONRAIN":
                    if(!player.hasPermission("togglePotionRain") || !player.isOp()) {
                        return true;
                    }
                    if(potionRainEvent.togglePotionRain) {
                        potionRainEvent.togglePotionRain = false;
                        player.sendMessage("Turned potion rain events off");
                    } else {
                        potionRainEvent.togglePotionRain = true;
                        player.sendMessage("Turned potion rain events on");
                    }
                    break;
            }
            //endregion
        } else if(command.getName().equalsIgnoreCase("SinisterClasses")) {
            //region UNKNOWN
            if(args.length == 0) {
                player.sendMessage("You will need to specify a parameter");
                return true;
            }
            switch(args[0]) {

            }
            //endregion
        }
        return true;
    }

    //region Items2
    private void spawnItem(String name, Player player) {
        if(Main.getPlugin().mobs.customItems.containsKey(name)) {
            player.getInventory().addItem(Main.getPlugin().mobs.customItems.get(name));
        }
    }
    //endregion

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabList = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("Sinister")) {
            if(args.length == 1) {
                tabList.add("spawnMob"); //add to mobs
                tabList.add("spawnItem"); //add to items
                tabList.add("reload"); //add to core
                tabList.add("killTasks"); //add to mobs
                tabList.add("changeSpawner"); //add to mobs
                return tabList;

            } else if( args.length == 2 && args[0].equalsIgnoreCase("changeSpawner")
                    || args.length == 2 && args[0].equalsIgnoreCase("spawnmob")) {
                //region Mobs3
                for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
                    tabList.add(file.getName().substring(0, file.getName().length() - 4));
                }
                //endregion
                return tabList;
            } else if( args.length == 2 && args[0].equalsIgnoreCase("spawnitem")) {
                //region Items3
                for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("items"))) {
                    tabList.add(file.getName().toLowerCase().substring(0, file.getName().length() - 4));
                }
                //endregion
                return tabList;
            }
        }
        //region ServerEvents2
        else if(command.getName().equalsIgnoreCase("PotionEvent")) {
            if(args.length == 1) {
                tabList.add("range");
                tabList.add("duration");
                tabList.add("power");
                tabList.add("intensity");
                tabList.add("start");
                tabList.add("stop");
                tabList.add("togglePotionRain");
                return tabList;
            } else {
                return null;
            }
        }
        //endregion
        return null;
    }

    public static boolean isNotInt(String string) {
        try {
            Integer.parseInt(string);
        } catch(NumberFormatException nfe) {
            return true;
        }
        return false;
    }

}