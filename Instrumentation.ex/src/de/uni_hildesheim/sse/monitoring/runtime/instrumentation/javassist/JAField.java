package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import javassist.CtField;
import javassist.NotFoundException;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IField;

/**
 * Implements a field.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JAField extends JAMember implements IField {

    /**
     * Stores the field.
     */
    private CtField field;
    
    /**
     * Returns the signature of this member.
     * 
     * @return the signature
     * 
     * @since 1.00
     */
    @Override
    public String getSignature() {
        return getSignature(field);
    }
    
    /**
     * Returns the signature of the given field.
     * 
     * @param field the field to return the signature for
     * @return the signature
     * 
     * @since 1.00
     */
    static String getSignature(CtField field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    /**
     * Returns the name of the type of this field.
     * 
     * @return the name of the type
     * 
     * @throws InstrumenterException in case of errors
     * @since 1.00
     */
    @Override
    public String getTypeName() throws InstrumenterException {
        try {
            return field.getType().getName();
        } catch (NotFoundException e) {
            Utils.warn(e);
            throw new InstrumenterException(e, true);
        }
    }
    
    /**
     * Attaches a javassist field.
     * 
     * @param field the javassist field
     * 
     * @since 1.00
     */
    void attach(CtField field) {
        super.attach(field);
        this.field = field;
    }
    
    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        field = null;
        super.release();
        JAClass.releaseField(this);
    }

}
