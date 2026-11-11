/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.io.File;
import java.util.ArrayList;

public class PruebaZombi {

    public static void main(String[] args) throws Exception {
        char[][] mapa = CargadorEscenarios.cargar(new File("escenarios/escenario_01.txt"));
        ArrayList<Zombi> zombis = GeneradorZombis.generar(mapa, 4);

        System.out.println("Zombis generados: " + zombis.size());
        dibujar(mapa, zombis);

        for (int paso = 1; paso <= 100; paso++) {
            for (Zombi z : zombis) {
                z.mover(mapa);
                if (mapa[z.getX()][z.getY()] == CargadorEscenarios.PARED) {
                    System.out.println("¡ERROR! Un zombi quedó dentro de una pared");
                    return;
                }
            }
        }

        System.out.println("Después de 100 pasos:");
        dibujar(mapa, zombis);
    }

    // Imprime el mapa con una Z donde hay un zombi
    private static void dibujar(char[][] mapa, ArrayList<Zombi> zombis) {
        char[][] copia = new char[mapa.length][];
        for (int i = 0; i < mapa.length; i++) {
            copia[i] = mapa[i].clone();
        }
        for (Zombi z : zombis) {
            copia[z.getX()][z.getY()] = 'Z';
        }
        for (char[] fila : copia) {
            System.out.println(new String(fila));
        }
        System.out.println();
    }
}
