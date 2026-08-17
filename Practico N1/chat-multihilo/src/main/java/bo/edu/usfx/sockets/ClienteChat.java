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

public class ClienteChat {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        Socket socket = new Socket(host, puerto);
        System.out.println("servidor " + host + ":" + puerto);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        // Hilo receptor
        Thread receptor = new Thread(() -> {
            try {
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    System.out.println(mensaje);
                }
            } catch (IOException e) {
                System.out.println("conexion cerrada por el servidor.");
            }
        }, "hilo-receptor");
        receptor.setDaemon(true);
        receptor.start();

        // Hilo principal enviando
        String texto;
        while ((texto = teclado.readLine()) != null) {
            out.println(texto);
            if (texto.equalsIgnoreCase("/salir")) {
                break;
            }
        }
        socket.close();
    }
}