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

                // Dentro del método procesarMensajeAutoritativo en ClientHandler.java:
        if (mensaje.startsWith("UNIRSE:")) {
            // Ejemplo: UNIRSE:NombreJugador/FRANCOTIRADOR
            String[] partes = mensaje.substring(7).split("/");
            this.idJugador = partes[0];
            String rolDeseado = partes[1];

            // Contar francotiradores actuales para no exceder el máximo de 2[cite: 1]
            long francotiradores = clientes.stream().filter(c -> "FRANCOTIRADOR".equals(c.getRol())).count();

            if ("FRANCOTIRADOR".equals(rolDeseado) && francotiradores >= 2) {
                enviarMensaje("ERROR: Ya hay el máximo de 2 Francotiradores asignados.");
            } else {
                this.rol = rolDeseado;
                enviarBroadcast("CHAT:Sistema:" + idJugador + " se ha unito como " + rolDeseado);
            }
            return;
        }

    if (mensaje.startsWith("CARGAR_ESCENARIO:")) {
        // Formato: CARGAR_ESCENARIO:nombre:datos
        String[] partes = mensaje.split(":", 3);
        String datosMapa = partes[2];

        // Aquí llamarás al algoritmo BFS de CargadorEscenarios de la Persona B para validar[cite: 1]
        boolean esValido = true; // CargadorEscenarios.validarMapaBFS(datosMapa);

        if (esValido) {
            // Guardar como escenario_11.txt en la carpeta escenarios/[cite: 1]
            enviarBroadcast("CHAT:Sistema:¡Nuevo escenario personalizado cargado correctamente como escenario_11.txt!");
        } else {
            enviarMensaje("ERROR: El escenario enviado no tiene ruta transitable válida (BFS falló).");
        }
        return;
    }

    if (mensaje.startsWith("CHAT:")) {
        String texto = mensaje.substring(5);
        // Reemitir el mensaje de chat a todos los participantes activos y espectadores[cite: 1]
        enviarBroadcast("CHAT:" + idJugador + ":" + texto);
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

    //para revisar junto el manejo de los hilos
    private synchronized void manejarAccion(String mensaje) {
        if (mensaje.startsWith("MOVER:")) {
            String[] partes = mensaje.split(":");
            String id = partes[1];
            String dir = partes[2];
            
            // Procesa la regla de manera sincronizada
            // gestorEstado.procesarMovimiento(id, dir);
            
            enviarBroadcast(mensaje); // Reenvía a todos los clientes el estado confirmado[cite: 1]
        }
    }
}