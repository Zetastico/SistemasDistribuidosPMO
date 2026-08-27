/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;


/**
 *
 * @author Wesley
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class BancoMercantilTCP {
    // Simulamos la base de datos con un HashMap (CI como llave)
    private static Map<String, List<String>> baseDatos = new HashMap<>();

    public static void main(String[] args) {
        // Caso de prueba 
        // CI: 11021654 :Cuenta 1515, Saldo 5100
        baseDatos.put("11021654", new ArrayList<>(Arrays.asList("1515-5100.0")));

        int puerto = 5001;

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor TCP Banco Mercantil iniciado en el puerto " + puerto + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                manejarCliente(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor TCP: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String peticion = in.readLine();
            if (peticion != null) {
                System.out.println("Mercantil (TCP) - Petición recibida: " + peticion);
                
                String respuesta = "";
                String[] partes = peticion.split(":");
                String operacion = partes[0].toLowerCase();

                // Manejo de la operación buscar
                if (operacion.equals("buscar") && partes.length > 1) {
                    String ci = partes[1];
                    respuesta = buscarCuentas(ci);
                } 
                // Manejo por si el Servidor Justicia envía solo el CI directamente
                else if (partes.length == 1) {
                    respuesta = buscarCuentas(peticion);
                }
                // Manejo básico de la orden de congelamiento solicitada por el Juez
                else if (operacion.equals("congelar") && partes.length > 2) {
                    // partes[1] = cuenta, partes[2] = monto
                    respuesta = "exito"; // Aquí iría la lógica de restar el saldo
                }

                out.println(respuesta);
                System.out.println("Mercantil (TCP) - Respuesta enviada: " + (respuesta.isEmpty() ? "[Cadena vacía]" : respuesta));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buscarCuentas(String ci) {
        List<String> cuentas = baseDatos.get(ci);
        if (cuentas != null && !cuentas.isEmpty()) {
            return String.join(":", cuentas);
        }
        return ""; 
    }
}
