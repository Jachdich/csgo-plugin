package com.cospox.csgoplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerData {
	public Player p;
	public Team preferredTeam, assignedTeam;
	public long cooldown;
	public int rounds = 0;
	public int reloadCooldown = -1;
	public int maxCooldown = -1;
	public boolean alive = true;
	public ItemStack selectedGun = null;
	Scoreboard sb;
	Objective ob;
	public PlayerData(Player p) {
		this.p = p;
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective ob = sb.registerNewObjective("asdf", "dummy", "asdf-asdf");
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("Ammo").setScore(0);
		p.setScoreboard(sb);
		this.sb = sb;
		this.ob = ob;
	}
	
	public void reset() {
		cooldown = 0;
		rounds = 0;
		preferredTeam = null;
		assignedTeam = null;
		selectedGun = null;
	}
	
	public Team getPossibleTeam() {
		if (this.assignedTeam == null) {
			return this.preferredTeam;
		} else {
			return this.assignedTeam;
		}
	}
	
	public void assignDefaultWeapons() {
		if (this.selectedGun == null) {
			if (getPossibleTeam() == Team.COUNTERTERRORIST) {
				this.selectedGun = Gun.guns[Gun.CTDEFAULT].getItemStack();
				this.reload(Gun.guns[Gun.CTDEFAULT]);
			} else {
				this.selectedGun = Gun.guns[Gun.TDEFAULT].getItemStack();
				this.reload(Gun.guns[Gun.TDEFAULT]);
			}
		}
	}
	
	public void clearWeapons() {
		selectedGun = null;
	}
	
	public void reload(Gun g) {
		this.rounds = g.maxRounds;
		p.setLevel(rounds);
		maxCooldown = g.reloadTime;
		reloadCooldown = g.reloadTime;
	}
	
	public void reloadWhatever() {
		int data = selectedGun.getItemMeta().getCustomModelData();
		reload(Gun.getGunByModelId(data));
	}
}