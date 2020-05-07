package com.myclass.school.data;


public class ClassroomPost {

    private String id;
    private String senderId;
    private String author;
    private String content;

    private Long date;

    public ClassroomPost() {

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

        if (senderId.contains("@"))
            this.senderId = senderId.substring(0, senderId.indexOf('@'));
        else
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
