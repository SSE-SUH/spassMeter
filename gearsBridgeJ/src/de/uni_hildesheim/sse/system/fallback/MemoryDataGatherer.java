package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;
import de.uni_hildesheim.sse.system.ObjectSizeEstimator;

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

    @Override
    public long getMemoryCapacity() {
        return -1; 
    }

    @Override
    public long getCurrentMemoryAvail() {
        return -1;
    }

    @Override
    public long getCurrentMemoryUse() {
        return -1;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_OBJECT_SIZE, value = "0")
    public long getObjectSize(Object object) {
        return ObjectSizeEstimator.getObjectSize(object);
    }
    
    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocation(Object allocated, long size, int recId) {
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag, long size, int recId) {
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void recordUnallocationByTag(long tag) {
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public void receiveUnallocations(IMemoryUnallocationReceiver receiver) {
    }

}
