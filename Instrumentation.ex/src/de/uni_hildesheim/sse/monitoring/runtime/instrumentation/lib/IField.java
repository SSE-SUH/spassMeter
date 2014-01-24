package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

/**
 * Defines a bytecode abstraction of an attribute.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IField extends IMember {

    /**
     * Returns the name of the type of this field.
     * 
     * @return the name of the type
     * 
     * @throws InstrumenterException in case of errors
     * @since 1.00
     */
    public String getTypeName() throws InstrumenterException;
    
}
