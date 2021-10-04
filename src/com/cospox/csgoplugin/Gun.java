package com.cospox.csgoplugin;

public class Gun {
	public static Gun[] guns = {new Gun(Gun.GunType.AK47, 0.2, 0.01, 0.5, 10, 200, 0.01, 10),
								new Gun(Gun.GunType.M4A1S,0.2, 0.01, 0.5, 10, 200, 0.01, 20),
								new Gun(Gun.GunType.AWP,  1.0, 0.01, 1.5, 15, 400, 0.02, 30)};
	
	public enum GunType {
		AK47,
		M4A1S,
		AWP,
	}
	public double sprayNormal, sprayCrouch, spraySprint;
	public int damage, range, cooldown;
	public double radius;
	public GunType type;
	
	public Gun(GunType type, double sprayNormal, double sprayCrouch, double spraySprint, int damage, int range, double radius, int cooldown) {
		this.type = type;
		this.sprayNormal = sprayNormal;
		this.sprayCrouch = sprayCrouch;
		this.spraySprint = spraySprint;
		this.damage = damage;
		this.range = range;
		this.radius = radius;
		this.cooldown = cooldown;
	}
	
	public static Gun getGunByModelId(int id) {
		return guns[id - 1];
	}
}
