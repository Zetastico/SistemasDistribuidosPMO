/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;

/**
 *
 * @author magne
 */
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClienteJuez {

    public static void main(String[] args) {

        try {

            Scanner sc = new Scanner(System.in);
            
            // Registry reg = LocateRegistry.getRegistry("26.222.84.246", 1099);
            
            Registry reg = LocateRegistry.getRegistry(1099);
            InterfazJusticia justicia =(InterfazJusticia) reg.lookup("Justicia");



            System.out.print("Ingrese CI: ");
            String ci = sc.nextLine();

            System.out.print("Ingrese nombres: ");
            String nombres = sc.nextLine();

            System.out.print("Ingrese apellidos: ");
            String apellidos = sc.nextLine();


            RespuestaCuenta respuesta = justicia.ConsultarCuentas(ci, nombres, apellidos);

            System.out.println("\nResultado");

            System.out.println("Error: " + respuesta.isError());
            System.out.println("Mensaje: " + respuesta.getMensaje());

            if (respuesta.getCuentas() != null) {

                for (Cuenta cuenta : respuesta.getCuentas()) {
                    System.out.println("Banco: " + cuenta.getBanco());
                    System.out.println("Cuenta: " + cuenta.getNrocuenta());
                    System.out.println("Saldo: " + cuenta.getSaldo());
                }
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());

        }
    }
}