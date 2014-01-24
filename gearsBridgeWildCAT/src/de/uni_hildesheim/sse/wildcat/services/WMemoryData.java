package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.MemoryDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IMemoryDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
public class WMemoryData extends AbstractWServiceData 
    implements IMemoryDataGatherer {

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
    public WMemoryData() {
        memoryDataGatherer = GathererFactory.getMemoryDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getMemoryCapacity() {
        long result = memoryDataGatherer.getMemoryCapacity();
        setSensorValue(MemoryDataConstants.MEMORY_CAPACITY, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryAvail() {
        long result = memoryDataGatherer.getCurrentMemoryAvail();
        setSensorValue(MemoryDataConstants.CURRENT_MEMORY_AVAIL, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryUse() {
        long result = memoryDataGatherer.getCurrentMemoryUse();
        setSensorValue(MemoryDataConstants.CURRENT_MEMORY_USE, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY)
    public void tagObject(Object object, long tag) {
        memoryDataGatherer.tagObject(object, tag);
    }

    @Override
    @Variability(id = AnnotationConstants.WITH_JVMTI_MEMORY, value = "0")
    public long getNextReleasedTag() {
        return memoryDataGatherer.getNextReleasedTag();
    }

    @Override
    public long getObjectSize(Object object) {
        return memoryDataGatherer.getObjectSize(object);
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getMemoryCapacity();
        getCurrentMemoryAvail();
        getCurrentMemoryUse();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(MemoryDataConstants.MEMORY_CAPACITY)) {
            return getMemoryCapacity();
        } else if (attributeName.equals(MemoryDataConstants.
                CURRENT_MEMORY_AVAIL)) {
            return getCurrentMemoryAvail();
        } else if (attributeName.equals(MemoryDataConstants.
                CURRENT_MEMORY_USE)) {
            return getCurrentMemoryUse();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
