package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;
import de.uni_hildesheim.sse.system.IDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;
import de.uni_hildesheim.sse.system.IProcessDataGatherer;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;

/**
 * Implements the gatherer factory for this fallback implementation.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
public class FallbackGathererFactory 
    extends de.uni_hildesheim.sse.system.GathererFactory {

    /**
     * Is called in case that a (new) context is available. Does nothing
     * as the context is ignored in the default implementation.
     * 
     * @param context operating system specific object, may be <b>null</b>
     * 
     * @since 1.00
     */
    public void setContext(Object context) {
    }
    
    /**
     * Provides the gatherer and operating system specific initialization
     * of the variables in this class.
     * 
     * @since 1.00
     */
    public void initialize() {
    }
    
    /**
     * Creates the battery data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
    protected IBatteryDataGatherer createBatteryDataGatherer() {
        return new BatteryDataGatherer();
    }
    
    /**
     * Creates the volume data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    protected IVolumeDataGatherer createVolumeDataGatherer() {
        return new VolumeDataGatherer();
    }
    
    /**
     * Creates the "this process" data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    protected IThisProcessDataGatherer createThisProcessDataGatherer() {
        return new ThisProcessDataGatherer();
    }

    /**
     * Creates the process data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    protected IProcessDataGatherer createProcessDataGatherer() {
        return new ProcessDataGatherer();
    }

    /**
     * Creates the screen data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    protected IScreenDataGatherer createScreenDataGatherer() {
        return new ScreenDataGatherer();
    }

    /**
     * Creates the processor data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA)
    protected IProcessorDataGatherer createProcessorDataGatherer() {
        return new ProcessorDataGatherer();
    }

    /**
     * Creates the network data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    protected INetworkDataGatherer createNetworkDataGatherer() {
        return new NetworkDataGatherer();
    }

    /**
     * Creates the memory data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    protected IMemoryDataGatherer createMemoryDataGatherer() {
        return new MemoryDataGatherer();
    }

    /**
     * Creates the general data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = {AnnotationConstants.VAR_GATHER_DATA, 
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)    
    protected IDataGatherer createDataGatherer() {
        return new DataGatherer();
    }

    /**
     * Creates the thread data gatherer. 
     * 
     * @return the gatherer instance
     */
    protected IThreadDataGatherer createThreadDataGatherer() {
        return new ThreadDataGatherer();
    }

}
