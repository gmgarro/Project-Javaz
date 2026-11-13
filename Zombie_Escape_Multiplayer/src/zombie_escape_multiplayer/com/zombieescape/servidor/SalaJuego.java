/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.servidor;


import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import zombie_escape_multiplayer.com.zombieescape.modelo.CargadorEscenarios;
import zombie_escape_multiplayer.com.zombieescape.modelo.Direccion;
import zombie_escape_multiplayer.com.zombieescape.modelo.Francotirador;
import zombie_escape_multiplayer.com.zombieescape.modelo.Partida;

/**
 * La sala de juego del servidor.
 *
 * Aquí está TODA la lógica del servidor: quién se une y con qué rol, cuándo
 * empieza la partida, los movimientos, los disparos del francotirador y la
 * carga de escenarios nuevos. Los ClientHandler solo le pasan los mensajes
 * que reciben (SalaJuego.procesar) y avisan cuando alguien se desconecta.
 *
 * Todo es "static" porque solo existe UNA sala para todo el servidor.
 */
public class SalaJuego {

    private static final int MAX_FRANCOTIRADORES = 2;
    private static final double PROBABILIDAD_DESVIO = 0.15;   // 15 % de desvío

    // Un cliente conectado que ya se unió (corredor o francotirador)
    private static class Participante {
        ClientHandler handler;
        String nombre;
        String rol;                 // "CORREDOR" o "FRANCOTIRADOR"
        Francotirador tirador;      // solo si es francotirador y ya empezó la partida

        // Zona del mapa que puede ver y a la que puede disparar (x = fila, y = columna)
        int filaIni, filaFin, colIni, colFin;

        Participante(ClientHandler handler, String nombre, String rol) {
            this.handler = handler;
            this.nombre = nombre;
            this.rol = rol;
        }

        boolean puedeApuntarA(int x, int y) {
            return x >= filaIni && x <= filaFin && y >= colIni && y <= colFin;
        }
    }

    private static CopyOnWriteArrayList<Participante> participantes = new CopyOnWriteArrayList<>();
    private static Partida partida;

    
    //  ENTRADA: aquí llega cada mensaje de los clientes
    

    // Devuelve true si el mensaje era del juego y ya fue atendido.
    public static synchronized boolean procesar(ClientHandler origen, String mensaje) {
        if (mensaje.startsWith("UNIRSE:")) {
            unirse(origen, mensaje.substring(7));
        } else if (mensaje.startsWith("INICIAR")) {
            iniciar(origen);
        } else if (mensaje.startsWith("CHAT:")) {
            chat(origen, mensaje.substring(5));
        } else if (mensaje.startsWith("MOVER:")) {
            mover(origen, mensaje);
        } else if (mensaje.startsWith("TOGGLE_ESCONDER")) {
            esconder(origen);
        } else if (mensaje.startsWith("DISPARO:")) {
            disparar(origen, mensaje);
        } else if (mensaje.startsWith("CARGAR_ESCENARIO:")) {
            cargarEscenario(origen, mensaje);
        } else {
            return false;
        }
        return true;
    }

    // Lo llama ClientHandler cuando un cliente se desconecta
    public static synchronized void desconectar(ClientHandler handler) {
        Participante p = buscar(handler);
        if (p == null) {
            return;
        }
        participantes.remove(p);
        if (partidaEnCurso() && p.rol.equals("CORREDOR")) {
            partida.quitarJugador(p.nombre);       // avisa DESCONEXION a todos
        }
        enviarATodos("CHAT:Sistema:" + p.nombre + " se desconectó.");

        if (participantes.isEmpty() && partida != null) {
            partida.terminar();
            partida = null;
        }
    }

    
    //  LOBBY: unirse, chat, iniciar
    

    private static void unirse(ClientHandler origen, String datos) {
        // Formato: nombre/ROL
        String[] partes = datos.split("/");
        if (partes.length < 2) {
            origen.enviarMensaje("ERROR: Formato de UNIRSE inválido.");
            return;
        }
        String nombre = partes[0].replace(":", "").trim();
        String rol = partes[1].trim();

        if (nombre.isEmpty() || nombre.equalsIgnoreCase("Sistema")) {
            origen.enviarMensaje("ERROR: Nombre de jugador no válido.");
            return;
        }
        if (!rol.equals("CORREDOR") && !rol.equals("FRANCOTIRADOR")) {
            origen.enviarMensaje("ERROR: Rol no válido.");
            return;
        }
        if (partidaEnCurso()) {
            origen.enviarMensaje("ERROR: La partida ya empezó.");
            return;
        }

        Participante actual = buscar(origen);

        // Nombre repetido (el nombre es el identificador del jugador)
        for (Participante otro : participantes) {
            if (otro != actual && otro.nombre.equalsIgnoreCase(nombre)) {
                origen.enviarMensaje("ERROR: Ya hay un jugador llamado " + nombre
                        + ". Reconéctate con otro nombre.");
                return;
            }
        }

        // Máximo 2 francotiradores
        if (rol.equals("FRANCOTIRADOR") && contarFrancotiradores(actual) >= MAX_FRANCOTIRADORES) {
            origen.enviarMensaje("ERROR: Ya hay el máximo de " + MAX_FRANCOTIRADORES
                    + " francotiradores.");
            if (actual != null) {
                rol = actual.rol;           // conserva el rol que ya tenía
            } else {
                rol = "CORREDOR";           // entra como corredor
            }
        }

        if (actual == null) {
            participantes.add(new Participante(origen, nombre, rol));
            enviarATodos("CHAT:Sistema:" + nombre + " se unió como " + rol);
        } else if (!actual.rol.equals(rol)) {
            actual.rol = rol;
            enviarATodos("CHAT:Sistema:" + nombre + " ahora es " + rol);
        }
        origen.enviarMensaje("ROL:" + nombre + ":" + rol);   // confirma el rol al cliente
    }

    private static void chat(ClientHandler origen, String texto) {
        Participante p = buscar(origen);
        if (p != null) {
            enviarATodos("CHAT:" + p.nombre + ":" + texto);
        }
    }

    private static void iniciar(ClientHandler origen) {
        if (partidaEnCurso()) {
            origen.enviarMensaje("ERROR: La partida ya está en curso.");
            return;
        }
        // cuenta los roles antes de dejar empezar
        int corredores = 0;
        int francotiradores = 0;
        for (Participante p : participantes) {
            if (p.rol.equals("CORREDOR")) {
                corredores++;
            } else {
                francotiradores++;
            }
        }
        if (corredores < 1) {
            origen.enviarMensaje("ERROR: Hace falta al menos 1 corredor para iniciar la partida.");
            return;
        }
        if (francotiradores < 1) {
            origen.enviarMensaje("ERROR: Hace falta al menos 1 francotirador para iniciar la partida.");
            return;
        }
        if (francotiradores > MAX_FRANCOTIRADORES) {
            origen.enviarMensaje("ERROR: Solo se permiten " + MAX_FRANCOTIRADORES + " francotiradores como máximo.");
            return;
        }

        if (corredores < 1) {
            origen.enviarMensaje("ERROR: Hace falta al menos 1 corredor para iniciar la partida.");
            return;
        }

        partida = new Partida(mensaje -> enviarDesdePartida(mensaje));
        int numeroTirador = 0;
        for (Participante p : participantes) {
            if (p.rol.equals("CORREDOR")) {
                p.tirador = null;
                partida.agregarJugador(p.nombre);
            } else {
                numeroTirador++;
                p.tirador = new Francotirador(p.nombre, numeroTirador);
            }
        }
        partida.iniciar();
    }

    
    //  JUEGO: mover, esconderse, disparar
    

    private static void mover(ClientHandler origen, String mensaje) {
        Participante p = buscar(origen);
        if (p == null || !p.rol.equals("CORREDOR") || !partidaEnCurso()) {
            return;
        }
        // Formato: MOVER:id:DIRECCION  (usamos el nombre real del cliente, no el del mensaje)
        String[] partes = mensaje.split(":");
        if (partes.length < 3) {
            return;
        }
        try {
            Direccion dir = Direccion.valueOf(partes[2]);
            partida.mover(p.nombre, dir);
        } catch (IllegalArgumentException e) {
            origen.enviarMensaje("ERROR: Dirección no válida.");
        }
    }

    private static void esconder(ClientHandler origen) {
        Participante p = buscar(origen);
        if (p != null && p.rol.equals("CORREDOR") && partidaEnCurso()) {
            partida.alternarEsconder(p.nombre);
        }
    }

    private static void disparar(ClientHandler origen, String mensaje) {
        Participante p = buscar(origen);
        if (!partidaEnCurso()) {
            origen.enviarMensaje("ERROR: No hay una partida en curso.");
            return;
        }
        if (p == null || p.tirador == null) {
            origen.enviarMensaje("ERROR: Acción no permitida. No eres francotirador.");
            return;
        }

        // Formato: DISPARO:x:y
        int x;
        int y;
        try {
            String[] partes = mensaje.split(":");
            x = Integer.parseInt(partes[1]);
            y = Integer.parseInt(partes[2]);
        } catch (RuntimeException e) {
            origen.enviarMensaje("ERROR: Disparo mal formado.");
            return;
        }

        // 1. ¿Está dentro de su cuadrante?
        if (!p.puedeApuntarA(x, y)) {
            origen.enviarMensaje("ERROR: Ese punto está fuera de tu cuadrante.");
            return;
        }
        // 2. ¿Le quedan balas? (disparar() descuenta una si hay)
        if (!p.tirador.disparar()) {
            origen.enviarMensaje("ERROR_DISPARO: Te quedaste sin munición.");
            return;
        }

        // 3. 15 % de probabilidad de desvío: x ± 1, y ± 1
        if (Math.random() < PROBABILIDAD_DESVIO) {
            x = x + (int) (Math.random() * 3) - 1;    // -1, 0 o 1
            y = y + (int) (Math.random() * 3) - 1;
        }

        // 4. ¿Había un zombi ahí? (si lo hay, la Partida lo mata y avisa a todos)
        boolean acierto = partida.matarZombiEn(x, y);

        origen.enviarMensaje("AMMO:" + p.nombre + ":" + p.tirador.getAmmo());
        if (acierto) {
            enviarATodos("CHAT:Sistema:¡" + p.nombre + " eliminó a un zombi!");
        } else {
            origen.enviarMensaje("CHAT:Sistema:Disparo fallido. Balas restantes: "
                    + p.tirador.getAmmo());
        }
    }

    
    //  ESCENARIOS NUEVOS (escenario 11, 12...)
    

    private static void cargarEscenario(ClientHandler origen, String mensaje) {
        // Formato: CARGAR_ESCENARIO:nombreArchivo:filas separadas por ;
        String[] partes = mensaje.split(":", 3);
        if (partes.length < 3) {
            origen.enviarMensaje("ERROR: Escenario mal formado.");
            return;
        }
        try {
            int nivel = CargadorEscenarios.guardarEscenarioNuevo(partes[2]);
            enviarATodos("CHAT:Sistema:¡Escenario guardado como escenario_" + nivel
                    + ".txt! Se jugará después de los anteriores.");
        } catch (IllegalArgumentException e) {
            origen.enviarMensaje("ERROR: El escenario no es válido. " + e.getMessage());
        } catch (IOException e) {
            origen.enviarMensaje("ERROR: No se pudo guardar el escenario en el servidor.");
        }
    }

    
    //  Mensajes que salen de la Partida
    

    // La Partida manda aquí todo lo que quiere avisar (MAPA, ESTADO, ZOMBI...)
    private static void enviarDesdePartida(String mensaje) {
        enviarATodos(mensaje);
        if (mensaje.startsWith("MAPA:")) {
            prepararFrancotiradores(mensaje);     // cada nivel nuevo: balas y zona
        }
    }

    // En cada escenario los francotiradores recuperan sus 10 balas y se les dice qué zona ven.
    // Con 1 francotirador ve todo el mapa; con 2, uno ve la mitad izquierda y otro la derecha.
    private static void prepararFrancotiradores(String mensajeMapa) {
        String[] partes = mensajeMapa.split(":");      // MAPA:nivel:filas:columnas:datos
        int filas = Integer.parseInt(partes[2]);
        int columnas = Integer.parseInt(partes[3]);

        int total = 0;
        for (Participante p : participantes) {
            if (p.tirador != null) {
                total++;
            }
        }

        for (Participante p : participantes) {
            if (p.tirador == null) {
                continue;
            }
            p.tirador.recargar();
            p.filaIni = 0;
            p.filaFin = filas - 1;
            if (total == 1) {
                p.colIni = 0;
                p.colFin = columnas - 1;
            } else if (p.tirador.getCuadranteAsignado() == 1) {
                p.colIni = 0;
                p.colFin = columnas / 2 - 1;
            } else {
                p.colIni = columnas / 2;
                p.colFin = columnas - 1;
            }
            p.handler.enviarMensaje("VISTA:" + p.nombre + ":" + p.filaIni + ":" + p.filaFin
                    + ":" + p.colIni + ":" + p.colFin);
            p.handler.enviarMensaje("AMMO:" + p.nombre + ":" + p.tirador.getAmmo());
        }
    }

    
    //  Ayudas
    

    private static void enviarATodos(String mensaje) {
        for (Participante p : participantes) {
            p.handler.enviarMensaje(mensaje);
        }
    }

    private static Participante buscar(ClientHandler handler) {
        for (Participante p : participantes) {
            if (p.handler == handler) {
                return p;
            }
        }
        return null;
    }

    // Cuenta los francotiradores, sin contar al participante "excepto" (para cambios de rol)
    private static int contarFrancotiradores(Participante excepto) {
        int total = 0;
        for (Participante p : participantes) {
            if (p != excepto && p.rol.equals("FRANCOTIRADOR")) {
                total++;
            }
        }
        return total;
    }

    private static boolean partidaEnCurso() {
        return partida != null && partida.isEnCurso();
    }
}
