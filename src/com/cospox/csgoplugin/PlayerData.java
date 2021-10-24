package com.cospox.csgoplugin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerData {
	public Player p;
	public Team preferredTeam, assignedTeam;
	public long cooldown;
	public long rounds = 0;
	public boolean alive = true;
	public ItemStack selectedGun = null, selectedKnife = null, selectedPistol = null;
	public PlayerData(Player p) {
		this.p = p;
	}
	
	public void reset() {
		cooldown = 0;
		rounds = 0;
		preferredTeam = null;
		assignedTeam = null;
		selectedGun = null;
		selectedKnife = null;
		selectedPistol = null;
	}
}