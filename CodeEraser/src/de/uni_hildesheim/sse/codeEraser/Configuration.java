package de.uni_hildesheim.sse.codeEraser;

import java.io.File;

/**
 * Realizes the configuration of the code eraser and the interpretation
 * of the command line. This class can be used for ANT as well as usual
 * command line processing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Configuration {
    
    /**
     * Stores the JAR input file name.
     */
    private String jar = null;
    
    /**
     * Stores the Jar output file name.
     */
    private String out = null;
    
    /**
     * Stores the currently active error message(s). <b>null</b> denotes no
     * error message(s).
     */
    private String errorMsg = null;
    
    /**
     * Changes the input JAR file name.
     * 
     * @param jar the input file name
     * 
     * @since 1.00
     */
    public void setJar(String jar) {
        this.jar = jar;
    }

    /**
     * Changes the input JAR file.
     * 
     * @param jar the input file 
     * 
     * @since 1.00
     */
    public void setJar(File jar) {
        if (null != jar) {
            this.jar = jar.getAbsolutePath();
        }
    }

    /**
     * Returns the current input JAR file name.
     * 
     * @return the current input file name
     * 
     * @since 1.00
     */
    public String getJar() {
        return jar;
    }

    /**
     * Changes the target output JAR file name.
     * 
     * @param out the new output file name
     * 
     * @since 1.00
     */
    public void setOut(String out) {
        this.out = out;
    }

    /**
     * Changes the target output JAR file.
     * 
     * @param out the new output file
     * 
     * @since 1.00
     */
    public void setOut(File out) {
        this.out = out.getAbsolutePath();
    }

    /**
     * Returns the target output JAR file name.
     * 
     * @return the output file name
     * 
     * @since 1.00
     */
    public String getOut() {
        return out;
    }
    
    /**
     * Adds an error message.
     * 
     * @param msg the error message to be added to the current
     *   list of error message(s)
     * 
     * @since 1.00
     */
    public void addErrorMsg(String msg) {
        if (null == errorMsg) {
            errorMsg = msg;
        } else {
            if (errorMsg.length() > 0) {
                errorMsg += "\n";
            }
            errorMsg += msg;
        }
    }

    /**
     * Changes the error message. (internal use only)
     * 
     * @param errorMsg the new error message 
     * 
     * @since 1.00
     */
    protected void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
    /**
     * Returns the most recent error message from command line
     * parsing and byte code processing.
     * 
     * @return the most recent error message or <b>null</b> in
     *   case that no errors occurred
     * 
     * @since 1.00
     */
    public String getErrorMsg() {
        return errorMsg;
    }
    
    /**
     * Validates the command line arguments and determines
     * the processing mode.
     * 
     * @since 1.00
     */
    public void validate() {
    }

}
