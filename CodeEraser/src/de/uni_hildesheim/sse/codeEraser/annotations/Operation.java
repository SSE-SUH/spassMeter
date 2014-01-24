package de.uni_hildesheim.sse.codeEraser.annotations;

/**
 * Operation to combine multiple variability id in 
 * {@link Variability}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum Operation {

    /**
     * Combine all id with a logical "and".
     */
    AND,

    /**
     * Combine all id with a logical "or".
     */
    OR,

    /**
     * Combine all id with a logical "xor".
     */
    XOR;
}
