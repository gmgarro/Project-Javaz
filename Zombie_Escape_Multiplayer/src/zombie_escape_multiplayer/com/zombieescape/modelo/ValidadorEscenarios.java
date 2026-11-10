/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


public class ValidadorEscenarios {

    public static void main(String[] args) {
        int total = CargadorEscenarios.contarEscenarios();
        System.out.println("Escenarios encontrados: " + total);

        for (int nivel = 1; nivel <= total; nivel++) {
            try {
                char[][] mapa = CargadorEscenarios.cargarNivel(nivel);
                System.out.println("Nivel " + nivel + ": OK (" + mapa.length + "x" + mapa[0].length + ")");
            } catch (Exception e) {
                System.out.println("Nivel " + nivel + ": ERROR -> " + e.getMessage());
            }
        }
    }
}
