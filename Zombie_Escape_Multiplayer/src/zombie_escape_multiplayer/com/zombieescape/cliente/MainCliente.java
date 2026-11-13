package zombie_escape_multiplayer.com.zombieescape.cliente;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class MainCliente {

    private static final int PUERTO = 5050;

    // Paleta temática "Ciudad Abandonada / Apocalipsis" (Sobria y sin tonos celestes)
    private static final Color FONDO_OSCURO = new Color(20, 20, 22);
    private static final Color PANEL_GRIS   = new Color(30, 30, 34);
    private static final Color CAMPO_TEXTO  = new Color(42, 42, 46);
    private static final Color BORDE_SUTIL  = new Color(65, 65, 70);
    private static final Color TEXTO_CLARO  = new Color(240, 240, 235);
    private static final Color TEXTO_MUTED  = new Color(150, 150, 145);
    private static final Color ROJO_SANGRE  = new Color(145, 30, 28);
    private static final Color NARANJA_ACC  = new Color(210, 120, 25);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            aplicarTemaGlobal();

            String[] datos = pedirDatos();
            if (datos == null) {
                return;
            }
            String nombre = datos[0];
            String ip = datos[1];
            String rol = datos[2];

            ClienteZombi cliente = new ClienteZombi(ip.trim(), PUERTO);
            VentanaPrincipal ventana = new VentanaPrincipal(cliente, nombre, rol);

            cliente.setVentana(ventana);

            if (!cliente.conectar(nombre, rol)) {
                JOptionPane.showMessageDialog(null, 
                    "No se pudo establecer conexión con el servidor en la IP indicada.", 
                    "Error de Conexión", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("Cliente conectado exitosamente como: " + nombre + " [" + rol + "]");
        });
    }

    private static void aplicarTemaGlobal() {
        UIManager.put("OptionPane.background", PANEL_GRIS);
        UIManager.put("Panel.background", PANEL_GRIS);
        UIManager.put("OptionPane.messageForeground", TEXTO_CLARO);
        UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Button.background", CAMPO_TEXTO);
        UIManager.put("Button.foreground", TEXTO_CLARO);
    }

    private static String[] pedirDatos() {
        final String[] resultado = new String[3];

        JDialog dialogo = new JDialog((Frame) null, "Zombie Escape - Acceso", true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO_OSCURO);
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));

        // Cabecera
        JPanel cabecera = new JPanel(new GridLayout(2, 1, 0, 4));
        cabecera.setOpaque(false);
        
        JLabel lblSub = new JLabel("ZOMBIE ESCAPE MULTIPLAYER");
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSub.setForeground(NARANJA_ACC);
        
        JLabel lblTitulo = new JLabel("Zona de Supervivencia");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(TEXTO_CLARO);
        
        cabecera.add(lblSub);
        cabecera.add(lblTitulo);
        raiz.add(cabecera, BorderLayout.NORTH);

        // Cuerpo del formulario
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        JTextField campoNombre = crearCampo("");
        JTextField campoIp = crearCampo("localhost");

        cuerpo.add(crearEtiqueta("Nombre de Superviviente"));
        cuerpo.add(campoNombre);
        cuerpo.add(Box.createVerticalStrut(12));
        
        cuerpo.add(crearEtiqueta("Dirección IP del Servidor"));
        cuerpo.add(campoIp);
        cuerpo.add(Box.createVerticalStrut(12));
        
        cuerpo.add(crearEtiqueta("Rol en la Partida"));
        
        // Botones de selección de rol con bordes redondeados estilo pastilla (pill)
        JToggleButton btnCorredor = new JToggleButton("CORREDOR");
        JToggleButton btnFrancotirador = new JToggleButton("FRANCOTIRADOR");
        
        ButtonGroup grupoRoles = new ButtonGroup();
        grupoRoles.add(btnCorredor);
        grupoRoles.add(btnFrancotirador);
        btnCorredor.setSelected(true);

        estilizarBotonRedondeado(btnCorredor, true);
        estilizarBotonRedondeado(btnFrancotirador, true);

        JPanel panelRoles = new JPanel(new GridLayout(1, 2, 10, 0));
        panelRoles.setOpaque(false);
        panelRoles.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelRoles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panelRoles.add(btnCorredor);
        panelRoles.add(btnFrancotirador);
        
        cuerpo.add(panelRoles);

        JLabel lblError = new JLabel(" ");
        lblError.setForeground(NARANJA_ACC);
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cuerpo.add(Box.createVerticalStrut(10));
        cuerpo.add(lblError);

        raiz.add(cuerpo, BorderLayout.CENTER);

        // Botones inferiores de acción (Entrar / Cancelar con mini-rounded)
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnEntrar = new JButton("ENTRAR");

        estilizarBotonRedondeado(btnCancelar, false);
        estilizarBotonRedondeado(btnEntrar, true);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnEntrar);
        
        raiz.add(panelBotones, BorderLayout.SOUTH);

        // Lógica de eventos intacta
        btnEntrar.addActionListener(e -> {
            String nombre = campoNombre.getText().replace(":", "").replace("/", "").trim();
            if (nombre.isEmpty()) {
                lblError.setText("Introduce un nombre válido.");
                campoNombre.requestFocusInWindow();
                return;
            }
            String ip = campoIp.getText().trim();
            if (ip.isEmpty()) {
                ip = "localhost";
            }
            resultado[0] = nombre;
            resultado[1] = ip;
            resultado[2] = btnFrancotirador.isSelected() ? "FRANCOTIRADOR" : "CORREDOR";
            dialogo.dispose();
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());

        dialogo.setContentPane(raiz);
        dialogo.getRootPane().setDefaultButton(btnEntrar);
        dialogo.setResizable(false);
        dialogo.setSize(390, 415);
        dialogo.setLocationRelativeTo(null);
        dialogo.setVisible(true);

        return resultado[0] == null ? null : resultado;
    }

    private static JLabel crearEtiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(TEXTO_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JTextField crearCampo(String inicial) {
        JTextField c = new JTextField(inicial);
        c.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.setBackground(CAMPO_TEXTO);
        c.setForeground(TEXTO_CLARO);
        c.setCaretColor(NARANJA_ACC);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_SUTIL, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return c;
    }

    /** Aplica un diseño personalizado con bordes redondeados y colores consistentes sin celeste */
    private static void estilizarBotonRedondeado(AbstractButton boton, boolean esAccionPrincipal) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setForeground(TEXTO_CLARO);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Pintado personalizado para lograr el efecto "mini-rounded" limpio
        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = c.getWidth();
                int h = c.getHeight();

                Color colorFondo;
                Color colorBorde;

                if (boton instanceof JToggleButton) {
                    boolean sel = ((JToggleButton) boton).isSelected();
                    colorFondo = sel ? ROJO_SANGRE : CAMPO_TEXTO;
                    colorBorde = sel ? NARANJA_ACC : BORDE_SUTIL;
                } else {
                    colorFondo = esAccionPrincipal ? ROJO_SANGRE : CAMPO_TEXTO;
                    colorBorde = esAccionPrincipal ? NARANJA_ACC : BORDE_SUTIL;
                }

                // Efecto al pasar el cursor o presionar
                if (boton.getModel().isPressed()) {
                    colorFondo = colorFondo.darker();
                } else if (boton.getModel().isRollover()) {
                    colorFondo = colorFondo.brighter();
                }

                // Dibujar fondo redondeado (radio de 8 píxeles)
                g2.setColor(colorFondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));

                // Dibujar borde sutil
                g2.setColor(colorBorde);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));

                g2.dispose();
                super.paint(g, c);
            }
        });
    }
}