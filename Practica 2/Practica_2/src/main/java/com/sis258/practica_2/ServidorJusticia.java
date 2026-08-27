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

public class ServidorJusticia {

    public static void main(String[] args) {

        try {

            Registry reg = LocateRegistry.createRegistry(1099);
            ClaseJusticia justicia = new ClaseJusticia();

            reg.rebind("Justicia", justicia);

            System.out.println("Servidor iniciado");
            System.out.println("Esperando peticiones");

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());

        }
    }
}