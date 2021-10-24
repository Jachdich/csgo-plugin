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
    private Inventory inv;
    private GameState gs;
	public InvGUI(GameState state) {
		inv = Bukkit.createInventory(null, 27 * 2, "Weapon Selector");
        inv.setItem(11,     createGuiItem(Material.NETHERITE_HOE, 1, ChatColor.RESET + "AK47",  "§asome gun stats here", "§aand here too"));
        inv.setItem(11 + 9, createGuiItem(Material.NETHERITE_HOE, 2, ChatColor.RESET + "M4A1S", "§asome gun stats here", "§aand here too"));
        inv.setItem(11 + 18,createGuiItem(Material.SPYGLASS,      3, ChatColor.RESET + "AWP",   "§asome gun stats here", "§aand here too"));
        inv.setItem(15,     createGuiItem(Material.WOODEN_SWORD,  1, ChatColor.RESET + "Knife", "§asome gun stats here", "§aand here too"));
        this.gs = state;
	}
    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
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
        if (e.getInventory() != inv) return;

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
        if (e.getInventory().equals(inv)) {
          e.setCancelled(true);
        }
    }
}
