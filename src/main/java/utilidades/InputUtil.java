package utilidades;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class InputUtil {

    /**
     * Muestra un diálogo modal para solicitar texto sin valor inicial.
     * @param parent
     * @param titulo
     * @param mensaje
     * @return 
     */
    public static String mostrarInput(Component parent, String titulo, String mensaje) {
        return mostrarInput(parent, titulo, mensaje, null);
    }

    /**
     * Muestra un diálogo modal para solicitar texto permitiendo un valor
     * inicial pre-cargado.
     * @param parent
     * @param titulo
     * @param mensaje
     * @param valorInicial
     * @return 
     */
    public static String mostrarInput(Component parent, String titulo, String mensaje, String valorInicial) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window instanceof Frame ? (Frame) window : null, titulo, Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        JLabel lblMensaje = new JLabel(mensaje);
        panelNorte.add(lblMensaje);
        dialog.add(panelNorte, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JTextField txtInput = new JTextField(25);
        if (valorInicial != null) {
            txtInput.setText(valorInicial);
            txtInput.selectAll();
        }
        panelCentro.add(txtInput);
        dialog.add(panelCentro, BorderLayout.CENTER);

        final String[] resultado = {null};

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.setPreferredSize(new Dimension(90, 30));
        btnCancelar.setPreferredSize(new Dimension(90, 30));

        Runnable aceptarAction = () -> {
            String texto = txtInput.getText();
            if (texto != null && !texto.trim().isEmpty()) {
                resultado[0] = texto.trim();
                dialog.dispose();
            } else {
                AlertaUtil.mostrarAdvertencia(dialog, "El campo no puede estar vacío.");
                txtInput.requestFocus();
            }
        };

        btnAceptar.addActionListener(e -> aceptarAction.run());
        btnCancelar.addActionListener(e -> {
            resultado[0] = null;
            dialog.dispose();
        });

        txtInput.addActionListener(e -> aceptarAction.run());
        dialog.getRootPane().registerKeyboardAction(e -> {
            resultado[0] = null;
            dialog.dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        panelSur.add(btnAceptar);
        panelSur.add(btnCancelar);
        dialog.add(panelSur, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtInput.requestFocusInWindow();
            }
        });

        dialog.setVisible(true);

        return resultado[0];
    }
}
