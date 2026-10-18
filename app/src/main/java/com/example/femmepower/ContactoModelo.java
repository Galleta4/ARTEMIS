package com.example.femmepower;

public class ContactoModelo {

    String id;
    String nombre;
    String numero;

    public ContactoModelo(String id, String nombre, String numero) {
        this.id = id;
        this.nombre = nombre;
        this.numero = numero;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNumero() {
        return numero;
    }
}