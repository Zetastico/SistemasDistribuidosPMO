package bo.edu.usfx.jgroups;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import org.jgroups.View;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author magne
 */
public class ChatGrupo implements Receiver {
    
    private JChannel canal;
    private final String nombre;
    
    public ChatGrupo(String nombre){
        this.nombre = nombre;
    }
    
    @Override
    public void viewAccepted(View vista){
        System.out.println("** Miembros (" + vista.size() + "): " + vista.getMembers());
    }
    
    @Override
    public void receive(Message msg){
        System.out.println(msg.getSrc() + " > " + msg.getObject());
    }
    
    // Ciclo de vida
    
    public void iniciar() throws Exception{
        canal = new JChannel();
        canal.name(nombre);
        canal.setReceiver(this);
        canal.connect("ChatSIS258");
        leerTeclado();
        canal.close();
    }
    
    private void leerTeclado() throws Exception{
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("escriba mensajes: /salir para terminar.");
        String linea;
        while((linea = teclado.readLine()) != null){
            if(linea.equals("/salir")) break;
            canal.send(new ObjectMessage(null, linea));
        }
    }
    
    public static void main(String[] args) throws Exception{
        String nombre = args.length > 0 ? args[0] : "anonimo";
        new ChatGrupo(nombre).iniciar();
    }
   
}
