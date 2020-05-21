package com.example.firebase_proyect.Models;

public class Grupos {

    private String ID, numero, nombre;


    public Grupos(String ID, String numero, String nombre) {
        this.ID = ID;
        this.numero = numero;
        this.nombre = nombre;
    }


    public Grupos(){

    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
