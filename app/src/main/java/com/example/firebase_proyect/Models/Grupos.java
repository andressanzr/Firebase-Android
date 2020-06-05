package com.example.firebase_proyect.Models;

public class Grupos {

    private String ID, nombre;
    private int numero;

    public Grupos(String ID, int numero, String nombre) {
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
