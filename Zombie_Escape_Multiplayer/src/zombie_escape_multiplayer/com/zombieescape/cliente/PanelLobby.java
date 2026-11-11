package zombie_escape_multiplayer.com.zombieescape.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

public class PanelLobby extends JPanel {
    private JComboBox<String> comboRol;
    private JButton botonListo;
    private JButton botonCargarMapa;
    private JButton botonIniciarPartida;
    private JLabel labelEstadoMapa;
    private PanelChat panelChat;
    private ClienteZombi cliente;
    private String nombreJugador;

    public PanelLobby(ClienteZombi cliente, String nombreJugador) {
        this.cliente = cliente;
        this.nombreJugador = nombreJugador;
        setLayout(new BorderLayout(10, 10));

        // --- PANEL SUPERIOR: Selección de Rol y Archivo de Escenario ---
        JPanel panelControles = new JPanel(new GridLayout(3, 2, 10, 10));
        panelControles.setBorder(BorderFactory.createTitledBorder("Configuración de Jugador y Sala"));

        panelControles.add(new JLabel(" Seleccionar Rol:"));
        comboRol = new JComboBox<>(new String[]{"CORREDOR", "FRANCOTIRADOR"});
        panelControles.add(comboRol);

        botonCargarMapa = new JButton("Cargar Escenario Custom (.txt)");
        panelControles.add(botonCargarMapa);

        labelEstadoMapa = new JLabel("Escenario: Mapas base (1 al 10)");
        panelControles.add(labelEstadoMapa);

        botonIniciarPartida = new JButton("¡INICIAR PARTIDA!");
        botonIniciarPartida.setBackground(new Color(46, 139, 87));
        botonIniciarPartida.setForeground(Color.WHITE);
        botonIniciarPartida.setFont(new Font("Arial", Font.BOLD, 14));
        
        panelControles.add(new JLabel("")); // Espacio vacío
        panelControles.add(botonIniciarPartida);

        add(panelControles, BorderLayout.NORTH);

        // --- PANEL CENTRAL: Chat Integrado ---
        panelChat = new PanelChat(cliente);
        add(panelChat, BorderLayout.CENTER);

        // --- EVENTOS ---

        // Evento cambio de Rol
        comboRol.addActionListener(e -> {
            String rolSeleccionado = (String) comboRol.getSelectedItem();
            cliente.enviarMensaje("UNIRSE:" + nombreJugador + "/" + rolSeleccionado);
        });

        // Evento Cargar Escenario desde el Lobby (JFileChooser)
        botonCargarMapa.addActionListener(e -> seleccionarYEnviarMapa());

        // Evento Iniciar Partida
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
                // Leer todo el contenido del archivo .txt del mapa
                String contenido = new String(Files.readAllBytes(archivoSeleccionado.toPath()));
                contenido = contenido.replace("\r\n", ";").replace("\n", ";"); // Formatear saltos de línea

                // Enviar comando al servidor para validar BFS y guardarlo como escenario_11.txt
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
}