package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

/**
 * Defines an instrumentation exception. 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InstrumenterException extends Exception {
    
    /**
     * Defines the id for serialization.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = -2853743807042651200L;

    /**
     * Stores whether this instrumentation has already been handled.
     */
    private boolean handled = false;
    
    /**
     * Creates an instrumenter exception by delegation. This exception is 
     * not marked as already being handled.
     * 
     * @param throwable the original throwable
     * 
     * @since 1.00
     */
    public InstrumenterException(Throwable throwable) {
        this(throwable, false);
    }
    
    /**
     * Creates an instrumenter exception by delegation. 
     * 
     * @param throwable the original throwable
     * @param handled whether this exception shall be marked as already handled
     * 
     * @since 1.00
     */
    public InstrumenterException(Throwable throwable, boolean handled) {
        super(throwable.getMessage(), throwable);
        this.handled = handled;
    }


    /**
     * Creates an instrumenter exception by delegation.
     * 
     * @param msgPrefix a prefix to be prepended before the message of the 
     *     original throwable
     * @param throwable the original throwable
     * 
     * @since 1.00
     */
    public InstrumenterException(String msgPrefix, Throwable throwable) {
        super(msgPrefix + throwable.getMessage(), throwable);
    }
    
    /**
     * Creates an instrumenter exception.
     * 
     * @param message the message of the exception
     * 
     * @since 1.00
     */
    public InstrumenterException(String message) {
        super(message);
    }
    
    /**
     * Returns whether this exception shall be considered as already being 
     * handled.
     * 
     * @return <code>true</code> if it shall be considered as already being
     *   handled (and it shall be consumed), <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isHandled() {
        return handled;
    }
    
}
