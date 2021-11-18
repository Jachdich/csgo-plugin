package com.cospox.csgoplugin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Gun {
	public static Gun[] guns = {new Gun(Gun.GunType.AK47, 0.2, 0.01, 0.5, 10, 200, 0.01, 10, 1, 20, 30),
								new Gun(Gun.GunType.M4A1S,0.2, 0.01, 0.5, 10, 200, 0.01, 20, 2, 30, 40),
								new Gun(Gun.GunType.AWP,  1.0, 0.01, 1.5, 15, 400, 0.02, 30, 3, 40, 50)};
	
	public enum GunType {
		AK47,
		M4A1S,
		AWP,
	}
	public double sprayNormal, sprayCrouch, spraySprint;
	public int damage, range, cooldown;
	public double radius;
	public GunType type;
	public int model;
	public int maxRounds;
	public int reloadTime;
	
	private Gun(GunType type, double sprayNormal, double sprayCrouch, double spraySprint, int damage,
				int range, double radius, int cooldown, int customModelData, int maxRounds, int reloadTime) {
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
			stack = new ItemStack(Material.NETHERITE_HOE, 1);
			break;
		case AWP:
			stack = new ItemStack(Material.SPYGLASS);
		default:
			System.err.println("Some invalid gun type passed to getItemStack()");
			assert(false);
		}
		ItemMeta meta = stack.getItemMeta();
		meta.setCustomModelData(model);
		stack.setItemMeta(meta);
		return stack;
	}
}
