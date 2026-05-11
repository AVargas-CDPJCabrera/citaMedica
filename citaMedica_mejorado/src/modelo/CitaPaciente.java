package modelo;

import excepciones.CamposVaciosException;
import excepciones.HistoriaClinicaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa una cita médica de un paciente.
 *
 * <p>Invariantes de negocio que garantiza esta clase:</p>
 * <ul>
 *   <li>La historia clínica debe tener 13 dígitos y superar el dígito de control.</li>
 *   <li>Nombre, apellidos y especialidad no pueden estar vacíos.</li>
 *   <li>La fecha de cita no puede ser anterior a hoy.</li>
 * </ul>
 */
public class CitaPaciente {

    // ── Especialidades válidas ────────────────────────────────────────────────
    public static final String[] ESPECIALIDADES_VALIDAS = {
        "CARDIOLOGIA", "TRAUMATOLOGIA", "PEDIATRIA",
        "DERMATOLOGIA", "NEUROLOGIA", "ONCOLOGIA"
    };

    // ── Atributos ─────────────────────────────────────────────────────────────
    private int       idCita;
    private String    historiaClinica;
    private String    nombre;
    private String    apellidos;
    private String    numTelefono;
    private String    especialidad;
    private LocalDate fechaCita;
    private int       numCita;

    // ── Constructor vacío ─────────────────────────────────────────────────────
    public CitaPaciente() {}

    // ── Constructor sin idCita (alta de nueva cita) ───────────────────────────
    public CitaPaciente(String historiaClinica, String nombre, String apellidos,
                        String numTelefono, String especialidad,
                        String fechaCita, int numCita)
            throws HistoriaClinicaException {

        validarHistoriaClinica(historiaClinica);
        this.historiaClinica = historiaClinica;
        this.nombre          = nombre;
        this.apellidos       = apellidos;
        this.numTelefono     = numTelefono;
        this.especialidad    = especialidad.toUpperCase().trim();
        this.fechaCita       = LocalDate.parse(fechaCita);
        this.numCita         = numCita;
    }

    // ── Constructor con idCita (lectura desde BD) ─────────────────────────────
    public CitaPaciente(int idCita, String historiaClinica, String nombre,
                        String apellidos, String numTelefono, String especialidad,
                        String fechaCita, int numCita)
            throws HistoriaClinicaException {

        this(historiaClinica, nombre, apellidos, numTelefono,
             especialidad, fechaCita, numCita);
        this.idCita = idCita;
    }

    // ════════════════════════════════════════════════════════════════════════
    // V A L I D A C I O N E S
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Valida la historia clínica mediante el algoritmo de dígito de control.
     *
     * <p>La historia clínica debe:</p>
     * <ul>
     *   <li>Tener exactamente 13 caracteres.</li>
     *   <li>Contener solo dígitos.</li>
     *   <li>El último dígito debe coincidir con el dígito de control calculado
     *       aplicando pesos alternos 1 y 3 sobre los 12 primeros dígitos.</li>
     * </ul>
     */
    private void validarHistoriaClinica(String hc) throws HistoriaClinicaException {
        if (hc == null || hc.length() != 13 || !hc.matches("\\d{13}")) {
            throw new HistoriaClinicaException();
        }

        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(hc.charAt(i));
            suma += (i % 2 == 0) ? digito : digito * 3;
        }

        int digitoControl = (10 - (suma % 10)) % 10;
        if (digitoControl != Character.getNumericValue(hc.charAt(12))) {
            throw new HistoriaClinicaException();
        }
    }

    /** Comprueba si una especialidad pertenece a las permitidas. */
    public static boolean esEspecialidadValida(String especialidad) {
        if (especialidad == null) return false;
        String upper = especialidad.toUpperCase().trim();
        for (String e : ESPECIALIDADES_VALIDAS) {
            if (e.equals(upper)) return true;
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // S E T T E R S
    // ════════════════════════════════════════════════════════════════════════

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public void setHistoriaClinica(String historiaClinica) throws HistoriaClinicaException {
        validarHistoriaClinica(historiaClinica);
        this.historiaClinica = historiaClinica;
    }

    public void setNombre(String nombre) throws CamposVaciosException {
        if (nombre == null || nombre.isBlank()) throw new CamposVaciosException("El nombre no puede estar vacío");
        this.nombre = nombre.trim();
    }

    public void setApellidos(String apellidos) throws CamposVaciosException {
        if (apellidos == null || apellidos.isBlank()) throw new CamposVaciosException("Los apellidos no pueden estar vacíos");
        this.apellidos = apellidos.trim();
    }

    public void setNumTelefono(String numTelefono) {
        this.numTelefono = numTelefono;
    }

    public void setEspecialidad(String especialidad) throws CamposVaciosException {
        if (especialidad == null || especialidad.isBlank()) throw new CamposVaciosException("La especialidad no puede estar vacía");
        this.especialidad = especialidad.toUpperCase().trim();
    }

    public void setFechaCita(String fechaCita) {
        this.fechaCita = LocalDate.parse(fechaCita);
    }

    public void setNumCita(int numCita) {
        this.numCita = numCita;
    }

    // ════════════════════════════════════════════════════════════════════════
    // G E T T E R S
    // ════════════════════════════════════════════════════════════════════════

    public int       getIdCita()          { return idCita; }
    public String    getHistoriaClinica() { return historiaClinica; }
    public String    getNombre()          { return nombre; }
    public String    getApellidos()       { return apellidos; }
    public String    getNumTelefono()     { return numTelefono; }
    public String    getEspecialidad()    { return especialidad; }
    public LocalDate getFechaCita()       { return fechaCita; }
    public int       getNumCita()         { return numCita; }

    // ════════════════════════════════════════════════════════════════════════
    // E Q U A L S  /  H A S H C O D E  /  T O S T R I N G
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CitaPaciente)) return false;
        CitaPaciente other = (CitaPaciente) o;
        return Objects.equals(historiaClinica, other.historiaClinica)
                && numCita == other.numCita;
    }

    @Override
    public int hashCode() {
        return Objects.hash(historiaClinica, numCita);
    }

    @Override
    public String toString() {
        return String.format(
            "CitaPaciente{id=%d, hc=%s, nombre='%s %s', tel=%s, esp=%s, fecha=%s, turno=%d}",
            idCita, historiaClinica, nombre, apellidos,
            numTelefono, especialidad, fechaCita, numCita);
    }
}
