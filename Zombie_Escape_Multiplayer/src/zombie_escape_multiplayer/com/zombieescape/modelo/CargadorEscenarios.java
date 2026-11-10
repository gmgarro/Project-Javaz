/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class CargadorEscenarios {

    public static final char PARED = '#';
    public static final char CAMINO = '.';
    public static final char ESCONDITE = 'E';
    public static final char ENTRADA = 'S';
    public static final char SALIDA = 'X';

    // Lee un archivo .txt, lo convierte en matriz y lo valida.
    public static char[][] cargar(File archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    lineas.add(linea);
                }
            }
        }
        char[][] mapa = construirMatriz(lineas);
        validar(mapa);
        return mapa;
    }

    // Para mapas recibidos por red (el lobby manda las filas separadas por ';').
    // Devuelve null si es válido, o el mensaje de error si no.
    public static String validarTexto(String datos) {
        try {
            List<String> lineas = new ArrayList<>();
            for (String fila : datos.split(";")) {
                fila = fila.trim();
                if (!fila.isEmpty()) {
                    lineas.add(fila);
                }
            }
            char[][] mapa = construirMatriz(lineas);
            validar(mapa);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // Convierte la lista de líneas en char[][] verificando forma y símbolos.
    public static char[][] construirMatriz(List<String> lineas) {
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("El mapa está vacío.");
        }
        int columnas = lineas.get(0).length();
        char[][] mapa = new char[lineas.size()][columnas];

        for (int f = 0; f < lineas.size(); f++) {
            String linea = lineas.get(f);
            if (linea.length() != columnas) {
                throw new IllegalArgumentException(
                    "La fila " + (f + 1) + " tiene largo distinto a la primera.");
            }
            for (int c = 0; c < columnas; c++) {
                char ch = linea.charAt(c);
                if ("#.ESX".indexOf(ch) < 0) {
                    throw new IllegalArgumentException(
                        "Símbolo inválido '" + ch + "' en fila " + (f + 1)
                        + ", columna " + (c + 1));
                }
                mapa[f][c] = ch;
            }
        }
        return mapa;
    }

    // Reglas del plan: existe X, S tiene >= 10 casillas y hay camino de S a X.
    public static void validar(char[][] mapa) {
        int cantidadS = 0;
        int cantidadX = 0;
        int[] inicio = null;

        for (int f = 0; f < mapa.length; f++) {
            for (int c = 0; c < mapa[f].length; c++) {
                if (mapa[f][c] == ENTRADA) {
                    cantidadS++;
                    if (inicio == null) inicio = new int[]{f, c};
                } else if (mapa[f][c] == SALIDA) {
                    cantidadX++;
                }
            }
        }

        if (cantidadX == 0) {
            throw new IllegalArgumentException("No hay casilla de salida X.");
        }
        if (cantidadS < 10) {
            throw new IllegalArgumentException(
                "La zona S necesita al menos 10 casillas (tiene " + cantidadS + ").");
        }
        if (!hayCamino(mapa, inicio[0], inicio[1])) {
            throw new IllegalArgumentException("No hay camino transitable de S a X.");
        }
    }

    // BFS: explora por capas desde (filaIni, colIni) hasta encontrar una X.
    private static boolean hayCamino(char[][] mapa, int filaIni, int colIni) {
        int filas = mapa.length;
        int cols = mapa[0].length;
        boolean[][] visitado = new boolean[filas][cols];
        ArrayDeque<int[]> cola = new ArrayDeque<>();

        cola.add(new int[]{filaIni, colIni});
        visitado[filaIni][colIni] = true;

        int[][] movimientos = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!cola.isEmpty()) {
            int[] actual = cola.poll();
            if (mapa[actual[0]][actual[1]] == SALIDA) return true;

            for (int[] m : movimientos) {
                int nf = actual[0] + m[0];
                int nc = actual[1] + m[1];
                boolean dentro = nf >= 0 && nf < filas && nc >= 0 && nc < cols;
                if (dentro && !visitado[nf][nc] && mapa[nf][nc] != PARED) {
                    visitado[nf][nc] = true;
                    cola.add(new int[]{nf, nc});
                }
            }
        }
        return false;
    }
    
    private static final String CARPETA = "escenarios";

    // Devuelve el archivo del nivel pedido: nivel 3 -> escenarios/escenario_03.txt
    public static File archivoDeNivel(int nivel) {
        return new File(CARPETA, String.format("escenario_%02d.txt", nivel));
    }

    // Carga y valida el escenario de un nivel concreto.
    public static char[][] cargarNivel(int nivel) throws IOException {
        return cargar(archivoDeNivel(nivel));
    }

    // Cuenta cuántos escenarios hay en la carpeta (1, 2, 3... hasta que falte uno).
    public static int contarEscenarios() {
        int n = 0;
        while (archivoDeNivel(n + 1).exists()) {
            n++;
        }
        return n;
    }

    // Para el archivo que el usuario elige con JFileChooser.
    // Devuelve null si es válido, o el mensaje de error si no.
    public static String validarArchivo(File archivo) {
        try {
            cargar(archivo);
            return null;
        } catch (IOException e) {
            return "No se pudo leer el archivo: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // Valida un mapa recibido (filas separadas por ';') y lo guarda como el
    // siguiente escenario: escenario_11.txt, luego escenario_12.txt, etc.
    // Devuelve el número de nivel asignado. Lanza IllegalArgumentException si es inválido.
    public static synchronized int guardarEscenarioNuevo(String datos) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (String fila : datos.split(";")) {
            fila = fila.trim();
            if (!fila.isEmpty()) {
                lineas.add(fila);
            }
        }
        validar(construirMatriz(lineas));   // si algo está mal, lanza la excepción aquí

        int nuevoNivel = contarEscenarios() + 1;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoDeNivel(nuevoNivel)))) {
            for (String fila : lineas) {
                bw.write(fila);
                bw.newLine();
            }
        }
        return nuevoNivel;
    }
}
