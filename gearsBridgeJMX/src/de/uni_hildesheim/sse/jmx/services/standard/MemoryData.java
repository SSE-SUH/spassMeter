package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IMemoryDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
public class MemoryData implements MemoryDataMBean {

    /**
     * Instance of {@link IMemoryDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private IMemoryDataGatherer memoryDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public MemoryData() {
        memoryDataGatherer = GathererFactory.getMemoryDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getMemoryCapacity() {
        return memoryDataGatherer.getMemoryCapacity();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryAvail() {
        return memoryDataGatherer.getCurrentMemoryAvail();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryUse() {
        return memoryDataGatherer.getCurrentMemoryUse();
    }

    @Override
    public long getObjectSize(Object object) {
        return memoryDataGatherer.getObjectSize(object);
    }

    @Override
    public void receiveUnallocations(IMemoryUnallocationReceiver arg0) {
        memoryDataGatherer.receiveUnallocations(arg0);
    }

    @Override
    public void recordUnallocation(Object arg0, long arg1, String arg2) {
        memoryDataGatherer.recordUnallocation(arg0, arg1, arg2);
    }

    @Override
    public void recordUnallocation(Object arg0, long arg1, int arg2,
        String... arg3) {
        memoryDataGatherer.recordUnallocation(arg0, arg1, arg2, arg3);
    }

    @Override
    public void recordUnallocationByTag(long arg0) {
        memoryDataGatherer.recordUnallocationByTag(arg0);
    }

    @Override
    public void recordUnallocationByTag(long arg0, long arg1, String arg2) {
        memoryDataGatherer.recordUnallocationByTag(arg0, arg1, arg2);
    }

    @Override
    public void recordUnallocationByTag(long arg0, long arg1, int arg2,
        String... arg3) {
        memoryDataGatherer.recordUnallocationByTag(arg0, arg1, arg2, arg3);
    }

}
