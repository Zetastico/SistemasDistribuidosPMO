/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;

/**
 *
 * @author magne
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ClaseJusticia extends UnicastRemoteObject
        implements InterfazJusticia {

    public ClaseJusticia() throws RemoteException {
        super();
    }

    @Override
    public RespuestaCuenta ConsultarCuentas(String ci, String nombres, String apellidos) throws RemoteException {

        ArrayList<Cuenta> cuentas = new ArrayList<>();

        if (ci.equals("11021654") && nombres.equals("Juan Perez") && apellidos.equals("Segovia")) {

            cuentas.add(new Cuenta(Banco.MERCANTIL, "1515", ci, nombres, apellidos, 5100.0));

            cuentas.add(new Cuenta(Banco.BCP, "6576", ci, nombres, apellidos, 6500.0));

            return new RespuestaCuenta(
                    false,
                    "Consulta exitosa",
                    cuentas
            );
        }

        return new RespuestaCuenta(
                true,
                "No se encontraron cuentas",
                cuentas
        );
    }

    @Override
    public void Congelar(Cuenta cuenta, double monto)
            throws RemoteException {

        System.out.println("Congelando cuenta: "
                + cuenta.getNrocuenta()
                + " por Bs. " + monto);
    }
}