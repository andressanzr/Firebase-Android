package com.example.firebase_proyect.Models;

import java.util.ArrayList;

public class Users {
    private String ID, nombre, apellido, email, password, foto;
    private int edad, tipoUsuario;
    // tipo usuario 0 alumno
    // tipo usuario 1 profesor
    private Grupos grupoUser;
    private ArrayList<Asignaturas> asignaturasUser;

    public Users() {
    }

    public Users(String ID, String nombre, String apellido, String email, String password, String foto, int edad, int tipoUsuario, Grupos grupoUser, ArrayList<Asignaturas> asignaturasUser) {
        this.ID = ID;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.foto = foto;
        this.edad = edad;
        this.tipoUsuario = tipoUsuario;
        this.grupoUser = grupoUser;
        this.asignaturasUser = asignaturasUser;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public int getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(int tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public Grupos getGrupoUser() {
        return grupoUser;
    }

    public void setGrupoUser(Grupos grupoUser) {
        this.grupoUser = grupoUser;
    }

    public ArrayList<Asignaturas> getAsignaturasUser() {
        return asignaturasUser;
    }

    public void setAsignaturasUser(ArrayList<Asignaturas> asignaturasUser) {
        this.asignaturasUser = asignaturasUser;
    }
}
