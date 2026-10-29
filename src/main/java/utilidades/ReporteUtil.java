package utilidades;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.HojaRutaDAO;

import java.awt.Component;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.*;
import com.toedter.calendar.JDateChooser;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class ReporteUtil {

    public static void generarPdfHojaRuta(Component parent, int hojaRutaId, String numRuta) {
        // Formato A4 con márgenes ajustados para aprovechar bien el espacio
        Document document = new Document(PageSize.A4, 25, 25, 25, 25);
        String nombreArchivo = "Hoja_Ruta_" + numRuta + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
            document.open();

            // 1. ENCABEZADO
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph titulo = new Paragraph("HOJA DE RUTA N° " + numRuta, fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Font fontFecha = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
            String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            Paragraph subFecha = new Paragraph("Fecha de impresión: " + fechaActual, fontFecha);
            subFecha.setAlignment(Element.ALIGN_CENTER);
            document.add(subFecha);

            document.add(new Paragraph(" ")); // Espacio pequeño

            // 2. CREACIÓN DE LA TABLA (Una sola tabla para todos los registros)
            PdfPTable table = new PdfPTable(6); // 6 columnas
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 3f, 3.5f, 4.5f, 2f, 1f});

            // Encabezados de la tabla
            String[] headers = {"Folio", "Tipo Doc.", "Cliente", "Dirección", "Monto", "Bulto"};
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // 3. OBTENER Y AGREGAR TODOS LOS REGISTROS A LA MISMA TABLA
            dao.HojaRutaDAO daoPdf = new dao.HojaRutaDAO();
            List<Object[]> detalle = daoPdf.obtenerDetalleRuta(hojaRutaId);

            Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
            double montoTotal = 0;
            int totalBultos = 0;

            // Recorremos la lista y agregamos TODAS las filas dentro de la MISMA tabla
            for (Object[] fila : detalle) {
                table.addCell(new Phrase(fila[0].toString(), fontData)); // Folio
                table.addCell(new Phrase(fila[1].toString(), fontData)); // Tipo Documento
                table.addCell(new Phrase(fila[2].toString(), fontData)); // Cliente
                table.addCell(new Phrase(fila[3].toString(), fontData)); // Dirección

                double monto = Double.parseDouble(fila[4].toString());
                montoTotal += monto;
                table.addCell(new Phrase("$" + String.format("%.0f", monto), fontData)); // Monto

                int bulto = Integer.parseInt(fila[5].toString());
                totalBultos += bulto;
                table.addCell(new Phrase(String.valueOf(bulto), fontData)); // Bultos
            }

            // Agregamos la tabla al documento (SE AGREGA UNA SOLA VEZ)
            document.add(table);

            // 4. RESUMEN FINAL / TOTALES
            document.add(new Paragraph(" "));
            Font fontResumen = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
            document.add(new Paragraph("Total Folios: " + detalle.size()
                    + "   |   Total Bultos: " + totalBultos
                    + "   |   Monto Total Ruta: $" + String.format("%.0f", montoTotal), fontResumen));

            document.close();

            // 5. ABRIR EL PDF
            File pdfFile = new File(nombreArchivo);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

        } catch (DocumentException | IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Error al generar el PDF. " + e.getMessage(), "Error PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarPdfRutasConsolidadas(Component parent, List<Integer> idsRutas, List<String> numerosRuta) {
        try {
            HojaRutaDAO dao = new HojaRutaDAO();
            List<Object[]> detalleConsolidado = new ArrayList<>();

            // 1. Unificar todos los documentos en una sola lista
            for (Integer idRuta : idsRutas) {
                List<Object[]> detalle = dao.obtenerDetalleRuta(idRuta);
                detalleConsolidado.addAll(detalle);
            }

            if (detalleConsolidado.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No se encontraron documentos para imprimir.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 2. Definir un solo nombre de archivo
            String textoRutas = String.join(", ", numerosRuta);
            String rutaArchivo = "Hoja_Ruta_Consolidada.pdf";

            // 3. Crear el documento PDF unificado (usando iText)
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(rutaArchivo));
            document.open();

            // Encabezado principal
            com.itextpdf.text.Font fontTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("HOJA DE RUTA N° " + textoRutas, fontTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);

            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph("Fecha de impresión: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
            fecha.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(fecha);
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Tabla con 6 columnas
            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1.5f, 3.5f, 4f, 4f, 2f, 1.5f});

            // Encabezados de la tabla
            String[] titulos = {"Folio", "Tipo Doc.", "Cliente", "Dirección", "Monto", "Bulto"};

            // Definimos la fuente para el encabezado: Helvetica, tamaño 10, NEGRITA y color BLANCO
            com.itextpdf.text.Font fontEncabezado = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                    10,
                    com.itextpdf.text.Font.BOLD,
                    com.itextpdf.text.BaseColor.WHITE
            );

            for (String t : titulos) {
                // Le pasamos la frase junto con la fuente blanca
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(t, fontEncabezado)
                );

                cell.setBackgroundColor(com.itextpdf.text.BaseColor.DARK_GRAY); // Fondo oscuro
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER); // (Opcional) Centra los títulos
                cell.setPadding(5); // (Opcional) Un poco de margen interno para mejor estilo

                tabla.addCell(cell);
            }

            // Acumuladores de totales
            int totalFolios = 0;
            int totalBultos = 0;
            double montoTotal = 0.0;

            // Llenar filas con los datos de TODAS las rutas seleccionadas
            for (Object[] fila : detalleConsolidado) {
                tabla.addCell(fila[0] != null ? fila[0].toString() : ""); // Folio
                tabla.addCell(fila[1] != null ? fila[1].toString() : ""); // Tipo Doc
                tabla.addCell(fila[2] != null ? fila[2].toString() : ""); // Cliente
                tabla.addCell(fila[3] != null ? fila[3].toString() : ""); // Dirección

                double monto = fila[4] != null ? Double.parseDouble(fila[4].toString()) : 0.0;
                int bultos = fila[5] != null ? Integer.parseInt(fila[5].toString()) : 0;

                tabla.addCell("$" + (long) monto);
                tabla.addCell(String.valueOf(bultos));

                totalFolios++;
                totalBultos += bultos;
                montoTotal += monto;
            }

            document.add(tabla);
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Pie con resumen de totales
            String resumen = String.format("Total Folios: %d  |  Total Bultos: %d  |  Monto Total Ruta: $%d", totalFolios, totalBultos, (long) montoTotal);
            com.itextpdf.text.Paragraph pResumen = new com.itextpdf.text.Paragraph(resumen);
            pResumen.setFont(new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD));
            document.add(pResumen);

            document.close();

            // 4. Abrir el único PDF generado
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(rutaArchivo));
            }

        } catch (DocumentException | HeadlessException | IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Error al generar el PDF consolidado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el consolidado de resumen de ventas por período.
     */
    /**
     * Genera e imprime en PDF el consolidado de resumen de ventas por período.
     *
     * @param tabla
     * @param jDateChooserInicial
     * @param jDateChooserFinal
     * @param lblTotalDoc
     * @param lblTotalNeto
     * @param lblTotalGeneral
     */
    public static void generarPDFResumenVentas(
            JTable tabla, JDateChooser jDateChooserInicial, JDateChooser jDateChooserFinal, JLabel lblTotalDoc, JLabel lblTotalNeto, JLabel lblTotalGeneral) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Extraer correctamente los objetos Date desde los JDateChooser
        java.util.Date fechaInicio = (jDateChooserInicial != null) ? jDateChooserInicial.getDate() : null;
        java.util.Date fechaFin = (jDateChooserFinal != null) ? jDateChooserFinal.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Resumen_Ventas_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado del Reporte
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("RESUMEN DE VENTAS POR PERÍODO", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            // Formatear las fechas de los JDateChooser de forma segura
            String strInicio = (fechaInicio != null) ? sdf.format(fechaInicio) : "";
            String strFin = (fechaFin != null) ? sdf.format(fechaFin) : "";

            String rangoFechas = "Período: " + strInicio + " al " + strFin;
            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(rangoFechas + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    if (j > 0) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Documentos: " + lblTotalDoc.getText()
                    + "   |   Total Neto: " + lblTotalNeto.getText()
                    + "   |   TOTAL GENERAL: " + lblTotalGeneral.getText(), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el reporte de Comisiones y Cobranzas por
     * vendedor.
     *
     * @param tabla
     * @param tituloReporte
     * @param lblTotal
     * @param archivoDestino
     * @return
     */
    public static boolean generarPDFComisiones(
            javax.swing.JTable tabla,
            String tituloReporte,
            javax.swing.JLabel lblTotal,
            java.io.File archivoDestino) {

        if (tabla == null || tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(archivoDestino));
            documento.open();

            // Estilos
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph pTitulo = new com.itextpdf.text.Paragraph(tituloReporte, fuenteTitulo);
            pTitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(pTitulo);

            com.itextpdf.text.Paragraph pFecha = new com.itextpdf.text.Paragraph("Fecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            pFecha.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(pFecha);

            // Tabla
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Imprimir Total desde el JLabel
            String textoTotal = (lblTotal != null) ? lblTotal.getText() : "0.00";
            com.itextpdf.text.Paragraph pTotal = new com.itextpdf.text.Paragraph("\nVENTA TOTAL: " + textoTotal, fuenteTotal);
            pTotal.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(pTotal);

            documento.close();

            // Abrir PDF automáticamente
            if (archivoDestino.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivoDestino);
            }

            return true;

        } catch (DocumentException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Genera e imprime en PDF el Libro de Compras filtrado por período.
     *
     * @param tabla
     * @param jDateChooserInicial
     * @param jDateChooserFinal
     * @param lblTotalDoc
     * @param lblTotalNeto
     * @param lblTotalGeneral
     */
    public static void generarPDFLibroCompras(
            JTable tabla, JDateChooser jDateChooserInicial, JDateChooser jDateChooserFinal, JLabel lblTotalDoc, JLabel lblTotalNeto, JLabel lblTotalGeneral) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte de compras.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Extraer correctamente los objetos Date desde los JDateChooser
        java.util.Date fechaInicio = (jDateChooserInicial != null) ? jDateChooserInicial.getDate() : null;
        java.util.Date fechaFin = (jDateChooserFinal != null) ? jDateChooserFinal.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Libro_De_Compras_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado del Reporte
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("LIBRO DE COMPRAS", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            // Formatear las fechas de forma segura (si son nulas muestra texto genérico)
            String strInicio = (fechaInicio != null) ? sdf.format(fechaInicio) : "Inicio";
            String strFin = (fechaFin != null) ? sdf.format(fechaFin) : "Actualidad";

            String rangoFechas = "Período: " + strInicio + " al " + strFin;
            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(rangoFechas + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha los valores monetarios (columnas finales)
                    if (j >= 3) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final utilizando los JLabel de la interfaz
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Doctos: " + lblTotalDoc.getText()
                    + "   |   Total Neto: " + lblTotalNeto.getText()
                    + "   |   TOTAL COMPRAS: " + lblTotalGeneral.getText(), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF del libro de compras: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Genera e imprime en PDF el Kardex de Mercadería con sus filtros y
     * totales.
     *
     * @param tabla
     * @param jDateChooserInicial
     * @param jDateChooserFinal
     * @param codProducto
     * @param descripcionProducto
     * @param lblEntradas
     * @param lblSalidas
     * @param lblSaldo
     */
    public static void generarPDFKardex(
            JTable tabla, JDateChooser jDateChooserInicial, JDateChooser jDateChooserFinal,
            String codProducto, String descripcionProducto,
            JLabel lblEntradas, JLabel lblSalidas, JLabel lblSaldo) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte del Kardex.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date fechaInicio = (jDateChooserInicial != null) ? jDateChooserInicial.getDate() : null;
        java.util.Date fechaFin = (jDateChooserFinal != null) ? jDateChooserFinal.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Kardex_" + codProducto + "_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("HISTORIAL DE MOVIMIENTOS - KARDEX", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            String strInicio = (fechaInicio != null) ? sdf.format(fechaInicio) : "Inicio";
            String strFin = (fechaFin != null) ? sdf.format(fechaFin) : "Actualidad";

            String infoProducto = "Producto: " + codProducto + " - " + descripcionProducto;
            String rangoFechas = "Período: " + strInicio + " al " + strFin;

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(infoProducto + "\n" + rangoFechas + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos (6 columnas: Fecha, Tipo Movimiento, N° Doc, Entrada, Salida, Saldo)
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha las columnas numéricas (Entrada, Salida, Saldo)
                    if (j >= 3) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Entradas: " + (lblEntradas != null ? lblEntradas.getText() : "0")
                    + "   |   Total Salidas: " + (lblSalidas != null ? lblSalidas.getText() : "0")
                    + "   |   Saldo Final en Stock: " + (lblSaldo != null ? lblSaldo.getText() : "0"), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF del Kardex: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el Reporte de Inventario al Corte.
     *
     * @param tabla
     * @param jDateChooserAlCorte
     * @param familiaSeleccionada
     * @param lblItems
     * @param lblTotalUni
     */
    public static void generarPDFInventario(
            JTable tabla, JDateChooser jDateChooserAlCorte, String familiaSeleccionada,
            JLabel lblItems, JLabel lblTotalUni) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte de inventario.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date fechaCorte = (jDateChooserAlCorte != null) ? jDateChooserAlCorte.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Inventario_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("REPORTE DE INVENTARIO VALORIZADO", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            String strCorte = (fechaCorte != null) ? sdf.format(fechaCorte) : sdf.format(new java.util.Date());
            String infoFiltro = "Familia: " + (familiaSeleccionada != null ? familiaSeleccionada : "TODAS");

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph("Al Corte: " + strCorte + "  |  " + infoFiltro + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha las columnas numéricas (Stock, Costo, Total Valorizado)
                    if (j >= 3) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Ítems: " + (lblItems != null ? lblItems.getText() : "0")
                    + "   |   Total Unidades: " + (lblTotalUni != null ? lblTotalUni.getText() : "0"), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF del inventario: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el Reporte de Inventario Crítico o Agotados.
     *
     * @param tabla
     * @param familiaSeleccionada
     * @param tipoFiltroCritico
     * @param lblTotalAlertas
     */
    public static void generarPDFInventarioCritico(
            JTable tabla, String familiaSeleccionada, String tipoFiltroCritico, JLabel lblTotalAlertas) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte crítico.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Inventario_Critico_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("REPORTE DE PRODUCTOS EN STOCK CRÍTICO", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            String infoFiltros = "Familia: " + (familiaSeleccionada != null ? familiaSeleccionada : "TODAS") + "  |  Estado: " + tipoFiltroCritico;

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(infoFiltros + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos (5 columnas: Código, Descripción, Stock, Stock Min, Faltante)
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(180, 50, 50)); // Rojo oscuro corporativo para alertas críticas
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha las columnas numéricas (Stock, Stock Min, Faltante)
                    if (j >= 2) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Productos en Alerta / Críticos: " + (lblTotalAlertas != null ? lblTotalAlertas.getText() : "0"), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF crítico: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el Reporte de Rotación de Ventas y Productos.
     *
     * @param tabla
     * @param familiaSeleccionada
     * @param tipoAgrupacion
     * @param jdcFechaInicio
     * @param jdcFechaFin
     * @param lblTotalUni
     * @param lblTotalVentas
     */
    public static void generarPDFRotacionVentas(
            JTable tabla, String familiaSeleccionada, String tipoAgrupacion,
            JDateChooser jdcFechaInicio, JDateChooser jdcFechaFin,
            JLabel lblTotalUni, JLabel lblTotalVentas) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte de rotación.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date fechaIni = (jdcFechaInicio != null) ? jdcFechaInicio.getDate() : null;
        java.util.Date fechaFin = (jdcFechaFin != null) ? jdcFechaFin.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Rotacion_Ventas_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("INFORME DE ROTACIÓN DE VENTAS Y PRODUCTOS", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            String strInicio = (fechaIni != null) ? sdf.format(fechaIni) : "Inicio";
            String strFin = (fechaFin != null) ? sdf.format(fechaFin) : "Actualidad";
            String infoFiltros = "Agrupado por: " + tipoAgrupacion + "  |  Familia: " + (familiaSeleccionada != null ? familiaSeleccionada : "TODAS") + "\nPeríodo: " + strInicio + " al " + strFin;

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(infoFiltros + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos (5 columnas: Código, Descripción, Cant.Vend, Total Vta, % Particip)
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153)); // Azul corporativo
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha las columnas numéricas
                    if (j >= 2) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Unidades Vendidas: " + (lblTotalUni != null ? lblTotalUni.getText() : "0")
                    + "   |   TOTAL VENTAS: " + (lblTotalVentas != null ? lblTotalVentas.getText() : "$0.00"), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (com.itextpdf.text.DocumentException | java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF de rotación: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el Informe de Ventas por Familia.
     *
     * @param tabla
     * @param jDateChooserInicial
     * @param jDateChooserFinal
     * @param familiaSeleccionada
     * @param lblTotalCate
     * @param lblMonto
     */
    public static void generarPDFVentasPorFamilia(
            JTable tabla, JDateChooser jDateChooserInicial, JDateChooser jDateChooserFinal,
            String familiaSeleccionada, JLabel lblTotalCate, JLabel lblMonto) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date fechaInicio = (jDateChooserInicial != null) ? jDateChooserInicial.getDate() : null;
        java.util.Date fechaFin = (jDateChooserFinal != null) ? jDateChooserFinal.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Ventas_Por_Familia_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("INFORME DE VENTAS POR FAMILIA", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            String strInicio = (fechaInicio != null) ? sdf.format(fechaInicio) : "Inicio";
            String strFin = (fechaFin != null) ? sdf.format(fechaFin) : "Actualidad";
            String infoFiltros = "Familia: " + (familiaSeleccionada != null ? familiaSeleccionada : "TODAS") + "\nPeríodo: " + strInicio + " al " + strFin;

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(infoFiltros + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153)); // Azul corporativo
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha las columnas numéricas (Cant. Vendida, Monto Total)
                    if (j >= 2) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Categorías: " + (lblTotalCate != null ? lblTotalCate.getText() : "0")
                    + "   |   Monto Total: " + (lblMonto != null ? lblMonto.getText() : "$0.00"), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (com.itextpdf.text.DocumentException | java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF de ventas por familia: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el reporte de Documentos en Cartera desde una
     * JTable.
     *
     * @param tabla
     * @param lblDoc
     * @param lblTotal
     */
    public static void generarPDFDocumentosCartera(
            javax.swing.JTable tabla, javax.swing.JLabel lblDoc, javax.swing.JLabel lblTotal) {

        if (tabla.getRowCount() != 0) {
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para imprimir el reporte.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String nombreArchivo = "Documentos_Cartera_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 15, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 9, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 8, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("ESTADO DE DOCUMENTOS EN CARTERA", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph("Fecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla PDF
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(4);
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Documentos: " + lblDoc.getText() + "   |   MONTO TOTAL CARTERA: " + lblTotal.getText(), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (com.itextpdf.text.DocumentException | java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF de cartera: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el Estadístico y Comportamiento Financiero de un
     * Cliente.
     *
     * @param parent
     * @param nombreCliente
     * @param lblSaldo
     * @param lblComprados
     * @param lblPromCom
     * @param lblTotalCom
     * @param lblUltimaCom
     * @param lblMaxCom
     * @param lblPagado
     * @param lblTotalPago
     * @param lblPromPago
     * @param lblUltimoPago
     * @param lblMaxPago
     */
    public static void generarPDFEstadisticoCliente(
            Component parent, String nombreCliente, javax.swing.JLabel lblSaldo,
            javax.swing.JLabel lblComprados, javax.swing.JLabel lblTotalCom, javax.swing.JLabel lblPromCom,
            javax.swing.JLabel lblUltimaCom, javax.swing.JLabel lblMaxCom,
            javax.swing.JLabel lblPagado, javax.swing.JLabel lblTotalPago, javax.swing.JLabel lblPromPago,
            javax.swing.JLabel lblUltimoPago, javax.swing.JLabel lblMaxPago) {

        if (nombreCliente == null || nombreCliente.isEmpty() || nombreCliente.contains("Seleccione")) {
            JOptionPane.showMessageDialog(parent, "Debe seleccionar un cliente válido para generar el reporte.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String nombreArchivo = "Estadistico_Cliente_" + System.currentTimeMillis() + ".pdf";

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            Font fuenteEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font fuenteValor = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            // Encabezado
            Paragraph titulo = new Paragraph("ESTADÍSTICO Y COMPORTAMIENTO FINANCIERO", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph subtitulo = new Paragraph("Cliente: " + nombreCliente + "\nFecha de Emisión: " + sdf.format(new Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // 1. Estado de Cuenta Actual
            PdfPTable tablaSaldo = new PdfPTable(2);
            tablaSaldo.setWidthPercentage(100);
            tablaSaldo.setWidths(new float[]{7f, 3f});

            PdfPCell cellHeaderSaldo = new PdfPCell(new Phrase("ESTADO DE CUENTA ACTUAL", fuenteSeccion));
            cellHeaderSaldo.setColspan(2);
            cellHeaderSaldo.setBackgroundColor(new BaseColor(51, 102, 153));
            cellHeaderSaldo.setPadding(6);
            tablaSaldo.addCell(cellHeaderSaldo);

            agregarFilaReporte(tablaSaldo, "Saldo Actual Pendiente:", lblSaldo.getText(), fuenteEtiqueta, fuenteValor);
            documento.add(tablaSaldo);
            documento.add(new Paragraph(" "));

            // 2. Historial de Compras
            PdfPTable tablaCompras = new PdfPTable(2);
            tablaCompras.setWidthPercentage(100);
            tablaCompras.setWidths(new float[]{7f, 3f});

            PdfPCell cellHeaderCompras = new PdfPCell(new Phrase("HISTORIAL DE COMPRAS", fuenteSeccion));
            cellHeaderCompras.setColspan(2);
            cellHeaderCompras.setBackgroundColor(new BaseColor(51, 102, 153));
            cellHeaderCompras.setPadding(6);
            tablaCompras.addCell(cellHeaderCompras);

            agregarFilaReporte(tablaCompras, "Veces que ha comprado:", lblComprados.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaCompras, "Total de compras:", lblTotalCom.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaCompras, "Promedio de compra:", lblPromCom.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaCompras, "Última compra:", lblUltimaCom.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaCompras, "Máximo comprado:", lblMaxCom.getText(), fuenteEtiqueta, fuenteValor);
            documento.add(tablaCompras);
            documento.add(new Paragraph(" "));

            // 3. Historial de Pagos
            PdfPTable tablaPagos = new PdfPTable(2);
            tablaPagos.setWidthPercentage(100);
            tablaPagos.setWidths(new float[]{7f, 3f});

            PdfPCell cellHeaderPagos = new PdfPCell(new Phrase("HISTORIAL DE PAGOS", fuenteSeccion));
            cellHeaderPagos.setColspan(2);
            cellHeaderPagos.setBackgroundColor(new BaseColor(51, 102, 153));
            cellHeaderPagos.setPadding(6);
            tablaPagos.addCell(cellHeaderPagos);

            agregarFilaReporte(tablaPagos, "Veces que ha pagado:", lblPagado.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaPagos, "Total de pagos:", lblTotalPago.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaPagos, "Promedio de pago:", lblPromPago.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaPagos, "Último pago:", lblUltimoPago.getText(), fuenteEtiqueta, fuenteValor);
            agregarFilaReporte(tablaPagos, "Máximo pagado:", lblMaxPago.getText(), fuenteEtiqueta, fuenteValor);
            documento.add(tablaPagos);

            documento.close();

            // Abrir el PDF automáticamente
            File file = new File(nombreArchivo);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            JOptionPane.showMessageDialog(parent, "Error al generar el PDF del estadístico: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void agregarFilaReporte(PdfPTable tabla, String etiqueta, String valor, Font fEti, Font fVal) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, fEti));
        c1.setPadding(5);
        tabla.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor, fVal));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(5);
        tabla.addCell(c2);
    }

    /**
     * Genera e imprime en PDF la Cartola de Cliente por Ventas con sus totales.
     *
     * @param tabla
     * @param nombreCliente
     * @param rutCliente
     * @param jDateChooserInicial
     * @param jDateChooserFinal
     * @param lblTotalFac
     * @param lblTotalAbonos
     * @param lblSaldo
     */
    public static void generarPDFCartolaClientePorVentas(
            JTable tabla, String nombreCliente, String rutCliente,
            JDateChooser jDateChooserInicial, JDateChooser jDateChooserFinal,
            JLabel lblTotalFac, JLabel lblTotalAbonos, JLabel lblSaldo) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte de cartola.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date fechaInicio = (jDateChooserInicial != null) ? jDateChooserInicial.getDate() : null;
        java.util.Date fechaFin = (jDateChooserFinal != null) ? jDateChooserFinal.getDate() : null;

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nombreArchivo = "Cartola_Ventas_" + (rutCliente != null ? rutCliente.replace(".", "").replace("-", "") : "Cliente") + "_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 9, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("CARTOLA DE CLIENTE POR VENTAS", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            String strInicio = (fechaInicio != null) ? sdf.format(fechaInicio) : "Inicio";
            String strFin = (fechaFin != null) ? sdf.format(fechaFin) : "Actualidad";

            String infoCliente = "Cliente: " + (nombreCliente != null ? nombreCliente : "N/A") + " (RUT: " + (rutCliente != null ? rutCliente : "N/A") + ")";
            String rangoFechas = "Período: " + strInicio + " al " + strFin;

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph(infoCliente + "\n" + rangoFechas + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla de Datos
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153)); // Azul corporativo
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha las columnas de montos/saldos (columnas 3, 4 y 5)
                    if (j >= 3) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al final
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph(
                    "\nTotal Facturado: " + (lblTotalFac != null ? lblTotalFac.getText() : "$0")
                    + "   |   Total Abonos: " + (lblTotalAbonos != null ? lblTotalAbonos.getText() : "$0")
                    + "   |   SALDO DEUDA: " + (lblSaldo != null ? lblSaldo.getText() : "$0"), fuenteTotal
            );
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir el PDF automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (com.itextpdf.text.DocumentException | java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF de la cartola: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el reporte de Cuentas por Cobrar con totales.
     *
     * @param tabla
     * @param tituloReporte
     * @param sectorZona
     * @param lblTotalCli
     * @param lblTotalCobrar
     */
    public static void generarPDFCuentasPorCobrar(
            javax.swing.JTable tabla, String tituloReporte, String sectorZona,
            javax.swing.JLabel lblTotalCli, javax.swing.JLabel lblTotalCobrar) {

        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte de cuentas por cobrar.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String nombreArchivo = "Cuentas_Por_Cobrar_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos de Fuentes
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph pTitulo = new com.itextpdf.text.Paragraph(tituloReporte, fuenteTitulo);
            pTitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(pTitulo);

            String infoFiltro = "Sector / Zona: " + (sectorZona != null ? sectorZona : "TODOS");
            com.itextpdf.text.Paragraph pSub = new com.itextpdf.text.Paragraph(infoFiltro + "\nFecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            pSub.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(pSub);

            // Tabla PDF
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Cabeceras de la tabla
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153)); // Azul corporativo
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas de la tabla
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    // Alinear a la derecha la última columna (montos)
                    if (j == tabla.getColumnCount() - 1) {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                    }
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Totales al pie del PDF
            String textoTotales = "\nTotal Clientes con Deuda: " + (lblTotalCli != null ? lblTotalCli.getText() : "0")
                    + "   |   TOTAL A COBRAR: " + (lblTotalCobrar != null ? lblTotalCobrar.getText() : "$0");

            com.itextpdf.text.Paragraph pTotal = new com.itextpdf.text.Paragraph(textoTotales, fuenteTotal);
            pTotal.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(pTotal);

            documento.close();

            // Abrir el PDF generado automáticamente
            java.io.File archivoDestino = new java.io.File(nombreArchivo);
            if (archivoDestino.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivoDestino);
            }

        } catch (com.itextpdf.text.DocumentException | java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF de cuentas por cobrar: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera e imprime en PDF el Reporte del Foliador de Hojas.
     * @param tabla
     * @param lblTotalRango
     */
    public static void generarPDFFoliadorHojas(JTable tabla, JLabel lblTotalRango) {
        if (tabla.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "No hay datos en la tabla para generar el reporte.", "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 36);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String nombreArchivo = "Foliador_Hojas_" + System.currentTimeMillis() + ".pdf";

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();

            // Estilos
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16, com.itextpdf.text.BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteCelda = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Font fuenteTotal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.BLACK);

            // Encabezado
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("CONTROL Y REGISTRO DE FOLIADOR DE HOJAS", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph("Fecha de Emisión: " + sdf.format(new java.util.Date()) + "\n\n", fuenteSubtitulo);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(subtitulo);

            // Tabla PDF
            com.itextpdf.text.pdf.PdfPTable pdfTabla = new com.itextpdf.text.pdf.PdfPTable(tabla.getColumnCount());
            pdfTabla.setWidthPercentage(100);

            // Encabezados
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tabla.getColumnName(i), fuenteHeader));
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(51, 102, 153)); // Azul corporativo
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTabla.addCell(cell);
            }

            // Filas
            for (int i = 0; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    Object val = tabla.getValueAt(i, j);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", fuenteCelda));
                    cell.setPadding(5);
                    pdfTabla.addCell(cell);
                }
            }

            documento.add(pdfTabla);

            // Total
            String totalText = (lblTotalRango != null) ? lblTotalRango.getText() : String.valueOf(tabla.getRowCount());
            com.itextpdf.text.Paragraph totales = new com.itextpdf.text.Paragraph("\nTotal Rangos Registrados: " + totalText, fuenteTotal);
            totales.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(totales);

            documento.close();

            // Abrir automáticamente
            java.io.File file = new java.io.File(nombreArchivo);
            if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

        } catch (com.itextpdf.text.DocumentException | java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
