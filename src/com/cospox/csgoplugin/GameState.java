package com.cospox.csgoplugin;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameState implements CommandExecutor {
	public ArrayList<PlayerData> t     = new ArrayList<PlayerData>();
	public ArrayList<PlayerData> ct    = new ArrayList<PlayerData>();
	public ArrayList<PlayerData> total = new ArrayList<PlayerData>();
	public GameState() {
		
	}
	
	public void playerJoin(Player p) {
		total.add(new PlayerData(p));
	}
	
	public void playerLeave(Player p) {
		removePlayer(total, p);
		removePlayer(t, p);
		removePlayer(ct, p);
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
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
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
	}
}