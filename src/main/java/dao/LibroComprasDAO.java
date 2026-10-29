package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JComboBox;
import modelo.LibroCompra;

public class LibroComprasDAO {

    // Clase auxiliar para manejar el ComboBox con ID y Nombre (Item del Combo)
    public static class ItemCombo {

        private final int id;
        private final String descripcion;

        public ItemCombo(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return descripcion; // Esto es lo que se muestra en el JComboBox
        }
    }

    // 1. Cargar proveedores en el ComboBox
    public void cargarProveedores(JComboBox<ItemCombo> cmbProveedor) {
        cmbProveedor.removeAllItems();
        cmbProveedor.addItem(new ItemCombo(0, "-- TODOS LOS PROVEEDORES --"));

        String sql = "SELECT id, nombre FROM proveedores ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                cmbProveedor.addItem(new ItemCombo(id, nombre));
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar proveedores en LibroComprasDAO: " + e.getMessage());
        }
    }

    // 2. Consultar el libro de compras aplicando filtros y devolviendo los datos para la tabla y totales
    public List<LibroCompra> consultarLibroCompras(Date fechaInicio, Date fechaFinal, int proveedorId,
            javax.swing.JLabel lblDoctos, javax.swing.JLabel lblNeto, javax.swing.JLabel lblTotal) {

        List<LibroCompra> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.fecha_emision, c.numero_factura, p.nombre AS proveedor_social, "
                + "c.subtotal, c.iva, c.total "
                + "FROM compras c "
                + "LEFT JOIN proveedores p ON c.proveedor_id = p.id "
                + "WHERE 1=1"
        );

        if (fechaInicio != null) {
            sql.append(" AND c.fecha_emision >= ?");
        }
        if (fechaFinal != null) {
            sql.append(" AND c.fecha_emision <= ?");
        }
        if (proveedorId > 0) {
            sql.append(" AND c.proveedor_id = ?");
        }

        sql.append(" ORDER BY c.fecha_emision ASC, c.id ASC");

        int totalDoctos = 0;
        double sumaNeto = 0.0;
        double sumaTotal = 0.0;

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (fechaInicio != null) {
                ps.setDate(index++, new java.sql.Date(fechaInicio.getTime()));
            }
            if (fechaFinal != null) {
                ps.setDate(index++, new java.sql.Date(fechaFinal.getTime()));
            }
            if (proveedorId > 0) {
                ps.setInt(index++, proveedorId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fecha = rs.getDate("fecha_emision");
                    String nFactura = rs.getString("numero_factura");
                    String proveedor = rs.getString("proveedor_social");
                    double neto = rs.getDouble("subtotal");
                    double iva = rs.getDouble("iva");
                    double total = rs.getDouble("total");

                    lista.add(new LibroCompra(fecha, nFactura, proveedor != null ? proveedor : "SIN PROVEEDOR", neto, iva, total));

                    totalDoctos++;
                    sumaNeto += neto;
                    sumaTotal += total;
                }
            }

            // Actualizar las etiquetas de resumen automáticamente
            java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols();
            simbolos.setGroupingSeparator('.');
            java.text.DecimalFormat formatoMoneda = new java.text.DecimalFormat("$ #,##0", simbolos);

            lblDoctos.setText(String.valueOf(totalDoctos));
            lblNeto.setText(formatoMoneda.format(sumaNeto));
            lblTotal.setText(formatoMoneda.format(sumaTotal));

        } catch (SQLException e) {
            System.err.println("Error al consultar libro de compras: " + e.getMessage());
        }

        return lista;
    }
}
