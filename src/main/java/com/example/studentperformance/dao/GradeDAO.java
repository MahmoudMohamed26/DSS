package com.example.studentperformance.dao;

import java.sql.Connection;
import java.util.List;

public interface GradeDAO {
    class Grade {
        private Integer studentId;
        private Integer subjectId;
        private Double grade;

        public Grade(int studentId, int subjectId, double grade) {
            this.studentId = studentId;
            this.subjectId = subjectId;
            this.grade = grade;
        }

        public Integer getStudentId() {
            return studentId;
        }

        public void setStudentId(Integer studentId) {
            this.studentId = studentId;
        }

        public Integer getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Integer subjectId) {
            this.subjectId = subjectId;
        }

        public Double getGrade() {
            return grade;
        }

        public void setGrade(Double grade) {
            this.grade = grade;
        }
    }
    
    Connection getConnection();

    void createGrade(int studentId, int subjectId, double grade) throws Exception;
    Grade readGrade(int studentId,int subjectId) throws Exception;
    void updateGrade(int studentId, int subjectId, double newGrade) throws Exception;
    void deleteGrade(int studentId,int subjectId) throws Exception;
    List<Grade> getAllGrades() throws Exception;
}