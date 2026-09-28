package com.sulqui.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria responsable de abrir la conexion JDBC hacia la base
 * de datos MySQL "sulqui_db". Los datos de conexion deben ajustarse
 * al entorno local antes de ejecutar el prototipo.
 */
public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/sulqui_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CONTRASENIA = "CAMBIAR_PASSWORD";

    private Conexion() { }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENIA);
    }
}
