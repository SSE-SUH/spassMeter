package test.manual;

import test.threadedTest.Data;

/**
 * Just to use T1.
 * 
 * @author Holger Eichelberger
 * @since 1.05
 * @version 1.05
 */
public class C1 implements T1 {

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
        for (int i = 0; i < 10000; i++) {
            data = new Data();
            if (i % 50 == 0) {
                System.gc();
            }
        }
    }
    
}
