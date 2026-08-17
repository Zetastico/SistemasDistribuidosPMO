/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

/**
 *
 * @author magne
 */

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EstadoServidor {
    
    
    private final Map<String, Sala> salas = new ConcurrentHashMap<>();
    
    private final Set<String> apodos = ConcurrentHashMap.newKeySet();
    
    private final Map<String, ManejadorCliente> clientesPorApodo = new ConcurrentHashMap<>();
    
    private final AtomicInteger conexionesHistoricas = new AtomicInteger(0);
    private final AtomicInteger clientesActivos = new AtomicInteger(0);

    public EstadoServidor() {
        salas.put("general", new Sala("general"));
    }

    public void incrementarConexiones() {
        conexionesHistoricas.incrementAndGet(); 
        clientesActivos.incrementAndGet();
    }

    public void decrementarClientesActivos() {
        clientesActivos.decrementAndGet();
    }

    public int getConexionesHistoricas() {
        return conexionesHistoricas.get();
    }

    public int getClientesActivos() {
        return clientesActivos.get();
    }

    public Map<String, Sala> getSalas() {
        return salas;
    }

    public boolean registrarApodo(String nuevoApodo, String apodoAnterior, ManejadorCliente cliente) {
        if (apodos.add(nuevoApodo)) {
            if (apodoAnterior != null) {
                apodos.remove(apodoAnterior);
                clientesPorApodo.remove(apodoAnterior);
            }
            clientesPorApodo.put(nuevoApodo, cliente);
            return true;
        }
        return false;
    }

    public void removerApodo(String apodo) {
        if (apodo != null) {
            apodos.remove(apodo);
            clientesPorApodo.remove(apodo);
        }
    }

    public ManejadorCliente getClientePorApodo(String apodo) {
        return clientesPorApodo.get(apodo);
    }
}