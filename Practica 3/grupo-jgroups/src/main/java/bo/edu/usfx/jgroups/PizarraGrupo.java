package bo.edu.usfx.jgroups;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.Address;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.jgroups.util.Util;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author magne
 */
public class PizarraGrupo implements Receiver {
    
    private JChannel canal;
    private final String nombre;
    private final List<String> historial = new ArrayList<>();
    
    public PizarraGrupo(String nombre){
        this.nombre = nombre;
    }
    
    private View vistaAnterior;
    
    @Override
    public void viewAccepted(View vista){


        if(vistaAnterior != null){
            Address[][] cambios = View.diff(vistaAnterior, vista);
            for(Address a : cambios[0]) System.out.println("** Entro:  " + a);
            for(Address a : cambios[1]) System.out.println("** Salio:  " + a);

        }
        vistaAnterior = vista;
        System.out.println("** Vista "+ vista.getViewId().getId()
                + " | coordinador: " + vista.getCoord() + 
                "| miembros: " + vista.getMembers());
    }
    
    @Override
    public void receive(Message msg){
        String texto = msg.getSrc() + ": " + msg.getObject();
        synchronized (historial) {
            historial.add(texto);
        }
        String tipo = (msg.getDest() == null) ? "" : "(privado)" ;
        
        System.out.println(tipo + texto);
    }
    
    // Ciclo de vida
    
    public void iniciar() throws Exception{
        canal = new JChannel(System.getProperty("config", "udp.xml"));
        canal.name(nombre);
        canal.setReceiver(this);
        canal.connect("PizarraSIS258");
        canal.getState(null, 10000);
        leerTeclado();
        canal.close();
    }
    
    private void leerTeclado() throws Exception{
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Comandos: /quien /historial /privado <nombre> <texto> /salir");
        String linea;
        while((linea = teclado.readLine()) != null){
            if(linea.equals("/salir")) break;
            if(linea.equals("/quien")){
                System.out.println("Miembros: " + canal.getView().getMembers() + " | yo: " + canal.getAddressAsString()); 
            }else if (linea.equals("/historial")) {
                synchronized (historial) { historial.forEach(System.out::println); }
            } else if (linea.startsWith("/privado ")) {
                enviarPrivado(linea);
            } else {
                canal.send(new ObjectMessage(null, linea)); // multicast al grupo
            }
            
        }
    }
    
    
    private void enviarPrivado(String linea) throws Exception{
        String[] partes = linea.split(" ", 3);
        if (partes.length < 3){
            System.out.println("Uso: ?pricado <nombre> <texto>");
            return;
        }
        Address destino = buscarPorNombre(partes[1]);
        
        if (destino == null) {
            System.out.println("No existe el miembro " + partes[1]);
            return;
        }
        canal.send(new ObjectMessage(destino, partes[2])); 
    }
    
    
    
    
    public void getState(OutputStream salida) throws Exception{
        synchronized (historial) {
            Util.objectToStream(historial, new DataOutputStream(salida));
        }
        System.out.println("** Estado enviado a un nuevo miembro (" + historial.size() + " lineas)");
    }
    
    private Address buscarPorNombre(String nombreBuscado) {
        for (Address a : canal.getView().getMembers()) {
            if (a.toString().equals(nombreBuscado)) return a; // nombre logico = toString()
        }
        return null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setState(InputStream entrada) throws Exception{
        List<String> recibido = Util.objectFromStream(new DataInputStream(entrada));
        synchronized (historial) {
            historial.clear();
            historial.addAll(recibido);
        }
        System.out.println("** Estado recibido: " + recibido.size() + " lineas");
        recibido.forEach(l -> System.out.println(" " + l));
    }
    
    
    public static void main(String[] args) throws Exception{
        String nombre = args.length > 0 ? args[0] : "anonimo";
        new PizarraGrupo(nombre).iniciar();
    }
    
}