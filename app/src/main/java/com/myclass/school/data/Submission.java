package com.myclass.school.data;

public class Submission {
    private String senderId; // who sent the file
    private ClassroomFile file; // file sent by student
    private String comment; // a comment sent with a submission

    public Submission() {

    }


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public ClassroomFile getFile() {
        return file;
    }

    public void setFile(ClassroomFile file) {
        this.file = file;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
