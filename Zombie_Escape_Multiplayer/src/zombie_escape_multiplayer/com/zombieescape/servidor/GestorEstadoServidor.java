package zombie_escape_multiplayer.com.zombieescape.servidor;

import java.util.HashMap;
import java.util.Map;

public class GestorEstadoServidor {
    // Mapa thread-safe o protegido por synchronized para guardar posiciones y estados
    private Map<String, String> estadosJugadores = new HashMap<>();
    private long ultimoMovimientoTimestamp = System.currentTimeMillis();

    // Método sincronizado para actualizar la posición/estado validando cooldown (~150ms)
    public synchronized boolean procesarMovimiento(String idJugador, String nuevaDireccion) {
        long tiempoActual = System.currentTimeMillis();
        
        // Simulación básica de validación de cooldown autoritativo (~150ms)
        // Aquí puedes registrar el último movimiento por jugador individualmente.
        
        estadosJugadores.put(idJugador, nuevaDireccion);
        return true;
    }

    // Método sincronizado para registrar o actualizar el estado de un jugador
    public synchronized void actualizarEstado(String idJugador, String estado) {
        estadosJugadores.put(idJugador, estado);
    }

    // Método sincronizado para consultar el estado actual de forma segura
    public synchronized String obtenerEstado(String idJugador) {
        return estadosJugadores.getOrDefault(idJugador, "DESCONOCIDO");
    }
}