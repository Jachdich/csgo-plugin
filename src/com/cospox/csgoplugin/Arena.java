package com.cospox.csgoplugin;

import org.bukkit.Location;

public class Arena {
	public Location bombA, bombB, tSpawn, ctSpawn;
	public Arena(Location ba, Location bb, Location ts, Location cts) {
		bombA = ba;
		bombB = bb;
		tSpawn = ts;
		ctSpawn = cts;
	}
}
