package com.example.firebase_proyect.Models;

public class Asignaturas {

    private String ID, nombre, curso, descripcion, foto;

    public Asignaturas(String ID, String nombre, String curso, String descripcion, String foto) {
        this.ID = ID;
        this.nombre = nombre;
        this.curso = curso;
        this.descripcion = descripcion;
        this.foto = foto;
    }

    public Asignaturas(){

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

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
