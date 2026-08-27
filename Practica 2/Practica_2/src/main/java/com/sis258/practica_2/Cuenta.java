/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;

/**
 *
 * @author magne
 */

import java.io.Serializable;

public class Cuenta implements Serializable {

    private Banco banco;
    private String nrocuenta;
    private String ci;
    private String nombres;
    private String apellidos;
    
    private double saldo;

    
    public Cuenta(Banco banco, String nrocuenta, String ci, String nombres, String apellidos, double saldo) {

        this.banco = banco;
        this.nrocuenta = nrocuenta;
        this.ci = ci;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.saldo = saldo;
    }

    public Banco getBanco() {
        return banco;
    }

    public String getNrocuenta() {
        return nrocuenta;
    }

    public String getCi() {
        return ci;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public double getSaldo() {
        return saldo;
    }
}