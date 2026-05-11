package excepciones;

public class CamposVaciosException extends Exception {
    public CamposVaciosException() {
        super("Hay campos obligatorios vacíos. Por favor, rellene todos los campos.");
    }
    public CamposVaciosException(String mensaje) {
        super(mensaje);
    }
}
