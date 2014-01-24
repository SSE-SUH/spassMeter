package de.uni_hildesheim.sse.codeEraser.tool;

import javassist.NotFoundException;

/**
 * Defines an element selected for removal. The original rationale for
 * this interface and the classes was that removals may require to take into
 * account the dependencies of the elements. In fact, removals work also without
 * so these instances allow polymorphic removal of Java elements.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
interface ElementForRemoval {
    
    /**
     * Remove this element from byte code.
     * 
     * @throws NotFoundException thrown if the element (or any related element) 
     *     was not found
     * 
     * @since 1.00
     */
    public void remove() throws NotFoundException;

}
