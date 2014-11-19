package test;

import java.awt.Dimension;

import test.dependent.DependentClass;

/**
 * Uses a point (for binary replacement). Actually the same as 
 * {@link ReplaceTest}, but refers to an externally packaged class.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ReplaceTest2 {

    /**
     * Stores the dependent class.
     */
    private DependentClass dependent;
    
    /**
     * Stores the dimension.
     */
    private Dimension dimension;
    
    /**
     * Creates a dimension.
     * 
     * @return the created dimension.
     * 
     * @since 1.00
     */
    public Dimension createDimension() {
        dependent = new DependentClass();
        System.out.println(dependent);
        if (null == dimension) {
            dimension = new Dimension(10, 20);
        }
        return dimension;
    }
    
    /**
     * Executes the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        ReplaceTest2 test = new ReplaceTest2();
        Dimension dimension = test.createDimension();
        System.out.println(dimension.getClass() + " " + dimension);
    }

}
