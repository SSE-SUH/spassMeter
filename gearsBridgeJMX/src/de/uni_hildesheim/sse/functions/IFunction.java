package de.uni_hildesheim.sse.functions;


/**
 * Defines an interface for calculating functions.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public interface IFunction {

    /**
     * Returns the type of the function (This type will be displayed if the 
     * function is used).
     * 
     * @return The type of the function (e.g. min, max, avg, ...).
     * 
     * @since 1.00
     */
    public String getFunctionType();
    
    /**
     * Sets the type of the function (This type will be displayed if the 
     * function is used).
     * 
     * @param functionType The type of the function 
     *           (e.g. min, max, avg, ...).
     * 
     * @since 1.00
     */
    public void setFunctionType(String functionType);
    
    /**
     * Returns the {@link IIdentity} of the function.
     * 
     * @return The {@link IIdentity}.
     * 
     * @since 1.00
     */
    public IIdentity getIdentity();
    
    /** 
     * Sets the {@link IIdentity} of a function.
     * 
     * @param identity The {@link IIdentity} to be set.
     * 
     * @since 1.00
     */
    public void setIdentity(IIdentity identity);
    
    /**
     * Returns the actual value.
     * 
     * @return The actual value.
     * 
     * @since 1.00
     */
    public Object getValue();
    
    /**
     * Calculates a new value.
     * 
     * @since 1.00
     */
    public void calculate();
    
    /**
     * Returns a string representation of the identity.
     * 
     * @return A string representation of the identity.
     * 
     * @see Object#toString()
     * 
     * @since 1.00
     */
    @Override
    public String toString();
    
}
