package test.manual;

import test.threadedTest.Data;

/**
 * Just to use T2.
 * 
 * @author Holger Eichelberger
 * @since 1.05
 * @version 1.05
 */
public class C2 implements T2 {

    /**
     * Stores some data.
     */
    @SuppressWarnings("unused")
    private Data data;
    
    /**
     * Calculates something.
     * 
     * @since 1.00
     */
    public void calculate() {
        System.gc();
    }
    
}
