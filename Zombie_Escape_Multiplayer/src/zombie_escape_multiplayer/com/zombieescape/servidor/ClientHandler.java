package zombie_escape_multiplayer.com.zombieescape.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import zombie_escape_multiplayer.com.zombieescape.cliente.VentanaPrincipal;
import zombie_escape_multiplayer.com.zombieescape.modelo.CargadorEscenarios; 

     
public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private CopyOnWriteArrayList<ClientHandler> clientes;
    private String idJugador = "Desconocido";
    private String rol = "CORREDOR"; // Por defecto empiezan como corredores

    private VentanaPrincipal ventana;
    
    public void serVentana(VentanaPrincipal ventana){
        this.ventana = ventana;
    }
    
    // Atributos específicos si este cliente es Francotirador (10 balas por escenario)
    private int municionFrancotirador = 10;

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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getIdJugador() {
        return idJugador;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensajeRecibido = entrada.readUTF();
                procesarMensajeAutoritativo(mensajeRecibido);
            }
        } catch (IOException e) {
            System.out.println("Jugador desconectado de la red.");
        } finally {
            removerCliente();
        }
    }

    // Actúa como la Autoridad Central del juego: valida reglas, turnos e intenciones
    private void procesarMensajeAutoritativo(String mensaje) {
        
        // Comando para iniciar la partida
        // Comando para iniciar la partida
        if (mensaje.startsWith("INICIAR")) {
            if (clientes.size() < 2) {
                enviarMensaje("ERROR: Se necesitan al menos 2 jugadores para iniciar la partida.");
                return;
            }
            
            // 1. Indicar a las ventanas de los clientes que cambió a la pantalla de juego
            enviarBroadcast("PARTIDA_INICIADA");
            
            try {
                // 2. Cargar la matriz del nivel 1 usando la clase CargadorEscenarios del proyecto
                char[][] mapaMatriz = CargadorEscenarios.cargarNivel(1);
                int filas = mapaMatriz.length;
                int columnas = mapaMatriz[0].length;
                
                // 3. Convertir la matriz bidimensional a un formato de texto plano separado por ';'
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < filas; i++) {
                    sb.append(new String(mapaMatriz[i]));
                    if (i < filas - 1) {
                        sb.append(";");
                    }
                }
                
                // 4. Enviar el comando MAPA estandarizado a todos los clientes conectados
                enviarBroadcast("MAPA:1:" + filas + ":" + columnas + ":" + sb.toString());
                
            } catch (Exception e) {
                enviarMensaje("ERROR: No se pudo cargar el archivo del mapa en el servidor.");
                e.printStackTrace();
            }
            return;
        }

        // Configuración inicial de nombre y rol en el Lobby
        if (mensaje.startsWith("UNIRSE:")) {
            // Estructura esperada: UNIRSE:NombreJugador/CORREDOR o FRANCOTIRADOR
            String[] partes = mensaje.substring(7).split("/");
            this.idJugador = partes[0];
            String rolDeseado = partes[1];

            // Validar que no se pasen del límite de 2 francotiradores permitidos
            long francotiradoresActuales = clientes.stream()
                    .filter(c -> "FRANCOTIRADOR".equals(c.getRol()) && c != this)
                    .count();

            if ("FRANCOTIRADOR".equals(rolDeseado) && francotiradoresActuales >= 2) {
                enviarMensaje("ERROR: Ya hay el máximo de 2 Francotiradores asignados.");
            } else {
                this.rol = rolDeseado;
                enviarBroadcast("CHAT:Sistema:" + idJugador + " se unió como " + rolDeseado);
            }
            return;
        }

        // Carga de escenarios personalizados y validación con BFS
        if (mensaje.startsWith("CARGAR_ESCENARIO:")) {
            String[] partes = mensaje.split(":", 3);
            String datosMapa = partes[2];

            // Aquí luego conectas el validador BFS de tu compañero (Persona B)
            boolean escenarioValido = true; 

            if (escenarioValido) {
                enviarBroadcast("CHAT:Sistema:¡Nuevo escenario personalizado cargado como escenario_11.txt!");
            } else {
                enviarMensaje("ERROR: El mapa no tiene una ruta transitable válida.");
            }
            return;
        }

        // Gestión del chat general en tiempo real
        if (mensaje.startsWith("CHAT:")) {
            String textoMensaje = mensaje.substring(5);
            enviarBroadcast("CHAT:" + idJugador + ":" + textoMensaje);
            return;
        }

        // Movimiento de corredores en el mapa
        if (mensaje.startsWith("MOVER:")) {
            manejarMovimiento(mensaje);
            return;
        }

        // Lógica autoritativa del disparo del Francotirador (Paso 4)
        if (mensaje.startsWith("DISPARO:")) {
            manejarDisparoFrancotirador(mensaje);
            return;
        }
    }

    // Valida y procesa el movimiento de los corredores
    private synchronized void manejarMovimiento(String mensaje) {
        // Aquí puedes agregar la validación de cooldown o posición si lo requieres
        enviarBroadcast(mensaje); // Reenvía el movimiento confirmado a todos
    }

    // Reglas autoritativas para el disparo del francotirador
    private synchronized void manejarDisparoFrancotirador(String mensaje) {
        // Validar que el que dispara realmente sea francotirador
        if (!"FRANCOTIRADOR".equals(this.rol)) {
            enviarMensaje("ERROR: Accion no permitida. No tienes el rol de francotirador.");
            return;
        }

        // Validar munición (máximo 10 balas por partida/escenario)
        if (municionFrancotirador <= 0) {
            enviarMensaje("ERROR_DISPARO: Te has quedado sin munición (0 balas).");
            return;
        }

        // Descontar una bala de forma autoritativa
        municionFrancotirador--;

        // Extraer coordenadas enviadas por el cliente
        String[] partes = mensaje.split(":");
        int xOriginal = Integer.parseInt(partes[1]);
        int yOriginal = Integer.parseInt(partes[2]);

        // Aplicar el 15% de desvío aleatorio exigido por el documento (x ± 1, y ± 1)
        int xFinal = xOriginal;
        int yFinal = yOriginal;
        
        double probabilidadDesvio = Math.random();
        if (probabilidadDesvio <= 0.15) {
            int desplazamientoX = (int) (Math.random() * 3) - 1; // Genera -1, 0 o 1
            int desplazamientoY = (int) (Math.random() * 3) - 1; // Genera -1, 0 o 1
            xFinal += desplazamientoX;
            yFinal += desplazamientoY;
            System.out.println("Disparo desviado por viento. Ajustado a: X=" + xFinal + ", Y=" + yFinal);
        }

        // Verificar si impacta a un zombi en esa celda exacta
        boolean impactoZombi = verificarImpactoEnZombi(xFinal, yFinal);

        if (impactoZombi) {
            enviarBroadcast("ZOMBI_ELIMINADO:" + xFinal + ":" + yFinal);
            enviarBroadcast("CHAT:Sistema:¡" + idJugador + " acertó un disparo! Balas restantes: " + municionFrancotirador);
        } else {
            enviarBroadcast("CHAT:Sistema:" + idJugador + " disparó pero falló. Balas restantes: " + municionFrancotirador);
        }
    }

    // Comprobación temporal de impacto en zombis (se conecta con la matriz de la Persona B)
    private boolean verificarImpactoEnZombi(int x, int y) {
        // De momento retorna true para pruebas; luego consultará la lista de zombis activos
        return true;
    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeUTF(mensaje);
            salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Envía un mensaje a todos los clientes conectados a la sala
    public void enviarBroadcast(String mensaje) {
        for (ClientHandler cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }

    private void removerCliente() {
        clientes.remove(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Cliente desconectado y removido. Quedan " + clientes.size() + " jugadores.");
    }
    
    private void procesarMensajeEntrante(String mensaje) {
        System.out.println("Mensaje recibido del servidor: " + mensaje);
        
        // Procesar mensajes del chat multijugador
        if (mensaje.startsWith("CHAT:")) {
            // Formato que emite el servidor: CHAT:Remitente:Texto
            String[] partes = mensaje.split(":", 3);
            if (partes.length >= 3) {
                String emisor = partes[1];
                String texto = partes[2];
                
                // Si la ventana principal y el lobby están activos, pintar el mensaje
                if (ventana != null && ventana.getPanelLobby() != null && ventana.getPanelLobby().getPanelChat() != null) {
                    ventana.getPanelLobby().getPanelChat().agregarMensaje(emisor, texto);
                }
            }
        }
    }
}