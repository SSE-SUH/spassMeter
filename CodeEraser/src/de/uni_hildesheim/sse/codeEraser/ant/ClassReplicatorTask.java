package de.uni_hildesheim.sse.codeEraser.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import de.uni_hildesheim.sse.codeEraser.copy.CodeProcessor;
import de.uni_hildesheim.sse.codeEraser.copy.Configuration;

/**
 * Implements the class replicator task.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ClassReplicatorTask extends AbstractTask {
    
    /**
     * Stores the (usually relative) path of the mappings file.
     */
    private File mappingsFile = null;
    
    /**
     * Stores the mappings read from the ANT file.
     */
    private List<Mapping> mappings = new ArrayList<Mapping>();
    
    /**
     * Create a mapping instance and adds it to the mapping list.
     * This method is used by ANT to hook in subelements.
     * 
     * @return the created instance
     * 
     * @since 1.00
     */
    public Mapping createMapping() {
        Mapping result = new Mapping();
        mappings.add(result);
        return result;
    }

    /**
     * Stores the (usually relative) path of the mappings file.
     * 
     * @param mappingsFile the new path to the mappings file
     */
    public void setMappingsFile(File mappingsFile) {
        this.mappingsFile = mappingsFile;
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

        for (Mapping mapping : mappings) {
            conf.addMapping(mapping.getOldName(), mapping.getNewName());
        }
        
        if (null != mappingsFile) {
            conf.readFromProperties(getAbsoluteFile(mappingsFile));
        }
        
        CodeProcessor processor = new CodeProcessor(conf);
        processor.process();
        
        if (null != conf.getErrorMsg()) {
            System.out.println(conf.getErrorMsg());
        }
    }
    
    /**
     * Changes the (usually relative) path of the input jar file.
     * 
     * @param jarFile the new path to the input jar file
     */
    public void setJarFile(File jarFile) {
        // ignore value
    }

}
