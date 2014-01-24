package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines bit flags for memory accounting strategies.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class MemoryAccountingConstants {
    
    /**
     * The bit flag for instrumenting at constructors.
     */
    static final int AT_CONSTRUCTOR = 1 << 0;

    /**
     * The bit flag for instrumenting at object creation.
     */
    static final int AT_OBJECT_CREATION = 1 << 1;
    
    /**
     * The bit flag for instrumenting at finalizers.
     */
    static final int AT_FINALIZER = 1 << 2;
    
    /**
     * The bit flag for instrumenting at array creations.
     */
    static final int AT_ARRAY_CREATION = 1 << 3;

    /**
     * Indicates native unallocation.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE_NATIVE)
    static final int NATIVE_UNALLOCATION = 1 << 4;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * 
     * @since 1.00
     */
    private MemoryAccountingConstants() {
    }
 
}
