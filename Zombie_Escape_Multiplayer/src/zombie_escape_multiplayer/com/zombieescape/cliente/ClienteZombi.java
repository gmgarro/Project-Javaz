package zombie_escape_multiplayer.com.zombieescape.cliente;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClienteZombi {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private String ipServidor;
    private int puerto;
    private boolean conectado;

    public ClienteZombi(String ipServidor, int puerto) {
        this.ipServidor = ipServidor;
        this.puerto = puerto;
    }

    public boolean conectar(String nombre, String rol) {
        try {
            socket = new Socket(ipServidor, puerto);
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());
            conectado = true;

            // Enviar mensaje inicial de unirse al lobby
            enviarMensaje("UNIRSE:" + nombre + "/" + rol);

            // Iniciar hilo para escuchar al servidor
            new Thread(new EscuchaServidor()).start();
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            if (conectado && salida != null) {
                salida.writeUTF(mensaje);
                salida.flush();
            }
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje: " + e.getMessage());
            desconectar();
        }
    }

    public void desconectar() {
        conectado = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hilo interno para leer respuestas y actualizar la UI de manera segura
    private class EscuchaServidor implements Runnable {
        @Override
        public void run() {
            try {
                while (conectado) {
                    String mensaje = entrada.readUTF();
                    
                    // Garantizar que las actualizaciones visuales pasen por SwingUtilities.invokeLater
                    SwingUtilities.invokeLater(() -> {
                        procesarMensajeEntrante(mensaje);
                    });
                }
            } catch (IOException e) {
                System.out.println("Se perdió la conexión con el servidor.");
                desconectar();
            }
        }
    }

    private void procesarMensajeEntrante(String mensaje) {
        // Aquí se interpretarán los comandos MAPA, ESTADO, ZOMBI, TIEMPO, CHAT, FIN, etc.
        System.out.println("Mensaje recibido del servidor: " + mensaje);
    }
}