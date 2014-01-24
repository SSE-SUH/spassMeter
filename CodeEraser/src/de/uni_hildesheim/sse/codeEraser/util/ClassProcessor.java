package de.uni_hildesheim.sse.codeEraser.util;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * An interface for on-the-fly processing of classes, i.e. classes are loaded,
 * processed and written back.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ClassProcessor {

    /**
     * Returns if the specified class should be processed.
     * 
     * @param name the name of the class
     * @return <code>true</code> if the class should be processed, 
     *     <code>false</code> if not but written to the output JAR, 
     *     if <b>null</b> the class should not be processed and not be written
     *     to the output JAR
     * 
     * @since 1.00
     */
    public Boolean doProcess(String name);
    
    /**
     * Processes the specified class.
     * 
     * @param cl the class to be processed
     * @throws CannotCompileException in case that the new code or the code
     *   modifications cannot be compiled
     * @throws NotFoundException in case that Java elements cannot be found
     * 
     * @since 1.00
     */
    public void process(CtClass cl) 
        throws CannotCompileException, NotFoundException;
    
}
