package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting processor information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IProcessorDataGatherer {

    /**
     * Returns the number of processors.
     * 
     * @return the number of processors 
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getNumberOfProcessors();

    /**
     * Returns the maximum physical speed of the processor(s).
     * 
     * @return the maximum physical speed of the processor(s) in Hertz
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getMaxProcessorSpeed();

    /**
     * Returns the (average) physical speed of the processor(s).
     * 
     * @return the (average) physical speed of the processor(s) in Hertz
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getCurrentProcessorSpeed();

    /**
     * Returns the the current estimated CPU load.
     * 
     * @return the current load in percent (0-100)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public double getCurrentSystemLoad();

}
