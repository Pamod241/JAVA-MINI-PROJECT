package model;

public class Mark {
    private String markId;
    private int    quiz1, quiz2, quiz3, assessment, mid, end;
    private String studentId;
    private String courseCode;

    public Mark(String markId, int quiz1, int quiz2, int quiz3,
                int assessment, int mid, int end,
                String studentId, String courseCode) {
        this.markId     = markId;
        this.quiz1      = quiz1;
        this.quiz2      = quiz2;
        this.quiz3      = quiz3;
        this.assessment = assessment;
        this.mid        = mid;
        this.end        = end;
        this.studentId  = studentId;
        this.courseCode = courseCode;
    }

    public String getMarkId()     {

        return markId;

    }
    public int    getQuiz1()      {

        return quiz1;

    }
    public int    getQuiz2()      {


        return quiz2;


    }
    public int    getQuiz3()      {

        return quiz3;

    }
    public int    getAssessment() {

        return assessment;
    }
    public int    getMid()        {

        return mid;
    }
    public int    getEnd()        {

        return end;
    }
    public String getStudentId()  {

        return studentId;
    }
    public String getCourseCode() {

        return courseCode;


    }

    public double getCAMark() {


        return (quiz1 + quiz2 + quiz3 + assessment + mid) / 5.0;
    }

    public int getFinalMark() {

        return end; }
}
