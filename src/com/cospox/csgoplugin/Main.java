package com.cospox.csgoplugin;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import javax.swing.plaf.BorderUIResource.EmptyBorderUIResource;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
    }
    
    public void fireTheGun(Player p, int dist, double radius, int damage, double spread) {
    	World w = p.getWorld();
    	Vector look = p.getEyeLocation().getDirection();
    	double r = look.length();
    	double theta = Math.acos(look.getZ() / r);
    	double phi = Math.atan2(look.getY(), look.getX());
    	Random rnd = ThreadLocalRandom.current();
    	theta += (rnd.nextDouble() - 0.5) * spread;
    	phi += (rnd.nextDouble() - 0.5) * spread;
   	
    	look.setX(r * Math.cos(phi) * Math.sin(theta));
    	look.setY(r * Math.sin(phi) * Math.sin(theta));
    	look.setZ(r * Math.cos(theta));
    	
    	RayTraceResult ray = w.rayTrace(p.getEyeLocation(), look, dist, FluidCollisionMode.NEVER, false, radius, humanPredicate(p));
    	
    	if (ray != null) {
    		Entity hit = ray.getHitEntity();
    		if (hit != null && hit.getType() == EntityType.PLAYER) {
    			Player hitPlayer = (Player)hit;
    			//hitPlayer.damage(damage);
    			hitPlayer.setVelocity(new Vector(0, 1, 0));
    		} else {
    			ray.getHitBlock().setType(Material.DIAMOND_BLOCK);
    		}
    	}
    }
    
    private static Predicate<Entity> humanPredicate(Player p1) {
    	return p -> p.getType() == EntityType.PLAYER && !p.equals(p1);
    }
    
    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {
    	if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && 
    	   	 e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NETHERITE_HOE)) {
    		//int modelId = e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getCustomModelData();
    		Gun gun = Gun.getGunByModelId(/*modelId*/0);
    		e.setCancelled(true);
    		double spray = gun.sprayNormal;
    		if (e.getPlayer().isSneaking()) {
    			spray = gun.sprayCrouch;
    		}
    		if (e.getPlayer().isSprinting()) {
    			spray = gun.spraySprint;
    		}
    		e.getPlayer().sendMessage("Range, radius, damage, spray: " + gun.range + ", " + gun.radius + ", " + gun.damage + ", " + spray);
    		fireTheGun(e.getPlayer(), gun.range, gun.radius, gun.damage, spray);
    	} else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_INGOT)) {
    		Location l = e.getClickedBlock().getLocation().add(0, 1, 0);
    		l.getBlock().setType(Material.ITEM_FRAME);
    		//l.getBlock().setBlockData();
    		e.getPlayer().sendMessage("ASDF");
    	}
    }
    
    @EventHandler
    public void sneakEvent(PlayerToggleSneakEvent e) {
    	if (!e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SPYGLASS)) return;
    	boolean isAccurate = e.getPlayer().isHandRaised();
    	if (!e.isSneaking()) {
    		if (isAccurate) {
    			fireTheGun(e.getPlayer(), 100, 0.05, 10, 0.01);
    		} else {
    			fireTheGun(e.getPlayer(), 100, 0.05, 8, 0.9);
    		}
       	}
    	if (e.isSneaking()) {
    		e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Fire when shift released"));
    	}
    }
}