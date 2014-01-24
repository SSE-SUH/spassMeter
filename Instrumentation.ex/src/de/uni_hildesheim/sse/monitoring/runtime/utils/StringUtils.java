package de.uni_hildesheim.sse.monitoring.runtime.utils;

/**
 * Some string utilities (may avoids instance creation but may also increase
 * method calls :|).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class StringUtils {

    /**
     * Prevents this utility class from being created from outside.
     * 
     * @since 1.00
     */
    private StringUtils() {
    }
    
    /**
     * Returns whether <code>cs</code> ends with <code>postfix</code>.
     * 
     * @param cs the string sequence to check for <code>postfix</code>
     * @param postfix the postfix to test for
     * @return <code>true</code> if <code>sb</code> ends with 
     *   <code>postfix</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean endsWith(CharSequence cs, String postfix) {
        boolean match;
        int sbLen = cs.length();
        int pLen = postfix.length();
        if (sbLen >= pLen) {
            match = true;
            for (int p = pLen - 1, s = sbLen - 1; match && p >= 0; p--, s--) {
                match = postfix.charAt(p) == cs.charAt(s); 
            }
        } else {
            match = false;
        }
        return match;
    }


    /**
     * Returns whether <code>cs</code> equals <code>text</code>.
     * 
     * @param cs the string sequence to check for <code>text</code>
     * @param text the text to check for
     * @return <code>true</code> if <code>sb</code> equals 
     *   <code>postfix</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean same(CharSequence cs, String text) {
        boolean match;
        int sbLen = cs.length();
        int pLen = text.length();
        if (sbLen == pLen) {
            match = true;
            for (int i = sbLen - 1; match && i >= 0; i--) {
                match = text.charAt(i) == cs.charAt(i); 
            }
        } else {
            match = false;
        }
        return match;
    }

}
