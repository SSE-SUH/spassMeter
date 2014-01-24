package de.uni_hildesheim.sse.codeEraser.tool;

import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

/**
 * A class capturing a field to be removed.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class FieldForRemoval extends AbstractElementForRemoval {
    
    /**
     * Stores the field to be removed.
     */
    private CtField field;
    
    /**
     * Creates a new instance of this class marking a field for removal.
     * 
     * @param field the field to be removed
     * 
     * @since 1.00
     */
    public FieldForRemoval(CtField field) {
        this.field = field;
    }
    
    /**
     * Remove this element from byte code.
     * 
     * @throws NotFoundException thrown if the element (or any related element) 
     *     was not found
     * 
     * @since 1.00
     */
    public void remove() throws NotFoundException {
        CtClass declaring = field.getDeclaringClass();
        declaring.removeField(field);
        System.out.println("- removed field " + declaring.getName() + "." 
            + field.getName());
    }

}
