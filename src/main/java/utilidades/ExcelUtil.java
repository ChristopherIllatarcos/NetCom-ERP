package utilidades;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

public class ExcelUtil {

    /**
     * Exporta el contenido de una JTable a un archivo compatible con Microsoft
     * Excel (.xls / .csv).
     *
     * @param tabla La tabla cuyos datos se van a exportar.
     * @param tituloHoja Título de la tabla para el encabezado del archivo.
     */
    public static void exportarTablaAExcel(JTable tabla, String tituloHoja) {
        if (tabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "No hay datos en la tabla para exportar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte en Excel");
        fileChooser.setSelectedFile(new File(tituloHoja + ".xls"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Excel (*.xls)", "xls"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Asegurar la extensión .xls
            if (!fileToSave.getName().toLowerCase().endsWith(".xls")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
            }

            try (FileWriter out = new FileWriter(fileToSave)) {
                TableModel model = tabla.getModel();

                // Escribir Encabezados de Columna
                for (int i = 0; i < model.getColumnCount(); i++) {
                    out.write(model.getColumnName(i) + "\t");
                }
                out.write("\n");

                // Escribir Filas de Datos
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        out.write((val != null ? val.toString() : "") + "\t");
                    }
                    out.write("\n");
                }

                JOptionPane.showMessageDialog(null, "El archivo Excel se exportó correctamente en:\n" + fileToSave.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error al guardar el archivo Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
