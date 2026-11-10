/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.io.File;

public class PruebaCargador {
    public static void main(String[] args) throws Exception {
        char[][] mapa = CargadorEscenarios.cargar(new File("escenarios/escenario_01.txt"));
        System.out.println("Mapa válido: " + mapa.length + " filas x " + mapa[0].length + " columnas");

        // Prueba del validador de texto (el que usará el servidor)
        System.out.println(CargadorEscenarios.validarTexto("#####;#SX.#;#####"));
        System.out.println("Escenarios en carpeta: " + CargadorEscenarios.contarEscenarios());
        System.out.println(CargadorEscenarios.validarArchivo(new java.io.File("escenarios/escenario_01.txt")));
        System.out.println(CargadorEscenarios.validarArchivo(new java.io.File("escenarios/no_existe.txt")));
    }  
}
