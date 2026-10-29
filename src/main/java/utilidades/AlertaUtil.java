package utilidades;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlertaUtil {

    public static void mostrar(String titulo, String mensaje, String tipo) {
        // Determinamos el color y el GIF según el tipo de alerta
        Color colorBorde = new Color(220, 53, 69); // Por defecto rojo (error)
        String rutaGif = "";

        if (tipo.equalsIgnoreCase("exito")) {
            colorBorde = new Color(40, 167, 69); // Verde éxito
            rutaGif = "/iconos/exito.gif";
        } else if (tipo.equalsIgnoreCase("advertencia")) {
            colorBorde = new Color(212, 160, 23); // Amarillo mostaza corporativo
            rutaGif = "/iconos/advertencia.gif";
        } else {
            rutaGif = "/iconos/error.gif"; // Por si usas error en el futuro
        }

        JDialog dialogo = new JDialog((java.awt.Frame) null, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 180);
        dialogo.setLocationRelativeTo(null); // Centrado en pantalla

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        try {
            java.net.URL urlGif = AlertaUtil.class.getResource(rutaGif);
            if (urlGif != null) {
                ImageIcon iconGif = new ImageIcon(urlGif);
                JLabel lblGif = new JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            } else {
                System.err.println("ADVERTENCIA: No se encontró el archivo " + rutaGif);
            }
        } catch (Exception e) {
        }

        // Título (Desplazado a la derecha para dar espacio al GIF)
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 22, 340, 25);
        panel.add(lblTitulo);

        // Mensaje (soporta HTML para saltos de línea automáticos, desplazado a la derecha)
        JLabel lblMensaje = new JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(60, 60, 60));
        lblMensaje.setBounds(75, 50, 340, 45);
        panel.add(lblMensaje);

        // Botón Aceptar (Mantenemos un gris oscuro elegante o puedes dejar el color del borde si prefieres)
        JButton btnAceptar = new JButton("ACEPTAR");
        btnAceptar.setBounds(155, 120, 130, 35);
        btnAceptar.setBackground(new Color(50, 50, 50));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> dialogo.dispose());

        // Esto hace que funcione la tecla Enter automáticamente
        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnAceptar);

        // Listener para asegurar el foco en el botón al abrir
        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnAceptar.requestFocusInWindow();
                });
            }
        });

        dialogo.add(panel);
        dialogo.setVisible(true);
    }

    public static JDialog mostrarCarga(String mensaje) {
        JDialog dialogoCarga = new JDialog((java.awt.Frame) null, "Procesando", true);
        dialogoCarga.setUndecorated(true);
        dialogoCarga.setSize(360, 130); // Un poco más ancho para que quepa bien el gif de 100x100
        dialogoCarga.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(51, 153, 255), 2));
        panel.setLayout(null);

        // 🖼️ CARGAR EL GIF 'cargando2.gif' (Ajustado para sus 100x100)
        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/cargando1.gif");
            if (urlGif != null) {
                javax.swing.ImageIcon iconOriginal = new javax.swing.ImageIcon(urlGif);

                java.awt.Image imagenEscalada = iconOriginal.getImage().getScaledInstance(70, 70, java.awt.Image.SCALE_DEFAULT);
                javax.swing.ImageIcon iconEscalado = new javax.swing.ImageIcon(imagenEscalada);

                JLabel lblGif = new JLabel(iconEscalado);
                lblGif.setBounds(20, 25, 80, 80); // Espacio reservado para el gif
                panel.add(lblGif);
            } else {
                System.out.println("⚠️ No se encontró la ruta /iconos/cargando1.gif");
            }
        } catch (Exception e) {
            System.out.println("Error al cargar el GIF de carga: " + e.getMessage());
        }

        // Texto de carga (desplazado a la derecha para no chocar con el gif)
        JLabel lblMensaje = new JLabel("<html><b>" + mensaje + "</b></html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(80, 80, 80));
        lblMensaje.setBounds(115, 35, 230, 50);
        panel.add(lblMensaje);

        dialogoCarga.add(panel);
        return dialogoCarga;
    }

    // Método con parent (para usar el 'this' desde tus formularios)
    public static boolean mostrarConfirmacion(java.awt.Component parent, String mensaje) {
        return mostrarConfirmacion(parent, "Confirmación", mensaje);
    }

    // Método central de confirmación
    public static boolean mostrarConfirmacion(java.awt.Component parent, String titulo, String mensaje) {
        final boolean[] respuesta = {false}; // Arreglo para almacenar la respuesta dentro del lambda

        JDialog dialogo = new JDialog((java.awt.Frame) null, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 180);
        dialogo.setLocationRelativeTo(parent); // ¡Aquí se usa el parent (this) perfectamente!

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Color colorBorde = new Color(51, 153, 255); // Azul corporativo para confirmaciones
        panel.setBorder(BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/pregunta.gif");
            if (urlGif != null) {
                ImageIcon iconGif = new ImageIcon(urlGif);
                JLabel lblGif = new JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            }
        } catch (Exception e) {
        }

        // Título (Desplazado a la derecha para dar espacio al icono)
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 22, 340, 25);
        panel.add(lblTitulo);

        // Mensaje (con HTML para saltos de línea automáticos, desplazado a la derecha)
        JLabel lblMensaje = new JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(60, 60, 60));
        lblMensaje.setBounds(75, 50, 340, 45);
        panel.add(lblMensaje);

        // Botón Sí / Aceptar
        JButton btnSi = new JButton("SÍ");
        btnSi.setBounds(100, 120, 100, 35);
        btnSi.setBackground(colorBorde);
        btnSi.setForeground(Color.WHITE);
        btnSi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSi.setFocusPainted(false);
        btnSi.setBorder(null);
        btnSi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSi.addActionListener(e -> {
            respuesta[0] = true;
            dialogo.dispose();
        });

        // Botón No / Cancelar
        JButton btnNo = new JButton("NO");
        btnNo.setBounds(220, 120, 100, 35);
        btnNo.setBackground(new Color(108, 117, 125));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNo.setFocusPainted(false);
        btnNo.setBorder(null);
        btnNo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNo.addActionListener(e -> {
            respuesta[0] = false;
            dialogo.dispose();
        });

        dialogo.getRootPane().setDefaultButton(btnSi);
        panel.add(btnSi);
        panel.add(btnNo);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnSi.requestFocusInWindow();
                });
            }
        });

        dialogo.add(panel);
        dialogo.setVisible(true);

        return respuesta[0];
    }

    // Método para confirmaciones personalizadas (Reemplaza al JOptionPane clásico)
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        final boolean[] respuesta = {false};

        JDialog dialogo = new JDialog((java.awt.Frame) null, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 180);
        dialogo.setLocationRelativeTo(null); // Centrado en pantalla (sin usar 'this')

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Color colorBorde = new Color(51, 153, 255); // Azul corporativo para confirmaciones
        panel.setBorder(BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/pregunta.gif");
            if (urlGif != null) {
                ImageIcon iconGif = new ImageIcon(urlGif);
                JLabel lblGif = new JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            }
        } catch (Exception e) {
        }

        // Título (Desplazado a la derecha para dar espacio al icono)
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 22, 340, 25);
        panel.add(lblTitulo);

        // Mensaje (con HTML para saltos de línea automáticos, desplazado a la derecha)
        JLabel lblMensaje = new JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(60, 60, 60));
        lblMensaje.setBounds(75, 50, 340, 45);
        panel.add(lblMensaje);

        // Botón Sí / Aceptar
        JButton btnSi = new JButton("SÍ");
        btnSi.setBounds(100, 120, 100, 35);
        btnSi.setBackground(colorBorde);
        btnSi.setForeground(Color.WHITE);
        btnSi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSi.setFocusPainted(false);
        btnSi.setBorder(null);
        btnSi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSi.addActionListener(e -> {
            respuesta[0] = true;
            dialogo.dispose();
        });

        // Botón No / Cancelar
        JButton btnNo = new JButton("NO");
        btnNo.setBounds(220, 120, 100, 35);
        btnNo.setBackground(new Color(108, 117, 125));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNo.setFocusPainted(false);
        btnNo.setBorder(null);
        btnNo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNo.addActionListener(e -> {
            respuesta[0] = false;
            dialogo.dispose();
        });

        dialogo.getRootPane().setDefaultButton(btnSi);

        panel.add(btnSi);
        panel.add(btnNo);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnSi.requestFocusInWindow();
                });
            }
        });

        dialogo.add(panel);
        dialogo.setVisible(true);

        return respuesta[0];
    }

    // Método específico para avisos de Guardado / Éxito
    public static void mostrarExito(String titulo, String mensaje) {
        mostrar(titulo, mensaje, "exito");
    }

    // Método específico para Advertencias
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        mostrar(titulo, mensaje, "advertencia");
    }

    // Método específico para Errores
    public static void mostrarError(String titulo, String mensaje) {
        mostrar(titulo, mensaje, "error");
    }

    // Método especializado para la confirmación de cierre de sesión con datos dinámicos
    public static boolean mostrarConfirmacionCierreSesion() {
        final boolean[] respuesta = {false};

        JDialog dialogo = new JDialog((java.awt.Frame) null, "CONFIRMAR CIERRE DE SESIÓN", true);
        dialogo.setUndecorated(true);
        dialogo.setSize(460, 230);
        dialogo.setLocationRelativeTo(null); // Centrado en pantalla

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Color colorBorde = new Color(220, 53, 69); // Rojo corporativo de alerta
        panel.setBorder(BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        // Título
        JLabel lblTitulo = new JLabel("CONFIRMAR CIERRE DE SESIÓN");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(25, 20, 410, 25);
        panel.add(lblTitulo);

        // Mensaje principal
        JLabel lblMensaje = new JLabel("¿Está seguro que desea salir del sistema?");
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMensaje.setForeground(new Color(60, 60, 60));
        lblMensaje.setBounds(25, 55, 410, 20);
        panel.add(lblMensaje);

        // --- DATOS DINÁMICOS DE SESIÓN ---
        JLabel lblUsrLabel = new JLabel("Usuario actual:");
        lblUsrLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsrLabel.setForeground(new Color(100, 100, 100));
        lblUsrLabel.setBounds(25, 85, 120, 20);
        panel.add(lblUsrLabel);

        // Aquí se inyecta el nombre real del usuario logueado
        JLabel lblUsrValor = new JLabel(utilidades.SesionUsuario.nombreUsuarioActual);
        lblUsrValor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsrValor.setForeground(new Color(40, 40, 40));
        lblUsrValor.setBounds(150, 85, 285, 20);
        panel.add(lblUsrValor);

        JLabel lblSesionLabel = new JLabel("Sesión iniciada:");
        lblSesionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSesionLabel.setForeground(new Color(100, 100, 100));
        lblSesionLabel.setBounds(25, 110, 120, 20);
        panel.add(lblSesionLabel);

        // Formateamos y mostramos la hora exacta en la que hizo login
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String horaFormateada = (utilidades.SesionUsuario.horaInicioSesion != null)
                ? sdf.format(utilidades.SesionUsuario.horaInicioSesion)
                : sdf.format(new java.util.Date());

        JLabel lblSesionValor = new JLabel(horaFormateada);
        lblSesionValor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSesionValor.setForeground(new Color(40, 40, 40));
        lblSesionValor.setBounds(150, 110, 285, 20);
        panel.add(lblSesionValor);
        // ---------------------------------

        // Botón CANCELAR
        JButton btnCancelar = new JButton("CANCELAR");
        btnCancelar.setBounds(110, 160, 130, 35);
        btnCancelar.setBackground(new Color(230, 230, 230));
        btnCancelar.setForeground(new Color(80, 80, 80));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(null);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> {
            respuesta[0] = false;
            dialogo.dispose();
        });

        // Botón CERRAR SESIÓN
        JButton btnCerrar = new JButton("CERRAR SESIÓN");
        btnCerrar.setBounds(255, 160, 150, 35);
        btnCerrar.setBackground(colorBorde);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(null);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Acción con retardo de 2 segundos y efecto de carga
        btnCerrar.addActionListener(e -> {
            respuesta[0] = true;

            // Deshabilitamos los botones para evitar doble clic
            btnCerrar.setEnabled(false);
            btnCancelar.setEnabled(false);

            // Cambiamos el texto del botón o del mensaje para indicar que está cargando
            btnCerrar.setText("CERRANDO...");
            lblMensaje.setText("Cerrando sesión, espere un momento...");
            lblMensaje.setForeground(colorBorde);

            // Creamos un Timer de 3000 milisegundos (3 segundos)
            javax.swing.Timer timer = new javax.swing.Timer(3000, evtTimer -> {
                dialogo.dispose(); // Cierra el diálogo tras los 2 segundos
            });
            timer.setRepeats(false); // Se ejecuta solo una vez
            timer.start();
        });

        dialogo.getRootPane().setDefaultButton(btnCancelar);
        panel.add(btnCancelar);
        panel.add(btnCerrar);

        dialogo.add(panel);
        dialogo.setVisible(true);

        return respuesta[0];
    }

    // Alerta de advertencia con botón bien contrastado
    public static void mostrarAdvertencia(java.awt.Component parent, String mensaje) {
        javax.swing.JDialog dialogo = new javax.swing.JDialog((java.awt.Frame) null, "Atención", true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 180);
        dialogo.setLocationRelativeTo(parent);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        java.awt.Color colorBorde = new java.awt.Color(212, 160, 23); // Amarillo mostaza corporativo
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        // 🎬 Carga segura del GIF animado
        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/advertencia.gif");
            if (urlGif != null) {
                javax.swing.ImageIcon iconGif = new javax.swing.ImageIcon(urlGif);
                javax.swing.JLabel lblGif = new javax.swing.JLabel(iconGif);
                // Si tu GIF original es muy grande, asegúrate de que mida aprox 32x32 o 40x40 píxeles desde el archivo para que no se desborde
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            } else {
                System.err.println("⚠️ ADVERTENCIA: No se encontró el archivo /iconos/advertencia.gif");
            }
        } catch (Exception e) {
        }

        // Título
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("ATENCIÓN");
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 22, 340, 25);
        panel.add(lblTitulo);

        // Mensaje con HTML
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblMensaje.setForeground(new java.awt.Color(60, 60, 60));
        lblMensaje.setBounds(75, 50, 340, 45);
        panel.add(lblMensaje);

        // Botón Aceptar
        javax.swing.JButton btnAceptar = new javax.swing.JButton("ACEPTAR");
        btnAceptar.setBounds(155, 120, 130, 35);
        btnAceptar.setBackground(new java.awt.Color(50, 50, 50));
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnAceptar);

        dialogo.add(panel);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnAceptar.requestFocusInWindow();
                });
            }
        });

        dialogo.setVisible(true);
    }

    // Alerta de información con botón bien contrastado
    public static void mostrarInformacion(java.awt.Component parent, String mensaje) {
        javax.swing.JDialog dialogo = new javax.swing.JDialog((java.awt.Frame) null, "Información", true);
        dialogo.setUndecorated(true);
        dialogo.setSize(420, 180);
        dialogo.setLocationRelativeTo(parent);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        java.awt.Color colorBorde = new java.awt.Color(23, 162, 184); // Azul corporativo
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        // Título
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("INFORMACIÓN");
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(25, 20, 370, 25);
        panel.add(lblTitulo);

        // Mensaje
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel(mensaje);
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblMensaje.setForeground(new java.awt.Color(60, 60, 60));
        lblMensaje.setBounds(25, 55, 370, 40);
        panel.add(lblMensaje);

        // Botón Aceptar (Gris oscuro sólido para contraste perfecto)
        javax.swing.JButton btnAceptar = new javax.swing.JButton("ACEPTAR");
        btnAceptar.setBounds(145, 115, 130, 35);
        btnAceptar.setBackground(new java.awt.Color(50, 50, 50));
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnAceptar);

        dialogo.add(panel);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnAceptar.requestFocusInWindow();
                });
            }
        });

        dialogo.setVisible(true);
    }

    public static void mostrarExito(java.awt.Component parent, String mensaje) {
        javax.swing.JDialog dialogo = new javax.swing.JDialog((java.awt.Frame) null, "Éxito", true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 180);
        dialogo.setLocationRelativeTo(parent);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        java.awt.Color colorBorde = new java.awt.Color(40, 167, 69); // Verde éxito corporativo
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/exito.gif");
            if (urlGif != null) {
                javax.swing.ImageIcon iconGif = new javax.swing.ImageIcon(urlGif);
                javax.swing.JLabel lblGif = new javax.swing.JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            } else {
                System.err.println("⚠️ ADVERTENCIA: No se encontró el archivo /iconos/exito.gif");
            }
        } catch (Exception e) {
        }

        // Título (Desplazado a la derecha para dar espacio al GIF)
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("¡ÉXITO!");
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 22, 340, 25);
        panel.add(lblTitulo);

        // Mensaje con HTML para salto de línea automático
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblMensaje.setForeground(new java.awt.Color(60, 60, 60));
        lblMensaje.setBounds(75, 50, 340, 45);
        panel.add(lblMensaje);

        // Botón Aceptar (Gris oscuro sólido para contraste perfecto)
        javax.swing.JButton btnAceptar = new javax.swing.JButton("ACEPTAR");
        btnAceptar.setBounds(155, 120, 130, 35);
        btnAceptar.setBackground(new java.awt.Color(50, 50, 50));
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnAceptar);

        dialogo.add(panel);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnAceptar.requestFocusInWindow();
                });
            }
        });

        dialogo.setVisible(true);
    }

    // Alerta de error con estilo moderno (Borde rojo)
    public static void mostrarError(java.awt.Component parent, String mensaje) {
        javax.swing.JDialog dialogo = new javax.swing.JDialog((java.awt.Frame) null, "Error", true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 180);
        dialogo.setLocationRelativeTo(parent);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        java.awt.Color colorBorde = new java.awt.Color(220, 53, 69); // Rojo error corporativo
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/error.gif");
            if (urlGif != null) {
                javax.swing.ImageIcon iconGif = new javax.swing.ImageIcon(urlGif);
                javax.swing.JLabel lblGif = new javax.swing.JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            } else {
                System.err.println("ADVERTENCIA: No se encontró el archivo /iconos/error.gif");
            }
        } catch (Exception e) {
        }

        // Título (Desplazado a la derecha para dar espacio al GIF)
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("ERROR");
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 22, 340, 25);
        panel.add(lblTitulo);

        // Mensaje con HTML para salto de línea automático
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblMensaje.setForeground(new java.awt.Color(60, 60, 60));
        lblMensaje.setBounds(75, 50, 340, 45);
        panel.add(lblMensaje);

        // Botón Aceptar (Gris oscuro sólido para contraste perfecto)
        javax.swing.JButton btnAceptar = new javax.swing.JButton("ACEPTAR");
        btnAceptar.setBounds(155, 120, 130, 35);
        btnAceptar.setBackground(new java.awt.Color(50, 50, 50));
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> dialogo.dispose());

        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnAceptar);

        dialogo.add(panel);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnAceptar.requestFocusInWindow();
                });
            }
        });

        dialogo.setVisible(true);
    }

// Método para mostrar un diálogo con múltiples opciones personalizadas (reemplazo de showOptionDialog)
    public static int mostrarOpciones(java.awt.Component parent, String titulo, String mensaje, String[] opciones) {
        final int[] seleccion = {-1};

        // Ampliamos el ancho de la ventana a 600 para que quepan holgados los 4 botones
        JDialog dialogo = new JDialog((java.awt.Frame) null, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(600, 190);
        dialogo.setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        Color colorBorde = new Color(51, 153, 255); // Azul corporativo
        panel.setBorder(BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/pregunta.gif");
            if (urlGif != null) {
                ImageIcon iconGif = new ImageIcon(urlGif);
                JLabel lblGif = new JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Título ajustado al nuevo ancho (500px de espacio útil)
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 20, 500, 25);
        panel.add(lblTitulo);

        // Mensaje con HTML
        JLabel lblMensaje = new JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(60, 60, 60));
        lblMensaje.setBounds(75, 48, 500, 45);
        panel.add(lblMensaje);

        // Cálculo dinámico para repartir los 4 botones de forma impecable
        if (opciones != null && opciones.length > 0) {
            int totalOpciones = opciones.length;
            int anchoBtn = 115; // Ancho individual óptimo para textos largos
            int altoBtn = 35;
            int espacio = 8;    // Espacio entre botones

            int anchoTotal = (totalOpciones * anchoBtn) + ((totalOpciones - 1) * espacio);
            int xInicial = 75 + (500 - anchoTotal) / 2; // Centrado automático perfecto

            for (int i = 0; i < totalOpciones; i++) {
                final int index = i;
                JButton btn = new JButton(opciones[i]);
                btn.setBounds(xInicial + (i * (anchoBtn + espacio)), 120, anchoBtn, altoBtn);

                // El primer botón destaca con el azul corporativo, el resto grises sólidos elegantes
                if (i == 0) {
                    btn.setBackground(colorBorde);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(new Color(108, 117, 125));
                    btn.setForeground(Color.WHITE);
                }

                btn.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Letra un poco más compacta para textos largos
                btn.setFocusPainted(false);
                btn.setBorder(null);
                btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                btn.addActionListener(e -> {
                    seleccion[0] = index;
                    dialogo.dispose();
                });

                panel.add(btn);

                if (i == 0) {
                    dialogo.getRootPane().setDefaultButton(btn);
                }
            }
        }

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    for (java.awt.Component comp : panel.getComponents()) {
                        if (comp instanceof JButton) {
                            comp.requestFocusInWindow();
                            break;
                        }
                    }
                });
            }
        });

        dialogo.add(panel);
        dialogo.setVisible(true);

        return seleccion[0];
    }

    public static String mostrarSelector(java.awt.Component parent, String titulo, String mensaje, Object[] elementos, Object seleccionInicial) {
        final String[] resultado = {null}; // Almacena la opción elegida

        javax.swing.JDialog dialogo = new javax.swing.JDialog((java.awt.Frame) null, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(420, 200);
        dialogo.setLocationRelativeTo(parent);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        java.awt.Color colorBorde = new java.awt.Color(51, 153, 255); // Azul corporativo
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

        // Título
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel(titulo);
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(25, 20, 370, 25);
        panel.add(lblTitulo);

        // Mensaje
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblMensaje.setForeground(new java.awt.Color(80, 80, 80));
        lblMensaje.setBounds(25, 50, 370, 35);
        panel.add(lblMensaje);

        // ComboBox con los elementos actuales
        javax.swing.JComboBox<Object> cmbElementos = new javax.swing.JComboBox<>(elementos);
        cmbElementos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        cmbElementos.setBounds(25, 95, 370, 35);
        if (seleccionInicial != null) {
            cmbElementos.setSelectedItem(seleccionInicial);
        }
        panel.add(cmbElementos);

        // Botón Cancelar
        javax.swing.JButton btnCancelar = new javax.swing.JButton("CANCELAR");
        btnCancelar.setBounds(120, 145, 120, 35);
        btnCancelar.setBackground(new java.awt.Color(230, 230, 230));
        btnCancelar.setForeground(new java.awt.Color(60, 60, 60));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(null);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> {
            resultado[0] = null; // Devuelve null si cancela
            dialogo.dispose();
        });

        // Botón Aceptar
        javax.swing.JButton btnAceptar = new javax.swing.JButton("ACEPTAR");
        btnAceptar.setBounds(250, 145, 145, 35);
        btnAceptar.setBackground(colorBorde);
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> {
            Object seleccionado = cmbElementos.getSelectedItem();
            if (seleccionado != null) {
                resultado[0] = seleccionado.toString();
            }
            dialogo.dispose();
        });

        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnCancelar);
        panel.add(btnAceptar);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> cmbElementos.requestFocusInWindow());
            }
        });

        dialogo.add(panel);
        dialogo.setVisible(true);

        return resultado[0];
    }

// 1. Versión para AGREGAR (solo 3 parámetros: parent, titulo, mensaje)
    public static String mostrarInput(java.awt.Component parent, String titulo, String mensaje) {
        return mostrarInput(parent, titulo, mensaje, null);
    }

    // 2. Versión para EDITAR y AGREGAR (4 parámetros: con valor inicial)
    public static String mostrarInput(java.awt.Component parent, String titulo, String mensaje, String valorInicial) {
        final String[] resultado = {null}; // Almacena el texto ingresado

        javax.swing.JDialog dialogo = new javax.swing.JDialog((java.awt.Frame) null, titulo, true);
        dialogo.setUndecorated(true);
        dialogo.setSize(440, 200);
        dialogo.setLocationRelativeTo(parent);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        java.awt.Color colorBorde = new java.awt.Color(51, 153, 255); // Azul corporativo
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(colorBorde, 2));
        panel.setLayout(null);

      
        try {
            java.net.URL urlGif = AlertaUtil.class.getResource("/iconos/pregunta.gif");
            if (urlGif != null) {
                javax.swing.ImageIcon iconGif = new javax.swing.ImageIcon(urlGif);
                javax.swing.JLabel lblGif = new javax.swing.JLabel(iconGif);
                lblGif.setBounds(25, 32, 35, 35);
                panel.add(lblGif);
            }
        } catch (Exception e) {
        }

        // Título
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel(titulo);
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblTitulo.setForeground(colorBorde);
        lblTitulo.setBounds(75, 20, 340, 25);
        panel.add(lblTitulo);

        // Mensaje con HTML
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel("<html>" + mensaje + "</html>");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        lblMensaje.setForeground(new java.awt.Color(80, 80, 80));
        lblMensaje.setBounds(75, 48, 340, 35);
        panel.add(lblMensaje);

        // Campo de Texto moderno
        javax.swing.JTextField txtInput = new javax.swing.JTextField(valorInicial != null ? valorInicial : "");
        txtInput.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        txtInput.setBounds(75, 90, 335, 32);

        txtInput.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
                javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        txtInput.setBackground(java.awt.Color.WHITE);
        txtInput.setForeground(new java.awt.Color(50, 50, 50));

        if (valorInicial != null && !valorInicial.isEmpty()) {
            txtInput.selectAll(); // Selecciona el texto si es para editar
        }
        panel.add(txtInput);

        // Botón Cancelar
        javax.swing.JButton btnCancelar = new javax.swing.JButton("CANCELAR");
        btnCancelar.setBounds(135, 145, 115, 32);
        btnCancelar.setBackground(new java.awt.Color(108, 117, 125));
        btnCancelar.setForeground(java.awt.Color.WHITE);
        btnCancelar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(null);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> {
            resultado[0] = null;
            dialogo.dispose();
        });

        // Botón Aceptar
        javax.swing.JButton btnAceptar = new javax.swing.JButton("ACEPTAR");
        btnAceptar.setBounds(260, 145, 150, 32);
        btnAceptar.setBackground(colorBorde);
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);
        btnAceptar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> {
            resultado[0] = txtInput.getText();
            dialogo.dispose();
        });

        dialogo.getRootPane().setDefaultButton(btnAceptar);
        panel.add(btnCancelar);
        panel.add(btnAceptar);

        dialogo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> txtInput.requestFocusInWindow());
            }
        });

        dialogo.add(panel);
        dialogo.setVisible(true);

        return resultado[0];
    }
}
