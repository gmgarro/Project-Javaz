/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.util.ArrayList;

public class Vision {

    // El alcance crece con el nivel: nivel 1 -> 4 casillas, nivel 10 -> 13 casillas
    public static int alcance(int nivel) {
        return 3 + nivel;
    }

    // ¿Este zombi ve a este jugador?
    public static boolean zombiVe(Zombi zombi, Jugador jugador, char[][] mapa, int nivel) {
        if (!zombi.isVivo()) {
            return false;
        }
        if (jugador.getEstado() != EstadoJugador.VIVO) {
            return false;      // escondido o muerto: no lo ve
        }
        if (jugador.isLlegoASalida()) {
            return false;      // ya escapó
        }

        int alcance = alcance(nivel);
        for (int paso = 0; paso <= alcance; paso++) {
            int casillaX = zombi.getX() + zombi.getDir().getDx() * paso;
            int casillaY = zombi.getY() + zombi.getDir().getDy() * paso;

            boolean fuera = casillaX < 0 || casillaX >= mapa.length
                         || casillaY < 0 || casillaY >= mapa[0].length;
            if (fuera || mapa[casillaX][casillaY] == CargadorEscenarios.PARED) {
                return false;  // el rayo chocó con una pared: se detiene
            }
            if (casillaX == jugador.getX() && casillaY == jugador.getY()) {
                return true;
            }
        }
        return false;
    }

    // Revisa a todos los jugadores contra todos los zombis.
    // Marca como MUERTO a los vistos y devuelve la lista de los que murieron ahora.
    public static ArrayList<Jugador> revisarMuertes(ArrayList<Zombi> zombis,
            ArrayList<Jugador> jugadores, char[][] mapa, int nivel) {

        ArrayList<Jugador> muertos = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            for (Zombi zombi : zombis) {
                if (zombiVe(zombi, jugador, mapa, nivel)) {
                    jugador.setEstado(EstadoJugador.MUERTO);
                    muertos.add(jugador);
                    break;     // ya murió, no hace falta revisar más zombis
                }
            }
        }
        return muertos;
    }
}