package com.example.studentperformance.dao;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceDAO {

    class Attendance {
        private Integer studentId;
        private Integer subjectId;
        private LocalDate date;
        private Boolean present;

        public Attendance(int studentId, int subjectId, LocalDate date, boolean present) {
            this.studentId = studentId;
            this.subjectId = subjectId;
            this.date = LocalDate.from(date);
            this.present = present;
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

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = LocalDate.from(date);
        }

        public Boolean getPresent() {
            return present;
        }

        public void setPresent(Boolean present) {
            this.present = present;
        }

        public boolean isPresent(){
            return present;
        }
        
    }

    void createAttendance(int studentId, int subjectId, LocalDate date, boolean present) throws Exception;

    Attendance readAttendance(int studentId, int subjectId, LocalDate date) throws Exception;

    void updateAttendance(int studentId, int subjectId, LocalDate date, boolean present) throws Exception;

    void deleteAttendance(int studentId, int subjectId, LocalDate date) throws Exception;

    List<Attendance> getAllAttendance() throws Exception;
}