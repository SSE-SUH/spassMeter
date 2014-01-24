package de.uni_hildesheim.sse.codeEraser.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Realizes the configuration of the code eraser and the interpretation
 * of the command line. This class can be used for ANT as well as usual
 * command line processing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Configuration 
    extends de.uni_hildesheim.sse.codeEraser.Configuration {
    
    /**
     * Defines the commands of the code eraser.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public enum Command {
        
        /**
         * Command currently unknown (right after staring
         * the program as long as no command line is read).
         */
        NONE, 
        
        /**
         * Print out help information either by command or due to an command
         * line parsing problem. 
         */
        HELP,
        
        /**
         * Process the byte code and remove attributes, methods and classes.
         */
        PROCESS,
        
        /**
         * List annotations and do not process bytecode.
         */
        LIST_ANNOTATIONS;
    }

    /**
     * Stores the current command.
     */
    private Command command = Command.NONE;
    
    /**
     * Stores all bindings, i.e. variability-variant mappings.
     */
    private Map<String, String> bindings = new HashMap<String, String>();
    
    /**
     * Enables recursive processing of annotations, i.e. annotations of 
     * superclasses and interfaces are also taken into account.
     */
    private boolean checkRecursively = true;
    
    /**
     * Returns the current variability bindings, i.e. variability-variant 
     * mappings.
     * 
     * @return all variability bindings as an unmodifiable map
     * 
     * @since 1.00
     */
    public Map<String, String> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    /**
     * Changes the command to be executed by the code eraser.
     * 
     * @param command the new command
     * 
     * @since 1.00
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * Returns the command to be executed by the code eraser.
     * 
     * @return the command to be executed
     * 
     * @since 1.00
     */
    public Command getCommand() {
        return command;
    }
    
    /**
     * Changes weather annotations should be considered recursively or
     * if only the annotations of the currently considered element
     * should be taken into account.
     * 
     * @param checkRecursively <code>true</code> if recursive annotations
     *     should be taken into account, <code>false</code> if only flat
     *     annotations should be considered
     * 
     * @since 1.00
     */
    public void setCheckRecursively(boolean checkRecursively) {
        this.checkRecursively = checkRecursively;
    }

    /**
     * Returns weather annotations should be considered recursively or
     * if only the annotations of the currently considered element
     * should be taken into account.
     * 
     * @return <code>true</code> if recursive annotations
     *     should be taken into account, <code>false</code> if only flat
     *     annotations should be considered
     * 
     * @since 1.00
     */
    public boolean checkRecursively() {
        return checkRecursively;
    }

    /**
     * Adds a binding to this configuration.
     * 
     * @param key the variability
     * @param value the enabled variant
     * 
     * @since 1.00
     */
    public void putBinding(Object key, Object value) {
        if (null != key) {
            if (null != value) {
                putBinding(key.toString(), value.toString());                
            } else {
                putBinding(key.toString(), null);
            }
        }
    }
    
    /**
     * Adds a binding to this configuration.
     * 
     * @param key the variability
     * @param value the enabled variant
     * 
     * @since 1.00
     */
    public void putBinding(String key, String value) {
        if (null != key) {
            bindings.put(key.trim(), value);            
        }
    }
    
    /**
     * Validates the command line arguments and determines
     * the processing mode.
     * 
     * @since 1.00
     */
    @Override
    public void validate() {
        String errorMsg = null;
        if (Command.HELP != command) {
            if (Command.LIST_ANNOTATIONS == command) {
                if (null == getJar()) {
                    errorMsg = "jar not specified";
                    command = Command.HELP;
                }
            } else {
                command = Command.PROCESS;
                if (null == getJar()) {
                    errorMsg = "jar not specified";
                    command = Command.HELP;
                } 
                if (null == getOut()) {
                    errorMsg = "out not specified";
                    command = Command.HELP;
                }
                if (bindings.isEmpty()) {
                    errorMsg = "no bindings specified";
                    command = Command.HELP;
                }
            }
        }
        setErrorMsg(errorMsg);
    }

    /**
     * Reads bindings from a Java properties file. This method may change
     * the error message in case of failures.
     * 
     * @param file the file to read
     * @return <code>true</code> if reading was done without
     *     problems, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean readBindingsFromProperties(File file) {
        return readBindingsFromProperties(file.getAbsoluteFile());
    }
    
    /**
     * Reads bindings from a Java properties file. This method may change
     * the error message in case of failures.
     * 
     * @param file the file to read
     * @return <code>true</code> if reading was done without
     *     problems, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean readBindingsFromProperties(String file) {
        boolean ok = true;
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(file));
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                if (null != entry.getValue()) {
                    bindings.put(entry.getKey().toString(), 
                        entry.getValue().toString());
                } else {
                    bindings.put(entry.getKey().toString(), null);
                }
            }
        } catch (IOException e) {
            addErrorMsg("I/O error - " + e.getMessage());
            ok = false;
            command = Command.HELP;
        }
        return ok;
    }

}
