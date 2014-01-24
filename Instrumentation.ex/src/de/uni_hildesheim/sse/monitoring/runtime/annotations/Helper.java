package de.uni_hildesheim.sse.monitoring.runtime.annotations;

/**
 * Some helper methods / constants for the annotations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Helper {

    /**
     * Enables or disables the IO handling strategy (to be removed when 
     * modifications are finished).
     */
    //public static final boolean NEW_IO_HANDLING = true;

    /**
     * Defines the internal id for overhead monitoring, i.e. monitoring the 
     * monitoring framework itself. Do not use this for annotating your
     * classes ({@value}).
     */
    public static final String RECORDER_ID = "*recorder*";

    /**
     * Defines the internal id for explicitly monitoring excluded parts. Do not 
     * use this for annotating your classes ({@value}).
     */
    public static final String EXCLUDED_ID = "*excluded*";
    
    /**
     * Defines the prefix for internally created ids ({@value}).
     */
    public static final String PSEUDO_ID_PREFIX = "*pseudo";
    
    /**
     * Defines the internal id for recording the monitored program itself. Do
     * not use this for annotating your classes ({@value}).
     */
    public static final String PROGRAM_ID = "program";
    
    /**
     * Currently used for abstract classes ({@value}). Consider generically 
     * for recording .
     */
    public static final String IGNORE_ID = "*";

    /**
     * Used for ids which are defined generically and should consider the
     * thread-stacked context id (if present).
     */
    public static final String CONTEXTUALIZE_ID = "*";

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Helper() {
    }
    
    /**
     * Returns if this annotation should be ignored.
     * 
     * @param group the annotation to be tested
     * @return <code>true</code> if it should be ignored, <code>false</code> 
     *   else
     * 
     * @since 1.00
     */
    public static boolean ignore(Monitor group) {
        boolean ignore = false;
        String[] ids = group.id();
        for (int i = 0; !ignore && i < ids.length; i++) {
            ignore = ignore(ids[i].trim());
        }
        return ignore;
    }
    
    /**
     * Returns if the given <code>recId</code> is equal to the (first and only)
     * recorder id in <code>group</code>.
     * 
     * @param group the group to be taken into account
     * @param recId the recorder id to test for
     * @return <code>true</code> if equals, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean isId(Monitor group, String recId) {
        boolean ok;
        String[] ids = group.id();
        if (1 == ids.length) {
            ok = ids[0].equals(recId);
        } else {
            ok = false;
        }
        return ok;
    }
    
    /**
     * Returns whether the given <code>recId</code> is a pseudo id.
     * 
     * @param recId the id to be tested
     * @return <code>true</code> if <code>recId</code> is a pseudo id, 
     *    <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean isPseudo(String recId) {
        return null != recId && recId.startsWith(PSEUDO_ID_PREFIX);
    }
    
    /**
     * Returns a pseudo id.
     * 
     * @param id a numeric id
     * @return the pseudo id including the numeric id
     * 
     * @since 1.00
     */
    public static String createPseudo(int id) {
        return PSEUDO_ID_PREFIX + id;
    }
    
    /**
     * Returns if <code>recId</code> should be ignored.
     * 
     * @param recId the id to be tested
     * @return <code>true</code> if it should be ignored, <code>false</code> 
     *   else
     * 
     * @since 1.00
     */
    public static boolean ignore(String recId) {
        return IGNORE_ID.equals(recId);
    }
    
    /**
     * Returns if the given id requires contextualization.
     * 
     * @param id the id to be checked
     * @return <code>true</code> if it is an id which requires 
     *    contextualization, <code>false</code> else
     */
    public static boolean isContextualizeId(String id) {
        return null != id && id.startsWith(CONTEXTUALIZE_ID);
    }

    /**
     * Checks the ID (for ignored ids) and returns the id (or a modified return
     * value).
     * 
     * @param recId the recording identification to be checked
     * @return <code>recId</code> or <b>null</b> in case that it 
     *   should be ignored
     * 
     * @since 1.00
     */
    public static String getCheckedId(String recId) {
        String result = recId;
        if (ignore(recId)) {
            result = null;
        }
        return result;
    }

    /**
     * Trims the recording id, i.e. removes leading and trailing spaces.
     * 
     * @param recId the recording id (may be <b>null</b>)
     * @return the trimmed id
     * 
     * @since 1.00
     */
    public static String trimId(String recId) {
        return (null == recId ? null : recId.trim());
    }

}
