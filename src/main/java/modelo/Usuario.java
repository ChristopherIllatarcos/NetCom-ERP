package modelo;

import java.sql.Timestamp;

public class Usuario {

    private int id;
    private String username;
    private String passwordHash;
    private String nombreCompleto;
    private String email;
    private String rol;
    private boolean activo;
    private String permisos; // Manejado como String JSON para PostgreSQL jsonb
    private Timestamp ultimoAcceso;
    private Timestamp createdAt;
    private Timestamp updatedAt;
   

    // Constructor vacío
    public Usuario() {
    }

    // Constructor para insertar nuevo usuario
    public Usuario(String username, String passwordHash, String nombreCompleto, String email, String rol, boolean activo, String permisos) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
        this.permisos = permisos;
       
    }

 

    // Constructor completo
    public Usuario(int id, String username, String passwordHash, String nombreCompleto, String email, String rol, boolean activo, String permisos, Timestamp ultimoAcceso, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
        this.permisos = permisos;
        this.ultimoAcceso = ultimoAcceso;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- GETTERS Y SETTERS ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }

    public Timestamp getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(Timestamp ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
