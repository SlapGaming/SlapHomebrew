package me.naithantu.SlapHomebrew;

import java.util.HashMap;

public class Sonic {
	HashMap<String, SonicPlayer> players = new HashMap<String, SonicPlayer>();

	public void addPlayer(String playerName) {
		players.put(playerName, new SonicPlayer(playerName));
	}

	public void addCheckpoint(String playerName, int checkpoint) {
		players.get(playerName).addCheckpoint(checkpoint);
	}
}
