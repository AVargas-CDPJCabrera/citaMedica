package excepciones;

public class AforoMaximoException extends Exception {
    public AforoMaximoException() {
        super("Aforo máximo alcanzado: esta especialidad no admite más citas para ese día (máximo 10).");
    }
    public AforoMaximoException(String mensaje) {
        super(mensaje);
    }
}
