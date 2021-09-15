package com.cospox.csgoplugin;

public class Gun {
	public static Gun[] guns = {new Gun(Gun.GunType.GunA, 0.2, 0.01, 0.5, 10, 200, 0.01)};
	
	public enum GunType {
		GunA,
		GunB,
		GunC,
	}
	public double sprayNormal, sprayCrouch, spraySprint;
	public int damage, range;
	public double radius;
	public GunType type;
	
	public Gun(GunType type, double sprayNormal, double sprayCrouch, double spraySprint, int damage, int range, double radius) {
		this.type = type;
		this.sprayNormal = sprayNormal;
		this.sprayCrouch = sprayCrouch;
		this.spraySprint = spraySprint;
		this.damage = damage;
		this.range = range;
		this.radius = radius;
	}
	
	public static Gun getGunByModelId(int id) {
		return guns[0];
	}
}
