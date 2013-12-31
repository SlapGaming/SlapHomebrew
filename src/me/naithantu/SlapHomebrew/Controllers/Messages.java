package me.naithantu.SlapHomebrew.Controllers;

import java.util.HashMap;

public class Messages extends AbstractController {
    
	private HashMap<String, MessageFactory> messagePlayers = new HashMap<String, MessageFactory>();

    public HashMap<String, MessageFactory> getMessagePlayers(){
        return messagePlayers;
    }

	@Override
	public void shutdown() {
		//Not needed
	}
}
