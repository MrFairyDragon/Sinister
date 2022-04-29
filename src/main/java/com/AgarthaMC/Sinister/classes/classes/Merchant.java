package com.AgarthaMC.Sinister.classes.classes;

import com.AgarthaMC.Sinister.classes.ClassAssignment;
import com.AgarthaMC.Sinister.classes.ClassList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.Random;

public class Merchant implements Listener {

    private Random random = new Random();

    //Passive
    @EventHandler
    private void onBlockBreak(BlockDropItemEvent event) {
        if(ClassAssignment.getClass(event.getPlayer()).equals(ClassList.MERCHANT)) {
            if(random.nextInt(20) == 0) {
                event.getItems().addAll(event.getItems());
            }
        }
    }

    //Active //Hero of the village
    @EventHandler
    private void on() {

    }


}
