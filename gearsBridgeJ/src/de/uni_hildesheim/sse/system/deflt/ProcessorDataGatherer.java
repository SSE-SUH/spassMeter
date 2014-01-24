package de.uni_hildesheim.sse.system.deflt;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * Implements a class for requesting processor information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class ProcessorDataGatherer implements IProcessorDataGatherer {

    /**
     * Returns the number of processors.
     * 
     * @return the number of processors 
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    private static native int getNumberOfProcessors0();

    /**
     * Returns the maximum physical speed of the processor(s).
     * 
     * @return the maximum physical speed of the processor(s) in Hertz
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    private static native int getMaxProcessorSpeed0();

    /**
     * Returns the (average) physical speed of the processor(s).
     * 
     * @return the (average) physical speed of the processor(s) in Hertz
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    private static native int getCurrentProcessorSpeed0();

    /**
     * Returns the the current estimated CPU load.
     * 
     * @return the current load in percent (0-100)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public static native double getCurrentSystemLoad0();
    
    /**
     * Returns the number of processors.
     * 
     * @return the number of processors 
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public int getNumberOfProcessors() {
        return getNumberOfProcessors0();
    }

    /**
     * Returns the maximum physical speed of the processor(s).
     * 
     * @return the maximum physical speed of the processor(s) in Hertz
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public int getMaxProcessorSpeed() {
        return getMaxProcessorSpeed0();
    }

    /**
     * Returns the (average) physical speed of the processor(s).
     * 
     * @return the (average) physical speed of the processor(s) in Hertz
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public int getCurrentProcessorSpeed() {
        return getCurrentProcessorSpeed0();
    }

    /**
     * Returns the the current estimated CPU load.
     * 
     * @return the current load in percent (0-100)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public synchronized double getCurrentSystemLoad() {
        return getCurrentSystemLoad0();
    }
    
}
