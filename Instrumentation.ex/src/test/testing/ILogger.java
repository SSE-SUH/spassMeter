package test.testing;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * An interface to notify the test environment about exceptions etc.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public interface ILogger {

    /**
     * Defines the message to be emitted in case that the monitoring group
     * is not available.
     */
    public static final String CANNOT_GET_GROUP = "Monitoring group not " 
        + "available - ignoring test case.";
    
    /**
     * Defines the message to be emitted in case that the recorder cannot
     * get the data.
     */
    public static final String CANNOT_GET_DATA = "Data acquisition failed " 
        + "- ignoring test case.";

    /**
     * Defines the message to be emitted in case that the recorder cannot
     * cast the result.
     */
    public static final String CANNOT_CAST_DATA = "Wrong data format " 
        + "- ignoring test case.";

    /**
     * Defines the message to be emitted in case that the recorder classes 
     * cannot be found by the class loader.
     */
    public static final String CLASS_NOT_FOUND = "Class not found " 
        + "- ignoring test cases.";

    /**
     * Defines the message to be emitted in case that the recorder instances 
     * cannot be called due to some reasons.
     */
    public static final String CANNOT_CALL = "Call failed " 
        + "- ignoring test cases.";
    
    /**
     * Defines the message to be emitted in case that configuration(s) 
     * cannot be retrieved.
     */
    public static final String CANNOT_GET_CONFIGURATION = 
        "Cannot get configuration";
    
    /**
     * A method to handle exceptions intended to enable/disable trace output.
     * 
     * @param text the text to be emitted (in any case)
     * @param exception the exception describing the problem which may 
     *    be emitted
     * 
     * @since 1.00
     */
    public void exception(String text, Exception exception); 
    
    /**
     * Prints a notice during testing. 
     * 
     * @param notice the text to be printed
     * 
     * @since 1.00
     */
    public void notice(String notice);
    
}
