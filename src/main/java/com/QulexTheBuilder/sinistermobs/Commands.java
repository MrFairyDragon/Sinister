package com.QulexTheBuilder.sinistermobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if(command.getName().equalsIgnoreCase("sinisterMobs")) {
            switch(args[0]) {
                case "spawnMob":
                    if(!player.hasPermission("spawnMob") || !player.isOp()){
                        return true;
                    }
                    if(args.length == 1) {
                        sender.sendMessage("You need to specify a mob");
                        return true;
                    } else if(args.length == 2) {
                        Main.getPlugin().mobs.spawnMob(args[1], player.getLocation());
                        return true;
                    }
                case "spawnItem":
                    if(!player.hasPermission("spawnItem") || !player.isOp()){
                        return true;
                    }
                    if(args.length == 1) {
                        sender.sendMessage("You need to specify a Item");
                        return true;
                    } else if(args.length == 2) {
                        spawnItem(args[1], player);
                        return true;
                    }
                case "reload":
                    if(!player.hasPermission("reload") || !player.isOp()){
                        return true;
                    }
                    CustomItems.updateItemList();
                    player.sendMessage("You have reloaded stuff");
                    return true;
                case "killTasks":
                    if(!player.hasPermission("killTasks") || !player.isOp()){
                        return true;
                    }
                    EntityList.taskIDs.clear();
                default:
                    return true;
            }
        }
        return false;
    }

    private void spawnItem(String name, Player player) {
        if(Main.getPlugin().mobs.customItems.containsKey(name)) {
            player.getInventory().addItem(Main.getPlugin().mobs.customItems.get(name));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabList = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("sinistermobs")) {
            if(args.length == 1) {
                tabList.add("spawnMob");
                tabList.add("spawnItem");
                tabList.add("reload");
                return tabList;
            } else if( args.length == 2 && args[0].equalsIgnoreCase("spawnmob")) {
                for(File file : InstantiateMobs.getFilesInDirectory("mobs")) {
                    tabList.add(file.getName().substring(0, file.getName().length()-4));
                }
                return tabList;
            } else if( args.length == 2 && args[0].equalsIgnoreCase("spawnitem")) {
                for(File file : InstantiateMobs.getFilesInDirectory("items")) {
                    tabList.add(file.getName().toLowerCase().substring(0, file.getName().length()-4));
                }
                return tabList;
            }
        }
        return null;
    }

}