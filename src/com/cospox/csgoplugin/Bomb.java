package com.cospox.csgoplugin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Bomb {
	public static final long DEFUSE_TIME = 10 * 20;
	public int time = 45;
	private long lastDefuseTick = 0;
	public long defuseToGo = DEFUSE_TIME;
	public boolean beingDefused = false;
	public Location loc;
	private ItemFrame frame;
	public boolean defused = false;
	private Main plugin;
	public Bomb(Location loc, ItemFrame frame, Main plugin) {
		this.loc = loc;
		this.frame = frame;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	public void run() {
        		updateTimer(loc.getWorld().getGameTime());
        	}
        }, 0, 2);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	public void run() {
        		updateBomb();
        	}
        }, 0, 20);
        
        this.plugin = plugin;
	}
	
    private void updateBomb() {
		this.time -= 1;
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			if (this.beingDefused) {
    			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Bomb being defused in " + this.defuseToGo / 20));
			} else if (!this.defused) {
    			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Bomb going off in " + this.time));
			}
		}
		if (this.time < 1) {
			this.explode();
			//notify the gamestate of the bomb exploding
			plugin.state.bombExplode();
		} else if (this.defused) {
			//notify the gamestate of the bomb being defused
			plugin.state.bombDefuse();
		}
    }
	
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
