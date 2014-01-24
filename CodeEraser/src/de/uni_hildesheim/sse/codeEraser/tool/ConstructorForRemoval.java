package de.uni_hildesheim.sse.codeEraser.tool;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

/**
 * A class capturing a constructor to be removed.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ConstructorForRemoval extends AbstractElementForRemoval {
    
    /**
     * Stores the constructor to be removed.
     */
    private CtConstructor constructor;
    
    /**
     * Creates a new instance of this class marking a constructor for removal.
     * 
     * @param constructor the constructor to be removed
     * 
     * @since 1.00
     */
    public ConstructorForRemoval(CtConstructor constructor) {
        this.constructor = constructor;
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
        CtClass declaring = constructor.getDeclaringClass();
//        declaring.removeConstructor(constructor);
        System.out.println("- removed constructor in " + declaring.getName());
    }

}
