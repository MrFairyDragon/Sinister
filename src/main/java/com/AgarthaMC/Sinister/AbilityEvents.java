package com.AgarthaMC.Sinister;

import com.AgarthaMC.Sinister.abilities.AbilityList;
import com.AgarthaMC.Sinister.conditions.ConditionList;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class AbilityEvents implements Listener {

    private final Random random = new Random();
    public static Map<UUID, List<Integer>> repeatMap = new HashMap<>();
    public static Map<UUID, Map<Integer, Long>> coolDown = new HashMap<>();

    //Events you want to start the process from each event (When a mob gets hurt player gets hurt mob dies etc.)
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

    //Checks if a ability is off cooldown and should repeat (This could be changed from global variable to entity dependant variables using MySQL?)
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

    //Checks the maps for entities who are repeatable
    private Boolean isRepeatable(Entity self, Integer iteration) {
        if(repeatMap.containsKey(self.getUniqueId())) {
            List<Integer> abilitiesDone = repeatMap.get(self.getUniqueId());
            return !abilitiesDone.contains(iteration);
        }
        return true;
    }

    //Checks the maps for entities who are cooldown dependent
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
    public static YamlConfiguration getFileByName(Entity entity) {
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

    //turns string between parentheses into usable
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

    //Calculates the target based on a string
    private List<Entity> calculateTargetFromString(Entity host, String function, Map<String, String> args) {
        List<Entity> entityList = new ArrayList<>();
        switch(function) {
            case "target":
                if(host instanceof Creature) {
                    entityList.add(((Creature) host).getTarget());
                    break;
                }
            case "passenger":
                entityList.addAll(host.getPassengers());
                break;
            case "passengerTarget":
                for(Entity entity : host.getPassengers()) {
                    if(entity instanceof Creature) {
                        if(((Creature) entity).getTarget() != null) {
                            entityList.add(((Creature) entity).getTarget());
                        }
                    }
                }
                break;
            case "selfRange":
                int range = 5;
                if(args.containsKey("range")) {
                    range = Integer.parseInt(args.get("range"));
                }
                //Get nearby entities is a very expansive method
                List<Entity> tempEntity = host.getNearbyEntities(range, range, range);
                for(Entity entity : tempEntity) {
                    if(entity instanceof LivingEntity) {
                        entityList.add(entity);
                    }
                }
                break;
            case "targetRange":
                int range2 = 5;
                if(args.containsKey("range")) {
                    range2 = Integer.parseInt(args.get("range"));
                }
                if(((Creature) host).getTarget() != null) {
                    //Get nearby entities is a very expansive method
                    List<Entity> tempEntity2 = Objects.requireNonNull(((Creature) host).getTarget()).getNearbyEntities(range2, range2, range2);
                    for(Entity entity : tempEntity2) {
                        if(entity instanceof LivingEntity) {
                            entityList.add(entity);
                        }
                    }
                }
                break;
            default:
                entityList.add(host);
        }
        return entityList;
    }


    //checks if the mob satisfies the health condition
    private Boolean calculateHealthCheck(Entity host, String string, String args) {
        if(string == null) {
            return true;
        }
        if(host instanceof Creature) {
            String[] operators = {"<=",">=","<",">","=="};
             double health = ((Creature) host).getHealth();
            AttributeInstance maxHealthInstance = ((Creature) host).getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth = Objects.requireNonNull(maxHealthInstance).getBaseValue();
            double remainingHealth = health/maxHealth*100;
            string = string.trim().replace("%", "");
            String match = "default";
            for (int i = 0; i < operators.length; i++) {
                if (string.contains(operators[i])) {
                    match = operators[i];
                    break;
                }
            }
            switch(match) {
                case "<=":
                    string = string.replaceAll("<=", "");
                    return remainingHealth <= Integer.parseInt(string);
                case ">=":
                    string = string.replaceAll(">=", "");
                    return remainingHealth >= Integer.parseInt(string);
                case ">":
                    string = string.replaceAll(">", "");
                    return remainingHealth > Integer.parseInt(string);
                case "<":
                    string = string.replaceAll("<", "");
                    return remainingHealth < Integer.parseInt(string);
                case "==":
                    string = string.replaceAll("==", "");
                    return remainingHealth == Integer.parseInt(string);
                default:
                    return true;
            }
        }
        return true;
    }

    //checks if the mob satisfies the chance condition
    private Integer calculateChance(String string, String args) {
        if(string == null) {
            return 100;
        }
        return Integer.parseInt(string.trim().replace("%", ""));
    }

    //checks a onTrigger ability should keep running
    public boolean checkIfDoAbility(Entity entity, Pair<String[], String[]> args ) {
        if(!entity.isValid()) {
            if(EntityList.taskIDs.containsKey(entity.getUniqueId())) {
                EntityList.taskIDs.remove(entity.getUniqueId());
            }
        }
        if(!calculateHealthCheck(entity, args.first[3], args.second[3])) {
            return false;
        }
        return random.nextInt(100) < calculateChance(args.first[4], args.second[4]);
    }

    //Finds the ability that it should run you can find all abilities under the abilities folder
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

    //The metaAbility runs a series of abilities right after another
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

    //The weightedAbility runs a SINGLE ability based on chance
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

    //Check conditions that you can specify in the meta-ability and weighted-ability
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

    //This turns a ability string into arguments that java can understand
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
}
