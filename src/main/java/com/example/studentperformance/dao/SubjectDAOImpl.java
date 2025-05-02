package com.example.studentperformance.dao;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SubjectDAOImpl extends AbstractDAO implements SubjectDAO {
    private final Connection connection;

    public SubjectDAOImpl(Connection connection) {
        super(connection);
        this.connection = connection;
    }

    @Override
    public void createSubject(String name) throws Exception {
        String sql = "INSERT INTO Subjects (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error creating subject: " + e.getMessage(), e);
        }
    }

    @Override
    public SubjectDAO.Subject readSubject(int subjectId) throws Exception {
        String sql = "SELECT name FROM Subjects WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    return new SubjectDAO.Subject(subjectId, name);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error reading subject: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateSubject(int subjectId, String newName) throws Exception {
        String sql = "UPDATE Subjects SET name = ? WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, subjectId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error updating subject: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteSubject(int subjectId) throws Exception {
        String sql = "DELETE FROM Subjects WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error deleting subject: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SubjectDAO.Subject> getAllSubjects() throws Exception {
        List<SubjectDAO.Subject> subjects = new ArrayList<>();
        String sql = "SELECT subject_id, name FROM Subjects";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int subjectId = rs.getInt("subject_id");
                String name = rs.getString("name");
                subjects.add(new SubjectDAO.Subject(subjectId, name));
            }
        } catch (SQLException e) {
            throw new Exception("Error getting all subjects: " + e.getMessage(), e);
        }
        return subjects;
    }

}