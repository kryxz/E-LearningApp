package com.myclass.school.data;


import java.util.UUID;

public class Notification {
    private String title;
    private String message;
    private long date;
    private String id;
    private String classroomId;
    private NotificationType type;

    public Notification() {

    }

    public Notification(String title, String message, long date, String classroomId, NotificationType type) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.classroomId = classroomId;
        this.type = type;
        id = UUID.randomUUID().toString().substring(0, 10);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}

