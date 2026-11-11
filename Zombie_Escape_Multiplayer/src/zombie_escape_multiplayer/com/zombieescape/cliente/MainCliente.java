/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.cliente;


import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainCliente {

    private static final int PUERTO = 5050;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String nombre = JOptionPane.showInputDialog(null, "Tu nombre:", "Zombi Escape",
                    JOptionPane.QUESTION_MESSAGE);
            if (nombre == null) {
                return;
            }
            // Los símbolos ":" y "/" se usan en el protocolo, así que los quitamos
            nombre = nombre.replace(":", "").replace("/", "").trim();
            if (nombre.isEmpty()) {
                return;
            }

            String ip = JOptionPane.showInputDialog(null, "IP del servidor:", "localhost");
            if (ip == null) {
                return;
            }

            Object[] roles = {"CORREDOR", "FRANCOTIRADOR"};
            int opcion = JOptionPane.showOptionDialog(null, "Elige tu rol:", "Zombi Escape",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
            if (opcion < 0) {
                return;
            }
            String rol = (String) roles[opcion];

            ClienteZombi cliente = new ClienteZombi(ip.trim(), PUERTO);
            VentanaPrincipal ventana = new VentanaPrincipal(cliente, nombre, rol);
            cliente.setVentana(ventana);

            if (!cliente.conectar(nombre, rol)) {
                JOptionPane.showMessageDialog(null, "No se pudo conectar al servidor.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ventana.setVisible(true);
        });
    }
}
