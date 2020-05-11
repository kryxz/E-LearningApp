package com.myclass.school.data;


/*
    Represents a chat in Chats Screen.
    Contains an id for the other user, their name, and the last message sent.
 */
public class Chat {
    private String id;
    private String name;
    private String userId;
    private Message lastMessage;

    public Chat() {

    }

    public Chat(String chatId, Message msg) {
        lastMessage = msg;
        id = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

