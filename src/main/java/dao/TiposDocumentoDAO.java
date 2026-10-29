package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ItemCombo; 

public class TiposDocumentoDAO {

    public List<ItemCombo> listarTiposDocumentoCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM tipos_documento WHERE activo = TRUE ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de documento: " + e.getMessage());
        }
        return lista;
    }
}
