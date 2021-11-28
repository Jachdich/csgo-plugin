package com.cospox.csgoplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class TeamSelGUI implements Listener {
    private Inventory inv;
    private GameState gs;
	public TeamSelGUI(GameState state) {
		inv = Bukkit.createInventory(null, InventoryType.SHULKER_BOX);

        inv.setItem(3, createGuiItem(Material.COPPER_INGOT, 1, "" + ChatColor.RED + ChatColor.BOLD + "Terrorists"));
        inv.setItem(4, createGuiItem(Material.COPPER_INGOT, 2, "" + ChatColor.GREEN + ChatColor.BOLD + "Counter Terrorists"));

        this.gs = state;
	}
    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }
    
    protected ItemStack createGuiItem(final Material material, final int modeldata, final String name) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setCustomModelData(modeldata);

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != inv) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() != Material.COPPER_INGOT) return;
        if (!clickedItem.getItemMeta().hasCustomModelData()) return;
        Team team;
        if (clickedItem.getItemMeta().getCustomModelData() == 1) {
        	team = Team.TERRORIST;
        } else if (clickedItem.getItemMeta().getCustomModelData() == 2) {
        	team = Team.COUNTERTERRORIST;
        } else {
        	//something weird and the customModelData isn't right
        	return;
        }
        final Player p = (Player) e.getWhoClicked();
        gs.playerJoinTeam(p, team);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
          e.setCancelled(true);
        }
    }
}
