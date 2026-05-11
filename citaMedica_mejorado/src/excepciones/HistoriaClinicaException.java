package excepciones;

public class HistoriaClinicaException extends Exception {
    public HistoriaClinicaException() {
        super("Código de historia clínica inválido: debe tener 13 dígitos numéricos con dígito de control correcto.");
    }
    public HistoriaClinicaException(String mensaje) {
        super(mensaje);
    }
}
