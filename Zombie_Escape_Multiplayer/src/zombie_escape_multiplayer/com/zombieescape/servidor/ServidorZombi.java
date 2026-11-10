package com.zombieescape.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;


public class ServidorZombi {
    private static final int PUERTO = 5050;
    private static final int MAX_JUGADORES = 10;
    
    private static CopyOnWriteArrayList<ClientHandler> clientes = new CopyOnWriteArrayList<>();

    public static void iniciarServidor() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Zombi iniciado en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = serverSocket.accept();

                // Rechazar conexión número 11 o superior
                if (clientes.size() >= MAX_JUGADORES) {
                    System.out.println("Conexión rechazada: Sala llena (máximo 10 jugadores).");
                    socketCliente.close();
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(socketCliente, clientes);
                clientes.add(clientHandler);
                
                new Thread(clientHandler).start();
                System.out.println("Nuevo jugador conectado. Total en sala: " + clientes.size());
            }

        } catch (IOException e) {
            System.err.println("Error en el ServidorZombi: " + e.getMessage());
        }
    }
}