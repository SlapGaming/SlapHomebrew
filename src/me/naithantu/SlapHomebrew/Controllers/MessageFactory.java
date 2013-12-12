package me.naithantu.SlapHomebrew.Controllers;

public class MessageFactory {
    private String messageName;
    private String message = "";

    public MessageFactory(String messageName){
        this.messageName = messageName;
    }

    public String getMessageName(){
        return messageName;
    }

    public String getMessage(){
        return message;
    }

    public void addMessage(String message){
        this.message += message;
    }
}