package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting memory information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
public interface IMemoryDataGatherer extends IObjectSizeDataGatherer {

    /**
     * Returns the size of the physical memory.
     * 
     * @return the size of the physical memory 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getMemoryCapacity();

    /**
     * Returns the size of the physical memory being currently available.
     * 
     * @return the size of the physical memory being currently available 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryAvail();

    /**
     * Returns the size of the physical memory being currently in use.
     * 
     * @return the size of the physical memory being currently in use 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryUse();
    
    /**
     * Tag the specified object with the given long value for receiving
     * memory unallocation information.
     * 
     * @param object the object to be tagged
     * @param tag the tag (do <b>not</b> use <code>0</code>)
     * 
     * @since 1.00
     */
//    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY)
//    public void tagObject(Object object, long tag);

    /**
     * Returns the next tag referring to a released object. This method
     * does not return the size of the object. It is assumed that the calling
     * program maintains a mapping between tags and objects.
     * 
     * @return the next tag or <code>0</code> if there is none at the moment
     * 
     * @since 1.00
     */
//    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY, value = "0")
//    public long getNextReleasedTag();

    /**
     * Records the data about the unallocation of an object. [SPASS-meter]
     * 
     * @param allocated the allocated object
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param recId an identifier to assign the <code>size</code> to upon 
     *   unallocation (as unique int)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocation(Object allocated, long size, int recId);

    /**
     * Records the data about the unallocation of an external tag. [SPASS-meter]
     * 
     * @param tag an (external) allocation tag
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param recId an identifier to assign the <code>size</code> to upon 
     *   unallocation (unique id)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag, long size, int recId);

    /**
     * Notifies about the actual unallocation of the external <code>tag</code>.
     * 
     * @param tag an (external) allocation tag
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag);

    /**
     * Receives information about unallocations. 
     * {@link IMemoryUnallocationReceiver#unallocated(int, long)} is called
     * for each stored recording identification.
     * 
     * @param receiver the receiver instance
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void receiveUnallocations(IMemoryUnallocationReceiver receiver);
    
}