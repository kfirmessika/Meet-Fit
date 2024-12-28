package com.example.meet_fit.models;

import java.util.List;

public class Info {
    private List<String>activities;
    private String age ;
    private String fitLevel;
    private String aboutMe;
    private String location;
    private String phoneNumber;


    public Info(List<String> activities, String phoneNumber, String location, String fitLevel, String aboutMe, String age) {
        this.activities = activities;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.fitLevel = fitLevel;
        this.aboutMe = aboutMe;
        this.age = age;
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
