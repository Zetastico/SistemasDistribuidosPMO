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

public class ManejadorCliente implements Runnable {
    private final Socket socket;
    private final int id;
    private final EstadoServidor estado;
    private PrintWriter out;
    private String apodo;
    private Sala salaActual;

    public ManejadorCliente(Socket socket, int id, EstadoServidor estado) {
        this.socket = socket;
        this.id = id;
        this.estado = estado;
        this.apodo = "Cliente-" + id;
    }

    public String getApodo() {
        return apodo;
    }

    // Sincronizado para evitar colisiones al escribir desde varios hilos (Requisito /privado)
    public synchronized void enviarMensaje(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter outWriter = new PrintWriter(socket.getOutputStream(), true)) {
            
            this.out = outWriter;
            estado.registrarApodo(this.apodo, null, this);
            
            // Asignar a sala "general" por defecto
            this.salaActual = estado.getSalas().get("general");
            this.salaActual.agregarMiembro(this);

            out.println("Estas en la sala: " + salaActual.getNombre());
            out.println("Apodo actual: " + apodo);
            

            String linea;
            while ((linea = in.readLine()) != null) {
                if (linea.startsWith("/")) {
                    if (procesarComando(linea)) break; // Comandos como /salir
                } else {
                    salaActual.difundir(apodo + "> " + linea, this);
                }
            }
        } catch (IOException e) {
            System.err.println("Error con cliente " + apodo + ": " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    private boolean procesarComando(String linea) {
        String[] partes = linea.split(" ", 2);
        String comando = partes[0].toLowerCase();
        String arg;
        if (partes.length > 1) {
            arg = partes[1].trim();
        } else {
            arg = "";
        }

        switch (comando) {
            case "/nick":
                if (arg.isEmpty()) {
                    out.println("Uso: /nick <apodo>");
                } else if (estado.registrarApodo(arg, this.apodo, this)) {
                    out.println("nuevo apodo: " + arg);
                    this.apodo = arg;
                } else {
                    out.println("el apodo '" + arg + "' ya esta en uso.");
                }
                break;

            case "/salas":
                out.println("Salas Disponibles:");
                estado.getSalas().forEach((nombre, sala) -> 
                    out.println("- " + nombre + " (" + sala.getMiembros().size() + " usuarios)")
                );
                break;

            case "/crear":
                if (arg.isEmpty()) {
                    
                    
                    out.println("Uso: /crear <nombre_sala>");
                    
                    
                } else if (estado.getSalas().containsKey(arg)) {
                    
                    out.println("La sala ya existe.");
                    
                } else {
                    
                    Sala nuevaSala = new Sala(arg);
                    estado.getSalas().put(arg, nuevaSala);
                    cambiarDeSala(nuevaSala);
                }
                break;

            case "/unirse":
                if (arg.isEmpty()) {
                    out.println("Uso: /unirse <nombre_sala>");
                } else if (!estado.getSalas().containsKey(arg)) {
                    out.println("La sala no existe.");
                } else {
                    cambiarDeSala(estado.getSalas().get(arg));
                }
                break;

            case "/quien":
                out.println("Usuarion en: " + salaActual.getNombre().toUpperCase());
                for (ManejadorCliente m : salaActual.getMiembros()) {
                    out.println("- " + m.getApodo());
                }
                break;

            case "/privado":
                String[] privPartes = arg.split(" ", 2);
                if (privPartes.length < 2) {
                    out.println("Uso: /privado <apodo> <mensaje>");
                } else {
                    ManejadorCliente destino = estado.getClientePorApodo(privPartes[0]);
                    if (destino != null) {
                        destino.enviarMensaje("[Privado de " + apodo + "]: " + privPartes[1]);
                        out.println("[Privado a " + privPartes[0] + "]: " + privPartes[1]);
                    } else {
                        out.println("Usuario no encontrado.");
                    }
                }
                break;

            case "/estado":
                out.println("Estado del Servidor");
                out.println("Usuarios conectados: " + estado.getClientesActivos());
                out.println("Total conexiones historicas: " + estado.getConexionesHistoricas());
                out.println("Cantidad de salas: " + estado.getSalas().size());
                break;

            case "/salir":
                return true;

            default:
                out.println("Comando no reconocido. Comandos: /nick, /salas, /crear, /unirse, /quien, /privado, /estado, /salir");
        }
        return false;
    }

    private void cambiarDeSala(Sala nuevaSala) {
        salaActual.difundir("*** " + apodo + " salio de la sala ***", this);
        salaActual.removerMiembro(this);
        this.salaActual = nuevaSala;
        salaActual.agregarMiembro(this);
        salaActual.difundir(apodo + " se unio a la sala", this);
        out.println("Te has unido a la sala: " + nuevaSala.getNombre());
    }

    private void desconectar() {
        if (salaActual != null) {
            salaActual.difundir(apodo + " se ha desconectado", esteCliente());
            salaActual.removerMiembro(this);
        }
        estado.removerApodo(this.apodo);
        estado.decrementarClientesActivos();
        try { socket.close(); } catch (IOException e) {}
        System.out.println("Cliente " + apodo + " desconectado.");
    }

    private ManejadorCliente esteCliente() { return this; }
}