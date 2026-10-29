package conexion;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipal extends JFrame {
    
    public MenuPrincipal() {
        setTitle("GORISOFT - Sistema de Gestión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Panel superior con título
        JLabel titulo = new JLabel("SISTEMA GORISOFT", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo, BorderLayout.NORTH);
        
        // Panel central con botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(9, 1, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        panelBotones.add(crearBoton("1. Ventas (Ingreso)"));
        panelBotones.add(crearBoton("2. Consulta Documentos"));
        panelBotones.add(crearBoton("3. Hojas de Ruta"));
        panelBotones.add(crearBoton("4. Mantención de Ventas"));
        panelBotones.add(crearBoton("5. Informe Ventas por Cliente"));
        panelBotones.add(crearBoton("6. Ficha de Clientes"));
        panelBotones.add(crearBoton("7. Comisiones x Ventas"));
        panelBotones.add(crearBoton("8. Comisiones x Cobranza"));
        panelBotones.add(crearBoton("9. Ficha de Vendedores"));
        
        add(panelBotones, BorderLayout.CENTER);
        
        // Panel inferior con estado
        JLabel estado = new JLabel("Listo", SwingConstants.CENTER);
        estado.setBorder(BorderFactory.createEtchedBorder());
        add(estado, BorderLayout.SOUTH);
    }
    
    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.PLAIN, 14));
        boton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Módulo: " + texto, 
                "GORISOFT", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        return boton;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });
    }
}
