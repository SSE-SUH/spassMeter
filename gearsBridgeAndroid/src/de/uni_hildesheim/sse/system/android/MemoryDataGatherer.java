package de.uni_hildesheim.sse.system.android;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;
import de.uni_hildesheim.sse.system.ObjectSizeEstimator;
//import android.app.ActivityManager;
//import android.app.ActivityManager.MemoryInfo;
import android.content.Context;

public class MemoryDataGatherer implements IMemoryDataGatherer {

	private Context context = null;

	/**
	 * Returns the size of the physical memory.
	 * 
	 * @return the size of the physical memory (in bytes, negative or zero if
	 *         invalid)
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
	 * @return the size of the physical memory being currently in use (in bytes,
	 *         negative or zero if invalid)
	 * 
	 * @since 1.00
	 */
	@Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
	private static native long getCurrentMemoryUse0();

    /**
     * Records the data about the unallocation of an object. [SPASS-meter]
     * 
     * @param allocated the allocated object
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param recId an identifier to assign the <code>size</code> to upon 
     *   unallocation
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static native void recordUnallocation0(Object allocated, long size, 
        String recId);

    /**
     * Records the data about the unallocation of an object. [SPASS-meter]
     * 
     * @param allocated the allocated object
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param count the number of elements to be considered in 
     *   <code>recId</code>
     * @param recId identifiers to assign the <code>size</code> to upon 
     *   unallocation
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static native void recordUnallocation1(Object allocated, long size, 
        int count, String[] recId);
    
    /**
     * Records the data about the unallocation of an external tag. [SPASS-meter]
     * 
     * @param tag an (external) allocation tag
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param recId an identifier to assign the <code>size</code> to upon 
     *   unallocation
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static native void recordUnallocation2(long tag, long size, 
        String recId);

    /**
     * Records the data about the unallocation of an external tag. [SPASS-meter]
     * 
     * @param tag an (external) allocation tag
     * @param size the size of the allocation (if called again this is treated
     *   as an increment/decrement)
     * @param count the number of elements to be considered in 
     *   <code>recId</code>
     * @param recId identifiers to assign the <code>size</code> to upon 
     *   unallocation
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static native void recordUnallocation3(long tag, long size, 
        int count, String[] recId);
    
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
     * {@link IMemoryUnallocationReceiver#unallocated(String, long)} is called
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
	 * Returns the size of the physical memory being currently available.
	 * 
	 * @return the size of the physical memory being currently available (in
	 *         bytes, negative or zero if invalid)
	 */
	@Override
	public long getCurrentMemoryAvail() {
	    return getCurrentMemoryAvail0();
	    // does not work with system values
		/*MemoryInfo mi = null;
		if (context != null) {
			mi = new MemoryInfo();
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			activityManager.getMemoryInfo(mi);
		}

		if (mi != null) {
			return mi.availMem;
		} else {
			return -1;
		}*/
	}

	/**
	 * Returns the size of the physical memory being currently in use.
	 * 
	 * @return the size of the physical memory being currently in use (in bytes,
	 *         negative or zero if invalid)
	 */
	@Override
	public long getCurrentMemoryUse() {
		return getCurrentMemoryUse0();
	}

	/**
	 * Returns the size of the physical memory.
	 * 
	 * @return the size of the physical memory (in bytes, negative or zero if
	 *         invalid)
	 */
	@Override
	public long getMemoryCapacity() {
		return getMemoryCapacity0();
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
    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY)
    public void tagObject(Object object, long tag) {
        // no JVMTI in Android!
    }

    /**
     * Returns the next tag referring to a released object.
     * 
     * @return the next tag or <code>0</code> if there is none (<code>0</code>
     *     by default)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY)
    public long getNextReleasedTag() {
        // no JVMTI in Android!
        return 0;
    }

	
	// TODO Java-Doc hinzufügen
	@Override
	public long getObjectSize(Object arg0) {
		return ObjectSizeEstimator.getObjectSize(arg0);
	}

	/**
	 * Function for setting context.
	 * 
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
	}
	
    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocation(Object allocated, long size, String recId) {
        recordUnallocation0(allocated, size, recId);
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocation(Object allocated, long size, int count, 
        String... recId) {
        recordUnallocation1(allocated, size, count, recId);
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag, long size, String recId) {
        recordUnallocation2(tag, size, recId);
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag, long size, int count, 
        String... recId) {
        recordUnallocation3(tag, size, count, recId);
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
