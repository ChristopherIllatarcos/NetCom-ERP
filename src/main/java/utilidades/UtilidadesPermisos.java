package utilidades;

import java.util.ArrayList;
import java.util.List;
import modelo.AccesoFuncion;

public class UtilidadesPermisos {

    // Lista maestra de funciones del sistema Casa Victoria
    public static List<AccesoFuncion> obtenerFuncionesBase(String tipoAsignacion, String referencia) {
        List<AccesoFuncion> lista = new ArrayList<>();

        // Módulo Archivos
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Archivos", "MNU_MANT_USUARIOS", "Mantenedor de Usuarios", false));
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Archivos", "MNU_ACCESOS_FUNC", "Accesos y Funciones", false));
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Archivos", "MNU_EMPRESA", "Datos de la Empresa", false));

        // Módulo Inventario / Bodega
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Bodega", "MNU_PRODUCTOS", "Mantenedor de Productos", false));
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Bodega", "MNU_BODEGAS", "Gestión de Bodegas", false));
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Bodega", "MNU_MOV_STOCK", "Movimientos de Stock", false));

        // Módulo Ventas
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Ventas", "MNU_PUNTO_VENTA", "Punto de Venta (POS)", false));
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Ventas", "MNU_CLIENTES", "Mantenedor de Clientes", false));

        // Módulo Compras
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Compras", "MNU_PROVEEDORES", "Mantenedor de Proveedores", false));
        lista.add(new AccesoFuncion(0, tipoAsignacion, referencia, "Compras", "MNU_ORDEN_COMPRA", "Órdenes de Compra", false));

        return lista;
    }
}
