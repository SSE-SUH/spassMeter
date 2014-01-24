package de.uni_hildesheim.sse.functions;

import java.text.DecimalFormat;

/**
 * Defines an {@link IFunction} which stores the minimum of a given attribute.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class MinFunction implements IFunction {

    /**
     * Stores the {@link IIdentity} of the class to watch.
     * 
     * @since 1.00
     */
    private IIdentity identity;

    /**
     * Stores the minimum value.
     * 
     * @since 1.00
     */
    private double min = Double.MAX_VALUE;

    /**
     * Stores the type of the function. This will be displayed if the function
     * is used.
     * 
     * @since 1.00
     */
    private String functionType = "min";

    /**
     * Public default Constructor.
     * 
     * @since 1.00
     */
    public MinFunction() {
    }

    /**
     * Constructor.
     * 
     * @param identity The {@link IIdentity} of the function.
     *
     * @since 1.00
     */
    public MinFunction(IIdentity identity) {
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
        return df.format(min);
    }

    @Override
    public void calculate() {
        double act = Double.MAX_VALUE;
        // getting the actual attribute value
        act = Double.valueOf(identity.getCls()
                .getSpecificAttribute(identity.getAttributeName()).toString());
        // calculating the new value
        if (act < min) {
            min = act;
        }
    }

    @Override
    public String toString() {
        return "Functiontype: " + functionType + " " + identity;
    }

}
