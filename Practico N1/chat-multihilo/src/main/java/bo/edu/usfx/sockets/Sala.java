/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package bo.edu.usfx.sockets;

/**
 *
 * @author magne
 */

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Sala {
    private final String nombre;
    private final Set<ManejadorCliente> miembros = new CopyOnWriteArraySet<>();

    public Sala(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Set<ManejadorCliente> getMiembros() {
        return miembros;
    }

    public void agregarMiembro(ManejadorCliente cliente) {
        miembros.add(cliente);
    }

    public void removerMiembro(ManejadorCliente cliente) {
        miembros.remove(cliente);
    }

    public void difundir(String mensaje, ManejadorCliente emisor) {
        for (ManejadorCliente m : miembros) {
            if (m != emisor) {
                m.enviarMensaje(mensaje);
            }
        }
    }
}