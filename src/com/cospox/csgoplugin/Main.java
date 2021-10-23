package com.cospox.csgoplugin;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
	private InvGUI gunSel;
	private Bomb currentBomb;
	private GameState state;
    @Override
    public void onEnable() {
    	this.gunSel = new InvGUI();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(gunSel, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
        	public void run() {
        		updateBomb();
        	}
        }, 0, 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
        	public void run() {
        		if (currentBomb != null) {
        			currentBomb.updateTimer(currentBomb.loc.getWorld().getGameTime());
        		}
        	}
        }, 0, 2);
        Arena arena = new Arena(
        		new Location(this.getServer().getWorlds().get(0), 10.5, 70.5, 0.5),
        		new Location(this.getServer().getWorlds().get(0), 10.5, 70.5, 10.5),
        		new Location(this.getServer().getWorlds().get(0), 0.5, 70.5, 0.5),
        		new Location(this.getServer().getWorlds().get(0), 0.5, 70.5, 10.5)
        );
        
        state = new GameState(arena);
        
        this.getCommand("jointeam").setExecutor(state);
        this.getCommand("startgame").setExecutor(state);
    }
    
    private void updateBomb() {
    	if (currentBomb != null) {
    		currentBomb.time -= 1;
    		for (Player p : getServer().getOnlinePlayers()) {
    			if (currentBomb.beingDefused) {
        			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Bomb being defused in " + currentBomb.defuseToGo / 20));
    			} else if (!currentBomb.defused) {
        			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Bomb going off in " + currentBomb.time));
    			}
    		}
    		if (currentBomb.time < 1) {
    			currentBomb.explode();
    			currentBomb = null;
    		} else if (currentBomb.defused) {
    			currentBomb = null;
    		}
    	}
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
    	
    	for (float n = 0.1f; n < dist; n *= 1.2f) {
    		Vector l2 = look.clone();
    		w.spawnParticle(Particle.ASH, p.getEyeLocation().add(l2.multiply(n)), 1, 0, 0, 0, 0);
    	}
    	if (ray != null) {
    		Entity hit = ray.getHitEntity();
    		if (hit != null/* && hit.getType() == EntityType.PLAYER*/) {
    			//Player hitPlayer = (Player)hit;
    			//hitPlayer.damage(damage);
    			LivingEntity e = ((LivingEntity)hit);
    			e.damage(damage);
    			//hitPlayer.setVelocity(new Vector(0, 1, 0));
    		} else {
    			//ray.getHitBlock().setType(Material.DIAMOND_BLOCK);
    		}
    	}
    }
    
    private static Predicate<Entity> humanPredicate(Player p1) {
    	return p -> p.getType() == EntityType.PLAYER && !p.equals(p1);
    }
    
    private void fireSpecificGun(int id, boolean sneaking, boolean sprinting, Player player) {
    	Gun gun = Gun.getGunByModelId(id);
    	PlayerData data = state.getData(player);
    	if ((data.cooldown + gun.cooldown) < player.getWorld().getGameTime()) {
        	//player.setCooldown(Material.NETHERITE_HOE, 1);
    		data.cooldown = player.getWorld().getGameTime();
        	double spray = gun.sprayNormal;
    		if (sneaking) {
    			spray = gun.sprayCrouch;
    		}
    		if (sprinting) {
    			spray = gun.spraySprint;
    		}
    		fireTheGun(player, gun.range, gun.radius, gun.damage, spray);
    	}
    }
    
    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {
    	if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && 
    	   	 e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NETHERITE_HOE)) {
    		ItemMeta itemMeta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
    		if (!itemMeta.hasCustomModelData()) return;
    		int modelId = itemMeta.getCustomModelData();
    		fireSpecificGun(modelId, e.getPlayer().isSneaking(), e.getPlayer().isSprinting(), e.getPlayer());
    		e.setCancelled(true);
    	} else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_INGOT) && currentBomb == null) {
    		Location l = e.getClickedBlock().getLocation().add(0, 1, 0);
    		Location l2 = l.add(0.5, 0, 0.5);
    		double d = 2.1;
    		if ((Math.abs(l2.getX() - state.arena.bombA.getX()) < d &&
    			 Math.abs(l2.getZ() - state.arena.bombA.getZ()) < d) ||
    			(Math.abs(l2.getX() - state.arena.bombB.getX()) < d &&
    			 Math.abs(l2.getZ() - state.arena.bombB.getZ()) < d)) {
        		ItemFrame entity = (ItemFrame)l.getWorld().spawnEntity(l, EntityType.ITEM_FRAME);
        		entity.setVisible(false);
        		ItemStack stack = new ItemStack(Material.IRON_INGOT, 1);
        		ItemMeta meta = stack.getItemMeta();
        		meta.setCustomModelData(1);
        		stack.setItemMeta(meta);
        		entity.setItem(stack);
        		e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
        		currentBomb = new Bomb(l, entity);
    		} else {
    			l.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, l, 10);
    			e.getPlayer().sendMessage("You can't place the bomb here!");
    		}
    	} else if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
    			   && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COPPER_INGOT)) {
    		gunSel.openInventory(e.getPlayer());
    	}
    }
    
    @EventHandler
    public void interactEntityEvent(PlayerInteractEntityEvent e) {
    	if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
    		e.setCancelled(true);
    		if (currentBomb != null) {
    			currentBomb.defuseAttempt(e.getPlayer().getWorld().getGameTime());
    		}
    	}
    }
    
    @EventHandler
    public void sneakEvent(PlayerToggleSneakEvent e) {
    	if (!e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SPYGLASS)) return;
    	ItemMeta itemMeta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
		if (!itemMeta.hasCustomModelData()) return;
		int modelId = itemMeta.getCustomModelData();
		
    	
    	if (!e.isSneaking()) {
    		fireSpecificGun(modelId, e.getPlayer().isHandRaised(), e.getPlayer().isSprinting(), e.getPlayer());
       	}
    	if (e.isSneaking()) {
    		e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Fire when shift released"));
    	}
    }
    
    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {
    	state.playerJoin(e.getPlayer());
    }
    
    @EventHandler
    public void leaveEvent(PlayerQuitEvent e) {
    	state.playerLeave(e.getPlayer());
    }
}