package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ObjectPool;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Poolable;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/** 
 * Defines an ExitElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class ClearTemporaryDataElement extends RecordingStrategiesElement 
    implements Poolable<ClearTemporaryDataElement> {
    
    /**
     * Defines an object pool for this class.
     */
    public static final ObjectPool<ClearTemporaryDataElement> POOL 
        = new ObjectPool<ClearTemporaryDataElement>(
            new ClearTemporaryDataElement(), 500);
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    ClearTemporaryDataElement() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.CLEARTEMPORARYDATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.clearTemporaryData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ClearTemporaryDataElement create() {
        return new ClearTemporaryDataElement();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        POOL.release(this);
    }
}