package de.uni_hildesheim.sse.codeEraser.replace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

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
     * Stores a prefix to differentiate between patterns and mappings
     * in a properties file.
     */
    private static final String PATTERN_PREFIX = "pattern:";
    
    /**
     * Stores the mappings.
     */
    private ClassMap mappings = new ClassMap();

    /**
     * Represents a pattern, i.e. a regular expression and a substitute
     * which is applied in case that the regular expression matches. 
     * 
     * @author eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class Pattern {
        
        /**
         * Stores the regular expression.
         */
        private String regEx;
        
        /**
         * Stores the substitute to be applied when <code>regEx</code> matches.
         */
        private String substitute;
        
        /**
         * Creates a new pattern.
         * 
         * @param regEx the regular expression
         * @param substitute the substitute
         * 
         * @since 1.00
         */
        public Pattern(String regEx, String substitute) {
            this.regEx = regEx;
            this.substitute = substitute;
        }
    }
    
    /**
     * Stores the patterns.
     */
    private List<Pattern> patterns = new ArrayList<Pattern>();

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
                if (key.startsWith(PATTERN_PREFIX)) {
                    if (key.length() > PATTERN_PREFIX.length()) {
                        key = key.substring(PATTERN_PREFIX.length());
                        patterns.add(new Pattern(key, value));
                    }
                } else {
                    mappings.put(key, value);
                }
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
     * Add a class name pattern.
     * 
     * @param regEx the pattern to be matched
     * @param substitute the substitute to replace the pattern in case of 
     *     a match
     * 
     * @since 1.00
     */
    public void addPattern(String regEx, String substitute) {
        try {
            "".matches(regEx);
            patterns.add(new Pattern(regEx, substitute));
        } catch (PatternSyntaxException e) {
            addErrorMsg("invalid pattern '" + regEx + "' -> '" + substitute 
                + ": " + e);
        }
    }
    
    /**
     * Returns weather this configuration has patterns.
     * 
     * @return <code>true</code> if patterns are defined, <code>false</code>
     *   else
     * 
     * @since 1.00
     */
    public boolean hasPatterns() {
        return !patterns.isEmpty();
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
     * Returns the substitute for <code>className</code> if a matching pattern
     * is defined.
     * 
     * @param className the class name to be checked for substitution
     * @return the substituted class name or <b>null</b> if no pattern matches
     * 
     * @since 1.00
     */
    public String getSubstitute(String className) {
        String result = null; 
        for (Pattern pattern : patterns) {
            if (className.matches(pattern.regEx)) {
                result = className.replaceAll(
                    pattern.regEx, pattern.substitute);
                break;
            }
        }
        return result;
    }
}
