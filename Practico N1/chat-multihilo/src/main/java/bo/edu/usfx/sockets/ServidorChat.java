/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

/**
 *
 * @author magne
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorChat {
    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int hilos = args.length > 1 ? Integer.parseInt(args[1]) : 4;

        ServerSocket servidor = new ServerSocket(puerto);
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        EstadoServidor estado = new EstadoServidor();

        System.out.println("servidor iniciado en puerto " + puerto + " con " + hilos + " hilos.");

        int contador = 0;
        while (true) {
            Socket cliente = servidor.accept(); // Solo acepta conexiones
            contador++;
            estado.incrementarConexiones();
            
            System.out.println("conexion N" + contador + " aceptada desde " + cliente.getInetAddress().getHostAddress());
            pool.execute(new ManejadorCliente(cliente, contador, estado)); // Delegacion inmediata
        }
    }
}