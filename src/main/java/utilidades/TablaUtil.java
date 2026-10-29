package utilidades;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;

public class TablaUtil {

    // Maneja el clic en la cabecera (columna 0) para marcar/desmarcar todos los checkboxes
    public static void configurarSeleccionarTodoHeader(JTable tabla) {
        tabla.getTableHeader().addMouseListener(new MouseAdapter() {
            private boolean seleccionarTodo = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                int columna = tabla.columnAtPoint(e.getPoint());
                if (columna == 0) {
                    seleccionarTodo = !seleccionarTodo;
                    DefaultTableModel model = (DefaultTableModel) tabla.getModel();

                    for (int i = 0; i < model.getRowCount(); i++) {
                        model.setValueAt(seleccionarTodo, i, 0);
                    }

                    String tituloHeader = seleccionarTodo ? "Sel. [✓]" : "Sel.";
                    tabla.getColumnModel().getColumn(0).setHeaderValue(tituloHeader);
                    tabla.getTableHeader().repaint();
                }
            }
        });
    }

    // Configura el botón por defecto con ENTER y los listeners en los JDateChooser
    public static void configurarEventosEnterBusqueda(
            JRootPane rootPane,
            JButton btnBuscar,
            JDateChooser fechaDesde,
            JDateChooser fechaHasta,
            ActionListener accionBuscar) {

        if (rootPane != null && btnBuscar != null) {
            rootPane.setDefaultButton(btnBuscar);
        }

        if (fechaDesde != null) {
            ((JTextField) fechaDesde.getDateEditor().getUiComponent()).addActionListener(accionBuscar);
        }

        if (fechaHasta != null) {
            ((JTextField) fechaHasta.getDateEditor().getUiComponent()).addActionListener(accionBuscar);
        }
    }

    /**
     * Aplica un renderizador personalizado a la tabla para pintar las filas
     * según el estado registrado en la columna indicada.
     * 
     * @param tabla La JTable a personalizar.
     * @param columnaEstado El índice de la columna que contiene el estado (por ejemplo, 5 para "Estado").
     */
    public static void aplicarColorPorEstado(JTable tabla, int columnaEstado) {
        // Guardamos el renderizador original para no perder el CheckBox de la Columna 0
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Obtener el valor de la columna Estado en la fila actual
                Object valorEstado = table.getValueAt(row, columnaEstado);
                String estado = (valorEstado != null) ? valorEstado.toString().trim().toUpperCase() : "";

                // Si la fila está seleccionada por el usuario, mantenemos la selección por defecto
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    // Colores según el estado
                    switch (estado) {
                        case "ANULADO", "ANULADA" -> {
                            c.setBackground(new Color(255, 218, 218)); // Rojo suave / rosado claro
                            c.setForeground(new Color(150, 0, 0));       // Texto rojo oscuro
                        }
                        case "EMITIDA", "EMITIDO" -> {
                            c.setBackground(new Color(230, 245, 230)); // Verde suave (opcional)
                            c.setForeground(Color.BLACK);
                        }
                        default -> {
                            // Color alternado blanco / gris para filas normales
                            if (row % 2 == 0) {
                                c.setBackground(Color.WHITE);
                            } else {
                                c.setBackground(new Color(245, 245, 245));
                            }   c.setForeground(Color.BLACK);
                        }
                    }
                }

                return c;
            }
        });
    }
}
