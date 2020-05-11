package com.myclass.school.data;


/*
    A simple model for chat messages
    Contains message text and date, plus who sent that message.
 */
public class Message {

    private String content;
    private String sender;
    private Long date;

    public Message() {

    }

    public Message(String content, String sender, Long date) {
        this.content = content;
        this.sender = sender;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
