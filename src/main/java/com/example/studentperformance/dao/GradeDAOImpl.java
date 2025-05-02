package com.example.studentperformance.dao;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.studentperformance.dao.StudentDAO.Student;

public class GradeDAOImpl extends AbstractDAO implements GradeDAO{

    public GradeDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public void createGrade(int studentId, int subjectId, double gradeValue) throws Exception {
        String sql = "INSERT INTO Grades (student_id,subject_id,grade_value) VALUES (?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            pstmt.setDouble(3, gradeValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error creating grade: " + e.getMessage(), e);
        }
    }

    @Override
    public GradeDAO.Grade readGrade(int studentId, int subjectId) throws Exception {
        String sql = "SELECT student_id,subject_id,grade_value FROM Grades WHERE student_id = ? and subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    
                    double gradeValue = rs.getDouble("grade_value");
                    return new GradeDAO.Grade(studentId, subjectId, gradeValue);
                 } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error reading grade: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateGrade(int studentId, int subjectId, double newGradeValue) throws Exception {
        String sql = "UPDATE Grades SET grade_value = ? WHERE student_id=? and subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, newGradeValue);
            pstmt.setInt(2, studentId);pstmt.setInt(3, subjectId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error updating grade: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteGrade(int studentId, int subjectId) throws Exception {
        String sql = "DELETE FROM Grades WHERE student_id = ? and subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
           pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error deleting grade: " + e.getMessage(), e);
        }
    }

    @Override
    public List<GradeDAO.Grade> getAllGrades() throws Exception {
        List<GradeDAO.Grade> grades = new ArrayList<>();
        String sql = "SELECT grade_id,student_id, subject_id, grade_value FROM Grades";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                int subjectId = rs.getInt("subject_id");
               double gradeValue = rs.getDouble("grade_value");grades.add(new GradeDAO.Grade(studentId, subjectId, gradeValue));
            }
        } catch (SQLException e) {
            throw new Exception("Error getting all grades: " + e.getMessage(), e);
        }
        return grades;
    }
    @Override
    public Connection getConnection() {
        return connection;
    }
    
	public void addGrade(Student student, int subjectId, double gradeValue) throws Exception{
    	String sql = "INSERT INTO Grades (student_id, subject_id, grade_value) VALUES (?, ?, ?)";
    	
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)){
    		pstmt.setInt(1, student.getStudentId());
    		pstmt.setInt(2, subjectId);
    		pstmt.setDouble(3, gradeValue);
    		
    		pstmt.executeUpdate();
    	}catch (SQLException e) {
            throw new Exception("Error adding grade: " + e.getMessage(), e);
        }
	}
}