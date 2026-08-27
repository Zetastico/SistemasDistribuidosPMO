/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author magne
 */
public interface InterfazJusticia extends Remote {

    RespuestaCuenta ConsultarCuentas(String ci, String nombres, String apellidos)
            throws RemoteException;

    void Congelar(Cuenta cuenta, double monto)
            throws RemoteException;
}