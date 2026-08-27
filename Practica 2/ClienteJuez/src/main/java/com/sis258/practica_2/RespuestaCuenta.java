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
import java.util.ArrayList;

public class RespuestaCuenta implements Serializable {

    private boolean error;
    private String mensaje;
    private ArrayList<Cuenta> cuentas;

    public RespuestaCuenta(boolean error, String mensaje, ArrayList<Cuenta> cuentas) {

        this.error = error;
        
        this.mensaje = mensaje;
        this.cuentas = cuentas;
    }

    public boolean isError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public ArrayList<Cuenta> getCuentas() {
        return cuentas;
    }
}