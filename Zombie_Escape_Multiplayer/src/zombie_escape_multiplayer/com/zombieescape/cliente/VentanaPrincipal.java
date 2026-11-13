/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.cliente;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaPrincipal extends JFrame {

    private static final String PANTALLA_LOBBY = "LOBBY";
    private static final String PANTALLA_JUEGO = "JUEGO";
    private static final String PANTALLA_RANKING = "RANKING";

    private ClienteZombi cliente;
    private String nombre;

    private CardLayout cartas = new CardLayout();
    private JPanel contenedor = new JPanel(cartas);

    private PanelLobby panelLobby;
    private PanelJuego panelJuego;
    private PanelChat chatDelJuego;
    private PanelRanking panelRanking;
    private boolean enJuego;

    public VentanaPrincipal(ClienteZombi cliente, String nombre, String rol) {
        super("Zombi Escape Multijugador - " + nombre);
        this.cliente = cliente;
        this.nombre = nombre;

        panelLobby = new PanelLobby(cliente, nombre, rol);
        panelJuego = new PanelJuego(cliente, nombre, rol);
        chatDelJuego = new PanelChat(cliente);
        panelRanking = new PanelRanking();

        // Pantalla de juego: el mapa al centro y el chat a la derecha
        JPanel pantallaJuego = new JPanel(new BorderLayout());
        pantallaJuego.add(panelJuego, BorderLayout.CENTER);
        pantallaJuego.add(chatDelJuego, BorderLayout.EAST);

        contenedor.add(panelLobby, PANTALLA_LOBBY);
        contenedor.add(pantallaJuego, PANTALLA_JUEGO);
        contenedor.add(panelRanking, PANTALLA_RANKING);
        add(contenedor);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                VentanaPrincipal.this.cliente.desconectar();
            }
        });
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    // Aquí llega TODO lo que manda el servidor (ClienteZombi lo llama dentro de invokeLater)
    public void procesarMensaje(String mensaje) {
        if (mensaje.startsWith("CHAT:")) {
            String[] partes = mensaje.split(":", 3);
            if (partes.length == 3) {
                agregarAlChat(partes[1], partes[2]);
            }
        } else if (mensaje.startsWith("ERROR")) {
            agregarAlChat("Sistema", mensaje);
        } else if (mensaje.startsWith("ROL:")) {
            String[] partes = mensaje.split(":");
            if (partes.length >= 3 && partes[1].equals(nombre)) {
                panelJuego.setRol(partes[2]);
            }
        } else if (mensaje.equals("PARTIDA_INICIADA")) {
            mostrarJuego();
        } else if (mensaje.startsWith("MAPA:")) {
            mostrarJuego();
            panelJuego.procesarMensaje(mensaje);
        } else if (mensaje.startsWith("FIN:")) {
            panelRanking.mostrar(panelJuego.getJugadores(), mensaje.substring(4));
            cartas.show(contenedor, PANTALLA_RANKING);
            enJuego = false;
        } else if (mensaje.startsWith("ESTADO:") || mensaje.startsWith("ZOMBI:")
                || mensaje.startsWith("TIEMPO:") || mensaje.startsWith("PUNTAJE:")
                || mensaje.startsWith("DESCONEXION:")
                || mensaje.startsWith("VISTA:") || mensaje.startsWith("AMMO:")) {
            panelJuego.procesarMensaje(mensaje);
        }
    }

    private void agregarAlChat(String emisor, String texto) {
        panelLobby.getPanelChat().agregarMensaje(emisor, texto);
        chatDelJuego.agregarMensaje(emisor, texto);
    }

    private void mostrarJuego() {
        if (!enJuego) {
            enJuego = true;
            cartas.show(contenedor, PANTALLA_JUEGO);
            SwingUtilities.invokeLater(() -> panelJuego.enfocar());
        }
    }
    
    public PanelLobby getPanelLobby() {
        return panelLobby;
    }
}
