package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.ProcessData;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/**
 * Defines an event for signalling that the current state should be printed.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class PrintCurrentState extends RecordingStrategiesElement {

    /**
     * Stores the {@link ProcessData}.
     */
    private ProcessData data;

    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public PrintCurrentState() {
    }
    
    /**
     * Creates a new empty instance.
     * 
     * @param data the {@link ProcessData} of the monitored program
     * 
     * @since 1.00
     */
    public PrintCurrentState(ProcessData data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        if (null != data) {
            out.writeShort(1);
            data.send(out);
        } else {
            out.writeShort(0);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        if (1 == in.readShort()) {
            data = ProcessData.POOL.getFromPool();
            data.read(in);
        } else {
            data = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.PRINTCURRENTSTATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.printCurrentState(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (null != data) {
            ProcessData.POOL.release(data);
        }
    }
}