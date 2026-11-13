/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.util.ArrayList;
import java.util.Random;

public class GeneradorZombis {

    private static final Random RANDOM = new Random();
    private static final int DISTANCIA_MINIMA_S = 8;   // lejos de la zona de jugadores
    private static final int DISTANCIA_MINIMA_X = 3;   // sin tapar la salida

    public static ArrayList<Zombi> generar(char[][] mapa, int cantidad) {
        // 1. Juntar todas las casillas donde SÍ puede aparecer un zombi
        ArrayList<int[]> candidatas = new ArrayList<>();

        for (int x = 0; x < mapa.length; x++) {
            for (int y = 0; y < mapa[x].length; y++) {
                if (mapa[x][y] == CargadorEscenarios.CAMINO && esSegura(mapa, x, y)) {
                    candidatas.add(new int[]{x, y});
                }
            }
        }

        // 2. Sacar al azar tantas como zombis se pidieron
        ArrayList<Zombi> zombis = new ArrayList<>();
        int id = 1;
        while (zombis.size() < cantidad && !candidatas.isEmpty()) {
            int posicion = RANDOM.nextInt(candidatas.size());
            int[] celda = candidatas.remove(posicion);
            Direccion dir = direccionLibre(mapa, celda[0], celda[1]);
            zombis.add(new Zombi(id, celda[0], celda[1], dir));
            id++;
        }
        return zombis;
    }

    // Una casilla es segura si está lejos de toda S y de la X.
    private static boolean esSegura(char[][] mapa, int x, int y) {
        for (int i = 0; i < mapa.length; i++) {
            for (int j = 0; j < mapa[i].length; j++) {
                int distancia = Math.abs(i - x) + Math.abs(j - y);
                if (mapa[i][j] == CargadorEscenarios.ENTRADA && distancia < DISTANCIA_MINIMA_S) {
                    return false;
                }
                if (mapa[i][j] == CargadorEscenarios.SALIDA && distancia < DISTANCIA_MINIMA_X) {
                    return false;
                }
            }
        }
        return true;
    }

    // Elige una dirección en la que el zombi pueda dar su primer paso.
    private static Direccion direccionLibre(char[][] mapa, int x, int y) {
        Direccion[] libres = new Direccion[4];
        int cantidad = 0;
        for (Direccion d : Direccion.values()) {
            int nx = x + d.getDx();
            int ny = y + d.getDy();
                if (nx >= 0 && nx < mapa.length && ny >= 0 && ny < mapa[0].length
                    && mapa[nx][ny] != CargadorEscenarios.PARED
                    && mapa[nx][ny] != CargadorEscenarios.ENTRADA) {
                libres[cantidad] = d;
                cantidad++;
            }
        }
        if (cantidad == 0) {
            return Direccion.DERECHA;
        }
        return libres[RANDOM.nextInt(cantidad)];
    }
}
