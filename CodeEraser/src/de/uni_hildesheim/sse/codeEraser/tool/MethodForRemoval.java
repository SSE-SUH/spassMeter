package de.uni_hildesheim.sse.codeEraser.tool;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * A class capturing a method to be removed.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class MethodForRemoval extends AbstractElementForRemoval {
    
    /**
     * Stores the method to be removed.
     */
    private CtMethod method;
    
    /**
     * Creates a new instance of this class marking a method for removal.
     * 
     * @param method the method to be removed
     * 
     * @since 1.00
     */
    public MethodForRemoval(CtMethod method) {
        this.method = method;
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
        CtClass declaring = method.getDeclaringClass();
        declaring.removeMethod(method);
        System.out.println("- removed method " + declaring.getName()
            + "." + method.getName());
    }

}
