/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zombie_escape_multiplayer.com.zombieescape.cliente;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

public class PanelRanking extends JPanel {

    private JLabel titulo;
    private DefaultTableModel modelo;

    public PanelRanking() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        titulo = new JLabel("Fin de la partida", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        add(titulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Puesto", "Jugador", "Puntaje"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton botonSalir = new JButton("Salir");
        botonSalir.addActionListener(e -> System.exit(0));
        JPanel abajo = new JPanel();
        abajo.add(botonSalir);
        add(abajo, BorderLayout.SOUTH);
    }

    // Muestra la tabla de mayor a menor puntaje. resultado: "VICTORIA" o "DERROTA"
    public void mostrar(ArrayList<PanelJuego.VistaJugador> jugadores, String resultado) {
        if (resultado.equals("VICTORIA")) {
            titulo.setText("¡VICTORIA! Todos escaparon");
            titulo.setForeground(new Color(30, 130, 60));
        } else {
            titulo.setText("DERROTA");
            titulo.setForeground(new Color(180, 40, 40));
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
}
