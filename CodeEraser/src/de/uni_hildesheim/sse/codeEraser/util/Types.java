package de.uni_hildesheim.sse.codeEraser.util;

import javassist.CtClass;

/**
 * Useful type-related functionality.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Types {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Types() {
    }

    /**
     * Returns the default value for the given type as defined in the JLS.
     * 
     * @param type the type the default value should be returned for
     * @return the default value
     * 
     * @since 1.00
     */
    public static final String getDefaultValue(CtClass type) {
        return getDefaultValue(type, null);
    }

    /**
     * Returns the default value for the given type as defined in the JLS.
     * 
     * @param type the type the default value should be returned for
     * @param override returned if not <b>null</b> and not empty
     * @return the default value
     * 
     * @since 1.00
     */
    public static final String getDefaultValue(CtClass type, String override) {
        String result;
        if (null != override && override.length() > 0) {
            result = override;
        } else {
            if (CtClass.booleanType == type) {
                result = "false";
            } else if (CtClass.byteType == type || CtClass.intType == type 
                || CtClass.shortType == type) {
                result = "0";
            } else if (CtClass.longType == type) {
                result = "0L";
            } else if (CtClass.charType == type) {
                result = "\u0000";
            } else if (CtClass.doubleType == type) {
                result = "0.0D";
            } else if (CtClass.floatType == type) {
                result = "0.0F";
            } else {
                result = "null";
            }
        }
        return result;
    }
}
