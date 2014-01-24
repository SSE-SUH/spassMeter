package de.uni_hildesheim.sse.monitoring.runtime.recording;

//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

// TODO remove

/**
 * A concrete stream notifier implementation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class StreamNotifier /*extends IoNotifier*/ {

    /**
     * Stores the stream type.
     */
    //private StreamType type;
    
    /**
     * Creates a new stream notifier.
     * 
     * @param type the stream type to notify for
     * 
     * @since 1.00
     */
    /*public StreamNotifier(StreamType type) {
        this.type = type;
    }*/
    
    /**
     * Notifies that an amount of bytes was read.
     * 
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
    /*@Override
    public void notifyRead(int bytes) {
        RecorderFrontend.instance.readIo(null, null, bytes, type);
    }*/

    /**
     * Notifies that an amount of bytes was written by 
     * <code>caller</code>.
     * 
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
    /*@Override
    public void notifyWrite(int bytes) {
        RecorderFrontend.instance.writeIo(null, null, bytes, type);
    }*/

    /**
     * Notifies that an amount of bytes was read on this notifier for
     * <code>recId</code>.
     * 
     * @param recId the recording id
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
    /*@Override
    public void notifyRead(String recId, int bytes) {
        RecorderFrontend.instance.readIo(recId, null, bytes, type);
    }*/

    /**
     * Notifies that an amount of bytes was written on this notifier for
     * <code>recId</code>.
     * 
     * @param recId the recording id
     * @param bytes the number of bytes
     * 
     * @since 1.00
     */
    /*@Override
    public void notifyWrite(String recId, int bytes) {
        RecorderFrontend.instance.writeIo(recId, null, bytes, type);
    }*/
    
}
