package com.myclass.school.data;

import com.myclass.school.CommonUtils;

import java.util.ArrayList;

public class Student implements User {

    // dob, phone

    private String grade;
    private String name;
    private String id;
    private String password;
    private ArrayList<String> classes = new ArrayList<>();
    private boolean isOnline;

    private String photoUrl;

    @Override
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String getEmail() {
        return id + CommonUtils.EMAIL_SUFFIX;
    }

    public void setSubjectGrade(String g) {
        setGrade(g);
    }

    public Student() {

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

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }


    public void setClasses(ArrayList<String> classes) {
        this.classes = classes;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String g) {
        this.grade = g;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }


}
