package model;

import java.time.LocalDate;

public class Medical {
    private String    medicalId;
    private String    description;
    private LocalDate sDate;
    private LocalDate eDate;
    private String    studentId;

    public Medical(String medicalId, String description,
                   LocalDate sDate, LocalDate eDate, String studentId) {
        this.medicalId   = medicalId;
        this.description = description;
        this.sDate       = sDate;
        this.eDate       = eDate;
        this.studentId   = studentId;
    }

    public String    getMedicalId()   {
        return medicalId;
    }
    public String    getDescription() {

        return description;

    }

    public LocalDate getSDate()       {
        return sDate;
    }

    public LocalDate getEDate()       {

        return eDate;

    }
    public String    getStudentId()   {

        return studentId;
    }
}
