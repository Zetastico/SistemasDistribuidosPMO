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

public class ServerOperacion {

    public static void main(String[] args) {
        int port = 5002;
        ServerSocket server;
        while (true) {
            try {
                server = new ServerSocket(port);
                System.out.println("Se inicio el servidor con éxito");
                Socket client;
                PrintStream toClient;
                client = server.accept(); 
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream())); 
                System.out.println("Cliente se conecto");
                String recibido = fromClient.readLine();
                System.out.println("El cliente envio el mensaje:" + recibido);
                toClient = new PrintStream(client.getOutputStream());
                String respuesta = procesarSolicitud(recibido);
                toClient.println(respuesta);

            } catch (IOException ex) {
                System.out.print(ex.getMessage());
            }
        }
    }

    public static String procesarSolicitud(String cadena) {
        try {
            // SUM-5-10
            String[] partes = cadena.split("-");
            String operacion = partes[0].toUpperCase();
            int num1 = Integer.parseInt(partes[1]);
            int num2 = Integer.parseInt(partes[2]);
            int resultado = 0;

            switch (operacion) {
                case "SUM": 
                    resultado = num1 + num2; 
                    break;
                    
                    
                case "RES": 
                    resultado = num1 - num2; 
                    break;
                    
                case "MUL": 
                    resultado = num1 * num2; 
                    break;
                    
                case "DIV": 
                    if (num2 != 0) 
                        resultado = num1 / num2;
                    else 
                        return "Error: Division por cero";
                    break;
                default: 
                    return "Operacion no valida";
            }
            return String.valueOf(resultado);
        } catch (Exception e) {
            return "Error en el formato del protocolo";
        }
    }
}