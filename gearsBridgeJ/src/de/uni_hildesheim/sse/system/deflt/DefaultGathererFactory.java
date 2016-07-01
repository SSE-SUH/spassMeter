package de.uni_hildesheim.sse.system.deflt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
 * Implements the gatherer factory for this default implementation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DefaultGathererFactory 
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
        if (null == System.getProperty(PROPERTY_EXTERNAL_LIB)) {
            // determine the name of the library dependent on the 
            // operating system and the underlying architecture
            String osName = System.getProperty("os.name").toUpperCase();
            String osArch = System.getProperty("os.arch").toUpperCase();
            String infix = System.getProperty(PROPERTY_INFIX, "");

            String libName = "";
            String libExtension = "";
            if (osName.contains("LINUX")) {
                libName = "locutor";
                libExtension = ".so";
            } else if (osName.contains("WINDOWS")) {
                libName = "locutor";
                libExtension = ".dll";
            } else if (osName.contains("MAC OS X")) {
                libName = "locutor_osx";
                libExtension = ".so";
            } 
            
            String error = "";
            boolean is64 = osArch.endsWith("64");
            if (is64) {
                error = loadLibrary(libName + "_64" + libExtension, 
                    determineOutName(libName + "_64", infix, libExtension));    
            }
            if (null != error) {
                String tmp = loadLibrary(libName + libExtension, 
                    determineOutName(libName, infix,  libExtension));
                if (null != tmp) {
                    if (error.length() > 0) {
                        error = "Error loading 64 bit library:\n" + error; 
                    }
                    error = tmp;
                }
            }
            if (null != error && error.length() > 0) {
                System.err.println("Error loading library: " + error);
            }
        }
    }
    
    /**
     * Determines a temporary file for the native library based on a given name.
     * 
     * @param name the file name
     * @return the temporary file
     * 
     * @since 1.20
     */
    private File determineTmpFile(String name) {
        return new File(System.getProperty("java.io.tmpdir"), name);
    }

    /**
     * Determines the output name of a feasible temporary file for the native.
     * 
     * @param libName the library name
     * @param infix the infix to use if not {@link #PROPERTY_OWNINSTANCE}, 
     *    may be empty
     * @param libExtension the extension of the library file name
     * @return the output name
     * 
     * @since 1.20
     */
    private String determineOutName(String libName, String infix, 
        String libExtension) {
        String result = null;
        if (Boolean.valueOf(System.getProperty(PROPERTY_OWNINSTANCE, "FALSE")
            .toUpperCase())) {
            int i = 0;
            do {
                String tmp = libName + i + libExtension;
                File f = determineTmpFile(tmp);
                if (!f.exists() || f.delete()) {
                    result = tmp;
                }
                i++;
            } while (null != result);
        } else {
            result = libName + infix + libExtension;
        }
        return result;
    }
    
    /**
     * Aims at loading the specified native library. This method checks the
     * file system as well as the containing JAR.
     * 
     * @param libName the name of the library
     * @param outName the output name (how the library shall be loaded)
     * @return <b>null</b> if the library was loaded, the error else
     * 
     * @since 1.00
     */
    private String loadLibrary(String libName, String outName) {
        String error = null;
        // load the library and set the library loaded flag
        if (libName.length() > 0) {
            boolean loadIt = true;
            File f = new File(libName);
            // for development - just try the direct file
            if (!f.exists()) {
                // for development under Eclipse - try bin
                f = new File("bin" + File.separator + libName);
            }
            if (!f.exists()) {
                // try to unpack and load from tmp
                ClassLoader loader = DataGatherer.class.getClassLoader();
                if (null == loader) {
                    loader = ClassLoader.getSystemClassLoader();
                }
                InputStream stream = loader
                    .getResourceAsStream("/" + libName);
                if (null == stream) {
                    // legacy
                    stream = loader.getResourceAsStream(libName);
                }
                if (null != stream) {
                    byte[] buffer = new byte[1024];
                    File tmpFile = determineTmpFile(outName);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(tmpFile);
                        int read = 0;
                        do {
                            read = stream.read(buffer, 0, buffer.length);
                            if (read > 0) {
                                fos.write(buffer, 0, read);
                            }
                        } while (read > 0);
                        fos.close();
                        f = tmpFile;
                    } catch (IOException e) {
                        if (null == fos && tmpFile.exists()) {
                            // while opening - assume other JVM uses lib
                            f = tmpFile;
                        } else {
                            loadIt = false;
                            error = "Error while extracting " + libName 
                                + " from jar: " + e.getMessage();
                        }
                    }
                }
            }
            if (loadIt && f.exists()) {
                try {
                    System.load(f.getAbsolutePath());
                    System.setProperty(PROPERTY_EXTERNAL_LIB, 
                        f.getAbsolutePath());
                } catch (Exception e) {
                    error = e.getMessage();
                }
                f.deleteOnExit();
            } else {
                error = "Library " + f.getAbsolutePath() + " not found!";
            }
        }
        return error;
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
