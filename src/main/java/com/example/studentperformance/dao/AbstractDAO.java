package com.example.studentperformance.dao;

import java.sql.Connection;

public abstract class AbstractDAO {
    protected Connection connection;

    public static class NotFoundException extends Exception {
        public NotFoundException(String message) {super(message);}
    }

    public AbstractDAO(Connection connection) {
        this.connection = connection;
    }
}