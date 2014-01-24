package de.uni_hildesheim.sse.codeEraser.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javassist.ClassMap;
import javassist.CtClass;

/**
 * Describes the configuration for the class replacer.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Configuration 
    extends de.uni_hildesheim.sse.codeEraser.Configuration {

    /**
     * Stores the mappings.
     */
    private ClassMap mappings = new ClassMap();

    /**
     * Returns the mapping for the given class name.
     * 
     * @param className the name the mapping should be returned for
     * @return the mapping or <b>null</b> if no mapping is defined
     * 
     * @since 1.00
     */
    public String getMapping(String className) {
        String result = null;
        Object mapping = mappings.get(className);
        if (mapping instanceof String) {
            result = (String) mapping;
        } else if (mapping instanceof CtClass) {
            result = ((CtClass) mapping).getName();
        }
        return result;
    }

    /**
     * Returns the mapping stored in this configuration.
     * 
     * @return the mapping
     * 
     * @since 1.00
     */
    public ClassMap getMapping() {
        return (ClassMap) mappings.clone();
    }
    
    /**
     * Reads the mapping from a properties file. This method may change
     * the error message in case of failures.
     * 
     * @param file the file to read
     * @return <code>true</code> if reading was done without
     *     problems, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean readFromProperties(File file) {
        return readFromProperties(file.getAbsolutePath());
    }

    /**
     * Reads the mapping from a properties file. This method may change
     * the error message in case of failures.
     * 
     * @param file the file to read
     * @return <code>true</code> if reading was done without
     *     problems, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean readFromProperties(String file) {
        boolean ok = true;
        try {
            mappings.clear();
            Properties prop = new Properties();
            prop.load(new FileInputStream(file));
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                mappings.put(key, value);
            }
        } catch (IOException e) {
            addErrorMsg("I/O error - " + e.getMessage());
            ok = false;
        }
        return ok;
    }
    
    /**
     * Add a class name mapping.
     * 
     * @param oldName the old class name to be mapped to <code>newName</code>
     * @param newName the new class name to be mapped
     * 
     * @since 1.00
     */
    public void addMapping(String oldName, String newName) {
        mappings.put(oldName, newName);
    }

    /**
     * Returns weather this configuration has mappings.
     * 
     * @return <code>true</code> if mappings are defined, <code>false</code>
     *   else
     * 
     * @since 1.00
     */
    public boolean hasMappings() {
        return !mappings.isEmpty();
    }

    /**
     * Changes the input JAR file name.
     * 
     * @param jar the input file name
     * 
     * @since 1.00
     */
    public void setJar(String jar) {
        // ignore value
    }

}
