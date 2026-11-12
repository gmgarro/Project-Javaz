package zombie_escape_multiplayer.com.zombieescape.cliente;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.JTextComponent;

public class PanelLobby extends JPanel {

    private static final Color FONDO      = new Color(18, 18, 22);
    private static final Color PANEL      = new Color(28, 28, 34);
    private static final Color CAMPO      = new Color(38, 38, 44);
    private static final Color BORDE      = new Color(55, 55, 62);
    private static final Color TEXTO      = new Color(240, 240, 235);
    private static final Color TEXTO_SUAVE= new Color(155, 155, 150);
    private static final Color SANGRE     = new Color(145, 30, 28);
    private static final Color NARANJA    = new Color(210, 120, 25);

    private JComboBox<String> comboRol;
    private JButton botonCargarMapa;
    private JButton botonIniciarPartida;
    private JLabel labelEstadoMapa;
    private PanelChat panelChat;
    private ClienteZombi cliente;
    private String nombreJugador;

    public PanelLobby(ClienteZombi cliente, String nombreJugador, String rolInicial) {
        this.cliente = cliente;
        this.nombreJugador = nombreJugador;
        setLayout(new BorderLayout());
        setBackground(FONDO);

        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(PANEL);
        barraSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, SANGRE),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("ZOMBIE ESCAPE");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(TEXTO);
        JLabel sub = new JLabel("Sala de Espera  ·  Superviviente: " + nombreJugador);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(TEXTO_SUAVE);
        sub.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        titulos.add(titulo);
        titulos.add(sub);
        barraSuperior.add(titulos, BorderLayout.WEST);

        JPanel zonaRol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        zonaRol.setOpaque(false);
        JLabel etiquetaRol = new JLabel("Rol asignado:");
        etiquetaRol.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiquetaRol.setForeground(TEXTO_SUAVE);
        
        comboRol = new JComboBox<>(new String[]{"CORREDOR", "FRANCOTIRADOR"});
        if (rolInicial != null) {
            comboRol.setSelectedItem(rolInicial);
        }
        estilizarCombo(comboRol);
        
        zonaRol.add(etiquetaRol);
        zonaRol.add(comboRol);
        barraSuperior.add(zonaRol, BorderLayout.EAST);

        add(barraSuperior, BorderLayout.NORTH);

        panelChat = new PanelChat(cliente);
        estilizarChat(panelChat);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        centro.add(panelChat, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel barraInferior = new JPanel(new BorderLayout(15, 0));
        barraInferior.setOpaque(false);
        barraInferior.setBorder(BorderFactory.createEmptyBorder(4, 20, 20, 20));

        JPanel zonaMapa = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        zonaMapa.setOpaque(false);
        
        botonCargarMapa = new BotonRedondeado("Cargar escenario (.txt)", false);
        botonCargarMapa.setPreferredSize(new Dimension(200, 42));
        
        labelEstadoMapa = new JLabel("Escenario: Mapas base (1 al 10)");
        labelEstadoMapa.setFont(new Font("SansSerif", Font.PLAIN, 13));
        labelEstadoMapa.setForeground(TEXTO_SUAVE);
        
        zonaMapa.add(botonCargarMapa);
        zonaMapa.add(labelEstadoMapa);
        barraInferior.add(zonaMapa, BorderLayout.CENTER);

        botonIniciarPartida = new BotonRedondeado("¡INICIAR PARTIDA!", true);
        botonIniciarPartida.setPreferredSize(new Dimension(210, 44));
        barraInferior.add(botonIniciarPartida, BorderLayout.EAST);

        add(barraInferior, BorderLayout.SOUTH);

        comboRol.addActionListener(e -> {
            String rolSeleccionado = (String) comboRol.getSelectedItem();
            cliente.enviarMensaje("UNIRSE:" + nombreJugador + "/" + rolSeleccionado);
        });

        botonCargarMapa.addActionListener(e -> seleccionarYEnviarMapa());

        botonIniciarPartida.addActionListener(e -> {
            cliente.enviarMensaje("INICIAR");
        });
    }

    private void seleccionarYEnviarMapa() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Escenario Personalizado");
        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            try {
                String contenido = new String(Files.readAllBytes(archivoSeleccionado.toPath()));
                contenido = contenido.replace("\r\n", ";").replace("\n", ";");

                cliente.enviarMensaje("CARGAR_ESCENARIO:" + archivoSeleccionado.getName() + ":" + contenido);
                labelEstadoMapa.setText("Enviado: " + archivoSeleccionado.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo de mapa: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public PanelChat getPanelChat() {
        return panelChat;
    }

    private static void estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.BOLD, 13));
        combo.setForeground(CAMPO);
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        combo.setFocusable(false);
        combo.setPreferredSize(new Dimension(170, 36));
        
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("▼");
                b.setFont(new Font("SansSerif", Font.PLAIN, 10));
                b.setBackground(CAMPO);
                b.setForeground(NARANJA);
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setFocusPainted(false);
                b.setOpaque(true);
                return b;
            }
            
            @Override
            public void paint(Graphics g, JComponent c) {
                g.setColor(CAMPO);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
                super.paint(g, c);
            }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setOpaque(true);
                l.setBackground(sel ? SANGRE : CAMPO);
                l.setForeground(TEXTO);
                l.setFont(new Font("SansSerif", Font.BOLD, 13));
                l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return l;
            }
        });
    }

    private static void estilizarChat(Component c) {
        if (c instanceof JPanel) {
            JPanel p = (JPanel) c;
            p.setOpaque(false);
            // Quitar el borde con título feo que rodea todo el chat
            p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        } else if (c instanceof JTextComponent) {
            JTextComponent t = (JTextComponent) c;
            t.setBackground(CAMPO);
            t.setForeground(TEXTO);
            t.setCaretColor(NARANJA);
            t.setSelectionColor(SANGRE);
            t.setSelectedTextColor(Color.WHITE);
            t.setFont(new Font("SansSerif", Font.PLAIN, 13));
            t.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.setBorder(BorderFactory.createLineBorder(BORDE, 1));
            sp.getViewport().setBackground(CAMPO);
        } else if (c instanceof JButton) {
            JButton b = (JButton) c;
            b.setOpaque(true);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setBackground(SANGRE);
            b.setForeground(TEXTO);
            b.setFont(new Font("SansSerif", Font.BOLD, 13));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Botón "Enviar" con bordes redondeados limpios y color claro al pasar el mouse
            b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent comp) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = comp.getWidth();
                    int h = comp.getHeight();
                    
                    Color fondo = b.getModel().isPressed() ? SANGRE.darker() : 
                                  b.getModel().isRollover() ? new Color(175, 45, 42) : SANGRE;
                    
                    g2.setColor(fondo);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 6, 6));
                    
                    g2.setColor(NARANJA);
                    g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 6, 6));
                    
                    g2.setFont(b.getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (w - fm.stringWidth(b.getText())) / 2;
                    int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                    
                    g2.setColor(Color.WHITE);
                    g2.drawString(b.getText(), tx, ty);
                    g2.dispose();
                }
            });
        } else if (c instanceof JLabel) {
            JLabel lbl = (JLabel) c;
            lbl.setForeground(TEXTO);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        }
        if (c instanceof Container) {
            for (Component hijo : ((Container) c).getComponents()) estilizarChat(hijo);
        }
    }

    private static class BotonRedondeado extends JButton {
        private final boolean principal;

        BotonRedondeado(String texto, boolean principal) {
            super(texto);
            this.principal = principal;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(TEXTO);
            setFont(new Font("SansSerif", Font.BOLD, principal ? 14 : 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color colorFondo = principal ? SANGRE : CAMPO;
            Color colorBorde = principal ? NARANJA : BORDE;

            if (getModel().isPressed()) {
                colorFondo = colorFondo.darker();
            } else if (getModel().isRollover()) {
                colorFondo = colorFondo.brighter();
            }

            g2.setColor(colorFondo);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));

            g2.setColor(colorBorde);
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.setColor(getForeground());
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }
}