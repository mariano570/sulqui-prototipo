package com.sulqui.modelo;

/** Administrador del sistema (Mariano). Hereda de Persona. */
public class Administrador extends Persona {

    private String usuario;
    private String contrasenia;   // hash SHA-256, nunca texto plano

    public Administrador() {
        super();
    }

    public Administrador(int idAdmin, String usuario, String contrasenia) {
        super(idAdmin);
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    public int getIdAdmin() { return getId(); }
    public void setIdAdmin(int idAdmin) { setId(idAdmin); }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    @Override
    public String getIdentificador() { return usuario; }

    @Override
    public String getRol() { return "Administrador"; }

    @Override
    public String toString() { return describir(); }
}
