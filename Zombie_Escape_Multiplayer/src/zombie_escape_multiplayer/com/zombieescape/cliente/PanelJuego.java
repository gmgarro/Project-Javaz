/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.cliente;


import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class PanelJuego extends JPanel {

    // Lo que la pantalla sabe de cada jugador y zombi (lo que dice el servidor)
    public static class VistaJugador {
        public String id;
        public int x;                 // fila
        public int y;                 // columna
        public String estado = "VIVO";
        public int puntaje;

        public VistaJugador(String id) {
            this.id = id;
        }
    }

    public static class VistaZombi {
        public int id;
        public int x;                 // fila
        public int y;                 // columna
        public String dir = "DERECHA";

        public VistaZombi(int id) {
            this.id = id;
        }
    }

    private static final int ALTO_HUD = 44;

    private static final Color[] COLORES = {
        new Color(66, 133, 244), new Color(244, 180, 0), new Color(219, 68, 155),
        new Color(255, 112, 67), new Color(0, 172, 193), new Color(171, 71, 188),
        new Color(124, 179, 66), new Color(255, 202, 40), new Color(141, 110, 99),
        new Color(236, 239, 241)
    };

    private ClienteZombi cliente;
    private String miId;
    private String miRol;

    private char[][] mapa;
    private int nivel;
    private int segundos;
    private int balas = 10;
    private boolean esEspectador;
    private ArrayList<VistaJugador> jugadores = new ArrayList<>();
    private ArrayList<VistaZombi> zombis = new ArrayList<>();

    public PanelJuego(ClienteZombi cliente, String miId, String miRol) {
        this.cliente = cliente;
        this.miId = miId;
        this.miRol = miRol;

        setBackground(new Color(20, 20, 24));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                teclaPresionada(e.getKeyCode());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                clicEnMapa(e.getX(), e.getY());
            }
        });
    }

    //  Entrada del jugador 

    private void teclaPresionada(int codigo) {
        if (!"CORREDOR".equals(miRol) || esEspectador || mapa == null) {
            return;
        }

        String dir = null;
        if (codigo == KeyEvent.VK_UP || codigo == KeyEvent.VK_W) {
            dir = "ARRIBA";
        } else if (codigo == KeyEvent.VK_DOWN || codigo == KeyEvent.VK_S) {
            dir = "ABAJO";
        } else if (codigo == KeyEvent.VK_LEFT || codigo == KeyEvent.VK_A) {
            dir = "IZQUIERDA";
        } else if (codigo == KeyEvent.VK_RIGHT || codigo == KeyEvent.VK_D) {
            dir = "DERECHA";
        }

        if (dir != null) {
            cliente.enviarMensaje("MOVER:" + miId + ":" + dir);
        } else if (codigo == KeyEvent.VK_E || codigo == KeyEvent.VK_SPACE) {
            cliente.enviarMensaje("TOGGLE_ESCONDER:" + miId);
        }
    }

    private void clicEnMapa(int pixelX, int pixelY) {
        if (!"FRANCOTIRADOR".equals(miRol) || mapa == null || balas <= 0) {
            return;
        }
        int tam = calcularTam();
        int dentroX = pixelX - calcularDesplazamientoX();
        int dentroY = pixelY - calcularDesplazamientoY();
        if (dentroX < 0 || dentroY < 0) {
            return;
        }

        int columna = dentroX / tam;
        int fila = dentroY / tam;
        if (fila < mapa.length && columna < mapa[0].length) {
            // x = fila, y = columna
            cliente.enviarMensaje("DISPARO:" + fila + ":" + columna);
            balas--;
            repaint();
        }
    }

    public void enfocar() {
        requestFocusInWindow();
    }

    public void setRol(String rol) {
        this.miRol = rol;
        repaint();
    }

    //  Mensajes que llegan del servidor 

    public void procesarMensaje(String mensaje) {
        try {
            // Capturar la orden directa del servidor para arrancar
            if (mensaje.equals("PARTIDA_INICIADA")) {
                esEspectador = false;
                repaint();
                return;
            }

            String[] p = mensaje.split(":");

            if (p[0].equals("MAPA")) {
                nivel = Integer.parseInt(p[1]);
                int filas = Integer.parseInt(p[2]);
                int columnas = Integer.parseInt(p[3]);
                String[] filasTexto = p[4].split(";");
                char[][] nuevo = new char[filas][columnas];
                for (int i = 0; i < filas; i++) {
                    for (int j = 0; j < columnas; j++) {
                        nuevo[i][j] = filasTexto[i].charAt(j);
                    }
                }
                mapa = nuevo;
                zombis.clear();
                esEspectador = false;
                balas = 10;

            } else if (p[0].equals("ESTADO")) {
                VistaJugador j = obtenerJugador(p[1]);
                j.x = Integer.parseInt(p[2]);
                j.y = Integer.parseInt(p[3]);
                j.estado = p[4];
                if (p[1].equals(miId)) {
                    esEspectador = p[4].equals("MUERTO");
                }

            } else if (p[0].equals("ZOMBI")) {
                int id = Integer.parseInt(p[1]);
                if (p[5].equals("MUERTO")) {
                    quitarZombi(id);
                } else {
                    VistaZombi z = obtenerZombi(id);
                    z.x = Integer.parseInt(p[2]);
                    z.y = Integer.parseInt(p[3]);
                    z.dir = p[4];
                }

            } else if (p[0].equals("TIEMPO")) {
                segundos = Integer.parseInt(p[1]);

            } else if (p[0].equals("PUNTAJE")) {
                obtenerJugador(p[1]).puntaje = Integer.parseInt(p[2]);

            } else if (p[0].equals("DESCONEXION")) {
                quitarJugador(p[1]);
            }
        } catch (RuntimeException e) {
            System.out.println("Mensaje ignorado (" + mensaje + "): " + e.getMessage());
        }
        repaint();
    }
    private VistaJugador obtenerJugador(String id) {
        for (VistaJugador j : jugadores) {
            if (j.id.equals(id)) {
                return j;
            }
        }
        VistaJugador nuevo = new VistaJugador(id);
        jugadores.add(nuevo);
        return nuevo;
    }

    private void quitarJugador(String id) {
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).id.equals(id)) {
                jugadores.remove(i);
                return;
            }
        }
    }

    private VistaZombi obtenerZombi(int id) {
        for (VistaZombi z : zombis) {
            if (z.id == id) {
                return z;
            }
        }
        VistaZombi nuevo = new VistaZombi(id);
        zombis.add(nuevo);
        return nuevo;
    }

    private void quitarZombi(int id) {
        for (int i = 0; i < zombis.size(); i++) {
            if (zombis.get(i).id == id) {
                zombis.remove(i);
                return;
            }
        }
    }

    public ArrayList<VistaJugador> getJugadores() {
        return jugadores;
    }

    //  Dibujo 

    private int calcularTam() {
        if (mapa == null) {
            return 1;
        }
        int porAncho = getWidth() / mapa[0].length;
        int porAlto = (getHeight() - ALTO_HUD) / mapa.length;
        return Math.max(Math.min(porAncho, porAlto), 4);
    }

    private int calcularDesplazamientoX() {
        return (getWidth() - calcularTam() * mapa[0].length) / 2;
    }

    private int calcularDesplazamientoY() {
        return ALTO_HUD + (getHeight() - ALTO_HUD - calcularTam() * mapa.length) / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dibujarHud(g2);

        if (mapa == null) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g2.drawString("Esperando el inicio de la partida...", 40, ALTO_HUD + 60);
            return;
        }

        int tam = calcularTam();
        int origenX = calcularDesplazamientoX();
        int origenY = calcularDesplazamientoY();

        dibujarMapa(g2, tam, origenX, origenY);
        dibujarZombis(g2, tam, origenX, origenY);
        dibujarJugadores(g2, tam, origenX, origenY);
    }

    private void dibujarHud(Graphics2D g2) {
        g2.setColor(new Color(35, 35, 42));
        g2.fillRect(0, 0, getWidth(), ALTO_HUD);

        int puntaje = 0;
        for (VistaJugador j : jugadores) {
            if (j.id.equals(miId)) {
                puntaje = j.puntaje;
            }
        }

        String tiempo = String.format("%02d:%02d", segundos / 60, segundos % 60);
        String texto = "TIEMPO: " + tiempo + "    NIVEL: " + nivel;
        if ("FRANCOTIRADOR".equals(miRol)) {
            texto = texto + "    AMMO: " + balas;
        } else {
            texto = texto + "    SCORE: " + puntaje;
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(240, 220, 150));
        g2.drawString(texto, 12, 27);

        String quien = miId + " (" + miRol + ")";
        if (esEspectador) {
            quien = miId + " - ESPECTADOR";
            g2.setColor(new Color(255, 120, 120));
        } else {
            g2.setColor(Color.WHITE);
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(quien, getWidth() - fm.stringWidth(quien) - 12, 27);
    }

    private void dibujarMapa(Graphics2D g2, int tam, int origenX, int origenY) {
        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[fila].length; col++) {
                char c = mapa[fila][col];
                if (c == '#') {
                    g2.setColor(new Color(58, 60, 70));
                } else if (c == 'E') {
                    g2.setColor(new Color(46, 125, 80));
                } else if (c == 'S') {
                    g2.setColor(new Color(60, 90, 150));
                } else if (c == 'X') {
                    g2.setColor(new Color(212, 175, 55));
                } else {
                    g2.setColor(new Color(150, 150, 140));
                }
                g2.fillRect(origenX + col * tam, origenY + fila * tam, tam, tam);

                if (c != '#') {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.drawRect(origenX + col * tam, origenY + fila * tam, tam, tam);
                }
                if (c == 'X' || c == 'E') {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(tam * 2 / 3, 8)));
                    FontMetrics fm = g2.getFontMetrics();
                    String letra = String.valueOf(c);
                    g2.drawString(letra, origenX + col * tam + (tam - fm.stringWidth(letra)) / 2,
                            origenY + fila * tam + (tam + fm.getAscent()) / 2 - 2);
                }
            }
        }
    }

    private void dibujarZombis(Graphics2D g2, int tam, int origenX, int origenY) {
        for (VistaZombi z : zombis) {
            int px = origenX + z.y * tam;       // columna -> horizontal
            int py = origenY + z.x * tam;       // fila -> vertical
            g2.setColor(new Color(76, 160, 60));
            g2.fillRoundRect(px + 2, py + 2, tam - 4, tam - 4, 6, 6);
            g2.setColor(new Color(200, 30, 30));

            // Un punto rojo indica hacia dónde mira
            int centroX = px + tam / 2;
            int centroY = py + tam / 2;
            int largo = tam / 2;
            int dx = 0;
            int dy = 0;
            if (z.dir.equals("ARRIBA")) {
                dy = -1;
            } else if (z.dir.equals("ABAJO")) {
                dy = 1;
            } else if (z.dir.equals("IZQUIERDA")) {
                dx = -1;
            } else {
                dx = 1;
            }
            g2.fillOval(centroX + dx * largo / 2 - 3, centroY + dy * largo / 2 - 3, 7, 7);
        }
    }

    private void dibujarJugadores(Graphics2D g2, int tam, int origenX, int origenY) {
        for (int i = 0; i < jugadores.size(); i++) {
            VistaJugador j = jugadores.get(i);
            int px = origenX + j.y * tam;
            int py = origenY + j.x * tam;
            Color color = COLORES[i % COLORES.length];

            if (j.estado.equals("MUERTO")) {
                g2.setColor(new Color(110, 110, 110));
                g2.fillOval(px + 3, py + 3, tam - 6, tam - 6);
                g2.setColor(new Color(200, 40, 40));
                g2.drawLine(px + 4, py + 4, px + tam - 4, py + tam - 4);
                g2.drawLine(px + tam - 4, py + 4, px + 4, py + tam - 4);
            } else if (j.estado.equals("ESCONDIDO")) {
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 110));
                g2.fillOval(px + 5, py + 5, tam - 10, tam - 10);
            } else if (j.estado.equals("SALIO")) {
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 160));
                g2.fillOval(px + 4, py + 4, tam - 8, tam - 8);
            } else {
                g2.setColor(color);
                g2.fillOval(px + 3, py + 3, tam - 6, tam - 6);
            }

            if (j.id.equals(miId)) {
                g2.setColor(Color.WHITE);
                g2.drawOval(px + 1, py + 1, tam - 3, tam - 3);
            }

            // Nombre sobre el personaje (el tuyo en amarillo)
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(tam * 2 / 5, 9)));
            FontMetrics fm = g2.getFontMetrics();
            String nombre = j.id;
            g2.setColor(Color.BLACK);
            g2.drawString(nombre, px + (tam - fm.stringWidth(nombre)) / 2 + 1, py - 1);
            g2.setColor(j.id.equals(miId) ? new Color(255, 235, 59) : Color.WHITE);
            g2.drawString(nombre, px + (tam - fm.stringWidth(nombre)) / 2, py - 2);
        }
    }
}
