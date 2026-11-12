
package zombie_escape_multiplayer.com.zombieescape.modelo;

public class Francotirador {
    private String idJugador;
    private int ammo;
    private int cuadranteAsignado; // Ej: 1 o 2 (para dividir la vista táctica)

    public Francotirador(String idJugador, int cuadranteAsignado) {
        this.idJugador = idJugador;
        this.ammo = 10; // 10 balas por escenario según el requerimiento
        this.cuadranteAsignado = cuadranteAsignado;
    }

    public boolean disparar() {
        if (ammo > 0) {
            ammo--;
            return true;
        }
        return false; // Sin munición
    }

    public int getAmmo() {
        return ammo;
    }

    public void recargar() {
        this.ammo = 10;
    }

    public String getIdJugador() {
        return idJugador;
    }

    public int getCuadranteAsignado() {
        return cuadranteAsignado;
    }
}