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

        // El alcance empieza chico y sube despacio
        comprobar("Alcance nivel 1 = 2", true, Vision.alcance(1) == 2);
        comprobar("Alcance nivel 4 = 4", true, Vision.alcance(4) == 4);
        comprobar("Alcance nivel 10 = 7", true, Vision.alcance(10) == 7);

        // Zombi en (1,2) mirando a la DERECHA. Nivel 1 -> alcance 2 (llega hasta y=4)
        Zombi zombi = new Zombi(1, 1, 2, Direccion.DERECHA);

        comprobar("Jugador a 1 casilla, de frente", true,  ve(zombi, 1, 3, mapa));
        comprobar("Jugador a 2 casillas (limite)",  true,  ve(zombi, 1, 4, mapa));
        comprobar("Jugador a 3 casillas (lejos)",   false, ve(zombi, 1, 5, mapa));
        comprobar("Jugador detras del zombi",       false, ve(zombi, 1, 1, mapa));
        comprobar("Jugador en otra fila",           false, ve(zombi, 2, 3, mapa));
        comprobar("Nivel 10 (alcance 7) ve a 7 casillas", true,
                Vision.zombiVe(zombi, nuevo(1, 9), mapa, 10));

        // Pared en medio: zombi en (2,1) mirando a la derecha, pared en (2,4)
        Zombi zombi2 = new Zombi(2, 2, 1, Direccion.DERECHA);
        comprobar("Jugador a la vista antes de la pared", true,
                Vision.zombiVe(zombi2, nuevo(2, 3), mapa, 1));
        comprobar("Jugador detras de una pared (aunque alcance de sobra)", false,
                Vision.zombiVe(zombi2, nuevo(2, 6), mapa, 10));

        // Casillas visibles (lo que se pinta de rojo en pantalla)
        ArrayList<int[]> casillas = Vision.casillasVisibles(mapa, 1, 2, Direccion.DERECHA, 1);
        comprobar("Nivel 1: el zombi ve 2 casillas", true, casillas.size() == 2);
        comprobar("La pared corta la vision (solo 2 casillas antes de la pared)", true,
                Vision.casillasVisibles(mapa, 2, 1, Direccion.DERECHA, 10).size() == 2);

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

        // Zona segura (S): nadie lo ve ahí, aunque esté de frente y cerca
        char[][] mapaConS = CargadorEscenarios.construirMatriz(Arrays.asList(
            "###########",
            "#SSSS.....#",
            "###########"));
        Zombi zombi4 = new Zombi(4, 1, 5, Direccion.IZQUIERDA);   // mira hacia la zona S
        Jugador enZonaS = nuevo(1, 4);                            // pegado al zombi, dentro de S
        comprobar("En la zona segura S no lo ven (aunque este pegado)", false,
                Vision.zombiVe(zombi4, enZonaS, mapaConS, 1));
        ArrayList<Zombi> zs2 = new ArrayList<>();
        zs2.add(zombi4);
        ArrayList<Jugador> js2 = new ArrayList<>();
        js2.add(enZonaS);
        comprobar("En la zona segura S no muere", true,
                Vision.revisarMuertes(zs2, js2, mapaConS, 1).size() == 0
                && enZonaS.getEstado() == EstadoJugador.VIVO);
        Zombi zombi5 = new Zombi(5, 1, 8, Direccion.IZQUIERDA);
        comprobar("Un paso fuera de S si lo ven", true, Vision.zombiVe(zombi5, nuevo(1, 6), mapaConS, 1));

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
