package com.cospox.csgoplugin;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;

public class GameState implements CommandExecutor {
	public ArrayList<PlayerData> t     = new ArrayList<PlayerData>();
	public ArrayList<PlayerData> ct    = new ArrayList<PlayerData>();
	public ArrayList<PlayerData> total = new ArrayList<PlayerData>();
	Arena arena;
	private Main plugin;
	public Bomb bomb;
	public int timeoutSeconds = -1;
	public int tWins = 0;
	public int ctWins = 0;
	public int totalRounds = 0;
	public static final int maxRounds = 10;
	//private Scoreboard board;
	public GameState(Arena arena, Main plugin) {
		this.arena = arena;
		//this.board = plugin.getServer().getScoreboardManager().getNewScoreboard();
		//Objective o = board.registerNewObjective("", "hjkl", "bnm");
		//o.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	public void run() {
        		if (timeoutSeconds > 0) timeoutSeconds -= 1;
        		if (timeoutSeconds == 0) { ctWin(); timeoutSeconds = -1; }
        	}
        }, 0, 20);
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	public void run() {
        		updateReload();
        	}
        }, 0, 1);
	}
	
	public void updateReload() {
		for (PlayerData pd : total) {
			if (pd.reloadCooldown > 0) {
				pd.reloadCooldown -= 1;
	    		pd.p.setExp((float)pd.reloadCooldown / (float)pd.maxCooldown);
			} else if (pd.reloadCooldown == 0){
				pd.reloadCooldown = -1;
				//pd.reload();
			}
		}
	}
	
	public void playerJoin(Player p) {
		p.setHealth(20);
		p.setGameMode(GameMode.ADVENTURE);
		total.add(new PlayerData(p));
		p.getInventory().clear();
		ItemStack selector = new ItemStack(Material.COPPER_INGOT, 1);
		ItemMeta meta = selector.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "Weapon Selector");
		meta.setCustomModelData(1);
		selector.setItemMeta(meta);
		p.getInventory().addItem(selector);
		p.teleport(arena.lobby);
		p.setBedSpawnLocation(arena.spawn);
		//p.setScoreboard(board);
	}
	
	public void giveArmour(Player p, Team t) {
		Color armourColour = t.equals(Team.TERRORIST) ? Color.fromRGB(0xc05f31) : Color.fromRGB(0x05057f);
		
		ItemStack head = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack body = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack feet = new ItemStack(Material.LEATHER_BOOTS, 1);
		
		setArmourMeta(head, armourColour);
		setArmourMeta(body, armourColour);
		setArmourMeta(legs, armourColour);
		setArmourMeta(feet, armourColour);
		p.getInventory().setBoots(feet);
		p.getInventory().setChestplate(body);
		p.getInventory().setLeggings(legs);
		p.getInventory().setHelmet(head);
	}
	
	public void setArmourMeta(ItemStack item, Color col) {
		ItemMeta meta = item.getItemMeta();
		LeatherArmorMeta ameta = (LeatherArmorMeta)meta;
		ameta.setColor(col);
		item.setItemMeta(ameta);
	}
	
	public void playerLeave(Player p) {
		removePlayer(total, p);
		removePlayer(t, p);
		removePlayer(ct, p);
	}
	
	public void playerDie(Player p) {
		getData(p).alive = false;
		p.setGameMode(GameMode.SPECTATOR);
		//check t and ct teams for alive-ness
		if (!isTeamAlive(t)) { if (bomb == null) { ctWin(); } }
		if (!isTeamAlive(ct)) tWin();
	}
	
	public boolean isTeamAlive(ArrayList<PlayerData> team) {
		boolean anyoneAlive = false;
		for (PlayerData d : team ) {
			if (d.alive) { anyoneAlive = true; break; }
		}
		return anyoneAlive;
	}

	public void resetGame() {
		total.clear();
		t.clear();
		ct.clear();
		timeoutSeconds = -1;
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			playerJoin(p);
		}
	}
	
	public void tWin() {
		if (bomb != null) {
			bomb.cancelTimers();
			bomb = null;
		}
		resetGame();
		for (PlayerData d : total) {
			d.p.sendTitle("" + ChatColor.RED + ChatColor.BOLD + "Terrorists" + ChatColor.RESET + " win!", null, 5, 60, 5);
		}
	}
	
	public void ctWin() {
		if (bomb != null) {
			bomb.cancelTimers();
			bomb = null;
		}
		resetGame();
		for (PlayerData d : total) {
			d.p.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "Counter terrorists" + ChatColor.RESET + " win!", null, 5, 40, 5);
		}
	}
			
	public void bombExplode() {
		bomb.cancelTimers();
		bomb = null;
		tWin();
	}
	
	public void bombDefuse() {
		bomb.cancelTimers();
		bomb = null;
		ctWin();
	}
	
	//TODO use hashmaps instead of linear searches if this becomes a problem (next 2 methods)
	private void removePlayer(ArrayList<PlayerData> data, Player p) {
		PlayerData toRemove = null;
		for (PlayerData d : data) {
			if (d.p == p) {
				toRemove = d;
				break;
			}
		}
		if (toRemove != null) {
			data.remove(toRemove);
		}
	}
	
	public PlayerData getData(Player p) {
		for (PlayerData d : total) {
			if (d.p == p) {
				return d;
			}
		}
		return null;
	}
	
	public void assignTeams() {
		for (PlayerData d : total) {
			if (d.preferredTeam == null) {
				if (t.size() > ct.size()) {
					ct.add(d);
					d.assignedTeam = Team.COUNTERTERRORIST;
					d.p.sendMessage("Automatically assigning you to the " + ChatColor.GREEN + ChatColor.BOLD + "Counter Terrorists");
				} else {
					t.add(d);
					d.assignedTeam = Team.TERRORIST;
					d.p.sendMessage("Automatically assigning you to the " + ChatColor.RED + ChatColor.BOLD + "Terrorists");
				}
			} else if (d.preferredTeam == Team.TERRORIST) {
				t.add(d);
				d.assignedTeam = Team.TERRORIST;
			} else if (d.preferredTeam == Team.COUNTERTERRORIST) {
				ct.add(d);
				d.assignedTeam = Team.COUNTERTERRORIST;
			}
			d.assignDefaultWeapons();
		}
		
		ItemStack knife = new ItemStack(Material.WOODEN_SWORD, 1);
		ItemMeta kmeta = knife.getItemMeta();
		kmeta.setCustomModelData(1);
		kmeta.setUnbreakable(true);
		kmeta.setDisplayName(ChatColor.RESET + "Knife");
		knife.setItemMeta(kmeta);
		
		for (PlayerData d : ct) {
			d.p.teleport(arena.ctSpawn);
			d.p.getInventory().clear();
			d.p.setGameMode(GameMode.ADVENTURE);
			giveArmour(d.p, Team.COUNTERTERRORIST);
			if (d.selectedGun != null) d.p.getInventory().addItem(d.selectedGun);
			d.p.getInventory().addItem(knife.clone());
			d.reloadWhatever();
		}

		for (PlayerData d : t) {
			d.p.teleport(arena.tSpawn);
			d.p.getInventory().clear();
			d.p.setGameMode(GameMode.ADVENTURE);
			giveArmour(d.p, Team.TERRORIST);
			if (d.selectedGun != null) d.p.getInventory().addItem(d.selectedGun);
			d.p.getInventory().addItem(knife.clone());
			d.reloadWhatever();
		}
		
    	Random rnd = ThreadLocalRandom.current();
		int pos = rnd.nextInt(t.size());
		ItemStack item = new ItemStack(Material.IRON_INGOT, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(1);
		meta.setDisplayName(ChatColor.RESET + "Bomb");
		item.setItemMeta(meta);
		t.get(pos).p.getInventory().addItem(item);
	}
	
	void startGame() {
		assignTeams();
		timeoutSeconds = 2 * 60;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if (name.equals("jointeam")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can do that!");
				return false;
			}
			Player p = (Player)sender;
			if (args[0].equals("t") || args[0].equals("terrorists") ) {
				getData(p).preferredTeam = Team.TERRORIST;
				getData(p).clearWeapons();
				sender.getServer().broadcastMessage(sender.getName() + " has joined the " + ChatColor.RED + ChatColor.BOLD + "terrorists");
			} else if (args[0].equals("ct") || args[0].equals("counterterrorists") ) {
				getData(p).preferredTeam = Team.COUNTERTERRORIST;
				getData(p).clearWeapons();
				sender.getServer().broadcastMessage(sender.getName() + " has joined the " + ChatColor.GREEN + ChatColor.BOLD + "counterterrorists");
			} else {
				sender.sendMessage("Unknown team \"" + args[0] + "\". Try using one of t, ct, terrorists, couunterterrorists");
			}
			return true;
		} else if (name.equals("startgame")) {
			startGame();
			return true;
		} else if (name.equals("reset")) {
			resetGame();
		}
		return true;
	}
	
	public Team getTeam(Player p) {
		return getData(p).assignedTeam;
	}
}