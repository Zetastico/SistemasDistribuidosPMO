/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteBasico {

    public static void main(String[] args) throws IOException {

        String ip = args.length > 0
                ? args[0]
                : "localhost";

        Socket socket = new Socket(ip, 5000);

        System.out.println(
                "Conectado al servidor " + ip
        );

        System.out.println(
                "Puerto local: "
                + socket.getLocalPort()
        );

        PrintWriter salida =
                new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

        BufferedReader entrada =
                new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()
                        )
                );

        BufferedReader teclado =
                new BufferedReader(
                        new InputStreamReader(
                                System.in
                        )
                );

        // Hilo que recibe mensajes del servidor
        Thread receptor = new Thread(() -> {

            try {

                String mensaje;

                while ((mensaje = entrada.readLine()) != null) {

                    System.out.println(
                            "\n" + mensaje
                    );

                    System.out.print("> ");
                }

            } catch (IOException e) {

                System.out.println(
                        "Conexion cerrada."
                );
            }

        });

        receptor.setDaemon(true);
        receptor.start();

        String mensaje;

        while ((mensaje = teclado.readLine()) != null) {

            salida.println(mensaje);
        }

        socket.close();
    }
}