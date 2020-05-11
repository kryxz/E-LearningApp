package com.myclass.school.data;

import com.myclass.school.CommonUtils;

import java.util.ArrayList;

public class Teacher implements User {


    private String subject;
    private String name;
    private String id;
    private ArrayList<String> classes = new ArrayList<>();
    private String password;
    private String photoUrl;
    private boolean isOnline;


    public Teacher() {

    }


    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public String getEmail() {
        return id + CommonUtils.EMAIL_SUFFIX;
    }


    @Override
    public void addToClass(String id) {
        if (!classes.contains(id))
            classes.add(id);
    }

    @Override
    public void removeFromClass(String id) {
        classes.remove(id);

    }

    @Override
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setSubjectGrade(String s) {
        setSubject(s);
    }

    public ArrayList<String> getClasses() {
        return classes;
    }


    public void setClasses(ArrayList<String> classes) {
        this.classes = classes;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getSubject() {
        return subject;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }


}
