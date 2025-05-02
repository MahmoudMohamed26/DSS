package com.example.studentperformance;

import java.sql.Connection;
import java.sql.Statement;

public class init_db {
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            

            
            // Create tables in correct order
            stmt.execute("CREATE TABLE Students (" +
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

            stmt.execute("CREATE TABLE Subjects (" +
                "subject_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

            stmt.execute("CREATE TABLE Grades (" +
                "grade_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER NOT NULL," +
                "subject_id INTEGER NOT NULL," +
                "grade_value REAL NOT NULL," +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE," +
                "FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE Attendance (" +
                "attendance_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER NOT NULL," +
                "subject_id INTEGER NOT NULL," +
                "date DATE NOT NULL," +
                "present BOOLEAN NOT NULL," +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE," +
                "FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE CASCADE)");

            // Create indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_student_id_grades ON Grades(student_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subject_id_grades ON Grades(subject_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_student_id_attendance ON Attendance(student_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subject_id_attendance ON Attendance(subject_id)");
            
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }
}
