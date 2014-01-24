package de.uni_hildesheim.sse.codeEraser.ant;

import java.io.File;

import org.apache.tools.ant.Task;

import de.uni_hildesheim.sse.codeEraser.Configuration;

/**
 * Realizes a base task class corresponding to 
 * {@link de.uni_hildesheim.sse.codeEraser.Configuration}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractTask extends Task {

    /**
     * Stores the base directory (used for deriving the 
     * absolute path of relative paths).
     */
    private File baseDir = null;
    
    /**
     * Stores the (usually relative) path of the input jar file.
     */
    private File jarFile = null;
    
    /**
     * Stores the (usually relative) path of the output jar file.
     */
    private File outFile = null;

    /**
     * Changes the base directory (used for deriving the 
     * absolute path of relative paths).
     * 
     * @param baseDir the new base directory
     */
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Changes the (usually relative) path of the input jar file.
     * 
     * @param jarFile the new path to the input jar file
     */
    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * Changes the (usually relative) path of the output jar file.
     * 
     * @param outFile the new path of the output jar file
     */
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    /**
     * Returns the base directory (used for deriving the 
     * absolute path of relative paths).
     * 
     * @return the new directory
     */
    protected File getBaseDir() {
        return baseDir;
    }

    /**
     * Returns the (usually relative) path of the input jar file.
     * 
     * @return the path to the input jar file
     */
    protected File getJarFile() {
        return jarFile;
    }

    /**
     * Returns the (usually relative) path of the output jar file.
     * 
     * @return the path of the output jar file
     */
    protected File getOutFile() {
        return outFile;
    }

    /**
     * Relocates <code>file</code> to {@link #baseDir}
     * if necessary.
     * 
     * @param file the file to be relocated if not absolute
     * @return the relocated absolute file
     * 
     * @since 1.00
     */
    protected File getAbsoluteFile(File file) {
        File result = file;
        if (null != file && !file.isAbsolute()) {
            result = new File(baseDir, file.toString());
        }
        return result;
    }
    
    /**
     * Initializes this task.
     * 
     * @since 1.00
     */
    public void init() {
        super.init();
    }
    
    /**
     * Initializes the given configuration based on the data gathered in this
     * task.
     * 
     * @param config the configuration to be modified as a side effect
     * 
     * @since 1.00
     */
    protected void initConfiguration(Configuration config) {
        config.setJar(getAbsoluteFile(jarFile));
        config.setOut(getAbsoluteFile(outFile));
    }


}
