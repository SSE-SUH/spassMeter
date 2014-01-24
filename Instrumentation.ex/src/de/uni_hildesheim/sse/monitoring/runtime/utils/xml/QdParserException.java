package de.uni_hildesheim.sse.monitoring.runtime.utils.xml;

/**
 * A specific parser exception.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class QdParserException extends Exception {
    
    /**
     * An unique ID for serializing this class.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = 7036390964929178470L;

    /**
     * Creates a new parser exception for a message.
     * 
     * @param message the message describing the problem
     * 
     * @since 1.00
     */
    public QdParserException(String message) {
        super(message);
    }

    /**
     * Creates a new parser exception for a caught cause.
     * 
     * @param cause a throwable representing the cause
     * 
     * @since 1.00
     */
    public QdParserException(Throwable cause) {
        super(cause);
    }

}
