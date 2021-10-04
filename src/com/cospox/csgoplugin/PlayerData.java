package com.cospox.csgoplugin;

import org.bukkit.entity.Player;

public class PlayerData {
	public Player p;
	public Team preferredTeam, assignedTeam;
	public long cooldown;
	public PlayerData(Player p) {
		this.p = p;
	}
}