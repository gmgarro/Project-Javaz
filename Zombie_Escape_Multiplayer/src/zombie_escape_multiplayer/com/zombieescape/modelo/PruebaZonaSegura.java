/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class PruebaZonaSegura {

    static int fallos = 0;

    public static void main(String[] args) throws Exception {

        // ---- 1. Un zombi que patrulla hacia la zona S se desvía en vez de entrar ----
        char[][] mapa = CargadorEscenarios.construirMatriz(Arrays.asList(
            "#########",
            "#SSS....#",
            "#SSS.##.#",
            "#SSS....#",
            "#########"));
        Zombi z = new Zombi(1, 1, 6, Direccion.IZQUIERDA);     // camina hacia la zona S
        boolean pisoS = false;
        for (int paso = 0; paso < 200; paso++) {
            z.mover(mapa);
            if (mapa[z.getX()][z.getY()] == CargadorEscenarios.ENTRADA) {
                pisoS = true;
            }
        }
        comprobar("El zombi nunca pisa la zona segura (200 pasos)", false, pisoS);

        // Al llegar al borde de S, se desvía (gira) en lugar de quedarse pegado
        z = new Zombi(2, 1, 4, Direccion.IZQUIERDA);           // pegado al borde de S, mirando hacia ella
        z.mover(mapa);
        comprobar("Al toparse con S gira hacia otra dirección", false, z.getDir() == Direccion.IZQUIERDA);

        // ---- 2. En los 10 escenarios reales, con muchos zombis y muchos pasos ----
        int total = CargadorEscenarios.contarEscenarios();
        boolean algunoEntro = false;
        for (int nivel = 1; nivel <= total; nivel++) {
            char[][] m = CargadorEscenarios.cargarNivel(nivel);
            ArrayList<Zombi> zombis = GeneradorZombis.generar(m, 12);
            for (int paso = 0; paso < 1500; paso++) {
                for (Zombi zombi : zombis) {
                    zombi.mover(m);
                    if (m[zombi.getX()][zombi.getY()] == CargadorEscenarios.ENTRADA) {
                        algunoEntro = true;
                    }
                }
            }
        }
        comprobar("En los " + total + " escenarios ningún zombi entra a la zona segura", false, algunoEntro);

        System.out.println(fallos == 0 ? "TODO OK" : "FALLOS: " + fallos);
    }

    static void comprobar(String nombre, boolean esperado, boolean real) {
        String resultado = (esperado == real) ? "OK     " : "FALLO  ";
        if (esperado != real) {
            fallos++;
        }
        System.out.println(resultado + nombre);
    }
}
