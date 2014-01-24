package de.uni_hildesheim.sse.functions;

/**
 * Demonstrates an additional function.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class MySpecificFunction implements IFunction {

    /**
     * Stores the {@link IIdentity} of the class to watch.
     * 
     * @since 1.00
     */
    private IIdentity i;

    /**
     * Stores the maximum value.
     * 
     * @since 1.00
     */
    private int value = 1;

    /**
     * Stores the type of the function. This will be displayed if the function
     * is used.
     * 
     * @since 1.00
     */
    private String type = "MyFunc";

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    public MySpecificFunction() {
    }

    @Override
    public String getFunctionType() {
        return type;
    }

    @Override
    public void setFunctionType(String type) {
        this.type = type;
    }

    @Override
    public IIdentity getIdentity() {
        return i;
    }

    @Override
    public void setIdentity(IIdentity identity) {
        i = identity;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void calculate() {
        value = value * 2;
    }

    @Override
    public String toString() {
        return "Functiontype: " + type + " " + i;
    }

}
