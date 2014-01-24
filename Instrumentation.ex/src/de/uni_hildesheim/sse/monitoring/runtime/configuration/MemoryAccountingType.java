package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import static de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MemoryAccountingConstants.*;

/**
 * Defines strategy types for accounting memory.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum MemoryAccountingType {

    /**
     * No memory accounting.
     */
    NONE(0),
    
    /**
     * Instrumented objects only including unallocation.
     */
    CONSTRUCTION_UNALLOCATION(AT_CONSTRUCTOR | AT_FINALIZER),

    /**
     * Instrumented objects only including native unallocation.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE_NATIVE)
    CONSTRUCTION_NATIVEUNALLOCATION(AT_CONSTRUCTOR | NATIVE_UNALLOCATION),

    /**
     * Instrumented objects only excluding unallocation.
     */
    CONSTRUCTION(AT_CONSTRUCTOR),
    
    /**
     * Instrumented objects with array creations including unallocation.
     */
    //CONSTRUCTION_UNALLOCATION_ARRAYS(AT_CONSTRUCTOR | AT_FINALIZER 
    //    | AT_ARRAY_CREATION), // leads to memory overflow

    /**
     * Instrumented objects with array creations including native unallocation.
     * Excludes uninstrumented objects from allocation recording.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE_NATIVE)
    CONSTRUCTION_NATIVEUNALLOCATION_ARRAYS(AT_CONSTRUCTOR | NATIVE_UNALLOCATION
        | AT_ARRAY_CREATION),

    /**
     * Instrumented objects with array creations excluding unallocation.
     */
    CONSTRUCTION_ARRAYS(AT_CONSTRUCTOR | AT_ARRAY_CREATION),
        
    /**
     * Based on object creations including unallocation. May include memory 
     * allocations of unknown (uninstrumented) objects or unallocation of not 
     * previously recorded instrumented objects.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    CREATION_UNALLOCATION(AT_OBJECT_CREATION | AT_FINALIZER),

    /**
     * Based on object creations including native unallocation.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE_NATIVE)
    CREATION_NATIVEUNALLOCATION(AT_OBJECT_CREATION | NATIVE_UNALLOCATION),

    /**
     * Based on object creations excluding unallocation.
     */
    CREATION(AT_OBJECT_CREATION),
    
    /**
     * Based on object creations with array creations including unallocation.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    CREATION_UNALLOCATION_ARRAYS (AT_OBJECT_CREATION | AT_FINALIZER 
        | AT_ARRAY_CREATION),

    /**
     * Based on object creations with array creations including native 
     * unallocation.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE_NATIVE)
    CREATION_NATIVEUNALLOCATION_ARRAYS (AT_OBJECT_CREATION 
        | NATIVE_UNALLOCATION | AT_ARRAY_CREATION),

    /**
     * Based on object creations with array creations excluding unallocation.
     */
    CREATION_ARRAYS (AT_OBJECT_CREATION | AT_ARRAY_CREATION);
    
    /**
     * Stores the flags.
     */
    private int flags;
    
    /**
     * Creates a new memory account type constant.
     * 
     * @param flags a combination of the flags in 
     *     {@link MemoryAccountingConstants}
     * 
     * @since 1.00
     */
    private MemoryAccountingType(int flags) {
        this.flags = flags;
    }

    /**
     * Returns whether instrumentation should happen in object constructors.
     * 
     * @return <code>true</code> if instrumentation should in object 
     *     constructors, <code>false</code> else
     */
    public boolean atConstructor() {
        return Flags.isSet(flags, AT_CONSTRUCTOR);
    }

    /**
     * Returns whether instrumentation should happen in object finalizers.
     * 
     * @return <code>true</code> if instrumentation should happen in finalizers,
     *     <code>false</code> else
     */
    public boolean atFinalizer() {
        return Flags.isSet(flags, AT_FINALIZER);
    }

    /**
     * Returns whether instrumentation should happen at array creations.
     * 
     * @return <code>true</code> if instrumentation should happen at array 
     *     creations, <code>false</code> else
     */
    public boolean atArrayCreation() {
        return Flags.isSet(flags, AT_ARRAY_CREATION);
    }

    /**
     * Returns whether instrumentation should happen at object creations.
     * 
     * @return <code>true</code> if instrumentation should happen at object 
     *     creations, <code>false</code> else
     */
    public boolean atObjectCreation() {
        return Flags.isSet(flags, AT_OBJECT_CREATION);
    }    
    
    /**
     * Returns whether unallocation should happen in native code.
     * 
     * @return <code>true</code> if unallocation should happen in native code, 
     *     <code>false</code> else
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE_NATIVE, 
        value = "false")
    public boolean nativeUnallocation() {
        return Flags.isSet(flags, NATIVE_UNALLOCATION);
    }

    /**
     * Returns whether unallocation should be accounted at all.
     * 
     * @return <code>true</code> if unallocation should be accounted, 
     *     <code>false</code> else
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE, value = "false")
    public boolean considerUnallocation() {
        return Flags.isSet(flags, NATIVE_UNALLOCATION) 
            || Flags.isSet(flags, AT_FINALIZER); 
    }
    
}
