package de.uni_hildesheim.sse.codeEraser.ant;

/**
 * Realizes a mapping between old and new class name. This class may also 
 * replace name patterns. A name pattern is a regular expression with 
 * substitution on the class files in the JAR file to be processed.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Mapping {

    /**
     * Stores the old class name.
     */
    private String oldName;
    
    /**
     * Stores the new class name.
     */
    private String newName;
    
    /**
     * Stores if the mapping is a pattern.
     */
    private boolean pattern = false;
    
    /**
     * Returns the old class name.
     * 
     * @return the old name
     * 
     * @since 1.00
     */
    public String getOldName() {
        return oldName;
    }
    
    /**
     * Returns the new class name.
     * 
     * @return the new class name
     * 
     * @since 1.00
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Returns weather this mapping represents a class renaming pattern.
     * 
     * @return weather this mapping is a pattern (<code>true</code>) or not 
     *     (<code>false</code>)
     * 
     * @since 1.00
     */
    public boolean isPattern() {
        return pattern;
    }
    
    /**
     * Changes the old class name.
     * 
     * @param oldName the old name
     * 
     * @since 1.00
     */
    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    /**
     * Changes the new class name.
     * 
     * @param newName the new name
     * 
     * @since 1.00
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

    /**
     * Changes weather this mapping represents a class renaming pattern.
     * 
     * @param pattern weather this mapping is a pattern or not 
     *     (<code>false</code>)
     * 
     * @since 1.00
     */
    public void setPattern(boolean pattern) {
        this.pattern = pattern;
    }

}
