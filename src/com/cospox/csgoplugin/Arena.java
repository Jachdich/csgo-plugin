package com.cospox.csgoplugin;

import org.bukkit.Location;

public class Arena {
	public Location bombA, bombB, tSpawn, ctSpawn, lobby, spawn;
	public Arena(Location ba, Location bb,
				Location ts, Location cts, Location l,
				Location spawn) {
		bombA = ba;
		bombB = bb;
		tSpawn = ts;
		ctSpawn = cts;
		lobby = l;
		this.spawn = spawn;
	}
}
