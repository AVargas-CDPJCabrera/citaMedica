package controlador;

import dao.CitasDao;
import excepciones.AforoMaximoException;
import excepciones.HistoriaClinicaException;
import modelo.CitaPaciente;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de citas médicas (capa de servicio).
 *
 * <p>Actúa de intermediario entre la vista y el DAO, centralizando
 * la lógica de negocio que no pertenece al modelo ni a la persistencia.</p>
 */
public class CitasController {

    private final CitasDao dao;

    public CitasController() {
        this.dao = new CitasDao();
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Devuelve todas las citas ordenadas. */
    public List<CitaPaciente> getAll()
            throws SQLException, HistoriaClinicaException {
        return dao.getAll();
    }

    /**
     * Devuelve las citas que coincidan con la especialidad indicada.
     *
     * @param especialidad texto a buscar (búsqueda parcial).
     */
    public List<CitaPaciente> buscarPorEspecialidad(String especialidad)
            throws SQLException, HistoriaClinicaException {
        return dao.getCitasPorEspecialidad(especialidad);
    }

    // ── Modificaciones ────────────────────────────────────────────────────────

    /**
     * Inserta una nueva cita.
     *
     * @throws AforoMaximoException si la especialidad ya tiene 10 citas ese día.
     */
    public void insertar(CitaPaciente cita)
            throws SQLException, AforoMaximoException {
        dao.agregar(cita);
    }

    /** Elimina la cita indicada. */
    public void eliminar(CitaPaciente cita) throws SQLException {
        dao.eliminar(cita);
    }

    /**
     * Modifica la fecha de una cita existente:
     * elimina el registro antiguo e inserta uno nuevo con la nueva fecha.
     *
     * @param original  cita que se quiere modificar.
     * @param nueva     cita con los datos actualizados.
     */
    public void modificar(CitaPaciente original, CitaPaciente nueva)
            throws SQLException, AforoMaximoException {
        dao.eliminar(original);
        dao.agregar(nueva);
    }
}
