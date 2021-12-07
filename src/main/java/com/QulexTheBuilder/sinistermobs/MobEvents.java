package com.QulexTheBuilder.sinistermobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MobEvents implements Listener {

    private final Random random = new Random();
    private Map<UUID, List<Integer>> repeatMap = new HashMap<>();
    private Map<UUID, Map<Integer, Long>> coolDown = new HashMap<>();
    private Abilities abilities = new Abilities();
    private Conditions conditions = new Conditions();

    //Events you want to start the process from each event (When a mob gets hurt player gets hurt mob dies etc.)

    //Death event start

    @EventHandler
    public void deathEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        YamlConfiguration yml = getFileByName(entity);
        if(yml == null) {
            return;
        }
        checkCondition(yml, entity, "onDeath");
    }

    @EventHandler
    public void onMonsterHurt(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof LivingEntity && (!(event.getEntity() instanceof Player))) {
            Entity entity = event.getEntity();
            YamlConfiguration yml = getFileByName(entity);
            if(yml == null) {
                return;
            }
            checkCondition(yml, entity, "onHurt");
        }
    }

    @EventHandler
    public void onMonsterHit(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof LivingEntity && (!(event.getDamager() instanceof Player))) {
            Entity entity = event.getDamager();
            YamlConfiguration yml = getFileByName(entity);
            if(yml == null) {
                return;
            }
            checkCondition(yml, entity, "onHit");
        }
    }

    public void checkCondition(YamlConfiguration yml, Entity self, String label) {
        int iteration = 0;
        for(String str : yml.getStringList("abilities")) {
            Pair<String[], String[]> args = separateStrings(str, 5);
            if(args.first[2].equalsIgnoreCase(label)) {
                Map<String, String> parameters = calcArguments(args.second[2]);
                boolean succesfullAbility = false;
                boolean repeat = true;
                long cooldown = 0;
                if(parameters.containsKey("repeat")) {
                    repeat = Boolean.getBoolean(parameters.get("repeat"));
                }
                if(parameters.containsKey("cooldown")) {
                    cooldown = Long.parseLong(parameters.get("cooldown"));
                }



                if(shouldRepeat(self, iteration) && offCooldown(self, iteration)) {
                    succesfullAbility = doAbility(self, args);
                }


                if(succesfullAbility) {
                    if(!repeat) {
                        if(repeatMap.containsKey(self.getUniqueId())) {
                            List<Integer> iterationList = repeatMap.get(self.getUniqueId());
                            iterationList.add(iteration);
                            repeatMap.put(self.getUniqueId(), iterationList);
                        } else {
                            List<Integer> iterationList = new ArrayList<>();
                            iterationList.add(iteration);
                            repeatMap.put(self.getUniqueId(), iterationList);
                        }
                    }
                    if(cooldown != 0) {
                        if(coolDown.containsKey(self.getUniqueId())) {
                            Map<Integer, Long> abilitiesDone = coolDown.get(self.getUniqueId());
                            abilitiesDone.put(iteration, System.currentTimeMillis()+cooldown);
                            coolDown.put(self.getUniqueId(), abilitiesDone);
                        } else {
                            List<Integer> iterationList = new ArrayList<>();
                            iterationList.add(iteration);
                            Map<Integer, Long> abilitiesDone = new HashMap<>();
                            abilitiesDone.put(iteration, System.currentTimeMillis()+cooldown);
                            coolDown.put(self.getUniqueId(), abilitiesDone);
                        }
                    }
                }
            }
            iteration++;
        }
    }

    private Boolean shouldRepeat(Entity self, Integer iteration) {
        if(repeatMap.containsKey(self.getUniqueId())) {
            List<Integer> abilitiesDone = repeatMap.get(self.getUniqueId());
            if(abilitiesDone.contains(iteration)) {
                return false;
            }
        }
        return true;
    }

    private Boolean offCooldown(Entity self, Integer iteration) {
        if(coolDown.containsKey(self.getUniqueId())) {
            Map<Integer, Long> abilitiesDone = coolDown.get(self.getUniqueId());
            if(abilitiesDone.containsKey(iteration)) {
                if(abilitiesDone.get(iteration) > System.currentTimeMillis()) {
                    return false;
                }
            }
        }
        return true;
    }

    //The backbone of the system these methods convert that string from the yml file into useful data.

    private YamlConfiguration getFileByName(Entity entity) {
        if(entity.getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "customMonster"), PersistentDataType.STRING)) {
            String name = entity.getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "customMonster"), PersistentDataType.STRING);
            for(File file : InstantiateMobs.filesInDirectory("mobs")) {
                if(file.getName().equalsIgnoreCase(name)) {
                    return YamlConfiguration.loadConfiguration(file);
                }
            }
        }
        return null;
    }

    public Map<String, String> calcArguments(String function) {
        Map<String, String> values = new HashMap<>();
        if(function == null) {
            return values;
        }
        String[] strings = function.trim().split(",");
        for(String arg : strings) {
            String[] args = arg.trim().split("=");
            if(args.length == 2) {
                values.put(args[0].trim(), args[1].trim());
            }
        }
        return values;
    }

    private List<Entity> calcTargetFromString(Entity host, String function, String args) {
        List<Entity> entityList = new ArrayList<>();
        if(function.equalsIgnoreCase("target") && host instanceof Creature) {
            entityList.add(((Creature) host).getTarget());
        } else if(function.equalsIgnoreCase("passenger")) {
            entityList.addAll(host.getPassengers());
        } else if(function.equalsIgnoreCase("passengerTarget")) {
            for(Entity entity : host.getPassengers()) {
                if(entity instanceof Creature) {
                    if(((Creature) entity).getTarget() != null) {
                        entityList.add(((Creature) entity).getTarget());
                    }
                }
            }
        } else {
            entityList.add(host);
        }
        return entityList;
    }

    private Boolean calcHealthCheck(Entity host, String string, String args) {
        if(string == null) {
            return true;
        }
        if(host instanceof Creature) {
            double health = ((Creature) host).getHealth();
            AttributeInstance maxHealthInstance = ((Creature) host).getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth = Objects.requireNonNull(maxHealthInstance).getBaseValue();
            double remainingHealth = health/maxHealth*100;
            string = string.trim().replace("%", "");
           if(string.contains("<=")) {
               string = string.replaceAll("<=", "");
               return remainingHealth <= Integer.parseInt(string);
           } else if(string.contains(">=")) {
               string = string.replaceAll(">=", "");
               return remainingHealth >= Integer.parseInt(string);
           } else if(string.contains(">")) {
               string = string.replaceAll(">", "");
               return remainingHealth > Integer.parseInt(string);
           } else if(string.contains("<")) {
               string = string.replaceAll("<", "");
               return remainingHealth < Integer.parseInt(string);
           } else if(string.contains("==")) {
               string = string.replaceAll("==", "");
               return remainingHealth == Integer.parseInt(string);
           } else {
               return true;
           }
        }
        return true;
    }
    private Integer calcChance(String string, String args) {
        if(string == null) {
            return 100;
        }
        return Integer.parseInt(string.trim().replace("%", ""));
    }

    public Boolean doAbility(Entity entity, Pair<String[], String[]> args) {
        if(args.first[0].equalsIgnoreCase("metaAbility")) {
            return metaAbility(calcArguments(args.second[0]), calcTargetFromString(entity, args.first[1],
                            args.second[1]), calcHealthCheck(entity, args.first[3], args.second[3]),
                    calcChance(args.first[4], args.second[4]), args, entity);
        } else if(args.first[0].equalsIgnoreCase("weightedAbility")) {
            return weightedAbility(calcArguments(args.second[0]), calcTargetFromString(entity, args.first[1],
                            args.second[1]), calcHealthCheck(entity, args.first[3], args.second[3]),
                    calcChance(args.first[4], args.second[4]), args, entity);
        } else {
            try {
                Class[] classes = {Map.class, List.class, Boolean.class, Integer.class, Entity.class};
                Method m = Abilities.class.getMethod(args.first[0] + "Ability", classes);
                m.setAccessible(true);
                Object invoke = m.invoke(abilities, calcArguments(args.second[0]), calcTargetFromString(entity, args.first[1], args.second[1]), calcHealthCheck(entity, args.first[3], args.second[3]), calcChance(args.first[4], args.second[4]), entity);
                return (Boolean) invoke;
            } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                System.out.println("There is no ability with this name");
                return false;
            }
        }
    }

    private Boolean metaAbility(Map<String, String> arg, List<Entity> targets, boolean healthCheck, Integer chance, Pair<String[], String[]> args, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        String ability = null;
        if(arg.containsKey("ability")) {
            ability = arg.get("ability");
        }
        for(Entity target : targets) {
            if(target == null) {
                return false;
            }
            if(!(target instanceof LivingEntity)) {
                return false;
            }
            YamlConfiguration yml = getFileByName(self);
            if(!checkAbilityConditions(yml, ability, target)) {
                return false;
            }

            List<String> abilityList = yml.getStringList(ability+".abilities");
            if(abilityList == null) {
                System.out.println("Could not find list");
            }
            boolean oneSuccessfullyAbility = false;
            for(String currentAbility : abilityList) {
                currentAbility = currentAbility.trim();
                args.second[0] = currentAbility.substring(currentAbility.indexOf("(") + 1, currentAbility.indexOf(")"));
                currentAbility = currentAbility.replaceAll("\\("+args.second[0]+"\\)", "");
                args.first[0] = currentAbility;
                boolean checkAbility = doAbility(self, args);
                if(checkAbility) {
                    oneSuccessfullyAbility = true;
                } else {
                    System.out.println("[Error] Ability was returned false" + args.first[0]+" "+args.second[0]);
                }
            }
            if(!oneSuccessfullyAbility) {
                return false;
            }
        }
        return true;
    }

    private Boolean weightedAbility(Map<String, String> arg, List<Entity> targets, boolean healthCheck, Integer chance, Pair<String[], String[]> args, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        String ability = null;
        if(arg.containsKey("ability")) {
            ability = arg.get("ability");
        }
        for(Entity target : targets) {
            if(target == null) {
                return false;
            }
            if(!(target instanceof LivingEntity)) {
                return false;
            }
            YamlConfiguration yml = getFileByName(self);
            if(!checkAbilityConditions(yml, ability, target)) {
                return false;
            }
            System.out.println("Passed conditions");
            List<String> abilityList = yml.getStringList(ability+".abilities");
            if(abilityList == null) {
                System.out.println("Could not find list");
            }
            int weightSum = 0;
            for(String currentAbility : abilityList) {
                Pair<String[], String[]> weightedAbility = separateStrings(currentAbility, 2);
                weightSum += Integer.parseInt(weightedAbility.first[1].trim());
            }
            System.out.println(weightSum);
            int rng = random.nextInt(weightSum);
            System.out.println(rng);
            int counter = 0;
            for(String currentAbility : abilityList) {
                Pair<String[], String[]> weightedAbility = separateStrings(currentAbility, 2);
                System.out.println(counter);
                if(Integer.parseInt(weightedAbility.first[1].trim()) + counter > rng) {
                    args.first[0] = weightedAbility.first[0];
                    args.second[0] = weightedAbility.second[0];
                    System.out.println(Arrays.toString(args.first));
                    System.out.println(Arrays.toString(args.second));
                    return doAbility(self, args);
                }
                counter += Integer.parseInt(weightedAbility.first[1].trim());
            }
        }
        return true;
    }



    private Boolean checkAbilityConditions(YamlConfiguration yml, String ability, Entity target) {
        List<String> arguments = yml.getStringList(ability+".conditions");
        if(arguments == null) {
            System.out.println("Could not find arguments for metaAbility");
        }
        for(String condition : arguments) {
            condition = condition.trim();
            String parameters = condition.substring(condition.indexOf("(") + 1, condition.indexOf(")"));
            condition = condition.replaceAll("\\("+parameters+"\\)", "").trim();
            Map<String, String> parameterMap = calcArguments(parameters);
            boolean succesFullcheck = false;
            try {
                Method m = Conditions.class.getMethod(condition+"Condition", Map.class, Entity.class);
                m.setAccessible(true);
                Object invoke = m.invoke(conditions, parameterMap, target);
                succesFullcheck = (Boolean) invoke;
            } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                System.out.println("There is no condition with this name");
                return false;
            }
            if(!succesFullcheck) {
                return false;
            }
        }
        return true;
    }

    public Pair<String[], String[]> separateStrings(String function, Integer size) {
        String[] command = new String[size];
        String[] args = new String[size];
        String mainString = function;
        while(mainString.contains("(")) {
            mainString = mainString.trim();
            String arg = mainString.substring(mainString.indexOf("(") + 1, mainString.indexOf(")"));
            String comma = mainString.substring(0, mainString.indexOf("("));
            int count = comma.length() - comma.replace(",", "").length();
            mainString = mainString.replaceAll("\\("+arg+"\\)", "");
            try {
                args[count] = arg;
            } catch(ArrayIndexOutOfBoundsException e) {
                System.out.println(ChatColor.RED+"You have to many commas in the config file, there should max be 4 commas");
            }
        }
        String[] commands = mainString.split(",");
        if(commands.length > size) {
            System.out.println(ChatColor.RED+"You have defined to many parameters in the config file this plugin only supports 5 mayor parameter use sub parameters instead");
        }
        int count = 0;
        for(String expression : commands) {
            command[count] = expression.trim();
            count++;
        }
        return new Pair<>(command, args);
    }

    //event that calculates drops doesn't work with looting or luck.

    @EventHandler
    public void monsterDropEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        YamlConfiguration yml = getFileByName(entity);
        if(yml == null) {
            return;
        }
        for(String str : yml.getStringList("drops")) {
            String[] strings = str.split(",");
            if(strings[0].equalsIgnoreCase("clear")) {
                event.getDrops().clear();
            } else {
                ItemStack item;
                if(Main.getPlugin().mobs.customItems.containsKey(strings[0].trim().toLowerCase())) {
                    item = new ItemStack(Main.getPlugin().mobs.customItems.get(strings[0].trim().toLowerCase()));
                } else {
                    item = new ItemStack(Material.valueOf(strings[0].trim().toUpperCase()), 1);
                }
                int min = 1;
                int max = 1;
                int chance = 100;
                if(strings.length >= 2) {
                    if(strings[1].contains("-")) {
                        min = Integer.parseInt(strings[1].trim().split("-")[0].trim());
                        max = Integer.parseInt(strings[1].trim().split("-")[1].trim());
                    } else {
                        min = Integer.parseInt(strings[1].trim());
                        max = min;
                    }
                }
                if(strings.length >= 3) {
                    chance = Integer.parseInt(strings[2].trim().split("%")[0].trim());
                }
                item = calculateDrop(item, min, max, chance);
                assert item != null;
                event.getDrops().add(item);
            }
        }
    }

    private ItemStack calculateDrop(ItemStack item, Integer amountMin, Integer amountMax, Integer chance) {
        item.setAmount(random.nextInt((amountMax+1-amountMin))+amountMin);
        if(random.nextInt(100)+1 <= chance) {
            return item;
        } else {
            return null;
        }
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerMoveEvent event) {
        List<UUID> mobId = new ArrayList<>();
        for(Entity entity : event.getPlayer().getNearbyEntities(30,30,30)) {
            mobId.add(entity.getUniqueId());
            if(Main.getPlugin().mobs.bossMobs.containsKey(entity.getUniqueId())) {
                BossBar bb = Main.getPlugin().mobs.bossMobs.get(entity.getUniqueId());
                bb.addPlayer(event.getPlayer());
            }
        }
        for(UUID id : Main.getPlugin().mobs.bossMobs.keySet()) {
            if(!(mobId.contains(id))) {
                BossBar bb = Main.getPlugin().mobs.bossMobs.get(id);
                bb.removePlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        if(Main.getPlugin().mobs.taskIDs.containsKey(event.getEntity().getUniqueId())) {
            List<BukkitTask> tasks = Main.getPlugin().mobs.taskIDs.get(event.getEntity().getUniqueId());
            for(BukkitTask task : tasks) {
                task.cancel();
            }
        }
        if(Main.getPlugin().mobs.bossMobs.containsKey(event.getEntity().getUniqueId())) {
            BossBar bb = Main.getPlugin().mobs.bossMobs.get(event.getEntity().getUniqueId());
            Main.getPlugin().mobs.bossMobs.remove(event.getEntity().getUniqueId());
            bb.removeAll();
        }
        repeatMap.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    private void onEntityHurt(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        if(Main.getPlugin().mobs.bossMobs.containsKey(entity.getUniqueId())) {
            BossBar bb = Main.getPlugin().mobs.bossMobs.get(entity.getUniqueId());
            AttributeInstance healthInstance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double healthMax = healthInstance.getBaseValue();
            double health = entity.getHealth();
            bb.setProgress(health/healthMax);
        }
    }

    @EventHandler
    private void EntityTakesFallDamage(EntityDamageEvent event) {
        if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            if(event.getEntity().getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "nofall"), PersistentDataType.STRING)) {
                if(event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "nofall"), PersistentDataType.STRING).trim().equalsIgnoreCase("true")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
