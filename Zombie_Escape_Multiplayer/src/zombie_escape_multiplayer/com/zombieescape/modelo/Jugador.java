/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


public class Jugador {

    private String id;
    private int x;                     // fila
    private int y;                     // columna
    private EstadoJugador estado;
    private boolean llegoASalida;
    private int puntaje;

    public Jugador(String id) {
        this.id = id;
        this.estado = EstadoJugador.VIVO;
    }

    public void colocarEn(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Se usa al empezar cada escenario. El puntaje acumulado NO se borra.
    public void reiniciarParaNivel(int x, int y) {
        colocarEn(x, y);
        estado = EstadoJugador.VIVO;
        llegoASalida = false;
    }

    // Tecla de esconderse. Devuelve true si el estado cambió.
    public boolean alternarEsconder(char[][] mapa) {
        if (estado == EstadoJugador.ESCONDIDO) {
            estado = EstadoJugador.VIVO;
            return true;
        }
        if (estado == EstadoJugador.VIVO && mapa[x][y] == CargadorEscenarios.ESCONDITE) {
            estado = EstadoJugador.ESCONDIDO;
            return true;
        }
        return false;
    }

    // Solo se puede mover si está vivo (no escondido, no muerto) y no llegó a la salida
    public boolean puedeMoverse() {
        return estado == EstadoJugador.VIVO && !llegoASalida;
    }

    public void sumarPuntos(int puntos) {
        puntaje = puntaje + puntos;
    }

    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public EstadoJugador getEstado() { return estado; }
    public void setEstado(EstadoJugador estado) { this.estado = estado; }
    public boolean isLlegoASalida() { return llegoASalida; }
    public void setLlegoASalida(boolean llegoASalida) { this.llegoASalida = llegoASalida; }
    public int getPuntaje() { return puntaje; }
}
