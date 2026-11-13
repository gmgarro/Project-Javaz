package zombie_escape_multiplayer.com.zombieescape.cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class PanelRanking extends JPanel {

    // Paleta temática "Ciudad Abandonada / Apocalipsis"
    private static final Color FONDO_OSCURO = new Color(20, 20, 22);
    private static final Color PANEL_GRIS   = new Color(30, 30, 34);
    private static final Color CAMPO_TEXTO  = new Color(42, 42, 46);
    private static final Color BORDE_SUTIL  = new Color(65, 65, 70);
    private static final Color TEXTO_CLARO  = new Color(240, 240, 235);
    private static final Color TEXTO_MUTED  = new Color(150, 150, 145);
    private static final Color ROJO_SANGRE  = new Color(145, 30, 28);
    private static final Color NARANJA_ACC  = new Color(210, 120, 25);

    private JLabel titulo;
    private DefaultTableModel modelo;
    private Runnable alSalir;

    public PanelRanking() {
        setLayout(new BorderLayout(15, 15));
        setBackground(FONDO_OSCURO);
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        // Cabecera temática
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);

        JLabel lblSub = new JLabel("ZOMBIE ESCAPE MULTIPLAYER");
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSub.setForeground(NARANJA_ACC);
        lblSub.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        titulo = new JLabel("Fin de la partida", SwingConstants.LEFT);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(TEXTO_CLARO);

        panelNorte.add(lblSub, BorderLayout.NORTH);
        panelNorte.add(titulo, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // Modelo de tabla protegido contra edición
        modelo = new DefaultTableModel(new String[]{"Puesto", "Jugador", "Puntaje"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(32);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabla.setBackground(PANEL_GRIS);
        tabla.setForeground(TEXTO_CLARO);
        tabla.setGridColor(BORDE_SUTIL);
        tabla.setSelectionBackground(CAMPO_TEXTO);
        tabla.setSelectionForeground(TEXTO_CLARO);

        // Estilizar la cabecera de la tabla
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(CAMPO_TEXTO);
        tabla.getTableHeader().setForeground(TEXTO_CLARO);
        tabla.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDE_SUTIL));

        // Centrar el contenido de las celdas de la tabla
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        centrado.setBackground(PANEL_GRIS);
        centrado.setForeground(TEXTO_CLARO);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }

        // Envolver en un JScrollPane adaptado al tema oscuro
        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(PANEL_GRIS);
        scrollPane.setBackground(PANEL_GRIS);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDE_SUTIL, 1));
        add(scrollPane, BorderLayout.CENTER);

        // Botón inferior de salida con diseño redondeado
        JButton botonSalir = new JButton("SALIR AL MENÚ");
        estilizarBotonRedondeado(botonSalir, true);
        botonSalir.addActionListener(e -> {
            if (alSalir != null) {
                alSalir.run();
            }
        });

        JPanel abajo = new JPanel();
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        abajo.add(botonSalir);
        add(abajo, BorderLayout.SOUTH);
    }

    // la ventana principal decide qué pasa al darle a salir
    public void setAlSalir(Runnable alSalir) {
        this.alSalir = alSalir;
    }    

    // Muestra la tabla de mayor a menor puntaje. resultado: "VICTORIA" o "DERROTA"
    public void mostrar(ArrayList<PanelJuego.VistaJugador> jugadores, String resultado) {
        if (resultado.equals("VICTORIA")) {
            titulo.setText("¡VICTORIA! Todos escaparon");
            titulo.setForeground(new Color(60, 180, 90)); // Verde apocalíptico sobrio
        } else {
            titulo.setText("DERROTA - La horda los ha alcanzado");
            titulo.setForeground(ROJO_SANGRE);
        }

        // Ordenar de mayor a menor (método de selección sobre una copia)
        ArrayList<PanelJuego.VistaJugador> orden = new ArrayList<>(jugadores);
        for (int i = 0; i < orden.size() - 1; i++) {
            int mejor = i;
            for (int j = i + 1; j < orden.size(); j++) {
                if (orden.get(j).puntaje > orden.get(mejor).puntaje) {
                    mejor = j;
                }
            }
            PanelJuego.VistaJugador temporal = orden.get(i);
            orden.set(i, orden.get(mejor));
            orden.set(mejor, temporal);
        }

        modelo.setRowCount(0);
        for (int i = 0; i < orden.size(); i++) {
            modelo.addRow(new Object[]{i + 1, orden.get(i).id, orden.get(i).puntaje});
        }
    }


    private void estilizarBotonRedondeado(AbstractButton boton, boolean esAccionPrincipal) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setForeground(TEXTO_CLARO);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(160, 38));

        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = c.getWidth();
                int h = c.getHeight();

                Color colorFondo = esAccionPrincipal ? ROJO_SANGRE : CAMPO_TEXTO;
                Color colorBorde = esAccionPrincipal ? NARANJA_ACC : BORDE_SUTIL;

                if (boton.getModel().isPressed()) {
                    colorFondo = colorFondo.darker();
                } else if (boton.getModel().isRollover()) {
                    colorFondo = colorFondo.brighter();
                }

                g2.setColor(colorFondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));

                g2.setColor(colorBorde);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));

                g2.dispose();
                super.paint(g, c);
            }
        });
    }
}