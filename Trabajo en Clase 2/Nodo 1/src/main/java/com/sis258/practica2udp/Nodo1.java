package com.sis258.practica2udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Nodo1 {

    public static void main(String[] args) {
        
        int puertoNodo1 = 6801;
        
        int puertoNodo2 = 6804;
        String ip = "192.168.137.1";
        
        Scanner sc = new Scanner(System.in);

        try {
            
            DatagramSocket socketUDP = new DatagramSocket(puertoNodo1);

            System.out.print("Introduzca una palabra o frase: ");
            String texto = sc.nextLine();
            int cant = texto.length();

            
            String datos = cant + "\n" + texto;
            byte[] mensaje = datos.getBytes();
            InetAddress host = InetAddress.getByName(ip);

            DatagramPacket peticion = new DatagramPacket(
                    mensaje, mensaje.length, host, puertoNodo2
            );
            socketUDP.send(peticion);

            System.out.println("enviado a ndo 2");
            
            System.out.println("esperando nodo 3");

            byte[] bufer = new byte[2000];
            DatagramPacket respuesta = new DatagramPacket(bufer, bufer.length);
            socketUDP.receive(respuesta);

            String resultado = new String(
                    respuesta.getData(), 0, respuesta.getLength()
            );

            System.out.println("\nResultado");
            System.out.println(resultado);

            socketUDP.close();
            
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}
