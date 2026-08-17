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

public class Cliente{
    public static void main(String[] args) {
        String ip = "10.192.41.219";
        int port = 5002;

        try (Socket socket = new Socket(ip, port);
             PrintStream toServer = new PrintStream(socket.getOutputStream());
             BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Ingrese trama (SUM-10-5 o RES-20-4): ");
            String trama = scanner.nextLine();

            toServer.println(trama);
            String respuesta = fromServer.readLine();
            System.out.println("Respuesta del Servidor: " + respuesta);

        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        }
    }
}