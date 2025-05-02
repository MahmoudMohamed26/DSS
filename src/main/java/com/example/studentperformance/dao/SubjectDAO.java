package com.example.studentperformance.dao;

import java.util.List;

public interface SubjectDAO {
    class Subject {
        private Integer subjectId;
        private String name;

        public Subject(Integer subjectId, String name) {
            this.subjectId = subjectId;
            this.name = name;
        }

        public Integer getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Integer subjectId) {
            this.subjectId = subjectId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    void createSubject(String name) throws Exception;
    Subject readSubject(int subjectId) throws Exception;
    void updateSubject(int subjectId, String newName) throws Exception;
    void deleteSubject(int subjectId) throws Exception;
    List<Subject> getAllSubjects() throws Exception;
}