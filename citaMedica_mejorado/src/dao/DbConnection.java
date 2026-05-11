package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Gestiona la conexión a la base de datos mediante el patrón Singleton.
 * Los parámetros de conexión se leen desde db.properties para no
 * exponer credenciales en el código fuente.
 */
public class DbConnection {

    private static final Logger LOGGER = Logger.getLogger(DbConnection.class.getName());

    // ── Configuración por defecto (si no existe db.properties) ───────────────
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/hospital2?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER     = "root";
    private static final String DEFAULT_PASSWORD = "";

    // ── Instancia única ───────────────────────────────────────────────────────
    private static DbConnection instance;

    private Connection conn;

    // ── Constructor privado ───────────────────────────────────────────────────
    private DbConnection() throws SQLException {
        String url, user, password;

        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                url      = props.getProperty("db.url",      DEFAULT_URL);
                user     = props.getProperty("db.user",     DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASSWORD);
            } else {
                LOGGER.warning("db.properties no encontrado; usando valores por defecto.");
                url      = DEFAULT_URL;
                user     = DEFAULT_USER;
                password = DEFAULT_PASSWORD;
            }

        } catch (IOException e) {
            LOGGER.warning("Error leyendo db.properties: " + e.getMessage());
            url      = DEFAULT_URL;
            user     = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }

        this.conn = DriverManager.getConnection(url, user, password);
        LOGGER.info("Conexión establecida con la base de datos.");
    }

    /**
     * Devuelve la instancia única (Singleton).
     * Si la conexión está cerrada, la reabre.
     */
    public static DbConnection getInstance() throws SQLException {
        if (instance == null || instance.conn.isClosed()) {
            instance = new DbConnection();
        }
        return instance;
    }

    /** Retorna la conexión activa. */
    public Connection getConnection() {
        return conn;
    }

    /** Cierra la conexión si está abierta. */
    public void disconnect() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            LOGGER.info("Conexión cerrada.");
        }
    }
}
