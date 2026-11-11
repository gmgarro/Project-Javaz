/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.util.ArrayList;

public class GestorPuntuacion {

    // Puntaje por llegar a la salida: más rápido = más puntos (mínimo 100)
    public static int calcularPuntaje(int tiempoEscape) {
        return Math.max(1000 - tiempoEscape * 10, 100);
    }

    // Devuelve una copia de la lista ordenada de mayor a menor puntaje
    public static ArrayList<Jugador> ranking(ArrayList<Jugador> jugadores) {
        ArrayList<Jugador> orden = new ArrayList<>(jugadores);

        for (int i = 0; i < orden.size() - 1; i++) {
            int mejor = i;
            for (int j = i + 1; j < orden.size(); j++) {
                if (orden.get(j).getPuntaje() > orden.get(mejor).getPuntaje()) {
                    mejor = j;
                }
            }
            Jugador temporal = orden.get(i);
            orden.set(i, orden.get(mejor));
            orden.set(mejor, temporal);
        }
        return orden;
    }
}
