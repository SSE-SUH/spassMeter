package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * Defines different accounting strategies.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum GroupAccountingType {
    
    /**
     * For use in annotations only. Points in configuration to 
     * {@link #getDefault()}.
     */
    DEFAULT,
    
    /**
     * Accounts only locally, i.e. possible calls due to polymorphism are 
     * ignored. Should be stated on global configuration level and disables
     * all other settings due to consistency issues.
     */
    LOCAL,
    
    /**
     * Accounts to the first match only.
     */
    DIRECT,
    
    /**
     * Accounts also to indirect monitoring groups in the same thread.
     */
    INDIRECT;

    /**
     * Returns the concrete value for {@link #DEFAULT}.
     * 
     * @return {@link #DIRECT}
     * 
     * @since 1.00
     */
    public static final GroupAccountingType getDefault() {
        return DIRECT;
    }
    
}
