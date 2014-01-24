package de.uni_hildesheim.sse.monitoring.runtime.boot;

// TODO remove

/**
 * A class providing static notifier instances to be called from instrumented
 * code in order to notify reading and writing from / to I/O devices. An 
 * abstract class is needed as this may be implemented internally by the 
 * recorder for some purpose.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class IoNotifier {

    // checkstyle: stop member visibility check
    
    /**
     * The notifier for network traffic.
     */
//    public static IoNotifier netNotifier = null;

    /**
     * The notifier for file traffic.
     */
//    public static IoNotifier fileNotifier = null;

    // checkstyle: resume member visibility check
    
    /**
     * Returns the valid instance for the given stream type. 
     * 
     * @param type the type to return the instance for
     * @return the related notifier
     * 
     * @since 1.00
     */
/*    private static IoNotifier getInstance(StreamType type) {
        IoNotifier notifier = null;
        switch(type) {
        case FILE:
            notifier = fileNotifier;
            break;
        case NET:
            notifier = netNotifier;
            break;
        default:
            break;
        }
        return notifier;
    }*/

    /**
     * Notifies that an amount of bytes was read on <code>type</code>.
     * 
     * @param type the stream type
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
/*    public static void notifyRead(StreamType type, int bytes) {
        IoNotifier notifier = getInstance(type);
        if (null != notifier) {
            notifier.notifyRead(bytes);
        }
    }*/

    /**
     * Notifies that an amount of bytes was written to <code>type</code>.
     * 
     * @param type the stream type
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
/*    public static void notifyWrite(StreamType type, int bytes) {
        IoNotifier notifier = getInstance(type);
        if (null != notifier) {
            notifier.notifyWrite(bytes);
        }
    }*/
    
    /**
     * Notifies that an amount of bytes was read on <code>type</code> for
     * <code>recId</code>.
     * 
     * @param type the stream type
     * @param recId the recording id
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
/*    public static void notifyRead(StreamType type, String recId, int bytes) {
        IoNotifier notifier = getInstance(type);
        if (null != notifier) {
            if (null == recId) {
                notifier.notifyRead(bytes);
            } else {
                notifier.notifyRead(recId, bytes);
            }
        }
    }*/

    /**
     * Notifies that an amount of bytes was written to <code>type</code> for
     * <code>recId</code>.
     * 
     * @param type the stream type
     * @param recId the recording id
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
/*    public static void notifyWrite(StreamType type, String recId, int bytes) {
        IoNotifier notifier = getInstance(type);
        if (null != notifier) {
            if (null == recId) {
                notifier.notifyWrite(bytes);
            } else {
                notifier.notifyWrite(recId, bytes);
            }
        }
    }*/

    /**
     * Notifies that an amount of bytes was read.
     * 
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
//    public abstract void notifyRead(int bytes);

    /**
     * Notifies that an amount of bytes was written by 
     * <code>caller</code>.
     * 
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
//    public abstract void notifyWrite(int bytes);

    /**
     * Notifies that an amount of bytes was read on this notifier for
     * <code>recId</code>.
     * 
     * @param recId the recording id
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
//    public abstract void notifyRead(String recId, int bytes);

    /**
     * Notifies that an amount of bytes was written on this notifier for
     * <code>recId</code>.
     * 
     * @param recId the recording id
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
//    public abstract void notifyWrite(String recId, int bytes);

}
