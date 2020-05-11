package com.myclass.school.data;

import java.util.ArrayList;


/*
    data model for classrooms
    Contains:
    instructor id in database.
    name and description
    unique id in the database.
    members who are in this classroom
 */
public class Classroom {
    private String instructor;
    private String description;
    private String name;
    private String id;
    private ArrayList<String> members = new ArrayList<>();


    public Classroom() {

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

    public ArrayList<String> getMembers() {
        return members;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public void addMember(String email) {
        String id = email.substring(0, email.indexOf('@'));
        if (!members.contains(id))
            members.add(id);

    }

    public void removeMember(String email) {
        String id = email.substring(0, email.indexOf('@'));
        members.remove(id);
    }

    public boolean contains(String email) {
        String id = email.substring(0, email.indexOf('@'));

        return members.contains(id);

    }

}