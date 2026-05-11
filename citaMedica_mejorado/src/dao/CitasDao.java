package dao;

import excepciones.AforoMaximoException;
import excepciones.HistoriaClinicaException;
import modelo.CitaPaciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Capa de acceso a datos para la entidad CitaPaciente.
 * Utiliza try-with-resources en todas las operaciones para garantizar
 * el cierre automático de recursos y evitar fugas de conexión.
 */
public class CitasDao {

    private static final Logger LOGGER  = Logger.getLogger(CitasDao.class.getName());
    private static final int    AFORO   = 10;

    // ── SQL ──────────────────────────────────────────────────────────────────
    private static final String SQL_GET_ALL =
            "SELECT * FROM CITAS ORDER BY especialidad ASC, fechaCita ASC, numCita ASC";

    private static final String SQL_COUNT_CITAS =
            "SELECT COUNT(*) FROM CITAS WHERE especialidad = ? AND fechaCita = ?";

    private static final String SQL_NUMS_USADOS =
            "SELECT numCita FROM CITAS WHERE especialidad = ? AND fechaCita = ? ORDER BY numCita";

    private static final String SQL_INSERT =
            "INSERT INTO CITAS (historiaClinica, nombre, apellidos, numTelefono, " +
            "especialidad, fechaCita, numCita) VALUES (?,?,?,?,?,?,?)";

    private static final String SQL_DELETE =
            "DELETE FROM CITAS WHERE idCita = ?";

    private static final String SQL_BUSCAR_ESPECIALIDAD =
            "SELECT * FROM CITAS WHERE especialidad LIKE ? ORDER BY fechaCita ASC, numCita ASC";

    // ════════════════════════════════════════════════════════════════════════
    // R E A D
    // ════════════════════════════════════════════════════════════════════════

    /** Devuelve todas las citas ordenadas por especialidad → fecha → número. */
    public List<CitaPaciente> getAll() throws SQLException, HistoriaClinicaException {
        List<CitaPaciente> lista = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /**
     * Devuelve las citas cuya especialidad coincide (búsqueda parcial).
     */
    public List<CitaPaciente> getCitasPorEspecialidad(String especialidad)
            throws SQLException, HistoriaClinicaException {

        List<CitaPaciente> lista = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_ESPECIALIDAD)) {
            ps.setString(1, "%" + especialidad + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    // ════════════════════════════════════════════════════════════════════════
    // C R E A T E
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Inserta una nueva cita.
     * Comprueba el aforo y asigna automáticamente el número de turno libre.
     *
     * @throws AforoMaximoException si ya hay {@value #AFORO} citas ese día en esa especialidad.
     */
    public void agregar(CitaPaciente cita) throws SQLException, AforoMaximoException {
        Connection conn = DbConnection.getInstance().getConnection();

        // 1. Comprobar aforo
        int total = contarCitas(conn, cita);
        if (total >= AFORO) {
            throw new AforoMaximoException();
        }

        // 2. Calcular el primer número de turno libre
        int numLibre = primerTurnoLibre(conn, cita);

        // 3. Insertar
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, cita.getHistoriaClinica());
            ps.setString(2, cita.getNombre());
            ps.setString(3, cita.getApellidos());
            ps.setString(4, cita.getNumTelefono());
            ps.setString(5, cita.getEspecialidad());
            ps.setDate  (6, Date.valueOf(cita.getFechaCita()));
            ps.setInt   (7, numLibre);
            ps.executeUpdate();
        }
        LOGGER.info("Cita insertada: " + cita.getNombre() + " - " + cita.getEspecialidad());
    }

    // ════════════════════════════════════════════════════════════════════════
    // D E L E T E
    // ════════════════════════════════════════════════════════════════════════

    /** Elimina la cita identificada por su id. */
    public void eliminar(CitaPaciente cita) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, cita.getIdCita());
            int filas = ps.executeUpdate();
            LOGGER.info("Citas eliminadas: " + filas + " (id=" + cita.getIdCita() + ")");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // H E L P E R S   P R I V A D O S
    // ════════════════════════════════════════════════════════════════════════

    /** Mapea una fila del ResultSet a un objeto CitaPaciente. */
    private CitaPaciente mapRow(ResultSet rs) throws SQLException, HistoriaClinicaException {
        return new CitaPaciente(
                rs.getInt   ("idCita"),
                rs.getString("historiaClinica"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getString("numTelefono"),
                rs.getString("especialidad"),
                rs.getString("fechaCita"),
                rs.getInt   ("numCita")
        );
    }

    /** Cuenta cuántas citas existen para una especialidad y fecha. */
    private int contarCitas(Connection conn, CitaPaciente cita) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_CITAS)) {
            ps.setString(1, cita.getEspecialidad());
            ps.setDate  (2, Date.valueOf(cita.getFechaCita()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Devuelve el primer número de turno disponible (entre 1 y {@value #AFORO}). */
    private int primerTurnoLibre(Connection conn, CitaPaciente cita) throws SQLException {
        List<Integer> usados = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_NUMS_USADOS)) {
            ps.setString(1, cita.getEspecialidad());
            ps.setDate  (2, Date.valueOf(cita.getFechaCita()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usados.add(rs.getInt(1));
                }
            }
        }

        for (int i = 1; i <= AFORO; i++) {
            if (!usados.contains(i)) return i;
        }
        return AFORO; // No debería llegar aquí (el aforo ya fue comprobado)
    }
}
