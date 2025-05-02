package com.example.studentperformance.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class StudentDAOImpl extends AbstractDAO implements StudentDAO {

    public StudentDAOImpl(Connection connection) {
        super(connection);
    }
    @Override
    public void createStudent(String name) throws Exception {
        String sql = "INSERT INTO Students (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error creating student: " + e.getMessage(), e);
        }
    }

    @Override
    public StudentDAO.Student readStudent(int studentId) throws Exception {
        String sql = "SELECT name FROM Students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    return new StudentDAO.Student(studentId, name);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error reading student: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStudent(int studentId, String newName) throws Exception {
        String sql = "UPDATE Students SET name = ? WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error updating student: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteStudent(int studentId) throws Exception {
        String sql = "DELETE FROM Students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error deleting student: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> getAllStudents() throws Exception {
        String sql = "SELECT student_id, name FROM Students";
        List<Student> students = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                String name = rs.getString("name");
                students.add(new Student(studentId, name));
            }
        } catch (SQLException e) {
            throw new Exception("Error getting all students: " + e.getMessage(), e);
        }
        return students;
    }
}