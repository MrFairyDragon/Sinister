package com.AgarthaMC.Sinister;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class VillagerDiamondEvent implements Listener {

    @EventHandler
    private void ChangePrice(InventoryOpenEvent event) {
        if(event.getView().getTopInventory() instanceof MerchantInventory) {
            MerchantInventory inventory = ((MerchantInventory) event.getView().getTopInventory());
            if(inventory.getHolder() instanceof Villager) {
                Villager villager = (Villager) inventory.getHolder();
                List<MerchantRecipe> merchList = new ArrayList<>();
                for(MerchantRecipe recipe : inventory.getMerchant().getRecipes()) {
                    List<ItemStack> ingredients = recipe.getIngredients();
                    if(recipe.getResult().getType().equals(Material.ENCHANTED_BOOK)) {
                        ItemStack diamond = (new ItemStack(Material.DIAMOND, recipe.getIngredients().get(0).getAmount()));
                        ingredients.set(0, diamond);

                    }
                    recipe.setIngredients(ingredients);
                    merchList.add(recipe);
                }
                villager.setRecipes(merchList);
            }
        }
    }
}
