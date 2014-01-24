package de.uni_hildesheim.sse.codeEraser.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import de.uni_hildesheim.sse.codeEraser.tool.CodeProcessor;
import de.uni_hildesheim.sse.codeEraser.tool.Configuration;

/**
 * Implements the code eraser task.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CodeEraserTask extends AbstractTask {

    /**
     * Stores the (usually relative) path of the bindings file.
     */
    private File bindingsFile = null;
    
    /**
     * Stores if annotations should be checked in recursive
     * or flat fashion.
     */
    private boolean checkFlat = false;
    
    /**
     * Stores the bindings read from the ANT file.
     */
    private List<Binding> validBindings = new ArrayList<Binding>();

    /**
     * Stores if annotations should only be listed or if bytecode should 
     * be processed.
     */
    private boolean listAnnotations = false;
    
    /**
     * Create a binding instance and adds it to the binding list.
     * This method is used by ANT to hook in subelements.
     * 
     * @return the created instance
     * 
     * @since 1.00
     */
    public Binding createBinding() {
        Binding result = new Binding();
        validBindings.add(result);
        return result;
    }


    /**
     * Stores the (usually relative) path of the bindings file.
     * 
     * @param bindingsFile the new path to the bindings file
     */
    public void setBindingsFile(File bindingsFile) {
        this.bindingsFile = bindingsFile;
    }
    
    /**
     * Changes if annotations should be checked in recursive
     * or flat fashion.
     * 
     * @param checkFlat if <code>true</code> annotations should
     *     be checked in flat mode, <code>false</code> (default)
     *     if for a class also superclasses and interfaces should
     *     be taken into account (reduces need for annotations)
     */
    public void setFlat(boolean checkFlat) {
        this.checkFlat = checkFlat;
    }
    
    /**
     * Changes the operation mode, here enables
     * or disables the list mode.
     * 
     * @param list <code>true</code> if the listing of used
     *    variabilities in the code should be emitted, <code>false</code>
     *    if processing should be enabled
     */
    public void setList(boolean list) {
        this.listAnnotations = list;
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
     * Executes this task.
     * 
     * @throws BuildException if an (configuration or build) 
     *     error occurred (illegal file matching regular 
     *     expressions)
     * 
     * @since 1.00
     */
    public void execute() throws BuildException {
        Configuration conf = new Configuration();
        if (listAnnotations) {
            conf.setCommand(Configuration.Command.LIST_ANNOTATIONS);
        }
        initConfiguration(conf);
        conf.setCheckRecursively(!checkFlat);

        for (Binding binding : validBindings) {
            conf.putBinding(binding.getId(), binding.getValue());
        }
        
        if (null != bindingsFile) {
            conf.readBindingsFromProperties(getAbsoluteFile(bindingsFile));
        }
            
        CodeProcessor processor = new CodeProcessor(conf);
        processor.process();
        
        if (null != conf.getErrorMsg()) {
            System.out.println(conf.getErrorMsg());
        }
    }

}
