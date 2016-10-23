package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * Implements a class for requesting processor information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
class ProcessorDataGatherer implements IProcessorDataGatherer {

    @Override
    public int getNumberOfProcessors() {
        return -1;
    }

    @Override
    public int getMaxProcessorSpeed() {
        return -1;
    }

    @Override
    public int getCurrentProcessorSpeed() {
        return -1;
    }

    @Override
    public synchronized double getCurrentSystemLoad() {
        return -1;
    }
    
}
