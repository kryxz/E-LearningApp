package com.myclass.school.data;

import java.util.ArrayList;

public interface User {
    String getEmail();

    String getPassword();

    String getName();

    String getPhotoUrl();

    boolean isOnline();

    void setName(String name);

    void setSubjectGrade(String s);


    ArrayList<String> getClasses();

    void addToClass(String id);

    void removeFromClass(String id);

}
