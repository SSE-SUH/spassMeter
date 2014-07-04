package test.manual;

/**
 * Used to test the recording of two different types.
 * 
 * @author Holger Eichelberger
 * @since 1.05
 * @version 1.05
 */
public class MultiTypeTest {

    /**
     * Prevent external creation.
     * 
     * @since 1.05
     */
    private MultiTypeTest() {
    }
    
    /**
     * Executes the test.
     * 
     * @param args ignored
     * 
     * @since 1.05
     */
    public static void main(String[] args) {
        C1 c1 = new C1();
        c1.calculate();
        
        C2 c2 = new C2();
        c2.calculate();
    }

}
