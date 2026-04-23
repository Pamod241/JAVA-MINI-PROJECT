package model;

import java.time.LocalDate;

public class Attendance {
    private String    attId;
    private LocalDate date;
    private String    attState;    // Present / Absent
    private String    sessionType; // Lecture / Practical
    private int       hour;
    private String    studentId;
    private String    medicalId;   // nullable
    private String    courseCode;

    public Attendance(String attId, LocalDate date, String attState, String sessionType,
                      int hour, String studentId, String medicalId, String courseCode) {
        this.attId       = attId;
        this.date        = date;
        this.attState    = attState;
        this.sessionType = sessionType;
        this.hour        = hour;
        this.studentId   = studentId;
        this.medicalId   = medicalId;
        this.courseCode  = courseCode;
    }

    public String    getAttId()       { return attId; }
    public LocalDate getDate()        { return date; }
    public String    getAttState()    { return attState; }
    public String    getSessionType() { return sessionType; }
    public int       getHour()        { return hour; }
    public String    getStudentId()   { return studentId; }
    public String    getMedicalId()   { return medicalId; }
    public String    getCourseCode()  { return courseCode; }
    public boolean   isPresent()      { return "Present".equals(attState); }
}
