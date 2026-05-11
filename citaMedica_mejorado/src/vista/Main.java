package vista;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Logger;

/**
 * Punto de entrada de la aplicación.
 * Lanza la ventana principal en el Event Dispatch Thread (EDT).
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Aplicar Look & Feel nativo del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.warning("No se pudo aplicar el Look & Feel del sistema: " + e.getMessage());
        }

        // Lanzar la UI siempre en el EDT (buena práctica en Swing)
        SwingUtilities.invokeLater(() -> {
            try {
                new frmCitas();
            } catch (Exception e) {
                LOGGER.severe("Error al iniciar la aplicación: " + e.getMessage());
            }
        });
    }
}
