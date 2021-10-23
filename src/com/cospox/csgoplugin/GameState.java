package com.cospox.csgoplugin;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class GameState implements CommandExecutor {
	public ArrayList<PlayerData> t     = new ArrayList<PlayerData>();
	public ArrayList<PlayerData> ct    = new ArrayList<PlayerData>();
	public ArrayList<PlayerData> total = new ArrayList<PlayerData>();
	Arena arena;
	public GameState(Arena arena) {
		this.arena = arena;
	}
	
	public void playerJoin(Player p) {
		total.add(new PlayerData(p));
		System.out.println("Adding player " + p.getName());
	}
	
	public void playerLeave(Player p) {
		removePlayer(total, p);
		removePlayer(t, p);
		removePlayer(ct, p);
		System.out.println("Removing player " + p.getName());
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
			d.p.getServer().getPlayer("KingJellyfishII").sendMessage(d.p.getDisplayName());
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
		}
		
		for (PlayerData d : ct) {
			d.p.teleport(arena.ctSpawn);
			d.p.getInventory().clear();
		}

		for (PlayerData d : t) {
			d.p.teleport(arena.tSpawn);
			d.p.getInventory().clear();
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
				sender.sendMessage("Successfully joined the terrorists");
			} else if (args[0].equals("ct") || args[0].equals("counterterrorists") ) {
				getData(p).preferredTeam = Team.COUNTERTERRORIST;
				sender.sendMessage("Successfully joined the counterterrorists");
			} else {
				sender.sendMessage("Unknown team \"" + args[0] + "\". Try using one of t, ct, terrorists, couunterterrorists");
			}
			return true;
		} else if (name.equals("startgame")) {
			assignTeams();
			return true;
		}
		return true;
	}
}