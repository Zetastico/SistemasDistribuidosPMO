/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sis258.practica_2;

/**
 *
 * @author magne
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ClaseJusticia extends UnicastRemoteObject
        implements InterfazJusticia {

    public ClaseJusticia() throws RemoteException {
        super();
    }

    @Override
    public RespuestaCuenta ConsultarCuentas( String ci, String nombres, String apellidos) throws RemoteException {

        ArrayList<Cuenta> cuentas = new ArrayList<>();

        try {
            String respuestaMercantil = consultarMercantil(ci);

            String respuestaBCP = consultarBCP(ci);

            if (!respuestaMercantil.isEmpty()) {
                String[] cuentasMercantil = respuestaMercantil.split(":");

                for (String dato : cuentasMercantil) {

                    String[] partes = dato.split("-");

                    String nroCuenta = partes[0];
                    double saldo = Double.parseDouble(partes[1]);

                    Cuenta cuenta = new Cuenta(Banco.MERCANTIL, nroCuenta, ci, nombres,  apellidos, saldo);

                    cuentas.add(cuenta);
                }
            }

            if (!respuestaBCP.isEmpty()) {

                String[] cuentasBCP = respuestaBCP.split(":");
                for (String dato : cuentasBCP) {

                    String[] partes = dato.split("-");

                    String nroCuenta = partes[0];
                    double saldo = Double.parseDouble(partes[1]);

                    Cuenta cuenta = new Cuenta(Banco.BCP, nroCuenta,ci, nombres, apellidos, saldo);

                    cuentas.add(cuenta);
                }
            }

            if (cuentas.isEmpty()) {

                return new RespuestaCuenta(true, "No se encontraron cuentas", cuentas);
            }

            return new RespuestaCuenta(false, "Consulta exitosa", cuentas);

        } catch (Exception e) {

            return new RespuestaCuenta(true, "Error al consultar las cuentas: " + e.getMessage(), cuentas);
        }
    }

    @Override
    public void Congelar(Cuenta cuenta, double monto)
            throws RemoteException {

        System.out.println("Congelando cuenta: " + cuenta.getNrocuenta() + " por " + monto+ "bs");
    }
    
    private String consultarMercantil(String ci) {

    try {

        //Socket socket = new Socket("localhost", 5001);
        Socket socket = new Socket("26.176.165.56", 5001);

        PrintStream salida = new PrintStream(socket.getOutputStream());

        salida.println("buscar:" + ci);

        BufferedReader entrada =new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String respuesta = entrada.readLine();

        socket.close();

        return respuesta;

    } catch (IOException e) {

        System.out.println("Error conectando con Mercantil: "+ e.getMessage());

        return "";
    }
    
}
    private String consultarBCP(String ci) {

    try {

        DatagramSocket socket = new DatagramSocket();

        String mensaje = "buscar:" + ci;

        byte[] datos = mensaje.getBytes();
        
        //InetAddress direccion = InetAddress.getByName("localhost");
        InetAddress direccion = InetAddress.getByName("26.176.165.56");

        DatagramPacket paquete =new DatagramPacket( datos, datos.length, direccion, 5002);

        socket.send(paquete);

        byte[] buffer = new byte[1024];

        DatagramPacket respuesta =new DatagramPacket(buffer, buffer.length);

        socket.receive(respuesta);

        String resultado =new String(respuesta.getData(),0,respuesta.getLength()).trim();

        socket.close();

        return resultado;

    } catch (IOException e) {

        System.out.println("Error conectando con BCP: "+ e.getMessage());

        return "";
    }
    }
}