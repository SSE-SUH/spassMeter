package test;

import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ValueChange;

/**
 * Performs some simple tests on being notified about value changes on
 * static / instance attributes.<br/>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_FIELDACCESS)
public class FieldAccessTest {
   
    /**
     * Defines the recorder identification for the static attribute access.
     */
    private static final String ID_STATIC = "STATIC";

    /**
     * Defines the recorder identification for the instance attribute access.
     */
    private static final String ID_INSTANCE = "INSTANCE";

    /**
     * Defines the new value for the static field.
     */
    private static final int NEWVALUE_STATIC = 5;

    /**
     * Defines the new value for the instance field.
     */
    private static final int NEWVALUE_INSTANCE = 10;
    
    // changing constants is not easy if even possible
    
    /**
     * Defines a static attribute.
     */
    @ValueChange(id = ID_STATIC)
    private static int staticField;
    
    /**
     * Defines an instance attribute.
     */
    @ValueChange(id = ID_INSTANCE)
    private int instanceField;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private FieldAccessTest() {
    }
    
    /**
     * Executes the tests.
     * 
     * @since 1.00
     */
    private static void execute() {
        staticField = NEWVALUE_STATIC;
        FieldAccessTest instance = new FieldAccessTest();
        instance.instanceField = NEWVALUE_INSTANCE;
        TestEnvironment.notice(staticField + " " + instance.instanceField);
    }

    /**
     * Aims at reading / writing to both attributes.
     * 
     * @param args command line arguments (ignored)
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        TestEnvironment.initialize();
        TestEnvironment.notice(FieldAccessTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
                    execute();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            execute();
        }
        TestEnvironment.notice("------------------ done: FieldAccess");
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        Object tmp = TestEnvironment.getAttributeData(ID_STATIC);
        TestEnvironment.assertType(ID_STATIC, tmp, Integer.class);
        TestEnvironment.assertTrue(ID_STATIC, 
            ((Integer) tmp).intValue() == NEWVALUE_STATIC);
        
        tmp = TestEnvironment.getAttributeData(ID_INSTANCE);
        TestEnvironment.assertType(ID_INSTANCE, tmp, Integer.class);
        TestEnvironment.assertTrue(ID_INSTANCE, 
            ((Integer) tmp).intValue() == NEWVALUE_INSTANCE);
        
        TestEnvironment.success(AnnotationId.ID_FIELDACCESS);
    }

}
