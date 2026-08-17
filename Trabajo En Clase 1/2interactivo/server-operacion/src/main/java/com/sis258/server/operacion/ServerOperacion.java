/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.sis258.server.operacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Dell
 */
public class ServerOperacion {

    public static void main(String[] args) {
        int port = 5002;
        ServerSocket server;
        try {
            server = new ServerSocket(port);
            while (true) {
                
                System.out.println("Se inicio el servidor con éxito");
                Socket client;
                PrintStream toClient;
                client = server.accept(); 
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream())); 
                System.out.println("Cliente se conecto");
                String recibido = fromClient.readLine();
                System.out.println("El cliente envio el mensaje:" + recibido);
                int numero1 = Integer.parseInt(recibido);
                
                toClient = new PrintStream(client.getOutputStream());
                
                toClient.println("introduzca el segundo numero");
                String recibido2 = fromClient.readLine();
                int numero2 = Integer.parseInt(recibido2);
                
                toClient.println("1.suma 2.resta 3. multiplicacion 4, division .introduzca la operacion");
                String recibido3 = fromClient.readLine();
                String respuesta;

                switch (recibido3.toLowerCase()) {
                    case "suma": case "1":
                        respuesta = String.valueOf(numero1 + numero2);
                        break;
                        
                    case "resta": case "2":
                        respuesta = String.valueOf(numero1 - numero2);
                        break; 
                        
                    case "multiplicacion": case "3":
                        respuesta = String.valueOf(numero1 * numero2);
                        break;
                        
                    case "division": case "4":
                        if (numero2 != 0) {
                            respuesta = String.valueOf(numero1 / numero2);
                        } else {
                            respuesta = "Error: No se puede dividir entre 0";
                        }
                        break;
                        
                    default:
                        respuesta = "Operacion no valida";
                        break;
                }

                
                toClient.println(respuesta);

                client.close();
            } 
        }catch (IOException ex) {
                System.out.print(ex.getMessage());
            }
    }
}