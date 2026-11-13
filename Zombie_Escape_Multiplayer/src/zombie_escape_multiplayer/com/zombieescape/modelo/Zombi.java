/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.util.Random;

public class Zombi {

    private static final Random RANDOM = new Random();

    private int id;
    private int x;          // fila
    private int y;          // columna
    private Direccion dir;
    private boolean vivo;

    public Zombi(int id, int x, int y, Direccion dir) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.vivo = true;
    }

    // Da UN paso. Más adelante, la Partida lo llamará cada ~600 ms.
    public void mover(char[][] mapa) {
        if (!vivo) {
            return;
        }

        int nuevoX = x + dir.getDx();
        int nuevoY = y + dir.getDy();

        if (puedePisar(mapa, nuevoX, nuevoY)) {
            x = nuevoX;
            y = nuevoY;
        } else {
            girar(mapa);
        }
    }

    // Se llama cuando el zombi chocó con una pared: elige otra dirección.
    private void girar(char[][] mapa) {
        Direccion[] opciones = new Direccion[4];
        int cantidad = 0;

        for (Direccion d : Direccion.values()) {
            if (d != dir.opuesta() && puedePisar(mapa, x + d.getDx(), y + d.getDy())) {
                opciones[cantidad] = d;
                cantidad++;
            }
        }

        if (cantidad > 0) {
            dir = opciones[RANDOM.nextInt(cantidad)];
        } else {
            dir = dir.opuesta();   // callejón sin salida: se devuelve
        }
    }

    // ¿Se puede pisar la casilla (px, py)? No si se sale del mapa o es pared.
    private boolean puedePisar(char[][] mapa, int px, int py) {
        if (px < 0 || px >= mapa.length) {
            return false;
        }
        if (py < 0 || py >= mapa[0].length) {
            return false;
        }
        // La zona segura (S) cuenta como pared para los zombis: no entran ahí
        return mapa[px][py] != CargadorEscenarios.PARED
            && mapa[px][py] != CargadorEscenarios.ENTRADA
            && mapa[px][py] != CargadorEscenarios.SALIDA;
    }

    public void matar() { vivo = false; }
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Direccion getDir() { return dir; }
    public boolean isVivo() { return vivo; }
}
