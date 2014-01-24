package test;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * A test for removing attributes and methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DataClass {

    /**
     * An arbitrary attribute to be removed if "mem" is not set.
     */
    @Variability(id = "mem")
    private int memSize = 0;

    /**
     * An arbitrary attribute to be removed if "io" is not set.
     */
    @Variability(id = "io")
    private int ioSize = 0;

    /**
     * Creates the data class.
     * 
     * @since 1.00
     */
    public DataClass() {
    }

    /**
     * Creates the data class (to be removed if "mem" is not set).
     * 
     * @param memSize the new value of {@link #memSize}
     * 
     * @since 1.00
     */
    @Variability(id = "mem")
    public DataClass(int memSize) {
        this.memSize = memSize;
    }
    
    /**
     * Returns the mem size value (to be removed if "mem" is not set).
     * 
     * @return the value of mem size
     * 
     * @since 1.00
     */
    @Variability(id = "mem")
    public int getMemSize() {
        return memSize;
    }

    /**
     * Returns the io size value (to be removed if "io" is not set).
     * 
     * @return the value of io size
     * 
     * @since 1.00
     */
    @Variability(id = "io")
    public int getIoSize() {
        return ioSize;
    }

    /**
     * Changes the mem size value (to be removed if "mem" is not set).
     * 
     * @param memSize the new value of mem size
     * 
     * @since 1.00
     */
    @Variability(id = "mem")
    public void setMemSize(int memSize) {
        this.memSize = memSize;
    }
    
    /**
     * Changes the io size value (to be removed if "io" is not set).
     * 
     * @param ioSize the new value of io size
     * 
     * @since 1.00
     */
    @Variability(id = "io")
    public void setIoSize(int ioSize) {
        this.ioSize = ioSize;
    }

    /**
     * Returns the sum of mem and io size values (to be removed if neither 
     * "mem" nor "io" is set).
     * 
     * @return the sum of both values
     * 
     * @since 1.00
     */
    @Variability(id = {"mem", "io" }, op = Operation.AND)
    public int getSum() {
        return memSize + ioSize;
    }
    
}
