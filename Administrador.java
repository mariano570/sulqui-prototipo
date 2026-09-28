package com.sulqui.modelo;

public class Administrador {
    private int idAdmin;
    private String usuario;
    private String contrasenia;

    public Administrador() { }

    public Administrador(int idAdmin, String usuario, String contrasenia) {
        this.idAdmin = idAdmin;
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int idAdmin) { this.idAdmin = idAdmin; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
}
