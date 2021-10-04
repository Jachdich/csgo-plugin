package com.cospox.csgoplugin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

public class Bomb {
	public static final long DEFUSE_TIME = 10 * 20;
	public int time = 40;
	private long lastDefuseTick = 0;
	public long defuseToGo = DEFUSE_TIME;
	public boolean beingDefused = false;
	public Location loc;
	private ItemFrame frame;
	public boolean defused = false;
	public Bomb(Location loc, ItemFrame frame) { this.loc = loc; this.frame = frame; }
	
	public void explode() {
		//loc.getWorld().createExplosion(loc, 10);
		World w = loc.getWorld();
		w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 100, new Random().nextFloat());
		w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 100);
		frame.remove();
	}
	
	public void updateTimer(long tick) {
		long delta = tick - lastDefuseTick;
		if (delta > 5) {
			defuseToGo = DEFUSE_TIME;
			beingDefused = false;
		}
	}
	
	public void defuseAttempt(long tick) {
		long delta = tick - lastDefuseTick;
		if (delta > 5 || delta < 0) {
			defuseToGo = DEFUSE_TIME;
			beingDefused = false;
		} else {
			defuseToGo -= delta;
			beingDefused = true;
			if (defuseToGo <= 0) {
				defused = true;
				frame.remove();
			}
		}
		lastDefuseTick = tick;
	}
}
