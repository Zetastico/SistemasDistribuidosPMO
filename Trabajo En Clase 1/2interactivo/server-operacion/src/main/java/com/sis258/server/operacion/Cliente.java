/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.server.operacion;

/**
 *
 * @author magne
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        String ip = "10.192.41.219";
        int port = 5003;

        try (Socket socket = new Socket(ip, port);
             PrintStream toServer = new PrintStream(socket.getOutputStream());
             BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            // primer numero
            System.out.print("Introduzca el primer numero: ");
            toServer.println(scanner.nextLine());

            // Sgundo numero
            System.out.println("Servidor: " + fromServer.readLine());
            toServer.println(scanner.nextLine());

            //operacion
            System.out.println("Servidor: " + fromServer.readLine());
            toServer.println(scanner.nextLine());

            // resultado final del servidor
            String resultado = fromServer.readLine();
            System.out.println("Resultado recibido: " + resultado);

        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        }
    }
}