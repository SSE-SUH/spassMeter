package de.uni_hildesheim.sse.codeEraser.ant;

import org.apache.tools.ant.BuildException;

import de.uni_hildesheim.sse.codeEraser.patchJavassist.CodeProcessor;
import de.uni_hildesheim.sse.codeEraser.Configuration;

/**
 * Implements the code patch Javassist task. It just needs an input and an 
 * output jar file.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class PatchJavassistTask extends AbstractTask {

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
        initConfiguration(conf);
            
        CodeProcessor processor = new CodeProcessor(conf);
        processor.process();
        
        if (null != conf.getErrorMsg()) {
            System.out.println(conf.getErrorMsg());
        }
    }

}
