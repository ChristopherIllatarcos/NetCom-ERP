package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.ItemCombo; 
import modelo.Producto;

public class ProductosDAO {

    public int obtenerIdPorCodigo(String codigo) {
        // CORREGIDO: Usamos únicamente la columna real 'codigo' y el 'id'
        String sql = "SELECT id FROM productos WHERE codigo = ? OR id::text = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ID del producto: " + e.getMessage());
        }
        return 1; // ID por defecto en caso de no hallarlo
    }

    public Producto obtenerProductoPorCodigo(String codigo) {
        // Usamos TRIM para evitar problemas si hay espacios de más al buscar
        String sql = "SELECT * FROM productos WHERE TRIM(codigo) = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setId(rs.getInt("id"));
                    p.setCodigoInterno(rs.getString("codigo"));
                    p.setCodigoBarra(rs.getString("codigo"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setCategoriaId(rs.getInt("familia_id"));
                    p.setActivo(rs.getBoolean("activo"));
                    p.setCostoNeto(rs.getDouble("precio_compra"));
                    p.setPrecioUnitario(rs.getDouble("precio_venta"));
                    p.setStockActual(rs.getDouble("stock_actual"));
                    p.setStockMinimo(rs.getDouble("stock_minimo"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por código: " + e.getMessage());
        }
        return null;
    }

    // 1. Cargar Familias (reemplaza a categorías)
    public void cargarCategorias(javax.swing.JComboBox<ItemCombo> cmb) {
        cmb.removeAllItems();
        cmb.addItem(new ItemCombo(0, "-- SELECCIONE FAMILIA --"));
        String sql = "SELECT id, nombre FROM familias ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cmb.addItem(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar familias: " + e.getMessage());
        }
    }

    // 2. Cargar Impuestos Adicionales
    public void cargarImpuestos(javax.swing.JComboBox<ItemCombo> cmb) {
        cmb.removeAllItems();
        cmb.addItem(new ItemCombo(0, "-- SIN IMPUESTO ADICIONAL --"));
    }

    // 3. Cargar Ubicaciones en Bodega
    public void cargarUbicaciones(javax.swing.JComboBox<ItemCombo> cmb) {
        cmb.removeAllItems();
        cmb.addItem(new ItemCombo(0, "-- BODEGA PRINCIPAL --"));
    }

    // 4. Contar Total de Productos Registrados
    public int contarProductos() {
        String sql = "SELECT COUNT(*) FROM productos";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar productos: " + e.getMessage());
        }
        return 0;
    }

    // 5. Guardar Producto
    public boolean guardar(Producto p) {
        String sql = "INSERT INTO productos (codigo, codigo_barra, nombre, descripcion, familia_id, precio_compra, precio_venta, "
                + "stock_actual, stock_minimo, stock_maximo, unidad_medida, ubicacion, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigoInterno());  // 1. codigo (interno)
            ps.setString(2, p.getCodigoBarra());    // 2. codigo_barra independiente
            ps.setString(3, p.getDescripcion());    // 3. nombre
            ps.setString(4, p.getDescripcion());    // 4. descripcion
            ps.setInt(5, p.getCategoriaId());       // 5. familia_id
            ps.setDouble(6, p.getCostoNeto());      // 6. precio_compra (NUMERIC)
            ps.setDouble(7, p.getPrecioUnitario()); // 7. precio_venta (NUMERIC)
            ps.setDouble(8, p.getStockActual());    // 8. stock_actual
            ps.setDouble(9, p.getStockMinimo());    // 9. stock_minimo
            ps.setDouble(10, 0.0);                  // 10. stock_maximo por defecto
            ps.setString(11, "UN");                 // 11. unidad_medida por defecto
            ps.setString(12, "Bodega Principal");     // 12. ubicacion por defecto
            ps.setBoolean(13, p.isActivo());        // 13. activo (BOOL)

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            return false;
        }
    }

    // 6. Buscar Producto para el módulo de Bodegas (por código interno o de barra)
    public Producto buscarProductoBodega(String codigo) {
        Producto p = null;
        String sql = "SELECT * FROM productos WHERE TRIM(codigo) = ? OR TRIM(codigo_barra) = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            String codigoTrim = codigo.trim();
            ps.setString(1, codigoTrim);
            ps.setString(2, codigoTrim);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Producto();
                    p.setId(rs.getInt("id"));

                    p.setCodigoInterno(rs.getString("codigo"));
                    p.setCodigoBarra(rs.getString("codigo_barra"));

                    p.setDescripcion(rs.getString("descripcion"));
                    p.setCategoriaId(rs.getInt("familia_id"));
                    p.setActivo(rs.getBoolean("activo"));
                    p.setCostoNeto(rs.getDouble("precio_compra"));
                    p.setMargenGanancia(0.0);
                    p.setPrecioUnitario(rs.getDouble("precio_venta"));
                    p.setImpuestoAdicionalId(0);
                    p.setStockActual(rs.getDouble("stock_actual"));
                    p.setStockMinimo(rs.getDouble("stock_minimo"));
                    p.setUbicacionBodegaId(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto de bodega: " + e.getMessage());
        }

        return p;
    }

    // 7. Actualizar / Editar Producto
    public boolean actualizar(Producto p) {
        String sql = "UPDATE productos SET codigo = ?, codigo_barra = ?, descripcion = ?, familia_id = ?, precio_compra = ?, precio_venta = ?, stock_actual = ?, stock_minimo = ?, activo = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigoInterno());
            ps.setString(2, p.getCodigoBarra());
            ps.setString(3, p.getDescripcion());
            ps.setInt(4, p.getCategoriaId());
            ps.setDouble(5, p.getCostoNeto());
            ps.setDouble(6, p.getPrecioUnitario());
            ps.setDouble(7, p.getStockActual());
            ps.setDouble(8, p.getStockMinimo());
            ps.setBoolean(9, p.isActivo());
            ps.setInt(10, p.getId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // 8. Eliminar Producto por ID
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    public String[] buscarDatosPorCodigo(String codigo) {
        String sql = "SELECT nombre, descripcion FROM productos WHERE codigo = ? AND activo = true";
        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String descripcion = rs.getString("descripcion");
                    return new String[]{nombre, descripcion};
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al buscar producto en DAO: " + e.getMessage());
        }
        return null;
    }
}
