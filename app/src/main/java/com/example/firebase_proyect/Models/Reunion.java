package com.example.firebase_proyect.Models;

import java.util.Date;

public class Reunion {
    private String ID, grupo, asignatura;
    private Date fecha;

    public Reunion() {
    }

    public Reunion(String ID, String grupo, String asignatura, Date fecha) {
        this.ID = ID;
        this.grupo = grupo;
        this.asignatura = asignatura;
        this.fecha = fecha;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
