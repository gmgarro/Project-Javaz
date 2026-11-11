/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.modelo;


import java.util.ArrayList;

public class PruebaPartida {

    static int fallos = 0;

    // Un Notificador de mentira: en vez de mandar por red, guarda los mensajes
    static class Registro implements Notificador {
        private ArrayList<String> mensajes = new ArrayList<>();

        public synchronized void enviarATodos(String m) {
            mensajes.add(m);
        }

        public synchronized boolean hay(String prefijo) {
            for (String m : mensajes) {
                if (m.startsWith(prefijo)) {
                    return true;
                }
            }
            return false;
        }

        public synchronized int contar(String prefijo) {
            int n = 0;
            for (String m : mensajes) {
                if (m.startsWith(prefijo)) {
                    n++;
                }
            }
            return n;
        }

        public synchronized String ultimo(String prefijo) {
            String resultado = null;
            for (String m : mensajes) {
                if (m.startsWith(prefijo)) {
                    resultado = m;
                }
            }
            return resultado;
        }
    }

    public static void main(String[] args) {
        // ---- Prueba 1: dos jugadores completan los 10 niveles sin morir ----
        Registro reg = new Registro();
        Partida p = new Partida(reg);
        p.agregarJugador("ana");
        p.agregarJugador("beto");
        p.iniciar();

        comprobar("Se envió el mapa del nivel 1", true, reg.hay("MAPA:1:20:20:"));
        comprobar("Se enviaron estados de los 2 jugadores", true, reg.contar("ESTADO:") == 2);
        comprobar("Se crearon 3 zombis (nivel 1 + 2)", true, p.getZombis().size() == 3);
        comprobar("Se envió el tiempo", true, reg.hay("TIEMPO:0"));

        for (int nivel = 1; nivel <= 10; nivel++) {
            completarNivel(p);
        }
        comprobar("Mensaje FIN:VICTORIA", true, reg.hay("FIN:VICTORIA"));
        comprobar("Partida ya no está en curso", false, p.isEnCurso());
        comprobar("Se cargaron 10 mapas", true, reg.contar("MAPA:") == 10);
        ArrayList<Jugador> ranking = GestorPuntuacion.ranking(p.getJugadores());
        // Casi 1000 por nivel (si el reloj alcanzó a marcar 1 segundo, se pierden 10 puntos)
        comprobar("Cada jugador suma cerca de 10 x 1000 puntos", true,
                ranking.get(1).getPuntaje() >= 9000 && ranking.get(0).getPuntaje() <= 10000);
        comprobar("El ranking viene de mayor a menor", true,
                ranking.get(0).getPuntaje() >= ranking.get(1).getPuntaje());

        // ---- Prueba 2: un jugador muere, el otro llega -> DERROTA ----
        Registro reg2 = new Registro();
        Partida p2 = new Partida(reg2);
        p2.agregarJugador("carla");
        p2.agregarJugador("dani");
        p2.iniciar();
        synchronized (p2) {
            p2.getZombis().clear();
            Jugador carla = p2.getJugadores().get(0);
            char[][] mapa = p2.getMapa();
            // pone un zombi pegado a Carla, mirándola
            for (Direccion d : Direccion.values()) {
                int zx = carla.getX() + d.getDx();
                int zy = carla.getY() + d.getDy();
                if (mapa[zx][zy] != CargadorEscenarios.PARED) {
                    p2.getZombis().add(new Zombi(99, zx, zy, d.opuesta()));
                    break;
                }
            }
            p2.moverZombis();
        }
        comprobar("Carla murió", true,
                reg2.hay("ESTADO:carla:") && reg2.ultimo("ESTADO:carla:").endsWith(":MUERTO"));
        comprobar("La partida sigue (Dani no ha llegado)", true, p2.isEnCurso());
        synchronized (p2) {
            p2.getZombis().clear();
            llevarASalida(p2, p2.getJugadores().get(1));
        }
        comprobar("Mensaje FIN:DERROTA", true, reg2.hay("FIN:DERROTA"));
        comprobar("No se cargó el nivel 2", true, reg2.contar("MAPA:") == 1);

        // ---- Prueba 3: esconderse, cooldown y desconexión ----
        Registro reg3 = new Registro();
        Partida p3 = new Partida(reg3);
        p3.agregarJugador("eli");
        p3.agregarJugador("fer");
        p3.iniciar();
        Jugador eli = p3.getJugadores().get(0);
        synchronized (p3) {
            p3.getZombis().clear();
            char[][] mapa = p3.getMapa();
            int antes = reg3.contar("ESTADO:");
            p3.alternarEsconder("eli");   // no está en una E: no pasa nada
            comprobar("Esconderse fuera de E no envía nada", true, reg3.contar("ESTADO:") == antes);

            // Ponemos a Eli sobre una casilla E
            for (int x = 0; x < mapa.length; x++) {
                for (int y = 0; y < mapa[0].length; y++) {
                    if (mapa[x][y] == 'E') {
                        eli.colocarEn(x, y);
                    }
                }
            }
            p3.alternarEsconder("eli");
            comprobar("Eli queda ESCONDIDO", true, eli.getEstado() == EstadoJugador.ESCONDIDO);
            int x0 = eli.getX();
            p3.mover("eli", Direccion.ARRIBA);
            comprobar("Escondido no se mueve", true, eli.getX() == x0);
            p3.alternarEsconder("eli");

            // Cooldown
            Jugador fer = p3.getJugadores().get(1);
            fer.setUltimoMovimiento(0);
            int fx = fer.getX();
            int fy = fer.getY();
            Direccion libre = null;
            for (Direccion d : Direccion.values()) {
                if (mapa[fx + d.getDx()][fy + d.getDy()] != '#') {
                    libre = d;
                    break;
                }
            }
            p3.mover("fer", libre);
            int fx1 = fer.getX();
            int fy1 = fer.getY();
            comprobar("Fer se movió una casilla", true, fx1 != fx || fy1 != fy);
            p3.mover("fer", libre.opuesta());   // inmediato: el cooldown lo ignora
            comprobar("Un segundo paso inmediato lo ignora el cooldown", true,
                    fer.getX() == fx1 && fer.getY() == fy1);

            p3.quitarJugador("fer");
            comprobar("Mensaje DESCONEXION", true, reg3.hay("DESCONEXION:fer"));
            comprobar("La partida sigue sin Fer", true,
                    p3.isEnCurso() && p3.getJugadores().size() == 1);
        }

        p.terminar();
        p2.terminar();
        p3.terminar();
        System.out.println(fallos == 0 ? "TODO OK" : "FALLOS: " + fallos);
        System.exit(0);
    }

    // Lleva a todos los jugadores a la salida (sin zombis) para pasar de nivel
    static void completarNivel(Partida p) {
        synchronized (p) {
            p.getZombis().clear();
            for (Jugador j : new ArrayList<>(p.getJugadores())) {
                llevarASalida(p, j);
            }
        }
    }

    // Coloca al jugador junto a la X y le da el último paso hacia ella
    static void llevarASalida(Partida p, Jugador j) {
        char[][] mapa = p.getMapa();
        int sx = 0;
        int sy = 0;
        for (int x = 0; x < mapa.length; x++) {
            for (int y = 0; y < mapa[0].length; y++) {
                if (mapa[x][y] == 'X') {
                    sx = x;
                    sy = y;
                }
            }
        }
        for (Direccion d : Direccion.values()) {
            int vecinoX = sx - d.getDx();
            int vecinoY = sy - d.getDy();
            if (mapa[vecinoX][vecinoY] != '#') {
                j.colocarEn(vecinoX, vecinoY);
                j.setUltimoMovimiento(0);
                p.mover(j.getId(), d);
                return;
            }
        }
    }

    static void comprobar(String nombre, boolean esperado, boolean real) {
        String resultado = (esperado == real) ? "OK     " : "FALLO  ";
        if (esperado != real) {
            fallos++;
        }
        System.out.println(resultado + nombre);
    }
}
