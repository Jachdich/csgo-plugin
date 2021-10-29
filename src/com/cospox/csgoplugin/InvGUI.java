package com.cospox.csgoplugin;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class InvGUI implements Listener {
    private Inventory tInv;
    private Inventory ctInv;
    private GameState gs;
	public InvGUI(GameState state) {
		tInv = Bukkit.createInventory(null, 27 * 2, "Weapon Selector");
		ctInv = Bukkit.createInventory(null, 27 * 2, "Weapon Selector");
		
        tInv.setItem(11,     createGuiItem(Material.NETHERITE_HOE, 1, ChatColor.RESET + "T AK47",  "§asome gun stats here", "§aand here too"));
        tInv.setItem(11 + 9, createGuiItem(Material.NETHERITE_HOE, 2, ChatColor.RESET + "T M4A1S", "§asome gun stats here", "§aand here too"));
        tInv.setItem(11 + 18,createGuiItem(Material.SPYGLASS,      3, ChatColor.RESET + "T AWP",   "§asome gun stats here", "§aand here too"));
        tInv.setItem(15,     createGuiItem(Material.WOODEN_SWORD,  1, ChatColor.RESET + "T Knife", "§asome gun stats here", "§aand here too"));
        
        ctInv.setItem(11,     createGuiItem(Material.NETHERITE_HOE, 1, ChatColor.RESET + "CT AK47",  "§asome gun stats here", "§aand here too"));
        ctInv.setItem(11 + 9, createGuiItem(Material.NETHERITE_HOE, 2, ChatColor.RESET + "CT M4A1S", "§asome gun stats here", "§aand here too"));
        ctInv.setItem(11 + 18,createGuiItem(Material.SPYGLASS,      3, ChatColor.RESET + "CT AWP",   "§asome gun stats here", "§aand here too"));
        ctInv.setItem(15,     createGuiItem(Material.WOODEN_SWORD,  1, ChatColor.RESET + "CT Knife", "§asome gun stats here", "§aand here too"));
        this.gs = state;
	}
    public void openInventory(final HumanEntity ent, Team t) {
        ent.openInventory(t == Team.COUNTERTERRORIST ? ctInv : tInv);
    }
    
    protected ItemStack createGuiItem(final Material material, final int modeldata, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        meta.setCustomModelData(modeldata);

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != tInv && e.getInventory() != ctInv) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        int group = (e.getRawSlot() % 9 - 2) / 2;
        //p.getInventory().addItem(clickedItem);
        //p.sendMessage("X: " + group);
        PlayerData d = gs.getData(p);
        if (group == 0) {
        	//guns
        	d.selectedGun = clickedItem.clone();
        } else if (group == 1) {
        	//pistols
        	d.selectedPistol = clickedItem.clone();
        } else if (group == 2) {
        	//knives
        	d.selectedKnife = clickedItem.clone();
        }
        p.sendMessage("Selected the " + clickedItem.getItemMeta().getDisplayName());
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(ctInv) || e.getInventory().equals(tInv)) {
          e.setCancelled(true);
        }
    }
}
