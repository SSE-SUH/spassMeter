package de.uni_hildesheim.sse.system;

import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.fallback.FallbackGathererFactory;

/**
 * Defines a factory for obtaining gatherer instances. Each gatherer
 * (defined by interfaces) can be obtained through individual methods.
 * A concrete implementation needs to implement this class and (so far) 
 * be called via reflection from here. Subclasses are responsible
 * for initializing the (native) library if required and for returning
 * proper instances of their classes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public abstract class GathererFactory {

    /**
     * Stores the JVM property to signal that the native library
     * was loaded. The assigned value is not considered in this 
     * library. It might be the physical location in the file system.
     */
    public static final String PROPERTY_EXTERNAL_LIB = "locutor.library";
    
    /**
     * Additional name infix to separate the native library across 
     * various JVMs.
     */
    public static final String PROPERTY_INFIX = "locutor.infix";
    
    /**
     * Forces individual libraries to support usage across various JVMs
     * on some operating systems. Takes precedence over {@link #PROPERTY_INFIX}.
     */
    public static final String PROPERTY_OWNINSTANCE = "locutor.ownInstance";
    
    /**
     * Stores the factories to be considered for initialization. Processed
     * in given sequence as long as there are no further factories and
     * none could be initialized.
     */
    private static String[] factories = {
        "de.uni_hildesheim.sse.system.deflt.DefaultGathererFactory",
        "de.uni_hildesheim.sse.system.android.GathererFactory"};
    
    /**
     * Stores the singleton instance.
     */
    private static GathererFactory instance;
    
    /**
     * Stores the battery data gatherer singleton instance.
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
    private static IBatteryDataGatherer batteryDataGatherer;

    /**
     * Stores the volume gatherer instance.
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    private static IVolumeDataGatherer volumeDataGatherer;

    /**
     * Stores the "this process" instance.
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    private static IThisProcessDataGatherer thisProcessDataGatherer; 

    /**
     * Stores the process gatherer instance.
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    private static IProcessDataGatherer processDataGatherer; 
    
    /**
     * Stores the screen gatherer instance.
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    private static IScreenDataGatherer screenDataGatherer; 

    /**
     * Stores the processor gatherer instance.
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA)
    private static IProcessorDataGatherer processorDataGatherer; 

    /**
     * Stores the network gatherer instance.
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    private static INetworkDataGatherer networkDataGatherer; 

    /**
     * Stores the memory gatherer instance.
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private static IMemoryDataGatherer memoryDataGatherer; 

    /**
     * Stores the thread gatherer instance.
     */
    private static IThreadDataGatherer threadDataGatherer;
    
    /**
     * Stores the general gatherer instance.
     */
    @Variability(id = {AnnotationConstants.VAR_GATHER_DATA, 
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)    
    private static IDataGatherer dataGatherer; 

    
    /**
     * Returns the current battery data gatherer.
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
    public static final IBatteryDataGatherer getBatteryDataGatherer() {
        if (null == batteryDataGatherer) {
            batteryDataGatherer = instance.createBatteryDataGatherer();
        }
        return batteryDataGatherer;
    }

    /**
     * Creates the battery data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
    protected abstract IBatteryDataGatherer createBatteryDataGatherer();


    /**
     * Returns the volume data gatherer instance.
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    public static final IVolumeDataGatherer getVolumeDataGatherer() {
        if (null == volumeDataGatherer) {
            volumeDataGatherer = instance.createVolumeDataGatherer();
        }
        return volumeDataGatherer;
    }

    /**
     * Creates the volume data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    protected abstract IVolumeDataGatherer createVolumeDataGatherer();
    
    
    /**
     * Returns the "this process" gatherer instance.
     * 
     * @return the "this process" gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    public static final IThisProcessDataGatherer getThisProcessDataGatherer() {
        if (null == thisProcessDataGatherer) {
            thisProcessDataGatherer = instance.createThisProcessDataGatherer();
        }
        return thisProcessDataGatherer;
    }

    /**
     * Creates the "this process" data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    protected abstract IThisProcessDataGatherer createThisProcessDataGatherer();
    

    /**
     * Returns the process gatherer instance.
     * 
     * @return the process gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    public static final IProcessDataGatherer getProcessDataGatherer() {
        if (null == processDataGatherer) {
            processDataGatherer = instance.createProcessDataGatherer();
        }
        return processDataGatherer;
    }

    /**
     * Creates the process data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    protected abstract IProcessDataGatherer createProcessDataGatherer();


    /**
     * Returns the screen gatherer instance.
     * 
     * @return the screen gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    public static final IScreenDataGatherer getScreenDataGatherer() {
        if (null == screenDataGatherer) {
            screenDataGatherer = instance.createScreenDataGatherer();
        }
        return screenDataGatherer;
    }

    /**
     * Creates the screen data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    protected abstract IScreenDataGatherer createScreenDataGatherer();

    /**
     * Returns the processor gatherer instance.
     * 
     * @return the processor gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA)
    public static final IProcessorDataGatherer getProcessorDataGatherer() {
        if (null == processorDataGatherer) {
            processorDataGatherer = instance.createProcessorDataGatherer();
        }
        return processorDataGatherer;
    }

    /**
     * Creates the processor data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA)
    protected abstract IProcessorDataGatherer createProcessorDataGatherer();

    
    /**
     * Returns the network gatherer instance.
     * 
     * @return the network gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    public static final INetworkDataGatherer getNetworkDataGatherer() {
        if (null == networkDataGatherer) {
            networkDataGatherer = instance.createNetworkDataGatherer();
        }
        return networkDataGatherer;
    }

    /**
     * Creates the network data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    protected abstract INetworkDataGatherer createNetworkDataGatherer();


    /**
     * Returns the memory gatherer instance.
     * 
     * @return the memory gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public static final IMemoryDataGatherer getMemoryDataGatherer() {
        if (null == memoryDataGatherer) {
            memoryDataGatherer = instance.createMemoryDataGatherer();
        }
        return memoryDataGatherer;
    }

    /**
     * Creates the memory data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    protected abstract IMemoryDataGatherer createMemoryDataGatherer();

    /**
     * Returns the thread gatherer instance.
     * 
     * @return the thread gatherer instance
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public static final IThreadDataGatherer getThreadDataGatherer() {
        if (null == threadDataGatherer) {
            threadDataGatherer = instance.createThreadDataGatherer();
        }
        return threadDataGatherer;
    }
    
    /**
     * Creates the thread data gatherer. 
     * 
     * @return the gatherer instance
     */
    protected abstract IThreadDataGatherer createThreadDataGatherer();

    /**
     * Returns the general gatherer instance.
     * 
     * @return the general gatherer instance
     */
    @Variability(id = {AnnotationConstants.VAR_GATHER_DATA, 
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)    
    public static final IDataGatherer getDataGatherer() {
        if (null == dataGatherer) {
            dataGatherer = instance.createDataGatherer();
        }
        return dataGatherer;
    }

    /**
     * Creates the general data gatherer. 
     * 
     * @return the gatherer instance
     */
    @Variability(id = {AnnotationConstants.VAR_GATHER_DATA, 
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)    
    protected abstract IDataGatherer createDataGatherer();
    
    /**
     * Initializes the gatherer factory on first load.
     */
    static {
        loadLibrary();
    }

    /**
     * Initializes the gatherer factory. Calls 
     * {@link #loadLibrary(Object, boolean)} with parameters 
     * <code><b>null</b>, false</code> so that the context is ignored.
     */
    public static synchronized void loadLibrary() {
        loadLibrary(null, false);
    }

    /**
     * Initializes the gatherer factory with the given context. Calls 
     * {@link #loadLibrary(Object, boolean)} with parameters 
     * <code>context, true</code> so that the context is set.
     * Allows setting the context later, lazily.
     * 
     * @param context operating system specific object, may be <b>null</b>
     */
    public static synchronized void loadLibrary(Object context) {
        loadLibrary(context, true);
    }
    
    /**
     * Initializes the gatherer factory.
     * 
     * @param context operating system specific object, may be <b>null</b>
     * @param considerContext consider or ignore context
     */
    private static synchronized void loadLibrary(Object context, 
        boolean considerContext) {
        if (null == instance) {
            Class<?> cls = null;
            int count = 0;
            do {
                try {
                    cls = Class.forName(factories[count++]);
                } catch (ClassNotFoundException e) {
                    // ignore here, handle if none is found in else case 
                    // for cls below
                }
            } while (null == cls && count < factories.length);
            String err = null;
            if (null != cls) {
                try {
                    instance = (GathererFactory) cls.newInstance();
                    instance.initialize();
                } catch (InstantiationException e) {
                    err = e.getMessage();
                } catch (IllegalAccessException e) {
                    err = e.getMessage();
                } catch (ClassCastException e) {
                    err = e.getMessage();
                } catch (IOException e) {
                    err = e.getMessage();
                }
            } else {
                err = "No gatherer factory found.";
            }
            if (null != err) {
                instance = new FallbackGathererFactory();
            }
        }
        if (considerContext && null != instance) {
            // allows setting the context later, lazily
            instance.setContext(context);
        }
    }

    /**
     * Provides the gatherer and operating system specific initialization.
     * 
     * @throws IOException if initializing the factory fails
     * 
     * @since 1.00
     */
    public abstract void initialize() throws IOException;

    /**
     * Is called in case that a (new) context is available.
     * 
     * @param context operating system specific object, may be <b>null</b>
     * 
     * @since 1.00
     */
    public abstract void setContext(Object context);

    /**
     * Returns additional (optional) information about the factory instance.
     * 
     * @return additional information, may be <b>null</b> for none
     * 
     * @since 1.20
     */
    protected String getInstanceInformation() {
        return null;
    }
    
    /**
     * Changes the current context.
     * 
     * @param context operating system specific object, may be <b>null</b>
     * 
     * @since 1.00
     */
    public static void changeContext(Object context) {
        instance.setContext(context);
    }
    
    /**
     * Returns the (class) name of the factory instance.
     * 
     * @return the class name
     * 
     * @since 1.20
     */
    public static String getInstanceName() {
        String result = "";
        if (null != instance) {
            String add = instance.getInstanceInformation();
            if (null != add && add.length() > 0) {
                add = " " + add;
            } else {
                add = "";
            }
            result = instance.getClass().getName() + add;
        }
        return result;
    }
    
}
