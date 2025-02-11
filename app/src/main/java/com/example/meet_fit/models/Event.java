package com.example.meet_fit.models;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public class Event {

        private String activity;
        private String fitLevel;
        private String aboutEvent;
        private String location;
        private Date date;
        private LocalTime time;
        private List<String> participants;
        private String uId;
        private String eventId;


    public String getUserNmae() {
        return uId;
    }

    public void setUserNmae(String userNmae) {
        this.uId = uId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Event(String activity, LocalTime time, String fitLevel, String aboutEvent, String location, Date date) {
        this.activity = activity;
        this.time = time;
        this.fitLevel = fitLevel;
        this.aboutEvent = aboutEvent;
        this.location = location;
        this.date = date;
    }

    public Event(String activity, LocalTime time, String fitLevel, String aboutEvent, String location, Date date, List<String>participants,String uId , String eventId) {
        this.activity = activity;
        this.time = time;
        this.fitLevel = fitLevel;
        this.aboutEvent = aboutEvent;
        this.location = location;
        this.date = date;
        this.participants=participants;
        this.uId=uId;
        this.eventId=eventId;
    }
    public Event()
    {

    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAboutEvent() {
        return aboutEvent;
    }

    public void setAboutEvent(String aboutEvent) {
        this.aboutEvent = aboutEvent;
    }

    public String getFitLevel() {
        return fitLevel;
    }

    public void setFitLevel(String fitLevel) {
        this.fitLevel = fitLevel;
    }
}
