package com.sulqui.servicio;

import com.sulqui.dao.AdministradorDAO;
import com.sulqui.modelo.Administrador;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;

/**
 * Inicio de sesion del administrador (RNF de seguridad). La contrasenia se
 * guarda como hash SHA-256 (columna administrador.contrasenia), nunca en texto plano.
 * Reemplaza el metodo login() que estaba en la entidad Administrador.
 */
public class AutenticacionService {

    private final AdministradorDAO administradorDAO = new AdministradorDAO();

    /** Devuelve el administrador autenticado o null si las credenciales no coinciden. */
    public Administrador login(String usuario, char[] contrasenia) throws SQLException {
        Administrador a = administradorDAO.buscarPorUsuario(usuario);
        if (a == null) return null;
        return a.getContrasenia().equalsIgnoreCase(sha256(new String(contrasenia))) ? a : null;
    }

    static String sha256(String texto) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
