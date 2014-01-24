package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

/**
 * Stores code generation data assigned to a 
 * {@link de.uni_hildesheim.sse.monitoring.runtime.annotations.MeasurementValue}
 * .
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class MeasurementValueData {

    /**
     * Stores the parameterized method for recording events.
     */
    private String recorderMethod;
    
    /**
     * Stores the type of the expression, used for keeping the value in a 
     * local variable during the instrumented call.
     */
    private String type;

    /**
     * Creates a new constant.
     * 
     * @param recorderMethod the (unqualified) method for instrumentation with 
     *   some parts to be replaced in the concrete context; may contain 
     *   &lt;&lt;value&gt;&gt; and &lt;&lt;caller&gt;&gt;
     * @param type the type of the expression, used for keeping the value in 
     *   a local variable during the instrumented call
     * 
     * @since 1.00
     */
    MeasurementValueData(String recorderMethod, String type) {
        this.recorderMethod = recorderMethod;
        this.type = type;
    }
    
    /**
     * Returns the method call for the concrete context.
     * 
     * @param recId expression to replace &lt;&lt;recId&gt;&gt; in 
     *     {@link #recorderMethod}
     * @param valueExpression expression to replace &lt;&lt;value&gt;&gt; in 
     *     {@link #recorderMethod}
     * @param callerExpression expression to replace &lt;&lt;caller&gt;&gt;  in 
     *     {@link #recorderMethod}
     * @param tag the memory tag
     * @return the method call ready for use
     * 
     * @since 1.00
     */
    public String getMethodCall(String recId, String valueExpression, 
        String callerExpression, String tag) {
        if (null == recId) {
            recId = "null";
        } else {
            recId = "\"" + recId + "\"";
        }
        return CodeModifier.RECORDER + "." 
            + recorderMethod.replace("<<value>>", valueExpression)
                .replace("<<recId>>", recId)
                .replace("<<caller>>", callerExpression)
                .replace("<<tag>>", tag);
    }
    
    /**
     * The type of the information (to be stored e.g. in a local variable
     * during the instrumented call).
     * 
     * @return the type of the variable as string
     * 
     * @since 1.00
     */
    public String getType() {
        return type;
    }

}
