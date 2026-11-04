package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ItemCombo;
import modelo.Producto;

public class ProductosDAO {

    public int obtenerIdPorCodigo(String codigo) {
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

        return -1; // Retornamos -1 como código centinela de "No encontrado"
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
    public List<ItemCombo> listarFamiliasCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        lista.add(new ItemCombo(0, "-- SELECCIONE FAMILIA --"));

        String sql = "SELECT id, nombre FROM familias ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar familias para combo: " + e.getMessage());
        }
        return lista;
    }

    // 2. Cargar Impuestos Adicionales
    public void cargarImpuestos(javax.swing.JComboBox<ItemCombo> cmb) {
        cmb.removeAllItems();
        cmb.addItem(new ItemCombo(0, "-- SIN IMPUESTO ADICIONAL --"));
    }

    // 3. Cargar Ubicaciones en Bodega
    public List<ItemCombo> listarUbicacionesCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        // Opción por defecto
        lista.add(new ItemCombo(0, "-- BODEGA PRINCIPAL --"));

        String sql = "SELECT id, nombre FROM ubicaciones ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ubicaciones para combo: " + e.getMessage());
        }
        return lista;
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

            ps.setString(1, p.getCodigoInterno());
            ps.setString(2, p.getCodigoBarra());
            ps.setString(3, p.getDescripcion());     // Usamos descripcion como nombre
            ps.setString(4, p.getDescripcion());     // Usamos descripcion como descripcion detallada
            ps.setInt(5, p.getCategoriaId());        // familia_id (cmbFamiliaCategoria)
            ps.setDouble(6, p.getCostoNeto());       // precio_compra
            ps.setDouble(7, p.getPrecioUnitario());  // precio_venta
            ps.setDouble(8, p.getStockActual());     // stock_actual
            ps.setDouble(9, p.getStockMinimo());     // stock_minimo
            ps.setDouble(10, 0.0);                   // stock_maximo por defecto (o puedes agregarlo a tu clase si lo necesitas)
            ps.setString(11, "UN");                  // unidad_medida por defecto
            ps.setInt(12, p.getUbicacionBodegaId()); // ID de la bodega desde cmbUbicacionBodega
            ps.setBoolean(13, p.isActivo());         // activo

            // Blindaje: Validamos que realmente se haya insertado al menos una fila
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            // Blindaje de excepciones: Detectar clave duplicada en PostgreSQL (SQLState 23505)
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error de BD: El código interno o código de barra ya se encuentra registrado.");
            } else {
                System.err.println("Error crítico al guardar producto: " + e.getMessage());
            }
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

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0; // Retorna true solo si realmente actualizó el registro

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error de BD: El código o código de barra ya pertenece a otro producto.");
            } else {
                System.err.println("Error al actualizar producto: " + e.getMessage());
            }
            return false;
        }
    }

    // 8. Eliminar Producto por ID
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0; // Retorna true solo si el producto realmente existía y fue borrado

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                System.err.println("Error de integridad: No se puede eliminar el producto porque tiene registros asociados (ventas/compras).");
            } else {
                System.err.println("Error al eliminar producto: " + e.getMessage());
            }
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
