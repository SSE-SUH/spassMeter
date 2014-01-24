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
 * Defines a PrintStatisticsElement. 
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class PrintStatisticsElement extends RecordingStrategiesElement {

    /**
     * Stores the {@link ProcessData}.
     */
    private ProcessData data;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public PrintStatisticsElement() {
    }
    
    /**
     * Creates a new instance with the given parameter.
     * 
     * @param data the {@link ProcessData}
     * 
     * @since 1.00
     */
    public PrintStatisticsElement(ProcessData data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        data.send(out);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        data = ProcessData.getFromPool();
        data.read(in);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.PRINT_STATISTICS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.printStatistics(data);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        ProcessData.release(data);
    }
}