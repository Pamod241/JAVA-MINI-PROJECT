package model;

import java.time.LocalTime;

public class Timetable {
    private String  ttId;
    private String  depId;
    private String  dayOfWeek;
    private String  cCode;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String  sessionType;

    public Timetable(String ttId, String depId, String dayOfWeek, String cCode,
                     LocalTime startTime, LocalTime endTime, String location, String sessionType) {
        this.ttId  = ttId;
        this.depId   = depId;
        this.dayOfWeek   = dayOfWeek;
        this.cCode = cCode;
        this.startTime   = startTime;
        this.endTime  = endTime;
        this.location  = location;
        this.sessionType = sessionType;
    }

    public String getTimetableId() {
        return ttId;
    }
    public String getDepId(){
        return depId;
    }
    public String getDepartment(){
        return depId;
    }
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    public String getCourseId() {
        return cCode;
    }
    public LocalTime getStartTime(){
        return startTime;
    }
    public LocalTime getEndTime(){
        return endTime;

    }
    public String getLocation(){
        return location;
    }
    public String getSessionType(){
        return sessionType;

    }
}
