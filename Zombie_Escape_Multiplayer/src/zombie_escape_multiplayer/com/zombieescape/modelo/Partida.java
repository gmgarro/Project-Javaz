/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Timer;

public class Partida {

    private static final int COOLDOWN_MS = 150;
    private static final int PASO_ZOMBIS_MS = 600;

    private Notificador notificador;
    private ArrayList<Jugador> jugadores = new ArrayList<>();
    private ArrayList<Zombi> zombis = new ArrayList<>();
    private char[][] mapa;
    private int nivel;
    private int totalNiveles;
    private int segundos;
    private boolean enCurso;
    private boolean huboMuerte;
    private Timer timerZombis;
    private Timer timerReloj;

    public Partida(Notificador notificador) {
        this.notificador = notificador;
        timerZombis = new Timer(PASO_ZOMBIS_MS, e -> moverZombis());
        timerReloj = new Timer(1000, e -> avanzarReloj());
    }

    //  Preparación 

    public synchronized void agregarJugador(String id) {
        if (!enCurso && buscar(id) == null) {
            jugadores.add(new Jugador(id));
        }
    }

    public synchronized void iniciar() {
        if (enCurso || jugadores.isEmpty()) {
            return;
        }
        totalNiveles = CargadorEscenarios.contarEscenarios();
        nivel = 1;
        enCurso = true;
        cargarNivel();
        if (enCurso) {
            timerZombis.start();
            timerReloj.start();
        }
    }

    private void cargarNivel() {
        try {
            mapa = CargadorEscenarios.cargarNivel(nivel);
        } catch (IOException e) {
            System.out.println("No se pudo cargar el nivel " + nivel + ": " + e.getMessage());
            terminarPartida("DERROTA");
            return;
        }

        segundos = 0;
        huboMuerte = false;

        // Casillas de entrada (S)
        ArrayList<int[]> entradas = new ArrayList<>();
        for (int x = 0; x < mapa.length; x++) {
            for (int y = 0; y < mapa[x].length; y++) {
                if (mapa[x][y] == CargadorEscenarios.ENTRADA) {
                    entradas.add(new int[]{x, y});
                }
            }
        }
        for (int i = 0; i < jugadores.size(); i++) {
            int[] celda = entradas.get(i % entradas.size());
            jugadores.get(i).reiniciarParaNivel(celda[0], celda[1]);
        }

        zombis = GeneradorZombis.generar(mapa, nivel + 2);

        // Avisar a todos el nuevo escenario
        notificador.enviarATodos("MAPA:" + nivel + ":" + mapa.length + ":"
                + mapa[0].length + ":" + mapaComoTexto());
        for (Jugador j : jugadores) {
            notificador.enviarATodos(mensajeEstado(j));
        }
        for (Zombi z : zombis) {
            notificador.enviarATodos(mensajeZombi(z));
        }
        notificador.enviarATodos("TIEMPO:" + segundos);
    }

    //  Acciones de los jugadores 

    public synchronized void mover(String id, Direccion dir) {
        Jugador j = buscar(id);
        if (!enCurso || j == null || !j.puedeMoverse()) {
            return;
        }

        long ahora = System.currentTimeMillis();
        if (ahora - j.getUltimoMovimiento() < COOLDOWN_MS) {
            return;
        }

        int nuevoX = j.getX() + dir.getDx();
        int nuevoY = j.getY() + dir.getDy();
        if (!esTransitable(nuevoX, nuevoY)) {
            return;
        }

        j.setUltimoMovimiento(ahora);
        j.colocarEn(nuevoX, nuevoY);

        if (mapa[nuevoX][nuevoY] == CargadorEscenarios.SALIDA) {
            j.setLlegoASalida(true);
            j.sumarPuntos(GestorPuntuacion.calcularPuntaje(segundos));
            notificador.enviarATodos("PUNTAJE:" + j.getId() + ":" + j.getPuntaje());
        }
        notificador.enviarATodos(mensajeEstado(j));

        revisarMuertes();
        verificarFinNivel();
    }

    public synchronized void alternarEsconder(String id) {
        Jugador j = buscar(id);
        if (enCurso && j != null && j.alternarEsconder(mapa)) {
            notificador.enviarATodos(mensajeEstado(j));
        }
    }

    // Para el francotirador: mata al zombi que esté en (x, y). Devuelve true si mató a alguno.
    public synchronized boolean matarZombiEn(int x, int y) {
        for (Zombi z : zombis) {
            if (z.isVivo() && z.getX() == x && z.getY() == y) {
                z.matar();
                notificador.enviarATodos(mensajeZombi(z));
                return true;
            }
        }
        return false;
    }

    public synchronized void quitarJugador(String id) {
        Jugador j = buscar(id);
        if (j == null) {
            return;
        }
        jugadores.remove(j);
        notificador.enviarATodos("DESCONEXION:" + id);
        verificarFinNivel();
    }

    //  Lo que hacen los timers 

    public synchronized void moverZombis() {
        if (!enCurso) {
            return;
        }
        for (Zombi z : zombis) {
            if (z.isVivo()) {
                z.mover(mapa);
                notificador.enviarATodos(mensajeZombi(z));
            }
        }
        revisarMuertes();
        verificarFinNivel();
    }

    private synchronized void avanzarReloj() {
        if (!enCurso) {
            return;
        }
        segundos++;
        notificador.enviarATodos("TIEMPO:" + segundos);
    }

    //  Reglas del juego 

    private void revisarMuertes() {
        ArrayList<Jugador> muertos = Vision.revisarMuertes(zombis, jugadores, mapa, nivel);
        for (Jugador m : muertos) {
            huboMuerte = true;
            notificador.enviarATodos(mensajeEstado(m));
        }
    }

    private void verificarFinNivel() {
        if (!enCurso) {
            return;
        }
        if (jugadores.isEmpty()) {
            terminar();
            return;
        }
        // Si alguien sigue jugando (vivo y sin llegar a la salida), el nivel continúa
        for (Jugador j : jugadores) {
            if (j.getEstado() != EstadoJugador.MUERTO && !j.isLlegoASalida()) {
                return;
            }
        }

        if (huboMuerte) {
            terminarPartida("DERROTA");
        } else if (nivel >= totalNiveles) {
            terminarPartida("VICTORIA");
        } else {
            nivel++;
            cargarNivel();
        }
    }

    private void terminarPartida(String resultado) {
        for (Jugador j : jugadores) {
            notificador.enviarATodos("PUNTAJE:" + j.getId() + ":" + j.getPuntaje());
        }
        notificador.enviarATodos("FIN:" + resultado);
        terminar();
    }

    public synchronized void terminar() {
        enCurso = false;
        timerZombis.stop();
        timerReloj.stop();
    }

    //  Ayudas 

    private boolean esTransitable(int x, int y) {
        return x >= 0 && x < mapa.length && y >= 0 && y < mapa[0].length
                && mapa[x][y] != CargadorEscenarios.PARED;
    }

    private Jugador buscar(String id) {
        for (Jugador j : jugadores) {
            if (j.getId().equals(id)) {
                return j;
            }
        }
        return null;
    }

    private String mapaComoTexto() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < mapa.length; i++) {
            if (i > 0) {
                texto.append(';');
            }
            texto.append(new String(mapa[i]));
        }
        return texto.toString();
    }

    private String mensajeEstado(Jugador j) {
        String estado = j.isLlegoASalida() ? "SALIO" : j.getEstado().name();
        return "ESTADO:" + j.getId() + ":" + j.getX() + ":" + j.getY() + ":" + estado;
    }

    private String mensajeZombi(Zombi z) {
        return "ZOMBI:" + z.getId() + ":" + z.getX() + ":" + z.getY() + ":"
                + z.getDir().name() + ":" + (z.isVivo() ? "VIVO" : "MUERTO");
    }

    public synchronized ArrayList<Jugador> getJugadores() { return jugadores; }
    public synchronized ArrayList<Zombi> getZombis() { return zombis; }
    public synchronized char[][] getMapa() { return mapa; }
    public synchronized int getNivel() { return nivel; }
    public synchronized int getSegundos() { return segundos; }
    public synchronized boolean isEnCurso() { return enCurso; }
}
