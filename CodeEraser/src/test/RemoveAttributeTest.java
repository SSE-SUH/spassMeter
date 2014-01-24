package test;

import de.uni_hildesheim.sse.codeEraser.annotations.SetValue;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Some basic tests for removing and setting attributes. Currently, constants 
 * cannot be removed / modified.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class RemoveAttributeTest {

    /**
     * A constant value to be set by the value of "debug". <i>Does not work
     * currently.</i>
     */
    // does not work
    @SetValue(id = "debug")
    private static final boolean CONSTANT = false;

    /**
     * A static value to be set by the value of "debug". <i>Does not work
     * currently.</i>
     */
    @SetValue(id = "debug")
    private static boolean debug = false;

    /**
     * An attribute to be removed on "var" and to be replaced by a given
     * value.
     */
    @Variability(id = "var", value = "101")
    private int myAttribute = 10;

    /**
     * A dependent getter which should return a specific value if "var" is
     * not set.
     * 
     * @return the value of the attribute
     */
    public int getMyAttribute() {
        return myAttribute;
    }
    
    /**
     * Executes the tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        System.out.println();
        System.out.println("TEST:");
        RemoveAttributeTest instance = new RemoveAttributeTest();
        System.out.println("This is myAttribute " + instance.myAttribute);
        instance.myAttribute = 20;
        System.out.println("This is myAttribute " + instance.myAttribute);
        System.out.println("This is myAttribute " + instance.getMyAttribute());
        System.out.println(CONSTANT);
        System.out.println(debug);
        
        DataClass data = new DataClass();
        data.setIoSize(100);
        data.setMemSize(1000);
        System.out.println(data.getSum());
        data = new DataClass(100);
        System.out.println(data);
        
        ToRemove remove = new ToRemove();
        System.out.println(remove.isEnabled());
        SuperClass su = remove;
        remove = (ToRemove) su;
    }

}
