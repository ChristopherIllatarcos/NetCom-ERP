package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;

public class ClienteDAO {

    // 1. BUSCAR POR RUT
    public Cliente obtenerClientePorRut(String rut) {
        String sql = "SELECT id, rut, nombre, direccion, comuna, ciudad, telefono, email, activo, "
                + "credito_autorizado, dias_credito, giro, vendedor_id, lista_precio_id "
                + "FROM clientes WHERE rut = ?";
        Cliente cliente = null;

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rut.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente = MapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por RUT: " + e.getMessage());
        }

        return cliente;
    }

    // 2. BUSCAR POR NOMBRE
    public Cliente obtenerClientePorNombre(String nombre) {
        String sql = "SELECT id, rut, nombre, direccion, comuna, ciudad, telefono, email, activo, "
                + "credito_autorizado, dias_credito, giro, vendedor_id, lista_precio_id "
                + "FROM clientes WHERE UPPER(nombre) LIKE UPPER(?) LIMIT 1";
        Cliente cliente = null;

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente = MapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por Nombre: " + e.getMessage());
        }

        return cliente;
    }

    // 3. GUARDAR (INSERTAR NUEVO CLIENTE)
    public boolean guardar(Cliente cliente) {
        String sql = "INSERT INTO clientes (rut, nombre, direccion, comuna, ciudad, telefono, email, activo, "
                + "credito_autorizado, dias_credito, giro, vendedor_id, lista_precio_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getRut());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getComuna());
            ps.setString(5, cliente.getCiudad());
            ps.setString(6, cliente.getTelefono());
            ps.setString(7, cliente.getEmail());
            ps.setBoolean(8, cliente.isActivo());
            ps.setDouble(9, cliente.getCupoLimiteCredito());
            ps.setInt(10, cliente.getDiasCredito());
            ps.setString(11, cliente.getGiroRubro());

            if (cliente.getVendedorId() > 0) {
                ps.setInt(12, cliente.getVendedorId());
            } else {
                ps.setNull(12, java.sql.Types.INTEGER);
            }
            if (cliente.getListaPrecioId() > 0) {
                ps.setInt(13, cliente.getListaPrecioId());
            } else {
                ps.setNull(13, java.sql.Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar cliente: " + e.getMessage());
            return false;
        }
    }

    // 4. ACTUALIZAR (EDITAR CLIENTE EXISTENTE)
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET rut = ?, nombre = ?, direccion = ?, comuna = ?, ciudad = ?, "
                + "telefono = ?, email = ?, activo = ?, credito_autorizado = ?, dias_credito = ?, giro = ?, "
                + "vendedor_id = ?, lista_precio_id = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getRut());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getComuna());
            ps.setString(5, cliente.getCiudad());
            ps.setString(6, cliente.getTelefono());
            ps.setString(7, cliente.getEmail());
            ps.setBoolean(8, cliente.isActivo());
            ps.setDouble(9, cliente.getCupoLimiteCredito());
            ps.setInt(10, cliente.getDiasCredito());
            ps.setString(11, cliente.getGiroRubro());

            if (cliente.getVendedorId() > 0) {
                ps.setInt(12, cliente.getVendedorId());
            } else {
                ps.setNull(12, java.sql.Types.INTEGER);
            }
            if (cliente.getListaPrecioId() > 0) {
                ps.setInt(13, cliente.getListaPrecioId());
            } else {
                ps.setNull(13, java.sql.Types.INTEGER);
            }

            ps.setInt(14, cliente.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    // 5. ELIMINAR CLIENTE POR ID
    public boolean eliminar(int id) {
        String sql = "UPDATE clientes SET activo = false WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar cliente (Borrado Lógico): " + e.getMessage());
            return false;
        }
    }

    // 6. OBTENER VENDEDORES PARA EL COMBO
    public List<Object[]> obtenerVendedoresCombo() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM vendedores WHERE activo = true ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{rs.getInt("id"), rs.getString("nombre")});
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar vendedores: " + e.getMessage());
        }
        return lista;
    }

    // 7. OBTENER LISTAS DE PRECIOS PARA EL COMBO
    public List<Object[]> obtenerListasPreciosCombo() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM listas_precios WHERE activo = true ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Object[]{rs.getInt("id"), rs.getString("nombre")});
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar listas de precios: " + e.getMessage());
        }
        return lista;
    }

    // HELPER PARA MAPEAR RESULTSET
    private Cliente MapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setRut(rs.getString("rut"));
        cliente.setRazonSocial(rs.getString("nombre"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setComuna(rs.getString("comuna"));
        cliente.setCiudad(rs.getString("ciudad"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        cliente.setActivo(rs.getBoolean("activo"));
        cliente.setCupoLimiteCredito(rs.getDouble("credito_autorizado"));
        cliente.setDiasCredito(rs.getInt("dias_credito"));
        cliente.setGiroRubro(rs.getString("giro"));
        cliente.setVendedorId(rs.getInt("vendedor_id"));
        cliente.setListaPrecioId(rs.getInt("lista_precio_id"));
        return cliente;
    }
}
