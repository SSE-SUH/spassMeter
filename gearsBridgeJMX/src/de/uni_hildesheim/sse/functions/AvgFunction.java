package de.uni_hildesheim.sse.functions;

import java.text.DecimalFormat;

/**
 * Defines an {@link IFunction} which stores the avg of a given attribute.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class AvgFunction implements IFunction {

    /**
     * Stores the {@link IIdentity} of the class to watch.
     * 
     * @since 1.00
     */
    private IIdentity identity;

    /**
     * Stores the avg value.
     * 
     * @since 1.00
     */
    private Double avg = Double.MAX_VALUE;

    /**
     * Stores the type of the function. This will be displayed if the function
     * is used.
     */
    private String functionType = "avg";

    /**
     * Stores a count for calculating the avg.
     * 
     * @since 1.00
     */
    private int count = 0;

    /**
     * Public default Constructor.
     * 
     * @since 1.00
     */
    public AvgFunction() {
    }

    /**
     * Constructor.
     * 
     * @param identity
     *            The {@link IIdentity} of the function.
     */
    public AvgFunction(IIdentity identity) {
        this.identity = identity;
    }

    
    @Override
    public String getFunctionType() {
        return functionType;
    }

    
    @Override
    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    
    @Override
    public IIdentity getIdentity() {
        return identity;
    }

    
    @Override
    public void setIdentity(IIdentity identity) {
        this.identity = identity;
    }

    @Override
    public Object getValue() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(avg);
    }

    @Override
    public void calculate() {
        Double act = avg;
        // getting the actual attribute value
        act = Double.valueOf(identity.getCls()
                .getSpecificAttribute(identity.getAttributeName()).toString());
        // calculating the new value
        if (count == 0) { // If there is no avg yet
            avg = act;
        } else {
            avg = ((count * avg) + act) / (count + 1);
        }
        count++;
    }

    @Override
    public String toString() {
        return "Functiontype: " + functionType + " " + identity;
    }

}
