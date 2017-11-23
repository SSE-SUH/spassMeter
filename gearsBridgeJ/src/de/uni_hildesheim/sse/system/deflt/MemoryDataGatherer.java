package de.uni_hildesheim.sse.system.deflt;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;

/**
 * Implements a class for requesting memory information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = {AnnotationConstants.VAR_MEMORY_DATA, 
        AnnotationConstants.VAR_OBJECT_SIZE }, op = Operation.AND)
class MemoryDataGatherer implements IMemoryDataGatherer {

    /**
     * Returns the size of the physical memory.
     * 
     * @return the size of the physical memory 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    private static native long getMemoryCapacity0();

    /**
     * Returns the size of the physical memory being currently available.
     * 
     * @return the size of the physical memory being currently available 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    private static native long getCurrentMemoryAvail0();

    /**
     * Returns the size of the physical memory being currently in use.
     * 
     * @return the size of the physical memory being currently in use 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    private static native long getCurrentMemoryUse0();

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
//    private static native void tagObject0(Object object, long tag);

    /**
     * Returns the next tag referring to a released object.
     * 
     * @return the next tag or <code>0</code> if there is none
     * 
     * @since 1.00
     */
//    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY)
//    private static native long getNextReleasedTag0();

    /**
     * Records the data about the unallocation of an object. [SPASS-meter]
     * 
     * @param allocated the allocated object
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param recId an identifier to assign the <code>size</code> to upon 
     *   unallocation (unique id)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static native void recordUnallocation0(Object allocated, long size, 
        int recId);

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
    private static native void recordUnallocation2(long tag, long size, 
        int recId);
    
    /**
     * Notifies about the actual unallocation of the external <code>tag</code>.
     * 
     * @param tag an (external) allocation tag
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static native void recordUnallocation4(long tag);

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
    private static native void receiveUnallocations0(
        IMemoryUnallocationReceiver receiver);
    
    /**
     * Defines the instance to be notified about aggregated memory unallocation
     * data.
     * 
     * @param receiver the receiver instance (may be <b>null</b> to disable 
     *   notifications)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static final native void setReceiver0(
        IMemoryUnallocationReceiver receiver);
    
    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method should (!) return the same value as in 
     * <code>java.lang.instrument.Instrumentation</code>.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "0")
    private static native long getObjectSize0(Object object);

    /**
     * Returns the size of the physical memory.
     * 
     * @return the size of the physical memory 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    public long getMemoryCapacity() {
        return getMemoryCapacity0(); 
    }

    /**
     * Returns the size of the physical memory being currently available.
     * 
     * @return the size of the physical memory being currently available 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    public long getCurrentMemoryAvail() {
        return getCurrentMemoryAvail0();
    }

    /**
     * Returns the size of the physical memory being currently in use.
     * 
     * @return the size of the physical memory being currently in use 
     *         (in bytes, negative or zero if invalid)
     * 
     * @since 1.00
     */
    public long getCurrentMemoryUse() {
        return getCurrentMemoryUse0();
    }

    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method should (!) return the same value as in 
     * <code>java.lang.instrument.Instrumentation</code>.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_OBJECT_SIZE, value = "0")
    public long getObjectSize(Object object) {
        return getObjectSize0(object); 
    }

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
//    public void tagObject(Object object, long tag) {
//        tagObject0(object, tag);
//    }

    /**
     * Returns the next tag referring to a released object.
     * 
     * @return the next tag or <code>0</code> if there is none
     * 
     * @since 1.00
     */
//    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY)
//    public long getNextReleasedTag() {
//        return getNextReleasedTag0();
//    }
    
    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocation(Object allocated, long size, int recId) {
        //recordUnallocation0(allocated, size, recId);
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag, long size, int recId) {
        recordUnallocation2(tag, size, recId);
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag) {
        recordUnallocation4(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void receiveUnallocations(IMemoryUnallocationReceiver receiver) {
        receiveUnallocations0(receiver);
    }

}
