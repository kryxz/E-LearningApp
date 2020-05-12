package com.myclass.school.data;

/*
    A model for a classroom file.
    Files can be uploaded by teachers, and by students as submissions
    Contains:
    author name, file name, url for file in database.
    file type, and a short description.
 */
public class ClassroomFile {
    private String id;
    private String author;
    private String name;
    private String downloadUrl;
    private String type;
    private String description;


    private Long date;

    public ClassroomFile() {

    }

    public ClassroomFile(String id, String author, String name, String type, String description, Long date) {
        this.id = id;
        this.author = author;
        this.name = name;
        this.type = type;
        this.description = description;
        this.date = date;
    }


    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

}
