package com.root.meetsync.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class EventForm {
    private String summary;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private String description;
    private boolean allDay;

    // Constructors
    public EventForm() {
    }

    public EventForm(String summary, String date, String startTime, String endTime,
                     String location, String description, boolean allDay) {
        this.summary = summary;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
        this.allDay = allDay;
    }

}