package de.uni_hildesheim.sse.codeEraser.ant;

/**
 * Realizes a variability binding to be read from an ANT file.
 * Currently we do not exploit variability bindings but recognize
 * only that a variability is enabled or disabled.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Binding {

    /**
     * Stores the variability id.
     */
    private String id;
    
    /**
     * Stores the value, i.e. the concrete variant being active.
     */
    private String value;
    
    /**
     * Returns the variability id.
     * 
     * @return the variability id
     * 
     * @since 1.00
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the concrete variant for the variability.
     * 
     * @return the variant for the variability
     * 
     * @since 1.00
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Changes the variability id.
     * 
     * @param id the new variability id
     * 
     * @since 1.00
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Changes the variant value.
     * 
     * @param value the new variant value
     * 
     * @since 1.00
     */
    public void setValue(String value) {
        this.value = value;
    }

}
