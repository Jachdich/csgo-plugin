package com.cospox.csgoplugin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class Gun {
	public static final Gun[] guns = {
			//                 spray: norm, crouch, sprint; damage; range; rad;  cool; model; rounds; reserve; reload
			new Gun(Gun.GunType.AK47, 0.2,  0.02,   0.5,    6,      200,   0.01, 5,    1,     30,     90,      50),
			new Gun(Gun.GunType.M4A1S,0.15, 0.01,   0.5,    5,      200,   0.01, 4,    2,     25,     75,      75),
			new Gun(Gun.GunType.AWP,  0.8,  0.01,   1.3,    20,		400,   0.02, 30,   3,     10,     30,     100),
			new Gun(Gun.GunType.GLOCK,0.2,  0.01,   0.5,    3, 		200,   0.01, 5,    4,     30,     90,      50),
			new Gun(Gun.GunType.USPS ,0.2,  0.01,   0.5,    3, 		200,   0.01, 5,    5,     30,     90,      50),
	};
	public static final int TDEFAULT = 1;
	public static final int CTDEFAULT = 0;
	public enum GunType {
		AK47,
		M4A1S,
		AWP,
		GLOCK,
		USPS,
	}
	public double sprayNormal, sprayCrouch, spraySprint;
	public int damage, range, cooldown;
	public double radius;
	public GunType type;
	public int model;
	public int maxRounds, roundsReserve;
	public int reloadTime;
	
	private Gun(GunType type, double sprayNormal, double sprayCrouch, double spraySprint, int damage,
				int range, double radius, int cooldown, int customModelData, int maxRounds, int roundsReserve,
				int reloadTime) {
		this.type = type;
		this.sprayNormal = sprayNormal;
		this.sprayCrouch = sprayCrouch;
		this.spraySprint = spraySprint;
		this.damage = damage;
		this.range = range;
		this.radius = radius;
		this.cooldown = cooldown;
		this.model = customModelData;
		this.maxRounds = maxRounds;
		this.roundsReserve = roundsReserve;
		this.reloadTime = reloadTime;
	}
	
	public static Gun getGunByModelId(int id) {
		return guns[id - 1];
	}
	
	public ItemStack getItemStack() {
		ItemStack stack = null;
		switch (type) {
		case M4A1S:
		case AK47:
		case USPS:
		case GLOCK:
			stack = new ItemStack(Material.STONE_HOE);
			break;
		case AWP:
			stack = new ItemStack(Material.SPYGLASS);
			break;
		default:
			System.err.println("Some invalid gun type passed to getItemStack()");
			assert(false);
		}
		ItemMeta meta = stack.getItemMeta();

		switch(type) {
		case M4A1S: meta.setDisplayName(ChatColor.RESET + "M4A1S"); break;
		case AK47:  meta.setDisplayName(ChatColor.RESET + "AK47");  break;
		case USPS:  meta.setDisplayName(ChatColor.RESET + "USPS");  break;
		case GLOCK: meta.setDisplayName(ChatColor.RESET + "Glock"); break;
		case AWP:   meta.setDisplayName(ChatColor.RESET + "AWP");   break;
		}
		meta.setCustomModelData(model);
		stack.setItemMeta(meta);
		return stack;
	}
}
