/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

/**
 *
 * @author magne
 */

import java.io.*;
import java.net.*;
public class Manejador implements Runnable {
    private final Socket cliente;
    private final int id;
    public Manejador(Socket cliente, int id) {
        this.cliente = cliente;
        this.id = id;
    }
    @Override
    public void run() { // se ejecuta en OTRO hilo
        String hilo = Thread.currentThread().getName();
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(cliente.getInputStream()));
            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true)) {
            out.println("Hilo asignado: " + hilo);
            String linea;
            while ((linea = in.readLine()) != null) {
                System.out.println("[" + hilo + "] cliente " + id + ": " + linea);
                out.println("ECO(" + hilo + "): " + linea);
            }
        } catch (IOException e) {
            System.err.println("error con el cliente " + id + ": " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (IOException e) { }
                System.out.println("Cliente " + id + " desconectado");
        }
    }
}