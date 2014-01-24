package test;

/**
 * A simple class in order to test package substitution.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ChangePackageTest {

    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private ChangePackageTest() {
    }
    
    /**
     * Executes the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        System.out.println(ChangePackageTest.class.getName());
    }
}
