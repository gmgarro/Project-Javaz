/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.cliente;


import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PruebaGui {

    public static void main(String[] args) throws Exception {
        String texto = new String(Files.readAllBytes(Paths.get("escenarios/escenario_01.txt")));
        final String datos = texto.trim().replace("\r\n", ";").replace("\n", ";");

        SwingUtilities.invokeLater(() -> {
            // Un cliente de mentira: en vez de mandar por red, imprime lo que enviaría
            ClienteZombi cliente = new ClienteZombi("localhost", 5050) {
                @Override
                public void enviarMensaje(String mensaje) {
                    System.out.println("Enviaría al servidor: " + mensaje);
                }
            };
            VentanaPrincipal ventana = new VentanaPrincipal(cliente, "ana", "CORREDOR");
            ventana.setVisible(true);

            // Mensajes como los mandaría el servidor
            ventana.procesarMensaje("MAPA:1:20:20:" + datos);
            ventana.procesarMensaje("ESTADO:ana:1:1:VIVO");
            ventana.procesarMensaje("ESTADO:beto:2:3:VIVO");
            ventana.procesarMensaje("ESTADO:carla:3:2:ESCONDIDO");
            ventana.procesarMensaje("ZOMBI:1:7:9:ABAJO:VIVO");
            ventana.procesarMensaje("ZOMBI:2:15:3:DERECHA:VIVO");
            ventana.procesarMensaje("TIEMPO:83");
            ventana.procesarMensaje("PUNTAJE:ana:850");
        });
    }
}
