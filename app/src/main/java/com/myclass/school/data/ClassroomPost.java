package com.myclass.school.data;


/*
    A model for classroom posts (messages)
    Contains sender name, their id, content, date of message
 */
public class ClassroomPost {

    private String id;
    private String senderId;
    private String author;
    private String content;

    private Long date;

    private boolean isMention;

    public ClassroomPost() {

    }

    public ClassroomPost(String postId, String sId, String text, String sender, Long d) {
        id = postId;
        senderId = sId;
        author = sender;
        content = text;
        date = d;
    }

    public boolean isMention() {
        return isMention;
    }

    public void setMention(boolean mention) {
        isMention = mention;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {

        this.senderId = senderId;

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
