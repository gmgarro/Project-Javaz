package zombie_escape_multiplayer.com.zombieescape.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelChat extends JPanel {
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private JButton botonEnviar;
    private ClienteZombi cliente;

    public PanelChat(ClienteZombi cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Chat Multijugador"));

        // Área donde se muestran los mensajes
        areaChat = new JTextArea(10, 25);
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(areaChat);

        // Panel inferior para escribir y enviar
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        campoMensaje = new JTextField();
        botonEnviar = new JButton("Enviar");

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Acciones para enviar mensaje al presionar Enter o dar clic en Enviar
        ActionListener accionEnviar = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarChat();
            }
        };

        botonEnviar.addActionListener(accionEnviar);
        campoMensaje.addActionListener(accionEnviar);
    }

    private void enviarChat() {
        String texto = campoMensaje.getText().trim();
        if (!texto.isEmpty() && cliente != null) {
            // Se envía la trama del protocolo CHAT:mensaje
            cliente.enviarMensaje("CHAT:" + texto);
            campoMensaje.setText("");
        }
    }

    // Método para agregar un mensaje recibido a la pantalla
    public void agregarMensaje(String emisor, String mensaje) {
        areaChat.append(emisor + ": " + mensaje + "\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }
}