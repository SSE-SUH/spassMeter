package de.uni_hildesheim.sse.monitoring.runtime.utils;

/**
 * An utility class for some basic formattings.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Formatter {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Formatter() {
    }
    
    /**
     * Formats nano time to milliseconds, seconds or minutes.
     * 
     * @param nano the time (difference) in nano seconds to be formatted
     * @return the formatted time including a suffix indicating the time 
     *     dimension
     * 
     * @since 1.00
     */
    public static String nanoToUsualTime(long nano) {
        double time = nano;
        String post = "ns";
        if (nano > 1000) {
            time /= 1000.0; //micro
            post = "ys";
            if (time > 1000) {
                time /= 1000.0; // milli
                post = "ms";
                if (time > 1000) {
                    time /= 1000.0; // seconds
                    post = "s";
                    if (time > 60) {
                        time /= 60;
                        post = "min";
                    }
                }
            }
        }
        return String.format("%.2f %s", time, post);
    }

    /**
     * Formats <code>fraction</code> or returns the percentage dependent on
     * <code>showPercentage</code>.
     * 
     * @param fraction the part of <code>total</code>
     * @param total a number representing 100%
     * @param showPercentage if <code>true</code> show the percentage, 
     *     if <code>false</code> the value of <code>fraction</code>
     * @return the formatted number
     * 
     * @since 1.00
     */
    public static String formatPercentageOrValue(long fraction, long total, 
        boolean showPercentage) {
        String result;
        if (showPercentage) {
            result = String.format("%d", fraction);
        } else {
            result = formatPercentage(fraction, total, null);
        }
        return result;
    }

    /**
     * Formats a percentage calculated from <code>fraction</code> and 
     * <code>total</code>. Percentages are given in 2 digits after
     * the floating point separator.
     * 
     * @param fraction the part of <code>total</code>
     * @param total a number representing 100%
     * @return the formatted percentage
     * 
     * @since 1.00
     */
    public static String formatPercentage(double fraction, double total) {
        return formatPercentage(fraction, total, null);
    }

    /**
     * Formats a percentage calculated from <code>fraction</code> and 
     * <code>total</code>. Percentages are given in 2 digits after
     * the floating point separator.
     * 
     * @param fraction the part of <code>total</code>
     * @param total a number representing 100%
     * @param prefix if not present (<b>null</b>), return <code>fraction</code>
     *   as value and percentage, else return the prefix and the percentage
     * @return the formatted percentage
     * 
     * @since 1.00
     */
    public static String formatPercentage(double fraction, double total, 
        String prefix) {
        double percentage;
        if (0 == total || 0 == fraction) {
            percentage = 0;
        } else {
            percentage = fraction / total * 100;
        }
        if (null == prefix) {
            return String.format("%.2f (%.2f%%)", fraction, percentage); 
        } else {
            return String.format("%s (%.2f%%)", prefix, percentage); 
        }
    }

    /**
     * Formats a percentage calculated from <code>fraction</code> and 
     * <code>total</code>. Percentages are given in 2 digits after
     * the floating point separator.
     * 
     * @param fraction the part of <code>total</code>
     * @param total a number representing 100%
     * @return the formatted percentage
     * 
     * @since 1.00
     */
    public static String formatPercentage(long fraction, long total) {
        return formatPercentage(fraction, total, null);
    }
    
    /**
     * Formats a percentage calculated from <code>fraction</code> and 
     * <code>total</code>. Percentages are given in 2 digits after
     * the floating point separator.
     * 
     * @param fraction the part of <code>total</code>
     * @param total a number representing 100%
     * @param prefix if not present (<b>null</b>), return <code>fraction</code>
     *   as value and percentage, else return the prefix and the percentage
     * @return the formatted percentage
     * 
     * @since 1.00
     */
    public static String formatPercentage(long fraction, long total, 
        String prefix) {
        double percentage;
        if (0 == total || 0 == fraction) {
            percentage = 0;
        } else {
            percentage = fraction / ((double) total) * 100;
        }
        if (null == prefix) {
            return String.format("%d (%.2f%%)", fraction, percentage); 
        } else {
            return String.format("%s (%.2f%%)", prefix, percentage); 
        }
    }
    
    /**
     * A helper method for printing the current stack trace in the order of 
     * call to the standard output stream.
     * 
     * @since 1.00
     */
    public static void printStackTace() {
        StackTraceElement[] elt = Thread.currentThread().getStackTrace();
        for (int e = elt.length - 1; e >= 0; e--) {
            System.out.println(" * " + elt[e].getClassName() + " " 
                + elt[e].getMethodName() + " " + elt[e].getLineNumber());
        }
    }

}
