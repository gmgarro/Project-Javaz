/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


public enum Direccion {

    ARRIBA(-1, 0),
    ABAJO(1, 0),
    IZQUIERDA(0, -1),
    DERECHA(0, 1);

    private final int dx;   // cuánto cambia x (la fila)
    private final int dy;   // cuánto cambia y (la columna)

    Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public Direccion opuesta() {
        if (this == ARRIBA) {
            return ABAJO;
        } else if (this == ABAJO) {
            return ARRIBA;
        } else if (this == IZQUIERDA) {
            return DERECHA;
        } else {
            return IZQUIERDA;
        }
    }
}