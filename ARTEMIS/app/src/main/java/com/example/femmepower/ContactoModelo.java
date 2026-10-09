package com.example.femmepower;

public class ContactoModelo {

    String nombre;
    String numero;

    public ContactoModelo(String nombre, String numero) {
        this.nombre = nombre;
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNumero() {
        return numero;
    }
}