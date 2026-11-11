/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.util.ArrayList;
import java.util.Arrays;

public class PruebaVision {

    static int fallos = 0;

    public static void main(String[] args) {
        char[][] mapa = CargadorEscenarios.construirMatriz(Arrays.asList(
            "############",
            "#..........#",
            "#...#...E..#",
            "############"));

        // Zombi en (1,2) mirando a la DERECHA. Nivel 1 -> alcance 4 (llega hasta y=6)
        Zombi zombi = new Zombi(1, 1, 2, Direccion.DERECHA);

        comprobar("Jugador a 3 casillas, de frente", true,  ve(zombi, 1, 5, mapa));
        comprobar("Jugador a 4 casillas (limite)",   true,  ve(zombi, 1, 6, mapa));
        comprobar("Jugador a 5 casillas (lejos)",    false, ve(zombi, 1, 7, mapa));
        comprobar("Jugador detras del zombi",        false, ve(zombi, 1, 1, mapa));
        comprobar("Jugador en otra fila",            false, ve(zombi, 2, 3, mapa));

        // Pared en medio: zombi en (2,1) mirando a la derecha, pared en (2,4)
        Zombi zombi2 = new Zombi(2, 2, 1, Direccion.DERECHA);
        comprobar("Jugador detras de una pared",     false, ve(zombi2, 2, 6, mapa));

        // Esconderse
        Jugador j = new Jugador("ana");
        j.colocarEn(2, 8);   // casilla E
        Zombi zombi3 = new Zombi(3, 2, 6, Direccion.DERECHA);
        comprobar("En escondite pero visible",       true,  Vision.zombiVe(zombi3, j, mapa, 1));
        j.alternarEsconder(mapa);
        comprobar("Escondido: estado ESCONDIDO",     true,  j.getEstado() == EstadoJugador.ESCONDIDO);
        comprobar("Escondido: el zombi no lo ve",    false, Vision.zombiVe(zombi3, j, mapa, 1));
        comprobar("Escondido: no puede moverse",     false, j.puedeMoverse());
        j.alternarEsconder(mapa);
        comprobar("Sale del escondite: VIVO",        true,  j.getEstado() == EstadoJugador.VIVO);

        Jugador k = new Jugador("beto");
        k.colocarEn(1, 4);   // casilla normal
        comprobar("Esconderse fuera de E no hace nada", false, k.alternarEsconder(mapa));

        // Muerte
        ArrayList<Zombi> zs = new ArrayList<>();
        zs.add(zombi3);
        ArrayList<Jugador> js = new ArrayList<>();
        js.add(j);
        ArrayList<Jugador> muertos = Vision.revisarMuertes(zs, js, mapa, 1);
        comprobar("revisarMuertes mata al visible",  true,  muertos.size() == 1 && j.getEstado() == EstadoJugador.MUERTO);

        // Nivel mayor, más alcance
        comprobar("Nivel 3 (alcance 6) ve a 5 casillas", true, Vision.zombiVe(zombi, nuevo(1, 7), mapa, 3));

        System.out.println(fallos == 0 ? "TODO OK" : "FALLOS: " + fallos);
    }

    static Jugador nuevo(int x, int y) {
        Jugador j = new Jugador("t");
        j.colocarEn(x, y);
        return j;
    }

    static boolean ve(Zombi z, int x, int y, char[][] mapa) {
        return Vision.zombiVe(z, nuevo(x, y), mapa, 1);
    }

    static void comprobar(String nombre, boolean esperado, boolean real) {
        String resultado = (esperado == real) ? "OK     " : "FALLO  ";
        if (esperado != real) {
            fallos++;
        }
        System.out.println(resultado + nombre);
    }
}
