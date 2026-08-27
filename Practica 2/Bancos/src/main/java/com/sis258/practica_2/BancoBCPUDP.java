/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;


/**
 *
 * @author Wesley
 */
import java.net.*;
import java.util.*;

public class BancoBCPUDP {
    private static Map<String, List<String>> baseDatos = new HashMap<>();

    public static void main(String[] args) {
        // Caso de 
        // CI: 11021654 -> Cuenta 6576, Saldo 6500
        baseDatos.put("11021654", new ArrayList<>(Arrays.asList("6576-6500.0")));

        int puerto = 5002;

        try (DatagramSocket socket = new DatagramSocket(puerto)) {
            System.out.println("Servidor UDP Banco BCP iniciado en el puerto " + puerto + "...");
            byte[] buffer = new byte[1024];

            while (true) {
                // 1. Recibir Datagrama
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String peticion = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("BCP (UDP) - Petición recibida: " + peticion);

                // 2. Procesar la petición (Formato: Operación:ci)
                String respuesta = "";
                String[] partes = peticion.split(":");
                String operacion = partes[0].toLowerCase();

                if (operacion.equals("buscar") && partes.length > 1) {
                    String ci = partes[1];
                    List<String> cuentas = baseDatos.get(ci);
                    if (cuentas != null && !cuentas.isEmpty()) {
                        respuesta = String.join(":", cuentas);
                    }
                } else if (operacion.equals("congelar")) {
                    respuesta = "exito"; 
                }

                // 3. Enviar Respuesta
                byte[] resData = respuesta.getBytes();
                DatagramPacket resPacket = new DatagramPacket(
                    resData, 
                    resData.length, 
                    packet.getAddress(), 
                    packet.getPort()
                );
                socket.send(resPacket);
                System.out.println("BCP (UDP) - Respuesta enviada: " + (respuesta.isEmpty() ? "[Cadena vacía]" : respuesta));
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor UDP: " + e.getMessage());
        }
    }
}