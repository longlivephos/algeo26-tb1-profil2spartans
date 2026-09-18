package algeo.core.matrix;

public class MatrixException extends Exception {
    public MatrixException(String message) {
        super(message);
    }
    public MatrixException(String message, Throwable cause) {
        super(message, cause);
    }
}