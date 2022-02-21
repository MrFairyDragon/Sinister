package com.QulexTheBuilder.sinistermobs;

import com.QulexTheBuilder.sinistermobs.abilities.AbilityList;
import com.QulexTheBuilder.sinistermobs.conditions.ConditionList;
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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class MobEvents implements Listener {

    private final Random random = new Random();
    private Map<UUID, List<Integer>> repeatMap = new HashMap<>();
    private Map<UUID, Map<Integer, Long>> coolDown = new HashMap<>();

    //Events you want to start the process from each event (When a mob gets hurt player gets hurt mob dies etc.)

    //Death event start

    @EventHandler
    public void onDeathEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        YamlConfiguration yml = getFileByName(entity);
        if(yml == null) {
            return;
        }
        checkCondition(yml, entity, "onDeath");
    }

    @EventHandler
    public void onMonsterHurt(EntityDamageEvent event) {
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

    @EventHandler
    public void onPlayerDeath(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            if(((Player) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
                Entity entity = event.getDamager();
                YamlConfiguration yml = getFileByName(entity);
                if(yml == null) {
                    return;
                }
                checkCondition(yml, entity, "onPlayerKill");
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof LivingEntity) {
            if(((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
                Entity entity = event.getDamager();
                YamlConfiguration yml = getFileByName(entity);
                if(yml == null) {
                    return;
                }
                checkCondition(yml, entity, "onKill");
            }
        }
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Arrow) {
            Entity entity = (Entity) ((Arrow) event.getDamager()).getShooter();
            assert entity != null;
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
                Map<String, String> parameters = calculateArguments(args.second[2]);
                boolean successfulAbility = false;
                boolean repeat = true;
                long cooldown = 0;
                if(parameters.containsKey("repeat")) {
                    repeat = Boolean.getBoolean(parameters.get("repeat"));
                }
                if(parameters.containsKey("cooldown")) {
                    cooldown = Long.parseLong(parameters.get("cooldown"));
                }
                if(isRepeatable(self, iteration) && isOffCooldown(self, iteration)) {
                    if(checkIfDoAbility(self, args)) {
                        successfulAbility = doAbility(self, args);
                    }
                }
                if(successfulAbility) {
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

    private Boolean isRepeatable(Entity self, Integer iteration) {
        if(repeatMap.containsKey(self.getUniqueId())) {
            List<Integer> abilitiesDone = repeatMap.get(self.getUniqueId());
            return !abilitiesDone.contains(iteration);
        }
        return true;
    }

    private Boolean isOffCooldown(Entity self, Integer iteration) {
        if(coolDown.containsKey(self.getUniqueId())) {
            Map<Integer, Long> abilitiesDone = coolDown.get(self.getUniqueId());
            if(abilitiesDone.containsKey(iteration)) {
                return abilitiesDone.get(iteration) <= System.currentTimeMillis();
            }
        }
        return true;
    }

    //The backbone of the system these methods convert that string from the yml file into useful data.

    private YamlConfiguration getFileByName(Entity entity) {
        if(entity.getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "customMonster"), PersistentDataType.STRING)) {
            String name = entity.getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "customMonster"), PersistentDataType.STRING);
            for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
                if(file.getName().equalsIgnoreCase(name)) {
                    return YamlConfiguration.loadConfiguration(file);
                }
            }
        }
        return null;
    }

    public Map<String, String> calculateArguments(String function) {
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

    private List<Entity> calculateTargetFromString(Entity host, String function, Map<String, String> args) {
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
        } else if(function.equalsIgnoreCase("selfRange")) {
            int range = 5;
            if(args.containsKey("range")) {
                range = Integer.parseInt(args.get("range"));
            }
            List<Entity> tempEntity = host.getNearbyEntities(range, range, range);
            for(Entity entity: tempEntity) {
                if(entity instanceof LivingEntity) {
                    entityList.add(entity);
                }
            }
        } else if(function.equalsIgnoreCase("targetRange") && host instanceof Creature) {
            int range = 5;
            if(args.containsKey("range")) {
                range = Integer.parseInt(args.get("range"));
            }
            if(((Creature) host).getTarget() != null) {
                List<Entity> tempEntity = Objects.requireNonNull(((Creature) host).getTarget()).getNearbyEntities(range, range, range);
                for(Entity entity: tempEntity) {
                    if(entity instanceof LivingEntity) {
                        entityList.add(entity);
                    }
                }
            }
        } else {
            entityList.add(host);
        }
        return entityList;
    }

    private Boolean calculateHealthCheck(Entity host, String string, String args) {
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
    private Integer calculateChance(String string, String args) {
        if(string == null) {
            return 100;
        }
        return Integer.parseInt(string.trim().replace("%", ""));
    }
    public boolean checkIfDoAbility(Entity entity, Pair<String[], String[]> args ) {
        if(!entity.isValid()) {
            
        }
        if(!calculateHealthCheck(entity, args.first[3], args.second[3])) {
            return false;
        }
        return random.nextInt(100) < calculateChance(args.first[4], args.second[4]);
    }

    public Boolean doAbility(Entity entity, Pair<String[], String[]> args) {
        if(args.first[0].equalsIgnoreCase("metaAbility")) {
            return metaAbility(calculateArguments(args.second[0]), calculateTargetFromString(entity, args.first[1],
                            calculateArguments(args.second[1])), args, entity);
        } else if(args.first[0].equalsIgnoreCase("weightedAbility")) {
            return weightedAbility(calculateArguments(args.second[0]), calculateTargetFromString(entity, args.first[1],
                            calculateArguments(args.second[1])), args, entity);
        } else {
            try {
                return AbilityList.valueOf(args.first[0].toUpperCase()).doAbility(entity, calculateTargetFromString(entity, args.first[1], calculateArguments(args.second[1])), calculateArguments(args.second[0]));
            } catch(IllegalArgumentException e) {
                System.out.println("There is no Ability with that name");
            }
            return false;
        }
    }

    private Boolean metaAbility(Map<String, String> arg, List<Entity> targets, Pair<String[], String[]> args, Entity self) {
        String ability = null;
        if(arg.containsKey("ability")) {
            ability = arg.get("ability");
        }
        YamlConfiguration yml = getFileByName(self);
        for(Entity target : targets) {
            if(target == null) {
                return false;
            }
            assert yml != null;
            if(!checkAbilityConditions(yml, ability, target)) {
                return false;
            }
        }
        assert yml != null;
        List<String> abilityList = yml.getStringList(ability+".abilities");
        if(abilityList.isEmpty()) {
            System.out.println("Could not find list");
        }
        boolean oneSuccessfullyAbility = false;
        String args1 = args.first[0];
        String args2 = args.second[0];
        for(String currentAbility : abilityList) {
            currentAbility = currentAbility.trim();
            args.second[0] = currentAbility.substring(currentAbility.indexOf("(") + 1, currentAbility.indexOf(")"));
            currentAbility = currentAbility.replaceAll("\\("+args.second[0]+"\\)", "");
            args.first[0] = currentAbility;
            boolean checkAbility = doAbility(self, args);
            if(checkAbility) {
                oneSuccessfullyAbility = true;
            } else {
                System.out.println("[Error] Ability was returned false: " + args.first[0]+" "+args.second[0]);
            }
        }
        args.first[0] = args1;
        args.second[0] = args2;
        return oneSuccessfullyAbility;
    }

    private Boolean weightedAbility(Map<String, String> arg, List<Entity> targets, Pair<String[], String[]> args, Entity self) {
        String ability = null;
        if(arg.containsKey("ability")) {
            ability = arg.get("ability");
        }
        YamlConfiguration yml = getFileByName(self);
        for(Entity target : targets) {
            if(target == null) {
                return false;
            }
            assert yml != null;
            if(!checkAbilityConditions(yml, ability, target)) {
                return false;
            }
        }
        assert yml != null;
        List<String> abilityList = yml.getStringList(ability+".abilities");
        if(abilityList.isEmpty()) {
            System.out.println("Could not find list");
        }
        int weightSum = 0;
        for(String currentAbility : abilityList) {
            Pair<String[], String[]> weightedAbility = separateStrings(currentAbility, 2);
            weightSum += Integer.parseInt(weightedAbility.first[1].trim());
        }
        int rng = random.nextInt(weightSum);
        int counter = 0;
        String args1 = args.first[0];
        String args2 = args.second[0];
        for(String currentAbility : abilityList) {
            Pair<String[], String[]> weightedAbility = separateStrings(currentAbility, 2);
            if(Integer.parseInt(weightedAbility.first[1].trim()) + counter > rng) {
                args.first[0] = weightedAbility.first[0];
                args.second[0] = weightedAbility.second[0];
                boolean success = doAbility(self, args);
                args.first[0] = args1;
                args.second[0] = args2;
                return success;
            }
            counter += Integer.parseInt(weightedAbility.first[1].trim());
        }
        return false;
    }



    private Boolean checkAbilityConditions(YamlConfiguration yml, String ability, Entity target) {
        List<String> arguments = yml.getStringList(ability+".conditions");
        if(arguments.isEmpty()) {
            System.out.println("Could not find arguments for metaAbility");
        }
        for(String condition : arguments) {
            condition = condition.trim();
            String parameters = condition.substring(condition.indexOf("(") + 1, condition.indexOf(")"));
            condition = condition.replaceAll("\\("+parameters+"\\)", "").trim();
            Map<String, String> parameterMap = calculateArguments(parameters);
            boolean succesFullcheck;
            try {
                succesFullcheck = ConditionList.valueOf(condition.toUpperCase()).checkCondition(target, parameterMap);
            } catch(IllegalArgumentException e) {
                System.out.println("There is no condition with this name");
                System.out.println(condition);
                succesFullcheck = false;
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
                System.out.println(ChatColor.RED+"You have to many commas in the config file, there should max be"+(size-1)+"commas");
            }
        }
        String[] commands = mainString.split(",");
        if(commands.length > size) {
            System.out.println(ChatColor.RED+"You have defined to many parameters in the config file this plugin only supports"+size+"mayor parameter use sub parameters instead");
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

    public static Location getAbilityLocation(Entity entity) {
        Location location = entity.getLocation();
        if(entity.getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "abilityPosition"), PersistentDataType.INTEGER_ARRAY)) {
            int[] abilityPositions = entity.getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "abilityPosition"), PersistentDataType.INTEGER_ARRAY);
            location = new Vector(abilityPositions[0], abilityPositions[1], abilityPositions[2]).toLocation(entity.getWorld());
        }
        return location;
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerMoveEvent event) {
        List<UUID> mobId = new ArrayList<>();
        for(Entity entity : event.getPlayer().getNearbyEntities(30,30,30)) {
            mobId.add(entity.getUniqueId());
            if(EntityList.bossMobs.containsKey(entity.getUniqueId())) {
                BossBar bb = EntityList.bossMobs.get(entity.getUniqueId());
                bb.addPlayer(event.getPlayer());
            }
        }
        for(UUID id : EntityList.bossMobs.keySet()) {
            if(!(mobId.contains(id))) {
                BossBar bb = EntityList.bossMobs.get(id);
                bb.removePlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        if(EntityList.taskIDs.containsKey(event.getEntity().getUniqueId())) {
            List<BukkitTask> tasks = EntityList.taskIDs.get(event.getEntity().getUniqueId());
            for(BukkitTask task : tasks) {
                task.cancel();
            }
        }
        if(EntityList.bossMobs.containsKey(event.getEntity().getUniqueId())) {
            BossBar bb = EntityList.bossMobs.get(event.getEntity().getUniqueId());
            EntityList.bossMobs.remove(event.getEntity().getUniqueId());
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
        if(EntityList.bossMobs.containsKey(entity.getUniqueId())) {
            BossBar bb = EntityList.bossMobs.get(entity.getUniqueId());
            AttributeInstance healthInstance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double healthMax = healthInstance.getBaseValue();
            double health = entity.getHealth();
            bb.setProgress(health/healthMax);
        }
    }

    @EventHandler
    private void onFallDamage(EntityDamageEvent event) {
        if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            if(event.getEntity().getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "nofall"), PersistentDataType.STRING)) {
                if(event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "nofall"), PersistentDataType.STRING).trim().equalsIgnoreCase("true")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onShootProjectile(EntityShootBowEvent event) {
        if(event.getEntity().getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "arrowColor"), PersistentDataType.STRING)) {
            String arrowColor = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "arrowColor"), PersistentDataType.STRING);
            Entity arrow = event.getProjectile();
            if(arrow instanceof Arrow) {
                ((Arrow) arrow).setColor(Color.fromRGB(Integer.parseInt(arrowColor, 16)));
            }
            event.setProjectile(arrow);
        }
    }
}
