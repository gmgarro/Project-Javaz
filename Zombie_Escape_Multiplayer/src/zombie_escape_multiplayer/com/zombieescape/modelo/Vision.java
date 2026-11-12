/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;

import java.util.ArrayList;

public class Vision {

    // El alcance empieza en 2 casillas y sube despacio:
    // nivel 1 -> 2, nivel 4 -> 4, nivel 6 -> 5, nivel 8 -> 6, nivel 10 -> 7
    public static int alcance(int nivel) {
        return 2 + nivel / 2;
    }

    // Casillas que ve un zombi parado en (x, y) mirando hacia 'dir'.
    // La línea de visión se corta al chocar con una pared o salir del mapa.
    // La usan el servidor (para matar) y la pantalla (para pintarlas de rojo).
    public static ArrayList<int[]> casillasVisibles(char[][] mapa, int x, int y,
            Direccion dir, int nivel) {

        ArrayList<int[]> casillas = new ArrayList<>();
        int alcance = alcance(nivel);

        for (int paso = 1; paso <= alcance; paso++) {
            int casillaX = x + dir.getDx() * paso;
            int casillaY = y + dir.getDy() * paso;

            boolean fuera = casillaX < 0 || casillaX >= mapa.length
                         || casillaY < 0 || casillaY >= mapa[0].length;
            if (fuera || mapa[casillaX][casillaY] == CargadorEscenarios.PARED) {
                break;         // el rayo chocó con una pared: se detiene
            }
            casillas.add(new int[]{casillaX, casillaY});
        }
        return casillas;
    }

    // Un jugador está protegido (los zombis no lo ven) si:
    // está escondido, ya murió, ya escapó o está en la zona segura de entrada (S).
    public static boolean estaProtegido(Jugador jugador, char[][] mapa) {
        if (jugador.getEstado() != EstadoJugador.VIVO) {
            return true;       // escondido o muerto
        }
        if (jugador.isLlegoASalida()) {
            return true;       // ya escapó
        }
        return mapa[jugador.getX()][jugador.getY()] == CargadorEscenarios.ENTRADA;
    }

    // ¿Este zombi ve a este jugador?
    public static boolean zombiVe(Zombi zombi, Jugador jugador, char[][] mapa, int nivel) {
        if (!zombi.isVivo() || estaProtegido(jugador, mapa)) {
            return false;
        }

        // En la misma casilla del zombi
        if (jugador.getX() == zombi.getX() && jugador.getY() == zombi.getY()) {
            return true;
        }

        // En alguna de las casillas que el zombi ve
        for (int[] casilla : casillasVisibles(mapa, zombi.getX(), zombi.getY(), zombi.getDir(), nivel)) {
            if (casilla[0] == jugador.getX() && casilla[1] == jugador.getY()) {
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
