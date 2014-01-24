package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/**
 * Defines a RecordingStrategieseElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public abstract class RecordingStrategiesElement {

    /**
     * Writes this instance to the given output stream.
     * 
     * @param out the output stream
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    public abstract void send(DataOutputStream out) throws IOException;

    /**
     * Reads this instance from the given input stream.
     * 
     * @param in the input stream
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    public abstract void read(DataInputStream in) throws IOException;

    /**
     * Returns the unambiguous identification of the element.
     * 
     * @return the identification of the element
     * 
     * @since 1.00
     */
    public abstract int getIdentification();

    /**
     * Hands the information of the element to the {@link RecorderStrategy}.
     * 
     * @param strategy the {@link RecorderStrategy}
     * 
     * @since 1.00
     */
    public abstract void process(RecorderStrategy strategy);

    /**
     * If a pool is used this method is for releasing the element from the pool.
     * 
     * @since 1.00
     */
    public abstract void clear();

    /**
     * Release this element from the pool if this instance is 
     * {@link de.uni_hildesheim.sse.monitoring.runtime.boot.Poolable}.
     * 
     * @since 1.00
     */
    public void release() {
    }
    
}
