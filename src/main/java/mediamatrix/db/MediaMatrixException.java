package mediamatrix.db;

public class MediaMatrixException extends Exception {

    private static final long serialVersionUID = 1L;

    public MediaMatrixException() {
    }

    public MediaMatrixException(String msg) {
        super(msg);
    }

    public MediaMatrixException(Throwable cause) {
        super(cause);
    }

    public MediaMatrixException(String message, Throwable cause) {
        super(message, cause);
    }
}
