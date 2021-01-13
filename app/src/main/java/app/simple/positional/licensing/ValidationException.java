package app.simple.positional.licensing;

/**
 * Indicates that an error occurred while validating the integrity of data managed by an
 * {@link Obfuscator}.}
 */
public class ValidationException extends Exception {
    public ValidationException(String s) {
        super(s);
    }
    
    private static final long serialVersionUID = 1L;
}
