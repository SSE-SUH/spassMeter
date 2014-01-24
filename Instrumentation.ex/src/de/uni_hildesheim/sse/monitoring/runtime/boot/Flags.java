package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * Some operations for working with bit flags.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Flags {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Flags() {
    }

    /**
     * Returns if the given <b>mask</b> is set in <code>flags</code>.
     * 
     * @param flags the current flags
     * @param mask the mask to check
     * @return <code>true</code> if <code>mask</code> is set in 
     *   <code>flags</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    public static final boolean isSet(int flags, int mask) {
        return ((flags & mask) == mask);
    }

    /**
     * Turns the given <b>mask</b> on in <code>flags</code>.
     * 
     * @param flags the current flags
     * @param mask the mask to set
     * @return the new flags value
     * 
     * @since 1.00
     */
    public static final int set(int flags, int mask) {
        return flags | mask;
    }

    /**
     * Turns the given <b>mask</b> off in <code>flags</code>.
     * 
     * @param flags the current flags
     * @param mask the mask to clear
     * @return the new flags value
     * 
     * @since 1.00
     */
    public static final int unset(int flags, int mask) {
        return flags & ~mask;
    }

    /**
     * Turns the given <b>mask</b> on or off in <code>flags</code>.
     * 
     * @param flags the current flags
     * @param mask the mask to clear
     * @param set if <b>true</b> call {@link #set(int, int)} 
     *     else {@link #unset(int, int)}.
     * @return the new flags value
     * 
     * @since 1.00
     */
    public static final int change(int flags, int mask, boolean set) {
        int result;
        if (set) {
            result = set(flags, mask);
        } else {
            result = unset(flags, mask);
        }
        return result;
    }

}

    