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

public class GunSelGUI implements Listener {
    private Inventory tInv;
    private Inventory ctInv;
    private GameState gs;
	public GunSelGUI(GameState state) {
		tInv = Bukkit.createInventory(null, 9, "Weapon Selector");
		ctInv = Bukkit.createInventory(null, 9, "Weapon Selector");

        tInv.setItem(3, createGuiItem(Material.STONE_HOE, 1, ChatColor.RESET + "AK47",  "§asome gun stats here", "§aand here too"));
        tInv.setItem(4, createGuiItem(Material.STONE_HOE, 4, ChatColor.RESET + "Glock", "§asome gun stats here", "§aand here too"));
        tInv.setItem(5, createGuiItem(Material.SPYGLASS,  3, ChatColor.RESET + "AWP",   "§asome gun stats here", "§aand here too"));
        
        ctInv.setItem(3, createGuiItem(Material.STONE_HOE, 2, ChatColor.RESET + "M4A1S", "§asome gun stats here", "§aand here too"));
        ctInv.setItem(4, createGuiItem(Material.STONE_HOE, 5, ChatColor.RESET + "USPS",  "§asome gun stats here", "§aand here too"));
        ctInv.setItem(5, createGuiItem(Material.SPYGLASS,  3, ChatColor.RESET + "AWP",   "§asome gun stats here", "§aand here too"));
        this.gs = state;
	}
    public void openInventory(final HumanEntity ent, Team t) {
        ent.openInventory(t == Team.COUNTERTERRORIST ? ctInv : tInv);
    }
    
    protected ItemStack createGuiItem(final Material material, final int modeldata, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        ///meta.setLore(Arrays.asList(lore));
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
        PlayerData d = gs.getData(p);
        d.selectedGun = Gun.getGunByModelId(clickedItem.getItemMeta().getCustomModelData()).getItemStack();
        p.sendMessage("Selected the " + clickedItem.getItemMeta().getDisplayName());
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().equals(ctInv) || e.getInventory().equals(tInv)) {
          e.setCancelled(true);
        }
    }
}
