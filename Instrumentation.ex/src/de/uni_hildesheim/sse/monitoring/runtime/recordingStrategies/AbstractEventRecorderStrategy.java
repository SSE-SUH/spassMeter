package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.AssignToAllElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.ClearTemporaryDataElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.Constants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.EndSystemElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.EnterConfigurationElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.EnterElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.ExitElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.FinishRecordingElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.IoElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.MemoryAllocatedElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.MemoryFreedByRecIdElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.NotifyProgramRecordCreationElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.NotifyTimerElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.NotifyValueChangeElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.PrintCurrentState;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.PrintStatisticsElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.RecordingStrategiesElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.RegisterElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.StartRecordingElement;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.StopTimeRecordingElement;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.RecordingStrategiesElementLinkedList;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.ShutdownMonitor;

/**
 * Implements an abstract strategy for buffering the incoming calls into
 * events, buffering them and analyzing them in parallel.
 * 
 * @author Stephan Dederichs, Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractEventRecorderStrategy 
    extends AbstractRecorderStrategy {

    /**
     * <code>true</code> if elements should be pooled, otherwise
     * <code>false</code>. This affects the {@link EnterElement},
     * {@link ExitElement} and the {@link MemoryAllocatedElement}.
     */
    private static final boolean DOPOOLING = true;

    /**
     * Stores the generated elements for sending them to the specified server.
     */
//    private LinkedList<RecordingStrategiesElement> elements 
//        = new LinkedList<RecordingStrategiesElement>();
    private RecordingStrategiesElementLinkedList elements
        = new RecordingStrategiesElementLinkedList();

    /**
     * The internal lock for the message producer-consumer.
     */
    private Object lock = new Object();
    
    /**
     * <code>true</code> while recording, otherwise <code>false</code>.
     */
    private boolean record = true;

    /**
     * Stores the handling thread.
     */
    private HandleThread thread;
    
    /**
     * Creates a new recorder strategy.
     * 
     * @param storage the storage object
     * 
     * @since 1.00
     */
    public AbstractEventRecorderStrategy(StrategyStorage storage) {
        super(storage);
        ShutdownMonitor.setWaitEndSystemNotification(true);
    }

    /**
     * Starts the {@link HandleThread}.
     * 
     * @since 1.00
     */
    protected void start() {
        thread = new HandleThread();
        thread.start();
    }
    
    /**
     * Returns whether recording is activated.
     * 
     * @return <code>true</code> if recording is activated, <code>false</code>
     *   else
     * 
     * @since 1.00
     */
    protected boolean isRecording() {
        return record;
    }

    // hint: please avoid superfluous object creation in this class ;)
    // -> use pooling ;)

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enter(String recId, long now, ThreadsInfo threadsInfo, 
        boolean exclude) {
        if (DOPOOLING) {
            EnterElement element = EnterElement.POOL.getFromPool();
            element.setRecId(recId);
            element.setNow(now);
            element.setThreadsInfo(threadsInfo);
            element.setExclude(exclude);
            add(element);
        } else {
            add(new EnterElement(recId, now, threadsInfo, exclude));
        }
        return false; // do not release, we will do this in element instance
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assignAllTo(String recId, boolean enter, long now, 
        ThreadsInfo threadInfo) {
        add(new AssignToAllElement(recId, enter, now, threadInfo));
        return false; // do not release, we will do this in the element instance
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterConfiguration(String id) {
        add(new EnterConfigurationElement(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exit(String recId, long now, ThreadsInfo threadsInfo, 
        boolean exclude) {
        if (DOPOOLING) {
            ExitElement element = ExitElement.POOL.getFromPool();
            element.setRecId(recId);
            element.setNow(now);
            element.setThreadsInfo(threadsInfo);
            element.setExclude(exclude);
            add(element);
        } else {
            add(new ExitElement(recId, now, threadsInfo, exclude));
        }
        return false; // do not release, we will do this in element instance
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean finishRecording(long now, long threadId, 
        LongLongHashMap curCpuTime) {
        add(new FinishRecordingElement(now, threadId, curCpuTime));
        // no not set record to false here, do this in event handling loop
        return false; // do not release, we will do this in element instance
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void memoryAllocated(String recId, long threadId, long id, 
        long size) {
        MemoryAllocatedElement elt;
        if (DOPOOLING) {
            elt = MemoryAllocatedElement.POOL.getFromPool();
            elt.setData(recId, threadId, id, size);
        } else {
            elt = new MemoryAllocatedElement(recId, threadId, 
                id, size);
        }
        add(elt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean printStatistics(ProcessData data) {
        add(new PrintStatisticsElement(data));
        return false; // do not release, we will do this in element instance
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endSystem() {
        add(new EndSystemElement());
        // wait for end of event processing
        while (isRecording() || thread.isRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readIo(String recId, String caller, long threadId, int bytes, 
        StreamType type) {
        add(new IoElement(recId, caller, threadId, bytes, type, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(long threadId, long newId, long threadTicks, 
        long now) {
        add(new RegisterElement(threadId, newId, threadTicks, now));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void writeIo(String recId, String caller, long threadId, int bytes, 
        StreamType type) {
        add(new IoElement(recId, caller, threadId, bytes, type, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void memoryFreedByRecId(String recId, long size) {
        MemoryFreedByRecIdElement elt 
            = MemoryFreedByRecIdElement.POOL.getFromPool();
        elt.setData(recId, size);
        add(elt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stopTimeRecording(long now, ThreadsInfo threadInfo) {
        add(new StopTimeRecordingElement(now, threadInfo));
        return false; // do not release, we will do this in this class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startRecording(long now, long threadId, 
        long threadTicks) {
        add(new StartRecordingElement(now, threadId, threadTicks));
        record = true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    public void notifyTimer(String id, TimerState state, long now, 
        long threadId) {
        add(new NotifyTimerElement(id, state, now, threadId));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean printCurrentState(ProcessData data) {
        add(new PrintCurrentState(data));
        return false; // do not release, we will do this in element instance
    }
    
    /**
     * {@inheritDoc}
     */
    public void clearTemporaryData() {
        add(ClearTemporaryDataElement.POOL.getFromPool());
    }
    
    /**
     * Notifies about a changing attribute value.
     * 
     * @param id the identification of the attribute (may overlap with 
     *    recorder ids)
     * @param type the type of the value
     * @param value the new value (after changing)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public void notifyValueChange(String id, ValueType type, Object value) {
        add(new NotifyValueChangeElement(id, type, value));
    }
    
    /**
     * Notify the listeners about the program record creation (if not done 
     * implicitly before).
     */
    public void notifyProgramRecordCreation() {
        add(new NotifyProgramRecordCreationElement());
    }
    
    /**
     * Adds an element to the processing list.
     * 
     * @param elt element the element to be added
     * 
     * @since 1.00
     */
    private void add(RecordingStrategiesElement elt) {
        synchronized (lock) {
            if (null != elt) { // shall not be null
                elements.addLast(elt);
                lock.notify();
            }
        }
    }
 
    /**
     * Implements a thread which sends the gathered data to the specified
     * server.
     * 
     * @author Stephan Dederichs, Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class HandleThread extends Thread {

        /**
         * Stores whether this thread is running.
         */
        private boolean isRunning = true;
        
        /**
         * Responsible for sending the information to the server.
         * 
         * @since 1.00
         */
        @Override
        public void run() {
            while (isRecording()) {
                RecordingStrategiesElement e;
                synchronized (lock) {
                    while (elements.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                    e = elements.removeFirst();
                }
                int id = processEvent(e);
                if (Constants.ENDSYSTEM == id) {
                    record = false;
                    ShutdownMonitor.endSystemNotification();
                }
            }
            isRunning = false;
        }
        
    }
    
    /**
     * Processes a single element.
     * 
     * @param event the event to be processed
     * @return the identification of the processed event
     * 
     * @since 1.00
     */
    protected int processEvent(RecordingStrategiesElement event) {
        int result = event.getIdentification();
        try {
            handleEvent(event);
//            event.clear();
            event.release();
        } catch (HandleException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        return result;
    }

    /**
     * A specific exception type which may occur during {@link #handleEvent}.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @SuppressWarnings("serial")
    protected class HandleException extends Exception {
       
        /**
         * Creates a delegating exception.
         * 
         * @param throwable the original cause
         * 
         * @since 1.00
         */
        public HandleException(Throwable throwable) {
            super(throwable);
        }
        
    }
    
    /**
     * Handles an event.
     * 
     * @param event the event to be handled
     * @throws HandleException any kind of throwable exception which should 
     *    cause aborting the event loop
     * 
     * @since 1.00
     */
    protected abstract void handleEvent(RecordingStrategiesElement event) 
        throws HandleException;

}