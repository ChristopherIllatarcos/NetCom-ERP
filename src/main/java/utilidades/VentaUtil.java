package utilidades;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class VentaUtil {

    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

    /**
     * Formatea un número a representación de moneda ($ 1.000)
     *
     * @param monto
     * @return
     */
    public static String formatearMoneda(double monto) {
        return FORMATO_MONEDA.format(monto);
    }

    /**
     * Calcula los totales de las columnas y actualiza los JTextField de la
     * parte inferior.
     *
     * @param model
     * @param lblTotalDoc
     * @param lblTotalNeto
     * @param lblTotalGeneral
     */
    public static void actualizarTotales(DefaultTableModel model, JLabel lblTotalDoc, JLabel lblTotalNeto, JLabel lblTotalGeneral) {
        int totalDoctos = 0;
        double acumuladoNeto = 0.0;
        double acumuladoTotal = 0.0;

        for (int i = 0; i < model.getRowCount(); i++) {
            totalDoctos += Integer.parseInt(model.getValueAt(i, 1).toString());

            Object valNeto = model.getValueAt(i, 2);
            Object valTotal = model.getValueAt(i, 3);

            // Si viene como Number (Double/Integer) se suma directo, si viene como String formateado se limpia primero
            if (valNeto instanceof Number number) {
                acumuladoNeto += number.doubleValue();
            } else if (valNeto != null) {
                acumuladoNeto += parsearMonto(valNeto.toString());
            }

            if (valTotal instanceof Number number) {
                acumuladoTotal += number.doubleValue();
            } else if (valTotal != null) {
                acumuladoTotal += parsearMonto(valTotal.toString());
            }
        }

        lblTotalDoc.setText(String.valueOf(totalDoctos));
        lblTotalNeto.setText(formatearMoneda(acumuladoNeto));
        lblTotalGeneral.setText(formatearMoneda(acumuladoTotal));
    }

// Método auxiliar de limpieza para remover $, puntos y comas de la cadena
    private static double parsearMonto(String texto) {
        try {
            // Elimina todo lo que no sea dígito o signo menos (remueve '$', '.', etc.)
            String limpio = texto.replaceAll("[^0-9-]", "");
            return limpio.isEmpty() ? 0.0 : Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Cargar ventas por cliente en el JTable
    public static void cargarTablaVentasCliente(JTable tabla, List<Object[]> listaVentas) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Object[] fila : listaVentas) {
            model.addRow(new Object[]{
                fila[0], // RUT
                fila[1], // Nombre Cliente
                fila[2], // N° Doc
                sdf.format((Date) fila[3]), // Fecha
                formatearMoneda((Double) fila[4]) // Total $
            });
        }
    }

    // Actualizar Totales para esta vista usando JLabel con decimales
    public static void actualizarTotalesCliente(DefaultTableModel model, JLabel lblTotalDoctos, JLabel lblTotalVentas) {
        int totalDoctos = model.getRowCount();
        double acumuladoVentas = 0.0;

        for (int i = 0; i < totalDoctos; i++) {
            Object valTotal = model.getValueAt(i, 4);
            if (valTotal != null) {
                // Remueve todo excepto números y punto/coma decimal para parsear de forma segura
                String strVal = valTotal.toString().replaceAll("[^0-9.]", "");
                if (!strVal.isEmpty()) {
                    acumuladoVentas += Double.parseDouble(strVal);
                }
            }
        }

        // Configuración con separador de miles por punto (.) y decimales por coma (,)
        java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "CL"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        // Patrón con 2 decimales (.00)
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0.00", simbolos);

        // Asignación a los JLabels
        lblTotalDoctos.setText(String.valueOf(totalDoctos));
        lblTotalVentas.setText(df.format(acumuladoVentas));
    }
}
