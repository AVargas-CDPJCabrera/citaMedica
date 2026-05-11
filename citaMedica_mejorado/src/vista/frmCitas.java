package vista;

import controlador.CitasController;
import excepciones.AforoMaximoException;
import excepciones.CamposVaciosException;
import excepciones.HistoriaClinicaException;
import modelo.CitaPaciente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Ventana principal de gestión de citas médicas.
 *
 * <p>Correcciones y mejoras sobre la versión original:</p>
 * <ul>
 *   <li>Bug de doble ejecución del bloque de modificación corregido.</li>
 *   <li>Validación de especialidades usando la lista centralizada del modelo.</li>
 *   <li>La tabla no añade una columna extra (numCita duplicado eliminado).</li>
 *   <li>Guardado de citas: separación clara entre alta y modificación.</li>
 *   <li>Método {@code modificar()} delegado al controlador con operación atómica.</li>
 *   <li>El filtro de consultas está implementado y funcional.</li>
 *   <li>Mensajes de error y confirmación mejorados.</li>
 *   <li>Se eliminan System.out.println de depuración de producción.</li>
 *   <li>Logger en lugar de System.out/err.</li>
 * </ul>
 */
public class frmCitas extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(frmCitas.class.getName());

    // ── Panel principal ──────────────────────────────────────────────────────
    private JPanel panel;

    // ── Panel Cita ───────────────────────────────────────────────────────────
    private JPanel     panelCita;
    private JTextField textNumCita;
    private JTextField textHistoriaClinica;
    private JTextField textNombre;
    private JTextField textApellidos;
    private JTextField textTelefono;
    private JTextField textEspecialidad;
    private JTextField textFechaCita;

    // ── Panel Mantenimiento ──────────────────────────────────────────────────
    private JPanel  panelMantenimiento;
    private JButton btnNuevaCita;
    private JButton btnModificar;
    private JButton btnBorrar;
    private JButton btnGuardar;
    private JButton btnDeshacer;

    // ── Panel Navegador ──────────────────────────────────────────────────────
    private JPanel  panelNavegador;
    private JButton btnPrimero;
    private JButton btnAtras;
    private JButton btnAdelante;
    private JButton btnUltimo;

    // ── Panel Grid ───────────────────────────────────────────────────────────
    private JPanel            panelGrid;
    private JComboBox<String> comboConsulta;
    private JTextField        textFiltrar;
    private JButton           btnFiltrar;
    private JScrollPane       scrollPane;
    private JTable            tblCitas;
    private DefaultTableModel dtm;

    // ── Estado ───────────────────────────────────────────────────────────────
    private final CitasController citasController;
    private List<CitaPaciente>    citas;
    private int     puntero       = 0;
    private boolean esNuevaCita   = false;
    private boolean esModificacion = false;

    // ════════════════════════════════════════════════════════════════════════
    // C O N S T R U C T O R
    // ════════════════════════════════════════════════════════════════════════

    public frmCitas() throws SQLException, HistoriaClinicaException {
        citasController = new CitasController();

        setTitle("G E S T I Ó N   D E   C I T A S   M É D I C A S");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 550);

        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setLayout(null);
        setContentPane(panel);

        construirVentana();
        registrarEventos();
        cargarDatos();

        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // C O N S T R U C C I Ó N   U I
    // ════════════════════════════════════════════════════════════════════════

    private void construirVentana() {
        construirPanelMantenimiento();
        construirPanelCita();
        construirPanelNavegador();
        construirPanelGrid();
    }

    private void construirPanelCita() {
        panelCita = crearPanelConBorde("Datos de la Cita", 28, 100, 340, 300);

        textNumCita         = agregarCampo(panelCita, "Nº Cita",         15,  25, 130, 60,  false);
        textHistoriaClinica = agregarCampo(panelCita, "Historia Clínica", 15,  55, 130, 190, false);
        textNombre          = agregarCampo(panelCita, "Nombre",           15,  85, 130, 190, false);
        textApellidos       = agregarCampo(panelCita, "Apellidos",        15, 115, 130, 190, false);
        textTelefono        = agregarCampo(panelCita, "Teléfono",         15, 145, 130, 120, false);
        textEspecialidad    = agregarCampo(panelCita, "Especialidad",     15, 175, 130, 190, false);
        textFechaCita       = agregarCampo(panelCita, "Fecha Cita",       15, 205, 130, 120, false);

        JLabel lblFormato = new JLabel("aaaa-MM-dd");
        lblFormato.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lblFormato.setBounds(255, 206, 78, 14);
        panelCita.add(lblFormato);
    }

    private void construirPanelMantenimiento() {
        panelMantenimiento = crearPanelConBorde("Gestión de Citas", 28, 11, 340, 80);

        btnNuevaCita = crearBoton(panelMantenimiento, "imagenes/botonNuevaCita.jpg",  "Solicitar Nueva Cita",    15,  20, true);
        btnModificar = crearBoton(panelMantenimiento, "imagenes/botonModificar.jpg",  "Modificar Fecha de Cita", 75,  20, true);
        btnBorrar    = crearBoton(panelMantenimiento, "imagenes/botonBorrar.jpg",     "Cancelar Cita",           135, 20, true);
        btnGuardar   = crearBoton(panelMantenimiento, "imagenes/botonGuardar.jpg",    "Guardar",                 210, 20, false);
        btnDeshacer  = crearBoton(panelMantenimiento, "imagenes/botonDeshacer.jpg",   "Deshacer",                270, 20, false);
    }

    private void construirPanelNavegador() {
        panelNavegador = crearPanelConBorde("Navegador", 28, 415, 230, 70);

        btnPrimero  = crearBoton(panelNavegador, "imagenes/navPri.jpg", "Primera cita",   10,  18, true);
        btnAtras    = crearBoton(panelNavegador, "imagenes/navIzq.jpg", "Cita anterior",  60,  18, true);
        btnAdelante = crearBoton(panelNavegador, "imagenes/navDer.jpg", "Cita siguiente", 110, 18, true);
        btnUltimo   = crearBoton(panelNavegador, "imagenes/navUlt.jpg", "Última cita",    160, 18, true);
    }

    private void construirPanelGrid() {
        // Barra de filtros
        JLabel lblConsulta = new JLabel("Consultas:");
        lblConsulta.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblConsulta.setBounds(400, 11, 80, 20);
        panel.add(lblConsulta);

        comboConsulta = new JComboBox<>(new String[]{
            "Todas las citas", "Por especialidad", "Historial paciente"
        });
        comboConsulta.setBounds(400, 38, 150, 22);
        panel.add(comboConsulta);

        textFiltrar = new JTextField();
        textFiltrar.setBounds(560, 38, 200, 22);
        panel.add(textFiltrar);

        btnFiltrar = new JButton("CONSULTAR");
        btnFiltrar.setBounds(770, 37, 110, 24);
        panel.add(btnFiltrar);

        // Tabla — 7 columnas, sin duplicados
        dtm = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        dtm.setColumnIdentifiers(new String[]{
            "Nº Cita", "H. Clínica", "Nombre", "Apellidos", "Teléfono", "Especialidad", "Fecha"
        });

        tblCitas = new JTable(dtm);
        tblCitas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblCitas.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblCitas.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblCitas.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblCitas.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblCitas.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblCitas.getColumnModel().getColumn(6).setPreferredWidth(90);

        // Clic en fila → navegar al registro
        tblCitas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblCitas.getSelectedRow() >= 0) {
                puntero = tblCitas.getSelectedRow();
                mostrarCita(puntero);
            }
        });

        panelGrid = new JPanel(new BorderLayout());
        panelGrid.setBounds(400, 70, 760, 415);
        panelGrid.add(new JScrollPane(tblCitas), BorderLayout.CENTER);
        panel.add(panelGrid);
    }

    // ════════════════════════════════════════════════════════════════════════
    // E V E N T O S
    // ════════════════════════════════════════════════════════════════════════

    private void registrarEventos() {
        btnPrimero .addActionListener(e -> inicio());
        btnAtras   .addActionListener(e -> anterior());
        btnAdelante.addActionListener(e -> siguiente());
        btnUltimo  .addActionListener(e -> ultimo());
        btnFiltrar .addActionListener(e -> filtrar());

        btnNuevaCita.addActionListener(e -> {
            esNuevaCita = true;
            limpiarPanelCita();
            habilitarNavegador(false);
            habilitarPanelMantenimiento(false);
            habilitarPanelCita(true);
        });

        btnModificar.addActionListener(e -> {
            esModificacion = true;
            habilitarNavegador(false);
            habilitarPanelMantenimiento(false);
            habilitarPanelCita(true);
            textHistoriaClinica.setEditable(false); // La HC no se puede cambiar
        });

        btnBorrar.addActionListener(e -> {
            try { borrarCita(); }
            catch (Exception ex) { mostrarError(ex.getMessage()); }
        });

        btnGuardar.addActionListener(e -> guardar());

        btnDeshacer.addActionListener(e -> {
            esNuevaCita    = false;
            esModificacion = false;
            if (citas != null && !citas.isEmpty()) mostrarCita(puntero);
            habilitarNavegador(true);
            habilitarPanelMantenimiento(true);
            habilitarPanelCita(false);
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // D A T O S
    // ════════════════════════════════════════════════════════════════════════

    private void cargarDatos() throws SQLException, HistoriaClinicaException {
        cargarGrid(citasController.getAll());
        if (citas != null && !citas.isEmpty()) mostrarCita(0);
    }

    /** Recarga la tabla con la lista recibida y actualiza la referencia local. */
    private void cargarGrid(List<CitaPaciente> lista) {
        citas = lista;
        dtm.setRowCount(0);
        for (CitaPaciente c : citas) {
            dtm.addRow(new Object[]{
                c.getNumCita(),
                c.getHistoriaClinica(),
                c.getNombre(),
                c.getApellidos(),
                c.getNumTelefono(),
                c.getEspecialidad(),
                c.getFechaCita()
            });
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // G U A R D A R
    // ════════════════════════════════════════════════════════════════════════

    private void guardar() {
        try {
            if (esNuevaCita) {
                guardarNuevaCita();
            } else if (esModificacion) {
                guardarModificacion();
            }
        } catch (CamposVaciosException | HistoriaClinicaException
                | AforoMaximoException | SQLException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void guardarNuevaCita()
            throws CamposVaciosException, HistoriaClinicaException,
                   AforoMaximoException, SQLException {

        String hc          = textHistoriaClinica.getText().trim();
        String nombre      = textNombre.getText().trim();
        String apellidos   = textApellidos.getText().trim();
        String telefono    = textTelefono.getText().trim();
        String especialidad = textEspecialidad.getText().trim().toUpperCase();
        String fecha       = textFechaCita.getText().trim();

        // Validar campos vacíos
        if (hc.isBlank() || nombre.isBlank() || apellidos.isBlank()
                || telefono.isBlank() || especialidad.isBlank() || fecha.isBlank()) {
            throw new CamposVaciosException();
        }

        // Validar especialidad con la lista centralizada del modelo
        if (!CitaPaciente.esEspecialidadValida(especialidad)) {
            throw new HistoriaClinicaException(
                "Especialidad no válida. Valores permitidos: " +
                String.join(", ", CitaPaciente.ESPECIALIDADES_VALIDAS));
        }

        CitaPaciente nueva = new CitaPaciente(hc, nombre, apellidos, telefono, especialidad, fecha, 0);
        citasController.insertar(nueva);

        JOptionPane.showMessageDialog(this, "✅ Cita añadida correctamente.");
        esNuevaCita = false;
        limpiarPanelCita();
        habilitarNavegador(true);
        habilitarPanelCita(false);
        habilitarPanelMantenimiento(true);
        cargarDatos();
    }

    private void guardarModificacion()
            throws HistoriaClinicaException, AforoMaximoException, SQLException {

        CitaPaciente original = citas.get(puntero);

        String hc          = original.getHistoriaClinica(); // La HC no cambia
        String nombre      = textNombre.getText().trim();
        String apellidos   = textApellidos.getText().trim();
        String telefono    = textTelefono.getText().trim();
        String especialidad = textEspecialidad.getText().trim().toUpperCase();
        String nuevaFecha  = textFechaCita.getText().trim();

        CitaPaciente modificada = new CitaPaciente(
            hc, nombre, apellidos, telefono, especialidad, nuevaFecha, 0);

        citasController.modificar(original, modificada);

        JOptionPane.showMessageDialog(this, "✅ Cita modificada correctamente.");
        esModificacion = false;
        habilitarNavegador(true);
        habilitarPanelCita(false);
        habilitarPanelMantenimiento(true);
        cargarDatos();
    }

    // ════════════════════════════════════════════════════════════════════════
    // B O R R A R
    // ════════════════════════════════════════════════════════════════════════

    private void borrarCita() throws SQLException, HistoriaClinicaException {
        if (citas == null || citas.isEmpty()) return;

        CitaPaciente cita = citas.get(puntero);
        String mensaje = String.format(
            "¿Desea cancelar la cita?\n\nPaciente: %s %s\nFecha: %s\nEspecialidad: %s\nTurno: %d",
            cita.getNombre(), cita.getApellidos(),
            cita.getFechaCita(), cita.getEspecialidad(), cita.getNumCita());

        int respuesta = JOptionPane.showConfirmDialog(
            this, mensaje, "Confirmar cancelación", JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            citasController.eliminar(cita);
            JOptionPane.showMessageDialog(this, "✅ Cita cancelada correctamente.");
            puntero = 0;
            cargarDatos();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // F I L T R A R
    // ════════════════════════════════════════════════════════════════════════

    private void filtrar() {
        try {
            String opcion = (String) comboConsulta.getSelectedItem();
            String texto  = textFiltrar.getText().trim();
            List<CitaPaciente> resultado;

            switch (opcion) {
                case "Por especialidad":
                    if (texto.isBlank()) {
                        JOptionPane.showMessageDialog(this, "Introduzca una especialidad para filtrar.");
                        return;
                    }
                    resultado = citasController.buscarPorEspecialidad(texto);
                    break;

                case "Historial paciente":
                    if (texto.isBlank()) {
                        JOptionPane.showMessageDialog(this, "Introduzca una historia clínica para filtrar.");
                        return;
                    }
                    resultado = citasController.getAll().stream()
                        .filter(c -> c.getHistoriaClinica().contains(texto))
                        .collect(java.util.stream.Collectors.toList());
                    break;

                default: // "Todas las citas"
                    resultado = citasController.getAll();
                    break;
            }

            cargarGrid(resultado);
            if (!resultado.isEmpty()) mostrarCita(0);

        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // N A V E G A D O R
    // ════════════════════════════════════════════════════════════════════════

    private void inicio()    { if (tieneRegistros()) { puntero = 0; mostrarCita(puntero); } }
    private void ultimo()    { if (tieneRegistros()) { puntero = citas.size() - 1; mostrarCita(puntero); } }
    private void anterior()  { if (tieneRegistros() && puntero > 0)               { puntero--; mostrarCita(puntero); } }
    private void siguiente() { if (tieneRegistros() && puntero < citas.size() - 1){ puntero++; mostrarCita(puntero); } }
    private boolean tieneRegistros() { return citas != null && !citas.isEmpty(); }

    // ════════════════════════════════════════════════════════════════════════
    // U T I L I D A D E S   U I
    // ════════════════════════════════════════════════════════════════════════

    private void mostrarCita(int idx) {
        CitaPaciente c = citas.get(idx);
        textNumCita        .setText(String.valueOf(c.getNumCita()));
        textHistoriaClinica.setText(c.getHistoriaClinica());
        textNombre         .setText(c.getNombre());
        textApellidos      .setText(c.getApellidos());
        textTelefono       .setText(c.getNumTelefono());
        textEspecialidad   .setText(c.getEspecialidad());
        textFechaCita      .setText(c.getFechaCita().toString());
        tblCitas.setRowSelectionInterval(idx, idx);
    }

    private void habilitarPanelCita(boolean sw) {
        textHistoriaClinica.setEditable(sw);
        textNombre         .setEditable(sw);
        textApellidos      .setEditable(sw);
        textTelefono       .setEditable(sw);
        textEspecialidad   .setEditable(sw);
        textFechaCita      .setEditable(sw);
    }

    private void habilitarPanelMantenimiento(boolean sw) {
        btnNuevaCita.setEnabled(sw);
        btnModificar.setEnabled(sw);
        btnBorrar   .setEnabled(sw);
        btnGuardar  .setEnabled(!sw);
        btnDeshacer .setEnabled(!sw);
    }

    private void habilitarNavegador(boolean sw) {
        btnPrimero .setEnabled(sw);
        btnAtras   .setEnabled(sw);
        btnAdelante.setEnabled(sw);
        btnUltimo  .setEnabled(sw);
    }

    private void limpiarPanelCita() {
        textNumCita.setText("");
        textHistoriaClinica.setText("");
        textNombre.setText("");
        textApellidos.setText("");
        textTelefono.setText("");
        textEspecialidad.setText("");
        textFechaCita.setText("");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, "❌ Error: " + mensaje,
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Helpers de construcción UI ───────────────────────────────────────────

    private JPanel crearPanelConBorde(String titulo, int x, int y, int w, int h) {
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBorder(new TitledBorder(new LineBorder(new Color(0, 128, 0), 2),
            titulo, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 128, 0)));
        p.setBounds(x, y, w, h);
        panel.add(p);
        return p;
    }

    private JTextField agregarCampo(JPanel padre, String etiqueta,
                                    int lx, int ly, int tx, int tw, boolean editable) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl.setBounds(lx, ly, 110, 20);
        padre.add(lbl);

        JTextField tf = new JTextField();
        tf.setEditable(editable);
        tf.setBounds(tx, ly - 2, tw, 22);
        padre.add(tf);
        return tf;
    }

    private JButton crearBoton(JPanel padre, String imgRuta, String tooltip,
                                int x, int y, boolean enabled) {
        ImageIcon ico = escalar(imgRuta);
        JButton btn = new JButton("", ico);
        btn.setToolTipText(tooltip);
        btn.setBounds(x, y, 50, 50);
        btn.setEnabled(enabled);
        padre.add(btn);
        return btn;
    }

    private ImageIcon escalar(String ruta) {
        ImageIcon ico = new ImageIcon(ruta);
        return new ImageIcon(ico.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
    }
}
