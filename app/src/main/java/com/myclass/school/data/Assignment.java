package com.myclass.school.data;

import java.util.ArrayList;

// a data model to represent an assignment
public class Assignment {
    private String title;
    private String content; // assignment description
    private String classroomId; // class of assignment
    private String id; // identifier
    private String classroomName;
    private Long date; // open date
    private Long dueDate; // close date

    private ClassroomFile file; // attached file
    private ArrayList<Submission> submissions = new ArrayList<>(); // student submissions

    public Assignment() {

    }


    public String getClassroomName() {
        return classroomName;
    }

    public Assignment(String title, String content, Long date, Long dueDate, ClassroomFile file) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.dueDate = dueDate;
        this.file = file;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public ClassroomFile getFile() {
        return file;
    }

    public void setFile(ClassroomFile file) {
        this.file = file;
    }

    public ArrayList<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(ArrayList<Submission> submissions) {
        this.submissions = submissions;
    }


    // returns true if student has already submitted!
    public boolean containsSubmission(String id) {
        for (Submission submission : submissions)
            if (submission.getSenderId().equals(id))
                return true;
        return false;
    }

    // returns submission download url
    public String downloadFile(String id) {
        ClassroomFile file = null;
        for (Submission submission : submissions)
            if (submission.getSenderId().equals(id))
                file = submission.getFile();
        return file.getDownloadUrl();
    }
}
