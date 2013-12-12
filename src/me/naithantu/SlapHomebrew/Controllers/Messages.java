package me.naithantu.SlapHomebrew.Controllers;

import java.util.HashMap;

public class Messages {
    private HashMap<String, MessageFactory> messagePlayers = new HashMap<String, MessageFactory>();

    public Messages() {
    }

    public HashMap<String, MessageFactory> getMessagePlayers(){
        return messagePlayers;
    }
}
