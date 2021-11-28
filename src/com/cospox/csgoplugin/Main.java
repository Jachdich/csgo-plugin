package com.cospox.csgoplugin;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.bukkit.Difficulty;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

//TODO testing
//TODO global jointeam messages
//TODO when someone dies u sometimes get their shit
//TODO clear inv when die
//TODO automatically set gamemode to peaceful
//TODO XP ammo counter, bar reload
//TODO bomb has been planted
//TODO gun fire cooldown animation: ender pearl cooldown
//TODO bomb still goes off even tho people die
//TODO guns should come loaded
//TODO disable friendly fire



//TODO rounds
//TODO score
//TODO fix num of rounds
//TODO new round 3 seconds after winning
//after every round u get moeny if you win or kill
//switch sides every time??
//time to get weapons and buy shit idk


//TODO awp shoot cooldown animation
//TODO different guns for t and ct
//TODO default knife
//TODO sound effects, bomb, dying, shooting
	//done ish: shooting
public class Main extends JavaPlugin implements Listener {
	private GunSelGUI gunSel;
	private TeamSelGUI teamSel;
	GameState state;
	private static final boolean FRIENDLY_FIRE_DISABLED = true;
    @Override
    public void onEnable() {
        Arena arena = new Arena(
        		new Location(this.getServer().getWorlds().get(0), 6.5, 111, -123.5),
        		new Location(this.getServer().getWorlds().get(0), -47.5, 110, -125.5),
        		new Location(this.getServer().getWorlds().get(0), -32.5, 114, -52.5),
        		new Location(this.getServer().getWorlds().get(0), -10, 107, -123),
        		new Location(this.getServer().getWorlds().get(0), -84.5, 100, -4.5),
        		new Location(this.getServer().getWorlds().get(0), -23, 131, -98) //TODO get the real one
        );
        
        state = new GameState(arena, this);
        this.gunSel = new GunSelGUI(state);
        this.teamSel = new TeamSelGUI(state);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(gunSel, this);
        getServer().getPluginManager().registerEvents(teamSel, this);
        this.getCommand("jointeam").setExecutor(state);
        this.getCommand("startgame").setExecutor(state);
        this.getCommand("reset").setExecutor(state);
        getServer().getWorlds().get(0).setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        getServer().getWorlds().get(0).setGameRule(GameRule.DO_TILE_DROPS, false);
        getServer().getWorlds().get(0).setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        getServer().getWorlds().get(0).setGameRule(GameRule.NATURAL_REGENERATION, false);
        getServer().getWorlds().get(0).setGameRule(GameRule.DO_ENTITY_DROPS, false);
        getServer().getWorlds().get(0).setGameRule(GameRule.KEEP_INVENTORY, true);
        getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);
        
        for (Player p: getServer().getOnlinePlayers()) {
        	state.playerJoin(p);
        }
    }
    
    @Override
    public void onDisable() {
    }
    
    public void fireTheGun(Player p, int dist, double radius, int damage, double spread) {
    	World w = p.getWorld();
    	
    	//this absolute mess of weird maths that I dont understand
    	//makes the gun fire in a slightly different (and random) direction
    	//to the player's look direction
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
		w.playSound(p.getEyeLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.BLOCKS, 100, 0.5f);
    	if (ray != null) {
    		Entity hit = ray.getHitEntity();
    		if (hit != null/* && hit.getType() == EntityType.PLAYER*/) {
    			//Player hitPlayer = (Player)hit;
    			//hitPlayer.damage(damage);
    			LivingEntity e = ((LivingEntity)hit);
    			if (e instanceof Player) {
    				Player phit = (Player)e;
    				if (state.getTeam(phit) == state.getTeam(p) && FRIENDLY_FIRE_DISABLED) {
    					return;
    				}
    			}
    			e.damage(damage);
    			//hitPlayer.setVelocity(new Vector(0, 1, 0));
    		} else {
    			//ray.getHitBlock().setType(Material.DIAMOND_BLOCK);
    		}
    	}
    }
    
    private static Predicate<Entity> humanPredicate(Player p1) {
    	return p -> /*p.getType() == EntityType.PLAYER && */!p.equals(p1);
    }
    
    private void fireSpecificGun(int id, boolean sneaking, boolean sprinting, Player player) {
    	Gun gun = Gun.getGunByModelId(id);
    	PlayerData data = state.getData(player);
    	if ((data.cooldown + gun.cooldown) < player.getWorld().getGameTime() && data.rounds > 0) {
    		data.cooldown = player.getWorld().getGameTime();
        	double spray = gun.sprayNormal;
    		if (sneaking) {
    			spray = gun.sprayCrouch;
    		}
    		if (sprinting) {
    			spray = gun.spraySprint;
    		}
    		fireTheGun(player, gun.range, gun.radius, gun.damage, spray);
    		data.rounds -= 1;
    		player.setLevel(data.rounds);
    		player.setCooldown(Material.NETHERITE_HOE, Gun.getGunByModelId(id).cooldown);
    		player.setCooldown(Material.SPYGLASS, Gun.getGunByModelId(id).cooldown);
    	}
    }
    
    private boolean isGun(ItemStack item) {
    	if (item.getType() != Material.NETHERITE_HOE && item.getType() != Material.SPYGLASS) return false;
    	ItemMeta meta = item.getItemMeta();
    	if (!meta.hasCustomModelData()) return false;
    	if (meta.getCustomModelData() > 0) return true;
    	return false;
    }
    
    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {
    	//if the player right clicked with a hoe
    	if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && 
    	   	 e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NETHERITE_HOE)) {
    		ItemMeta itemMeta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
    		
    		//ignore regular hoe that doesnt do anything
    		if (!itemMeta.hasCustomModelData()) return;
    		
    		PlayerData pd = state.getData(e.getPlayer());
    		//can't fire while reloading
    		if (pd.reloadCooldown != -1) return;
    		
    		int modelId = itemMeta.getCustomModelData();
    		fireSpecificGun(modelId, e.getPlayer().isSneaking(), e.getPlayer().isSprinting(), e.getPlayer());
    		e.setCancelled(true);

    		//ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
            //meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("AttackSpeed", -1, Operation.ADD_NUMBER));
            //e.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);

    	//if the player right clicked with the bomb (iron ingot)
    	} else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
    			   e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_INGOT)
    			   && state.bomb == null) {
    		e.getPlayer().sendMessage("something");
    		//block above clicked block because the item frame goes on top
    		Location l = e.getClickedBlock().getLocation().add(0, 1, 0);
    		//get middle of block
    		Location l2 = l.add(0.5, 0, 0.5);
    		
    		if ((Math.abs(l2.getX() - state.arena.bombA.getX()) < 2.1 &&
    			((l2.getZ() - state.arena.bombA.getZ() < 3.1) ||
    			(l2.getZ() - state.arena.bombA.getZ() > -2.1))) ||
    			(Math.abs(l2.getX() - state.arena.bombB.getX()) < 3.1 &&
    			 Math.abs(l2.getZ() - state.arena.bombB.getZ()) < 2.1)) {
    			//spawn invisible item frame to put the bomb in
        		ItemFrame entity = (ItemFrame)l.getWorld().spawnEntity(l, EntityType.ITEM_FRAME);
        		entity.setVisible(false);
        		//make the bomb using model data of 1
        		ItemStack stack = new ItemStack(Material.IRON_INGOT, 1);
        		ItemMeta meta = stack.getItemMeta();
        		meta.setCustomModelData(1);
        		stack.setItemMeta(meta);
        		entity.setItem(stack);
        		//remove one from the number of bombs in the player's inventory
        		//this will usually set the amount to zero
        		e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
        		state.bomb = new Bomb(l, entity, this);
        		getServer().broadcastMessage("The bomb has been planted");
        		for (PlayerData pd : state.total) {
        			pd.p.sendTitle("", "The bomb has been planted", 20, 20, 20);
        		}
    		} else {
    			l.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, l, 10);
    			e.getPlayer().sendMessage("You can't place the bomb here!");
    		}
    	//clicked using the gun selector item
    	} else if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
    			   && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.PAPER)) {
    		Team t = state.getData(e.getPlayer()).getPossibleTeam();
    		if (t == null) {
    			e.getPlayer().sendMessage("Lolwut you shouldn't be able to see this message, so there's some kind of issue. Try selecting a team again");
    		} else {
    			gunSel.openInventory(e.getPlayer(), t);
    		}
    	//clicked using the team selector item
    	} else if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
    			   && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GOLD_INGOT)) {
    		teamSel.openInventory(e.getPlayer());
    	}
    	//left click using gun, reload
    	else if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) &&
    			  isGun(e.getPlayer().getInventory().getItemInMainHand())) {
    		int modelId = e.getItem().getItemMeta().getCustomModelData();
    		state.getData(e.getPlayer()).reload(Gun.getGunByModelId(modelId));
    	}
    }
    
    @EventHandler
    public void interactEntityEvent(PlayerInteractEntityEvent e) {
    	if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
    		e.setCancelled(true);
    		if (state.bomb != null) {
    			state.bomb.defuseAttempt(e.getPlayer().getWorld().getGameTime());
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
    		PlayerData pd = state.getData(e.getPlayer());
    		//can't fire while reloading
    		if (pd.reloadCooldown != -1) return;
    		fireSpecificGun(modelId, e.getPlayer().isHandRaised(), e.getPlayer().isSprinting(), e.getPlayer());
       	}
    	if (e.isSneaking()) {
    		e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Fire when shift released"));
    	}
    }
    
    @EventHandler
    public void itemPickupEvent(EntityPickupItemEvent e) {
    	//this whole thing makes it so that only the t side can pick up the bomb
    	if (e.getEntity() instanceof Player) {
    		Player p = (Player)e.getEntity();
    		ItemStack item = e.getItem().getItemStack();
    		//check the bomb is being picked up
    		if (item.getType().equals(Material.IRON_INGOT) &&
    			item.getItemMeta().hasCustomModelData() &&
    			item.getItemMeta().getCustomModelData() == 1 &&
    			state.getData(p).assignedTeam.equals(Team.COUNTERTERRORIST)) {
    			e.setCancelled(true);
    		}
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
    
    @EventHandler
    public void dieEvent(PlayerDeathEvent e) {
    	state.playerDie(e.getEntity());
    }
    
    @EventHandler
    public void damageEvent(EntityDamageEvent e) {
    	if (e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)e).getDamager() instanceof Player) {
    		 Player damager = (Player)((EntityDamageByEntityEvent)e).getDamager();
    		 if (!damager.getInventory().getItemInMainHand().getType().equals(Material.WOODEN_SWORD)) {
    			 e.setCancelled(true);
    		 }
    	}
    }
}