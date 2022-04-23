package com.AgarthaMC.Sinister.classes;

import com.AgarthaMC.Sinister.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

public class ClassAssignment implements Listener {

    private static HashMap<UUID, ClassList> PlayerClass = new HashMap<>();


    public static void updateList() {
        try {
            Statement statement = Main.getPlugin().connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM PlayerClass");
            while(rs.next()) {
                Player player = Bukkit.getPlayer(rs.getString("PlayerName"));
                ClassList playerClass = ClassList.valueOf(rs.getString("ClassName"));
                assert player != null;
                PlayerClass.put(player.getUniqueId(), playerClass);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void joinClass(Player player, ClassList classList) {
        if(PlayerClass.containsKey(player.getUniqueId())) {
            removeClass(player);
        }
        try {
            PreparedStatement statement = Main.getPlugin().connection.prepareStatement("INSERT INTO CustomSpawnerLocations(PlayerName, ClassName) VALUES ('"+player.getName()+"','"+classList.name()+"')");
            statement.executeUpdate();
            PlayerClass.put(player.getUniqueId(), classList);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeClass(Player player) {
        if(!(PlayerClass.containsKey(player.getUniqueId()))) {
            return;
        }
        try {
            PreparedStatement statement = Main.getPlugin().connection.prepareStatement("DELETE FROM CustomSpawnerLocations WHERE x='"+player.getName()+"'");
            statement.executeUpdate();
            PlayerClass.remove(player.getUniqueId());
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static ClassList getClass(Player player) {
        return PlayerClass.get(player.getUniqueId());
    }

}
