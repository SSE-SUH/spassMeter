package de.uni_hildesheim.sse.monitoring.runtime.recording;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    IRecordingEndListener;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.ObjectSizeCache;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IPluginParameter;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.Plugin;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.internal.
    InternalPluginRegistry;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;

// TODO extend protocol for value notifications and timer events

/**
 * Implements the frontend to be called from instrumented code. This class is
 * responsible for translating any program-specific information such as thread
 * instances, object instances or platform/OS measurements to symbolic 
 * information such as thread ids, recording ids or measurement values, 
 * particularly for monitoring over network. Therefore most
 * members are static. Currently, we use individual methods for the "events"
 * registered in the code. In fact, this might be done via event objects but
 * so far the impact on the memory footprint is not clear (TBD).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Recorder extends RecorderFrontend 
    implements IMemoryUnallocationReceiver {

    // TODO revise static attributes
    
    /**
     * Stores the size of a map entry (for overhead recording).
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public static final long MAP_ENTRY_SIZE;

    /**
     * Stores the size of a collection entry (for overhead recording).
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public static final long COLLECTION_ENTRY_SIZE;

    /**
     * Stores if the recorder (i.e. the static sizes) should be calibrated.
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public static final boolean CALIBRATE_RECORDER = false;

    /**
     * Stores if recording is enabled.
     */
    protected static boolean isRecording = false;
    
    /**
     * Stores the threadId the program started with.
     */
    protected static long programThreadId;
    
    /**
     * Stores if overhead recording is enabled, i.e. if {@link #isRecording}
     * and overhead recording.
     */
    //@Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    //private static boolean isOverheadRecording = false;

    /**
     * Stores the recording strategy. 
     */
    private static final RecorderStrategy STRATEGY;
    
    /**
     * Stores the thread-id-context stacks.
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    private static final LongHashMap<ArrayList<String>> CONTEXT_STACKS 
        = new LongHashMap<ArrayList<String>>();

    /**
     * Stores the thread stack sizes.
     */
    private static final LongLongHashMap THREAD_STACKS;

    /**
     * Stores whether unallocation recording shall be done.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE, value = "false")
    private static boolean doUnallocationRecording = false;
    
    static {
        LongLongHashMap threadStacks = null;
        Configuration conf = Configuration.INSTANCE;
        if (conf.programUseFromJvm()) {
            threadStacks = new LongLongHashMap();
        }
        THREAD_STACKS = threadStacks;
    }
        
    /**
     * Stores the plugins.
     */
    private static ArrayList<Plugin> plugins = new ArrayList<Plugin>();
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    Recorder() {
    }
    
    /**
     * Produces calibration if needed and initializes the strategy.
     */
    static {
        if (CALIBRATE_RECORDER) {
            // do this only once or on changes of the Java library
            java.util.Map<Object, Object> test 
                = new java.util.HashMap<Object, Object>();
            test.put(test, test);
            long size = 0;
            for (Map.Entry<Object, Object> entry : test.entrySet()) {
                size = ObjectSizeProvider.getInstance().getObjectSize(entry);
                break;
            }
            MAP_ENTRY_SIZE = size;
            // be careful, this may include the size of the array itself
            Object array = new Object[2];
            COLLECTION_ENTRY_SIZE 
                = ObjectSizeProvider.getInstance().getObjectSize(array) / 2;
            System.out.println("Map.Entry size " + MAP_ENTRY_SIZE 
                + " ref size " + COLLECTION_ENTRY_SIZE );
        } else {
            MAP_ENTRY_SIZE = 24;
            COLLECTION_ENTRY_SIZE = 12;
        }

        IPluginParameter parameter = new IPluginParameter() {
            
            @Override
            public String get(String key) {
                return Configuration.INSTANCE.getUnrecognizedParam(key);
            }
        };
        // do not refer directly as this is optional!
        //registerPlugin("de.uni_hildesheim.sse.jmx.plugins." 
        //    + "JMXPlugin", parameter);
        //registerPlugin("de.uni_hildesheim.sse.wildcat.plugins." 
        //    + "WildCATPlugin", parameter);
        String tmp = System.getProperty("spassmeter.plugins");
        if (null != tmp) {
            StringTokenizer tokens = new StringTokenizer(tmp, ",");
            while (tokens.hasMoreTokens()) {
                registerPlugin(tokens.nextToken(), parameter);
            }
        }
        try {
            ClassLoader loader = Recorder.class.getClassLoader();
            if (null == loader) {
                loader = ClassLoader.getSystemClassLoader();
            }
            InputStream in = loader.getResourceAsStream("plugin.lst");
            if (null != in) {
                LineNumberReader lnr = new LineNumberReader(
                    new InputStreamReader(in));
                String line;
                do {
                    line = lnr.readLine();
                    if (null != line) {
                        registerPlugin(line, parameter);
                    }
                } while (null != line);
                lnr.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        Configuration configuration = Configuration.INSTANCE;
        switch (configuration.getRecordingType()) {
        case TCP:
            STRATEGY = new TCPRecorderStrategy();
            break;
        case LOCAL_ASYNCHRONOUS:
            STRATEGY = new DefaultEventRecorderStrategy(
                new DefaultRecorderStrategy(new TabFormatter()));
            break;
        case LOCAL_SYNCHRONOUS:
        default:
            STRATEGY = new DefaultRecorderStrategy(new TabFormatter());
            break;
        }
        /*if (null == configuration.getTCPHostname() 
            || configuration.getTCPPort() <= 0) {
            STRATEGY = new DefaultEventRecorderStrategy(
                new DefaultRecorderStrategy(new TabFormatter()));
            //STRATEGY = new DefaultRecorderStrategy(new TabFormatter());
        } else {
            STRATEGY = new TCPRecorderStrategy();
        }*/
        
        STRATEGY.notifyProgramRecordCreation();
    }

    /**
     * Cause loading this class.
     * 
     * @since 1.00
     */
    public static void initialize() {
        RecorderFrontend.instance = new Recorder();
    }
    
    /**
     * Registers the given plugin.
     * 
     * @param className the name of the class to be registered
     * @param params unrecognized configuration params
     * 
     * @since 1.00
     */
    private static void registerPlugin(String className, 
        IPluginParameter params) {
        try {
            Class<?> pluginClass = Class.forName(className);
            if (Plugin.class.isAssignableFrom(pluginClass)) {
                Plugin plugin = (Plugin) pluginClass.newInstance(); 
                plugins.add(plugin);
                plugin.start(params);
            }
        } catch (ClassNotFoundException e) {
            Configuration.LOG.info("Plugin class " + className + " not found");
        } catch (InstantiationException e) {
            Configuration.LOG.info("Plugin class " + className 
                + " cannot be instantiated (functional)");
        } catch (IllegalAccessException e) {
            Configuration.LOG.info("Plugin class " + className 
                + " cannot be instantiated (illegal access)");
        }
    }

    /**
     * Registers a given class for recording. Optional, a (group) of
     * <code>id</code>s might be specified to which the measurements are 
     * assigned to. Ids are unique strings without inherent semantics. If
     * not given, the <code>className</code> is used for grouping and assigning
     * measurements (implicit 1-groups). [Java call, native call]
     * 
     * @param className the name of the class measurements should be registered 
     *        for
     * @param settings the monitoring group settings including all configuration
     *        information (reference should not be stored, will be freed 
     *        explicitly)
     * 
     * @since 1.00
     */
    @Override
    public final void registerForRecording(String className, 
        MonitoringGroupSettings settings) {
        Configuration mConf = Configuration.INSTANCE;
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        MonitoringGroupConfiguration conf;
        String recId = mConf.getRecId(settings.getId());
        if (null != recId) {
            // allow late overriding
            conf = mConf.getMonitoringGroupConfiguration(recId);
            if (conf == MonitoringGroupConfiguration.DEFAULT) {
                // dont override just with defaults!
                conf = null;
            }
            conf = null;
        } else {
            conf = null;
        }
        if (null == conf) {
            conf = MonitoringGroupConfiguration.create(
                settings.getDebugStates(), settings.getAccountingType(), 
                settings.getResources());
        }
        Lock.registerGroup(className, recId, conf);
        // boolean as parameter
        STRATEGY.registerForRecording(className, recId, conf, settings);
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }   

    /**
     * Register the given input stream as overhead stream, i.e. set the 
     * attribute added by instrumentation via reflection so that the recorder
     * is notified via {@link Recorder.OverheadStreamIoNotifier}. [Java call]
     * 
     * @param in the stream to be registered
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    @Override
    public final void registerAsOverheadStream(java.io.InputStream in) {
        if (null != in && Configuration.INSTANCE.instrumentJavaLib()) {
            try {
                Class<?> c = in.getClass();
                if (c.getName().equals("java.net.SocketInputStream")) {
                    Field f = c.getDeclaredField("recId");
                    f.setAccessible(true);
                    f.set(in, Helper.RECORDER_ID);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Register the given output stream as overhead stream, i.e. set the 
     * attribute added by instrumentation via reflection so that the recorder
     * is notified via {@link Recorder.OverheadStreamIoNotifier}. [Java call]
     * 
     * @param out the stream to be registered
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    @Override
    public final void registerAsOverheadStream(
        java.io.OutputStream out) {
        if (null != out && Configuration.INSTANCE.instrumentJavaLib()) {
            try {
                Class<?> c = out.getClass();
                if (c.getName().equals("java.net.SocketOutputStream")) {
                    Field f = c.getDeclaredField("recId");
                    f.setAccessible(true);
                    f.set(out, Helper.RECORDER_ID);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Returns whether unallocation recording shall be performed.
     * 
     * @return <code>true</code> if unallocations shall be performed, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    static boolean doUnallocationRecording() {
        return doUnallocationRecording;
    }
    
    /**
     * Notifies about start of monitoring. [Java call, native call]
     * 
     * @since 1.00
     */
    @Override
    public void notifyProgramStart() {
        GathererFactory.loadLibrary();
        programThreadId = SystemMonitoring.getCurrentThreadId();
        // pseudo, -> memAcc LOCK
        Lock.pushToStack(programThreadId, Helper.RECORDER_ID); 
        if (!isRecording) {
            isRecording = true;
            doUnallocationRecording = Configuration.INSTANCE.
                getMemoryAccountingType().considerUnallocation();
            long[] ids = SystemMonitoring.getAllThreadIds();
            long now = System.nanoTime();
            // register all JVM threads including the main thread
            for (int i = 0; i < ids.length; i++) {
                STRATEGY.register(ids[i], RecorderStrategy.REGISTER_FORCE, 
                    SystemMonitoring.getTicks(ids[i]), now);
            }
            SystemMonitoring.startTimer();
            STRATEGY.startRecording(now, programThreadId, 
                SystemMonitoring.getTicks(programThreadId));
            // must be after startRecording!
            //isOverheadRecording = Configuration.INSTANCE.recordOverhead();
            // do this always, particularly in case of TCP recording
        }
    }
    
    /**
     * Notifies about end of monitoring (this might occur at different point
     * of time than {@link #endSystem()} and at multiple times). [Java call, 
     * native call]
     * 
     * @since 1.00
     */
    @Override
    public void notifyProgramEnd() {
        if (isRecording) {
            // tid may be shutdownthread
            SystemMonitoring.stopTimer();
            long now = System.nanoTime();
            ThreadsInfo info = SystemMonitoring.getThreadInfo(programThreadId);
            boolean release = STRATEGY.stopTimeRecording(now, info);
            // map is currently not pooled - ignore return value
            STRATEGY.finishRecording(now, programThreadId,
                SystemMonitoring.getAllThreadTicks());
            isRecording = false;
            //isOverheadRecording = false;
            if (release) {
                ThreadsInfo.POOL.release(info);
            }
            Configuration.INSTANCE.close();
            for (int i = 0; i < plugins.size(); i++) {
                plugins.get(i).stop();
            }
            IRecordingEndListener listener = 
                InternalPluginRegistry.getRecordingEndListener();
            if (null != listener) {
                listener.notifyRecordingEnd();
            }
            // pseudo, see above
            Lock.popFromStack(programThreadId, Helper.RECORDER_ID); 
        }
    }
    
    /**
     * Changes if automated variability detection should be enabled or not. 
     * [Java call, native call]
     * 
     * @param enable <code>true</code> if it should be enabled (can be 
     * switched off by annotation) or if manual notification via annotation
     * should be considered only
     * 
     * @since 1.00
     */
    @Override
    public final void enableVariabilityDetection(boolean enable) {
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        STRATEGY.enableVariabilityDetection(enable);
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }
    
    /**
     * Notifies about a started thread. [Java call]
     * 
     * @param thread the started thread
     * 
     * @since 1.00
     */
    @Override
    public final void notifyThreadStart(Thread thread) {
        notifyThreadStart(thread.getId());
    }

    /**
     * Notifies about a started thread. [native call]
     * 
     * @param newThreadId the identification of the started thread
     * 
     * @since 1.00
     */
    @Override
    public final void notifyThreadStart(long newThreadId) {
        // register the new thread with the currently running thread
        long tid = SystemMonitoring.getCurrentThreadId();
        
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        STRATEGY.register(tid, newThreadId, 
            SystemMonitoring.getTicks(tid), 
            System.nanoTime());
        if (null != THREAD_STACKS) {
            synchronized (THREAD_STACKS) {
                // synchronize with DefaultRecorderStrategy.register
                if (THREAD_STACKS.containsKey(tid)) {
                    long count = THREAD_STACKS.get(tid);
                    if (count > 0) {
                        THREAD_STACKS.put(newThreadId, count);
                    }
                }
            }
        }
        Lock.THREAD_STACKS.clone(tid, newThreadId);
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }
    
    /**
     * Notifies about the end of the current thread. [Java call]
     * 
     * @since 1.00
     */
    @Override
    public final void notifyThreadEnd() {
        notifyThreadEnd(Thread.currentThread().getId());
    }

    /**
     * Notifies about an ended thread. [native call]
     * 
     * @param threadId the identification of the ended thread
     * 
     * @since 1.00
     */
    @Override
    public final void notifyThreadEnd(long threadId) {
        long accMem = Lock.isStackTopMemoryAccounting(threadId);
        STRATEGY.register(threadId, RecorderStrategy.REGISTER_END, 
            SystemMonitoring.getTicks(threadId), 
            System.nanoTime());
        Lock.THREAD_STACKS.remove(threadId);
        Lock.setStackTopMemoryAccounting(threadId, accMem);
    }
        
    /**
     * Notifies that the following values (if not further calls 
     * to this method or other recording ids occur) should be assigned
     * to the given <code>recId</code>. [Java call, native call]
     * 
     * @param recId the recording id (may be also the variability prefix)
     * @param enter <code>true</code> if this is a method enter event, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public final void assignAllTo(String recId, boolean enter) {
        //TODO add overhead
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        ThreadsInfo info = SystemMonitoring.getThreadInfo(tid);
        if (STRATEGY.assignAllTo(recId, enter, System.nanoTime(), info)) {
            ThreadsInfo.POOL.release(info);
        }
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }
    
    /**
     * Returns an unique identification for an object.
     * 
     * @param object the object to return the identification for
     * @return the identification of <code>object</code>
     * 
     * @since 1.00
     */
    protected static final long getId(Object object) {
        return System.identityHashCode(object);
    }
        
    /**
     * Notifies about the end of the monitored program. [Java call, native call]
     * 
     * @since 1.00
     */
    @Override
    public final void endSystem() {
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        STRATEGY.endSystem();
        SystemMonitoring.finishTimer();
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }

    /**
     * Notifies the recorder about a method being called. [Java call, 
     * native call]
     * 
     * @param caller the type of the caller
     * @param recId the recorder id (may be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * 
     * @since 1.00
     */
    @Override
    public void enter(String caller, String recId, 
        boolean exclude, boolean directId) {
        if (isRecording) {
            long tid = SystemMonitoring.getCurrentThreadId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            if (null != THREAD_STACKS) {
                synchronized (THREAD_STACKS) {
                    if (!THREAD_STACKS.containsKey(tid)) {
                        THREAD_STACKS.put(tid, 1);
                    } else {
                        THREAD_STACKS.put(tid, 
                            THREAD_STACKS.get(tid) + 1);
                    }
                }
            }
            /*long now;
            if (isOverheadRecording) {
                now = System.nanoTime();
            } else {
                now = 0;
            }*/
            recId = assignId(recId, caller, directId);
            ThreadsInfo info = SystemMonitoring.getThreadInfo(tid);
            if (STRATEGY.enter(recId, System.nanoTime(), info, exclude)) {
                ThreadsInfo.POOL.release(info);
            }

            /*if (isOverheadRecording) {
                long diff = System.nanoTime() - now;
                //long frac = SystemMonitoring.estimateTimeFraction(diff, info);
                STRATEGY.overhead(diff, 0, recId, tid);
            }*/
            Lock.setStackTopMemoryAccounting(tid, accMem);
            Lock.pushToStack(tid, recId);
        }
    }
    
    /**
     * Notifies the recorder about a method being exited. [Java call, 
     * native call]
     * 
     * @param caller the type of the caller
     * @param recId the recorder id (may be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * 
     * @since 1.00
     */
    @Override
    public void exit(String caller, String recId, 
        boolean exclude, boolean directId) {
        if (isRecording) {
            long tid = SystemMonitoring.getCurrentThreadId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            recId = assignId(recId, caller, directId);            
            if (accMem > 0) {
                STRATEGY.memoryAllocated(recId, tid, 0, accMem);
            }
            ThreadsInfo info = SystemMonitoring.getThreadInfo(tid);
            if (STRATEGY.exit(recId, System.nanoTime(), info, exclude)) {
                ThreadsInfo.POOL.release(info);
            }

            if (null != THREAD_STACKS) {
                synchronized (THREAD_STACKS) {
                    // if threadId was not put before this will cause a 
                    // runtime exception
                    if (THREAD_STACKS.containsKey(tid)) {
                        long count = THREAD_STACKS.get(tid) - 1;
                        if (count > 0) {
                            THREAD_STACKS.put(tid, count);
                        } else {
                            THREAD_STACKS.remove(tid);
                        }
                    }
                }
            }

            
            Lock.setStackTopMemoryAccounting(tid, accMem);
            Lock.popFromStack(tid, recId);
            
            if (doUnallocationRecording) {
                SystemMonitoring.MEMORY_DATA_GATHERER
                    .receiveUnallocations(this);
            }
        }
    }
    
    /**
     * Notify the recorder that a new variability configuration might have 
     * been entered. The recorder should resolve the concrete configuration
     * by tracking the configuration activations. [Java call, native call]
     * 
     * @param ids an optional list of group identifications
     * 
     * @since 1.00
     */
    @Override
    public void configurationChange(String ids) {
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        STRATEGY.enterConfiguration(ids);
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }
    
    /**
     * Notifies the recorder about an object allocated to memory. Please note
     * that dependent on the concrete instrumentation not all allocated objects
     * might be cleaned up. [Java call]
     * 
     * @param allocated the allocated
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    @Override
    public void memoryAllocated(Object allocated) {
        if (isRecording)  {
            long tid = SystemMonitoring.getCurrentThreadId();
            RecordingStack stack = Lock.THREAD_STACKS.get(tid);
            if (null != stack) {
                long account = stack.top(-1);
                if (account >= 0) {
                    long size = ObjectSizeCache.INSTANCE.getSize(
                        allocated, true);
                    account += size;
                    if (doUnallocationRecording) {
                        stack.recordUnallocation(allocated, size);
                    }
                    stack.top(account);
                }
            } 
            
/*            
            // TODO cleanup -> native
            if (false && account>=0) {
                String recId = null;
                //MemoryAccountingType acc = Configuration.
                //    INSTANCE.getMemoryAccountingType();
                //boolean nativeUnallocation = acc.nativeUnallocation();
                long tag;
                tag = 0;
                //if (nativeUnallocation || (acc.atFinalizer() 
                //    && Configuration.INSTANCE.isInstrumented(
                //        allocated.getClass().getName()))) {
                //    tag = getId(allocated);
                //} else {
                //    tag = 0;
                //}
                STRATEGY.memoryAllocated(recId, tid, tag, 
                    ObjectSizeCache.INSTANCE.getSize(allocated, true));
                //if (nativeUnallocation) {
                //    boolean tagIt = true;
                //    if (null != THREAD_STACKS) {
                //        synchronized (THREAD_STACKS) {
                //            tagIt = THREAD_STACKS.containsKey(threadId);
                //        }
                //    }
                //    if (tagIt) {
                //        GathererFactory.getMemoryDataGatherer()
                //            .tagObject(allocated, tag);
                //    }
                //}
                stack.top(account);
            }*/
        }
    }

    /**
     * Notifies the recorder about an object allocated to memory. Please note
     * that sending the notification about an unallocated object is the 
     * responsibility of the caller. [native call]
     * 
     * @param tag the identification of the memory object, e.g. its memory 
     *   address
     * @param size the allocated memory size
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    @Override
    public void memoryAllocated(long tag, long size) {
        if (isRecording)  {
            long tid = SystemMonitoring.getCurrentThreadId();
            RecordingStack stack = Lock.THREAD_STACKS.get(tid);
            if (null != stack) {
                long account = stack.top(-1);
                if (account >= 0) {
                    account += size;
                    if (doUnallocationRecording) {
                        stack.recordUnallocation(tag, size);
                    }
                    stack.top(account);
                }
            } 
        }
/*        
        long tid = SystemMonitoring.getCurrentThreadId();
        RecordingStack stack = Lock.THREAD_STACKS.get(tid);
        long account;
        if (null != stack) {
            account = stack.top(-1);
        } else {
            account = -1;
        }
        if (account >= 0) {
            STRATEGY.memoryAllocated(null, tid, tag, size);
            stack.top(account);
        }*/
    }

    /**
     * Notifies the recorder about an object allocated to memory in the sense 
     * of adjusting an individual allocation due to value notifications. 
     * [Java call]
     * 
     * @param tag an object which serves as the identification for object size 
     *   to be freed
     * @param size the allocated memory size (considered as increment or 
     *   decrement to the actual size)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    @Override
    public final void memoryAllocated(Object tag, long size) {
        if (isRecording)  {
            long tid = SystemMonitoring.getCurrentThreadId();
            RecordingStack stack = Lock.THREAD_STACKS.get(tid);
            if (null != stack) {
                long account = stack.top(-1);
                if (account >= 0) {
                    account += size;
                    if (doUnallocationRecording) {
                        stack.recordUnallocation(tag, size);
                    }
                    stack.top(account);
                }
            } 
        }        
        // TODO CLEANUP
/*        long tid = SystemMonitoring.getCurrentThreadId();
        RecordingStack stack = Lock.THREAD_STACKS.get(tid);
        long account;
        if (null != stack) {
            account = stack.top(-1);
        } else {
            account = -1;
        }
        if (account >= 0) {
            STRATEGY.memoryAllocated(null, tid, getId(tag), size);
            stack.top(account);
        }*/
    }
    
    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented, garbage collector not called
     * when JVM is running, etc.). [Java call]
     * 
     * @param freed the freed object
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    @Override
    public void memoryFreed(Object freed) {
        if (isRecording && null != freed) {
            memoryFreed(getId(freed), 
                ObjectSizeCache.INSTANCE.getSize(freed, false));
        }
    }

    /**
     * Notifies the recorder about an object freed from memory. This method
     * is required for explicit notifications. [Java call]
     * 
     * @param tag an object which serves as the identification for object size 
     *   to be freed
     * @param size the size of the freed object (considered as increment or 
     *   decrement to the actual size)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    @Override
    public final void memoryFreed(Object tag, long size) {
        if (isRecording)  {
            long tid = SystemMonitoring.getCurrentThreadId();
            RecordingStack stack = Lock.THREAD_STACKS.get(tid);
            if (null != stack) {
                long account = stack.top(-1);
                if (account >= 0) {
                    // no change of account here
                    if (doUnallocationRecording) {
                        stack.recordUnallocation(tag, size);
                    }
                    stack.top(account);
                }
            } 
        }        
        // TODO cleanup
        //memoryFreed(getId(tag), size);
    }
    
    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented, garbage collector not called
     * when JVM is running, etc.). [Java call, native call]
     * 
     * @param tag the identification of the object to be freed, e.g. its memory
     *   address
     * @param size the size of the freed object (ignored)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    @Override
    public void memoryFreed(long tag, long size) { // TODO remove size
        if (isRecording)  {
            long tid = SystemMonitoring.getCurrentThreadId();
            RecordingStack stack = Lock.THREAD_STACKS.get(tid);
            if (null != stack) {
                long account = stack.top(-1);
                if (account >= 0) {
                    // no change of account here
                    SystemMonitoring.MEMORY_DATA_GATHERER.
                        recordUnallocationByTag(tag);
                    stack.top(account);
                }
            } 
        }
        // TODO cleanup
        /*if (isRecording && 0 != tag) {
            long tid = SystemMonitoring.getCurrentThreadId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            STRATEGY.memoryFreed(tag, size);
            Lock.setStackTopMemoryAccounting(tid, accMem);
        }*/
    }

    /**
     * Notifies the recorder about an amount of bytes read from some I/O 
     * channel. [Java call, native call]
     * 
     * @param recId an the target group identification
     * @param caller the type of the caller
     * @param bytes the number of bytes
     * @param type the type of the channel
     * @return bytes
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    @Override
    public int readIo(String recId, String caller, 
        int bytes, StreamType type) {
        if (isRecording && bytes > 0) {
            long tid = SystemMonitoring.getCurrentThreadId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            recId = Helper.getCheckedId(recId);
            //recId = assignId(recId, caller);
            STRATEGY.readIo(recId, caller, tid, bytes, type);
            Lock.setStackTopMemoryAccounting(tid, accMem);
        }
        return bytes;
    }

    /**
     * Notifies the recorder about an amount of bytes written to some I/O 
     * channel. [Java call, native call]
     * 
     * @param recId an the target group identification
     * @param caller the type of the caller
     * @param bytes the number of bytes
     * @param type the type of the channel
     * @return bytes
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    @Override
    public int writeIo(String recId, String caller,
        int bytes, StreamType type) {
        if (isRecording && bytes > 0) {
            long tid = SystemMonitoring.getCurrentThreadId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            recId = Helper.getCheckedId(recId);
            //recId = assignId(recId, caller);
            STRATEGY.writeIo(recId, caller, tid, bytes, type);
            Lock.setStackTopMemoryAccounting(tid, accMem);
        }
        return bytes;
    }
    
    /**
     * Returns if the specified string ends with a given char.
     * 
     * @param string the string to be tested
     * @param chr the char to be searched at the end of <code>string</code>
     * @return <code>true</code> if <code>c</code> is at the end of 
     *     <code>string</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    private static final boolean endsWith(String string, char chr) {
        boolean result;
        if (null != string) {
            int len = string.length();
            result = len > 0 && string.charAt(len - 1) == chr;
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * Notifies the recorder about changing the value context. [Java call, 
     * native call]
     * 
     * @param id the value identification representing the context
     * @param push if <code>true</code> push the context to the context stack, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void changeValueContext(String id, boolean push) {
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        ArrayList<String> stack;
        synchronized (CONTEXT_STACKS) {
            stack = CONTEXT_STACKS.get(tid);
            if (null == stack) {
                stack = new ArrayList<String>();
                CONTEXT_STACKS.put(tid, stack);
            }
        }
        if (push) {
            stack.add(id);
        } else {
            for (int i = stack.size() - 1; i >= 0; i--) {
                if (stack.get(i).equals(id)) {
                    stack.remove(i);
                    break;
                }
            }
        }
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }
    
    /**
     * Contextualizes the given id if required.
     * 
     * @param id the id to be tested for contextualizing
     * @return the contextualized id, <code>id</code> or <b>null</b>
     *     if id should be contextualized but the context is missing
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    private static final String getContextualizedValueId(String id) {
        String result = Helper.getCheckedId(id);
        if (Helper.isContextualizeId(id)) {
            ArrayList<String> stack;
            synchronized (CONTEXT_STACKS) {
                stack = CONTEXT_STACKS.get(
                    SystemMonitoring.getCurrentThreadId());
            }
            if (null != stack && !stack.isEmpty()) {
                String suffix;
                if (Helper.CONTEXTUALIZE_ID.equals(id)) {
                    suffix = "";
                } else {
                    suffix = id.substring(Helper.CONTEXTUALIZE_ID.length());
                }
                result = stack.get(stack.size() - 1) + suffix;
            } else {
                result = null;
            }
        } 
        return result;
    }
    
    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, Object newValue) {
        notifyValueChange(recId, ValueType.OBJECT, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, int newValue) {
        notifyValueChange(recId, ValueType.INT, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, byte newValue) {
        notifyValueChange(recId, ValueType.BYTE, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, char newValue) {
        notifyValueChange(recId, ValueType.CHAR, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, short newValue) {
        notifyValueChange(recId, ValueType.SHORT, newValue);
    }
    
    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, long newValue) {
        notifyValueChange(recId, ValueType.LONG, newValue);
    }

    /**
     * Notifies the recorder strategy about a changing value. [Java call, 
     * native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param type the value type
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    private void notifyValueChange(String recId, ValueType type, 
        Object newValue) {
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        recId = getContextualizedValueId(recId);
        if (endsWith(recId, STRATEGY.getVariabilitySeparatorChar())) {
            STRATEGY.enterConfiguration(recId + newValue);
        }
        STRATEGY.notifyValueChange(recId, type, newValue);
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, 
        double newValue) {
        notifyValueChange(recId, ValueType.DOUBLE, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, 
        float newValue) {
        notifyValueChange(recId, ValueType.FLOAT, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, 
        boolean newValue) {
        notifyValueChange(recId, ValueType.BOOLEAN, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void notifyValueChange(String recId, 
        String newValue) {
        notifyValueChange(recId, ValueType.STRING, newValue);
    }
    
    /**
     * Assign the proper recording id with respect to the object identity of
     * the <code>caller</code>, i.e. check the actual type of 
     * <code>caller</code> and pass adjust the recording id if possible.
     * 
     * @param recId the recording id delivered from instrumented code (by the 
     *     caller)
     * @param caller the name of the type of the calling object (may be 
     *     <b>null</b>, e.g. in case of stream classes)
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * @return the adjusted recording id
     * 
     * @since 1.00
     */
    private static String assignId(String recId, String caller, 
        boolean directId) {
        if (null != caller && null != recId && !directId) {
            if (!Helper.isPseudo(recId)) {
                String id = STRATEGY.getRecorderId(caller);
                if (null != id) {
                    recId = id;
                }
            }
        }
        return Helper.getCheckedId(recId);
    }
    
    /**
     * Prints the current (aggregated) state to the output formatter. Do not
     * lock this method as it may be called concurrently. [Java call, 
     * native call]
     * 
     * @since 1.00
     */
    @Override
    public void printCurrentState() {
        if (isRecording) {
            long tid = SystemMonitoring.getCurrentThreadId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            ProcessData p = SystemMonitoring.getProcessData();
            if (STRATEGY.printCurrentState(p)) {
                ProcessData.release(p);
            }
            Lock.setStackTopMemoryAccounting(tid, accMem);
        }
    }
    
    /**
     * Emits the values collected so far. [Java call, native call]
     * 
     * @since 1.00
     */
    public void printStatistics() {
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        try {
            ProcessData p = SystemMonitoring.getProcessData();
            if (STRATEGY.printStatistics(p)) {
                ProcessData.release(p);
            }
        } catch (NullPointerException e) {
            // usually this happens only when the SUM is forcibly terminated
            Configuration.LOG.log(Level.WARNING, 
                "exception while printing statistics", e);
        }
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }

    /**
     * Notifies about a timer event. [Java call, native call]
     * 
     * @param id the timer identification (may overlap with recorder ids)
     * @param state the new timer state
     * @param considerThreads <code>false</code> if the <code>id</code> is
     *     thread save and threads must not be considered explicitly, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    @Override
    public void notifyTimer(String id, TimerState state, 
        boolean considerThreads) {
        long now = System.currentTimeMillis();
        long tid = SystemMonitoring.getCurrentThreadId();
        long accMem = Lock.isStackTopMemoryAccounting(tid);
        //TODO check thread issues
        id = getContextualizedValueId(id);
        long threadId;
        if (considerThreads) {
            threadId = tid;
        } else {
            threadId = -1;
        }
        STRATEGY.notifyTimer(id, state, now, threadId);
        Lock.setStackTopMemoryAccounting(tid, accMem);
    }
    
    /**
     * Registers a JVM thread with its native counterpart. Needed only if the
     * JVM/JDK is not able to handle this, e.g. in 1.4 or Android. [Java call]
     * 
     * @param register register or unregister the thread
     * 
     * @since 1.00
     */
    public void registerThisThread(boolean register) {
        GathererFactory.loadLibrary();
        GathererFactory.getDataGatherer().registerThisThread(register);
    }
    
    /**
     * Notify that temporary data e.g. from instrumentation shall be cleared.
     * The specific cleanup implementation may decide upon this request whether
     * a cleanup is actually required.
     * 
     * @since 1.00
     */
    public void clearTemporaryData() {
        STRATEGY.clearTemporaryData();
    }
    
    /**
     * Returns whether this recorder is recording.
     * 
     * @return <code>true</code> if it is recording, <code>false</code> else
     * 
     * @since 1.00
     */
    protected boolean isRecording() {
        return isRecording;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unallocated(int recId, long size) {
        STRATEGY.memoryFreedByRecId(Lock.getRecorderId(recId), size);
    }
    
}
