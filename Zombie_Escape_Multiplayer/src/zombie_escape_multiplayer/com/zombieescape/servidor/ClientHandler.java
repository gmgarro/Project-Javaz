package com.zombieescape.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private CopyOnWriteArrayList<ClientHandler> clientes;
    private String idJugador;

    public ClientHandler(Socket socket, CopyOnWriteArrayList<ClientHandler> clientes) {
        this.socket = socket;
        this.clientes = clientes;
        try {
            this.entrada = new DataInputStream(socket.getInputStream());
            this.salida = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensajeRecibido = entrada.readUTF();
                procesarMensajeAutoritativo(mensajeRecibido);
            }
        } catch (IOException e) {
            System.out.println("Jugador desconectado.");
        } finally {
            removerCliente();
        }
    }

    // Actuar como Autoridad Central: validar reglas e intenciones[cite: 1]
    private void procesarMensajeAutoritativo(String mensaje) {
        if (mensaje.startsWith("INICIAR")) {
            // Ignorar el comando INICIAR si hay menos de 2 jugadores en la sala[cite: 1]
            if (clientes.size() < 2) {
                enviarMensaje("ERROR: Se necesitan al menos 2 jugadores para iniciar la partida.");
                return;
            }
            enviarBroadcast("PARTIDA_INICIADA");
            return;
        }

        if (mensaje.startsWith("MOVER:") || mensaje.startsWith("DISPARO:")) {
            // Aquí aplicarás la validación de reglas, cooldowns y lógica autoritativa[cite: 1]
            enviarBroadcast(mensaje); // Reenvía el estado confirmado a los clientes
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeUTF(mensaje);
            salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reenviar estados confirmados a todos los clientes conectados[cite: 1]
    public void enviarBroadcast(String mensaje) {
        for (ClientHandler cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }

    private void removerCliente() {
        clientes.remove(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Cliente removido. Jugadores restantes: " + clientes.size());
    }
}