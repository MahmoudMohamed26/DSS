package com.example.studentperformance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class AttendanceDAOImpl extends AbstractDAO implements AttendanceDAO {

    public AttendanceDAOImpl(Connection connection) {
        super(connection);
    }

    @Override
    public void createAttendance(int studentId, int subjectId, LocalDate date, boolean present) throws Exception {
        String sql = "INSERT INTO Attendance (student_id, subject_id, date, present) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            // Store date as string in ISO format (YYYY-MM-DD)
            pstmt.setString(3, date.toString());
            pstmt.setBoolean(4, present);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error creating attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public AttendanceDAO.Attendance readAttendance(int studentId, int subjectId, LocalDate date) throws Exception {
        String sql = "SELECT student_id, subject_id, date, present FROM Attendance WHERE student_id = ? AND subject_id = ? AND date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            // Use string representation for the query
            pstmt.setString(3, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean present = rs.getBoolean("present");
                    // No need to parse date here as we already have it
                    return new Attendance(studentId, subjectId, date, present);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error reading attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAttendance(int studentId, int subjectId, LocalDate date, boolean present) throws Exception {
        String sql = "UPDATE Attendance SET present = ? WHERE student_id = ? AND subject_id = ? AND date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, present);
            pstmt.setInt(2, studentId);
            pstmt.setInt(3, subjectId);
            // Use string representation for the query
            pstmt.setString(4, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error updating attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAttendance(int studentId, int subjectId, LocalDate date) throws Exception {
        String sql = "DELETE FROM Attendance WHERE student_id = ? AND subject_id = ? AND date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            // Use string representation for the query
            pstmt.setString(3, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error deleting attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Attendance> getAllAttendance() throws Exception {
        String sql = "SELECT student_id, subject_id, date, present FROM Attendance";
        List<Attendance> attendances = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                int subjectId = rs.getInt("subject_id");
                // Get date as string and parse to LocalDate
                String dateStr = rs.getString("date");
                LocalDate date = LocalDate.parse(dateStr);
                boolean present = rs.getBoolean("present");
                attendances.add(new AttendanceDAO.Attendance(studentId, subjectId, date, present));
            }
        } catch (SQLException e) {
            throw new Exception("Error getting all attendance: " + e.getMessage(), e);
        }
        return attendances;
    }
}