package com.example.meet_fit.models;

import java.util.List;

public class Info {
    private String userName;
    private List<String>activities;
    private String age ;
    private String fitLevel;
    private String aboutMe;
    private String location;
    private String phoneNumber;
    private String photo;

    public Info(String userName, List<String> activities, String age, String fitLevel,
                String aboutMe, String location, String phoneNumber, String photo) {
        this.userName = userName;
        this.activities = activities;
        this.age = age;
        this.fitLevel = fitLevel;
        this.aboutMe = aboutMe;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
    }

    public Info(List<String> activities, String age, String fitLevel, String aboutMe, String location, String photo) {
        this.activities = activities;
        this.age = age;
        this.fitLevel = fitLevel;
        this.aboutMe = aboutMe;
        this.location = location;
        this.photo = photo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Info() {
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getFitLevel() {
        return fitLevel;
    }

    public void setFitLevel(String fitLevel) {
        this.fitLevel = fitLevel;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
