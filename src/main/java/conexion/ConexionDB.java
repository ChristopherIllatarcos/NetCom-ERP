package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class ConexionDB {

    private static final String URL = "jdbc:postgresql://localhost:5432/gorisoft";
    private static final String USER = "postgres";
    private static final String PASSWORD = "nA4pYRSr2ziagy"; 

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL no encontrado");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método de prueba ejecutable
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null) {
                String sql = "SELECT rut_licenciatario, razon_social, estado FROM licencia LIMIT 1;";
                try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                    if (rs.next()) {
                        String rut = rs.getString("rut_licenciatario");
                        String empresa = rs.getString("razon_social");
                        String estado = rs.getString("estado");

                        JOptionPane.showMessageDialog(null,
                                "¡CONEXIÓN EXITOSA A GORISOFT!\n\n"
                                + "Empresa: " + empresa + "\n"
                                + "RUT: " + rut + "\n"
                                + "Estado Licencia: " + estado,
                                "Prueba de Base de Datos",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error de conexión con PostgreSQL:\n" + e.getMessage(),
                    "Error Conexión",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
