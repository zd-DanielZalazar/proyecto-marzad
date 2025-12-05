package com.sga.marzad.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL =
            "jdbc:mysql://localhost:3306/sga_db"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=UTF-8"
                    + "&serverTimezone=America/Argentina/Buenos_Aires";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Se devuelve una nueva conexión por llamada para evitar que
            // el cierre en métodos anidados invalide ResultSets abiertos.
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado", e);
        }
    }
}
