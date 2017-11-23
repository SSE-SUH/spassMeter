package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.*;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.internal.*;
import de.uni_hildesheim.sse.monitoring.runtime.boot.*;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.*;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.*;

/**
 * Implements the usual instrumentation for methods. Call {@link #clear()} after
 * usage! This class is public for testing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class MethodEditor extends BehaviorEditor {

    /**
     * Stores all classes which need instrumentation of a 
     * <code>getOutputStream</code> method. Do not refer 
     * via classes as these classes should not be loaded
     * before (dynamic) instrumentation.
     */
    public static final String[] GET_OUTPUT_STREAM_CLASSES = {
        "java.net.Socket"//, "java.net.URLConnection"
    };

    /**
     * Stores all classes which need instrumentation of a 
     * <code>getInputStream</code> method. Do not refer 
     * via classes as these classes should not be loaded
     * before (dynamic) instrumentation.
     */
    public static final String[] GET_INPUT_STREAM_CLASSES = {
        "java.net.Socket"//, "java.net.URLConnection"
    };

    /**
     * Stores all classes which need instrumentation of a 
     * <code>getErrorStream</code> (as InputStream) method. Do not refer 
     * via classes as these classes should not be loaded
     * before (dynamic) instrumentation.
     */
    public static final String[] GET_INPUT_ERROR_STREAM_CLASSES = {
        //"java.net.HttpURLConnection"
    };

    /**
     * Stores all classes which need instrumentation of a 
     * <code>getOpenStream</code> method. Do not refer 
     * via classes as these classes should not be loaded
     * before (dynamic) instrumentation.
     */
    public static final String[] GET_OPEN_STREAM_CLASSES = {
    };

    /**
     * Defines the call to determine the type of a stream.
     */
    protected static final String URL_TYPE_EXPR = StreamType.class.getName()
        + ".getForURL($0.getProtocol())";
    
    /**
     * Stores the relation among annotated fields and recording ids (may be 
     * <b>null</b> if not specified).
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    private static HashMap<String, String> annotatedFields 
        = new HashMap<String, String>();
    
    /**
     * Stores the instance pool.
     */
    private static final ArrayList<MethodEditor> POOL 
        = new ArrayList<MethodEditor>();
    
    /**
     * Stores the type of the last field access.
     */
    private String lastFieldAccessType = null;

    /**
     * Stores the context annotation of the last field access.
     */
    private ValueContext lastFieldAccessContext = null;
    
    /**
     * Stores the (temporary) instrumentation record of memory resouces to 
     * facilitate safe monitoring-class redefinition.
     */
    private int memRecord = 0;

    /**
     * Stores the semantics collector (may be <b>null</b>).
     */
    private ISemanticsCollector collector;
    
    /**
     * Stores the semantics to be applied.
     */
    private Monitor semantics;
    
    /**
     * Stores whether CPU time shall be monitored.
     */
    private boolean monitorCpuTime;

    /**
     * Stores how memory use shall be monitored.
     */
    private MemoryAccountingType memoryAccounting;
    
    /**
     * Stores the group accounting type.
     */
    private GroupAccountingType groupAccounting;
    
    /**
     * Stores whether file I/O shall be monitored.
     */
    private boolean monitorFileIo;

    /**
     * Stores whether network I/O shall be monitored.
     */
    private boolean monitorNetIo;
    
    /**
     * Creates a new method editor (and prevents instantiation from outside 
     * this package).
     * 
     * @since 1.00
     */
    private MethodEditor() {
    }
    
    /**
     * Returns an instance from the pool.
     * 
     * @param semantics the actual group annotation (may be <b>null</b>)
     * @param useSumResources use the sum resources specified in the 
     *   configuration instead of <code>group</code>
     * @param collector the instance to be used for collecting semantics for 
     *   future retransformations (may be <b>null</b>)
     * @param inherited whether <code>semantics</code> is inherited
     * @return an instance
     * 
     * @since 1.00
     */
    public static final synchronized MethodEditor getFromPool(
        Monitor semantics, boolean useSumResources, 
        ISemanticsCollector collector, boolean inherited) {
        MethodEditor result;
        int size = POOL.size();
        if (size > 0) {
            result = POOL.remove(size - 1);
        } else {
            result = new MethodEditor();
        }
        result.setGroup(semantics, useSumResources, collector, inherited);
        return result;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param editor the instance to release
     * 
     * @since 1.00
     */
    public static final synchronized void release(MethodEditor editor) {
        editor.clear();
        POOL.add(editor);
    }
        
    /**
     * Processes a net I/O method call (intended to be called if library
     * is not instrumented or overhead instrumentation is done).
     * 
     * @param mod the statement modifier instance
     * @param name the name of the method being called
     * @return was code modification done or not
     * @throws InstrumenterException if new code cannot be compiled
     * 
     * @since 1.00
     */
    protected boolean processNetIoMethodCall(IStatementModifier mod, 
        String name) 
        throws InstrumenterException {
        boolean done = false;
        boolean isInputStream = false;
        boolean isOutputStream = false;
        StreamType type = StreamType.NET;
        TypeExpressions expr = null;
        isOutputStream = "getOutputStream".equals(name) 
            && mcDeclaringClassInstanceOf(mod, GET_OUTPUT_STREAM_CLASSES);
        if (!isOutputStream) {
            isInputStream = "getInputStream".equals(name) 
                && mcDeclaringClassInstanceOf(mod, GET_INPUT_STREAM_CLASSES);
            isInputStream |= "openStream".equals(name)
                && mcDeclaringClassInstanceOf(mod, GET_OPEN_STREAM_CLASSES);
            if ("openStream".equals(name)
                && mod.mcDeclaringClassInstanceOf("java.net.URL")
                && instrumentNetIo()) {
                type = null;
                // lib is not instrumented
                expr = TypeExpressions.URL_TYPE;
                isInputStream = true;
            }
            isInputStream |= "getErrorStream".equals(name)
                && mcDeclaringClassInstanceOf(mod, 
                    GET_INPUT_ERROR_STREAM_CLASSES);
            if (isInputStream) {
                getCodeModifier().rechainStreamCreation(type, expr, true);
                done = true;
            }
        } else {
            getCodeModifier().rechainStreamCreation(type, expr, false);
            done = true;
        }
        return done;
    }
    
    /**
     * Returns whether the declaring class of the current method call 
     * is instance of at least one of the given <code>types</code>, 
     * whereby <code>types</code> may be superclasses.
     * 
     * @param mod the statement modifier
     * @param types the classes to test for
     * 
     * @return <code>true</code> if <code>clazz</code> is a subclass of 
     *     at least one of <code>types</code>, <code>false</code> else
     * @throws InstrumenterException declaring class not found
     * 
     * @since 1.00
     */
    protected boolean mcDeclaringClassInstanceOf(
        IStatementModifier mod, String[] types) 
        throws InstrumenterException {
        boolean found = false;
        for (int i = 0; !found && i < types.length; i++) {
            found = mod.mcDeclaringClassInstanceOf(types[i]);
        }
        return found;
    }
    
    /**
     * Processes other I/O method (in case that library is instrumented).
     * 
     * @param mod the statement modifier instance
     * @param name the name of the method being called
     * @return was code modification done or not
     * @throws InstrumenterException in case that new code cannot be compiled
     * 
     * @since 1.00
     */
    protected boolean processOtherIoMethodCall(IStatementModifier mod, 
        String name)
        throws InstrumenterException {
        boolean done;
        if ("openStream".equals(name)
            && mod.mcDeclaringClassInstanceOf("java.net.URL")
            && instrumentNetIo()) {
            // lib is instrumented, consider underlying file input stream
            getCodeModifier().rechainStreamCreation(null, 
                TypeExpressions.URL_TYPE, true);
            done = true;
        } else {
            done = false;
        }
        return done;
/*
 
        String name = method.getName();
        IClass cl = method.getDeclaringClass();
        boolean done;
        if ("openStream".equals(name)
            && cl.isInstanceOf("java.net.URL")) {
            // lib is instrumented, consider underlying file input stream
            getCodeModifier().rechainStreamCreation(null, URL_TYPE_EXPR, true, 
                isOverheadEditor);
            done = true;
        } else {
            done = false;
        }
        cl.release();
        return done;
          
          
 */
    }

    /**
     * Defines the actual group annotation.
     * 
     * @param semantics the actual annotation holding the monitoring group
     *   semantics
     * @param useSumResources use the sum resources specified in the 
     *   configuration instead of <code>group</code>
     * @param collector the instance to be used for collecting semantics for 
     *   future retransformations (may be <b>null</b>)
     * @param inherited whether <code>semantics</code> is inherited
     * 
     * @since 1.00
     */
    public void setGroup(Monitor semantics, boolean useSumResources, 
        ISemanticsCollector collector, boolean inherited) {
        Configuration config = Configuration.INSTANCE;
        ResourceType[] accountable;

        this.collector = collector;
        this.semantics = semantics;
        groupAccounting = config.getGroupAccountingType(); // TODO !!!
        monitorCpuTime = false;
        memoryAccounting = MemoryAccountingType.NONE;
        monitorNetIo = false;
        monitorFileIo = false;
        
        if (!useSumResources && config.getScopeType().isGroup()) {
            if (null != semantics) {
                accountable = semantics.resources();
                if (ResourceType.SET_DEFAULT == accountable) {
                    accountable = config.getDefaultGroupResources();
                } else {
                    accountable = ResourceType.ensureSubset(
                        config.getAccountableResources(), 
                        semantics.resources());
                }
            } else {
                accountable = config.getAnywayResources();
            }
        } else {
            accountable = config.getSumResources();
        }
        if (null != accountable && accountable != ResourceType.SET_NONE) {
            if (ResourceType.isAll(accountable)) {
                if (inherited) {
                    accountable = config.getAnywayResources();
                } else {
                    accountable = config.getDefaultGroupResources();
                }
            } 
            if (ResourceType.isAll(accountable)) {
                monitorCpuTime = true;
                memoryAccounting = config.getMemoryAccountingType();
                monitorNetIo = true;
                monitorFileIo = true;
            } else {
                for (int i = 0; i < accountable.length; i++) {
                    ResourceType acc = accountable[i];
                    monitorCpuTime |= (acc == ResourceType.CPU_TIME);
                    if (acc == ResourceType.MEMORY) {
                        memoryAccounting = config.getMemoryAccountingType();
                    }
                    monitorNetIo |= (acc == ResourceType.NET_IO);
                    monitorFileIo |= (acc == ResourceType.FILE_IO);
                }
            }
        }
        if (inherited) {
            if (MemoryAccountingType.NONE != memoryAccounting) {
                memRecord++;
            } else {
                if (memRecord  > 0) {
                    memoryAccounting = config.getMemoryAccountingType();
                }
            }
        }
    }
    
    /**
     * Returns whether CPU time shall be monitored.
     * 
     * @return <code>true</code> if CPU time shall be monitored, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean instrumentCpuTime() {
        return monitorCpuTime;
    }

    /**
     * Returns whether network I/O shall be monitored.
     * 
     * @return <code>true</code> if network I/O shall be monitored, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean instrumentNetIo() {
        return monitorNetIo;
    }

    /**
     * Returns whether file I/O shall be monitored.
     * 
     * @return <code>true</code> if file I/O shall be monitored, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean instrumentFileIo() {
        return monitorFileIo;
    }

    /**
     * Returns the memory accounting type.
     * 
     * @return the memory accounting type
     * 
     * @since 1.00
     */
    public MemoryAccountingType getMemoryAccountingType() {
        return memoryAccounting;
    }
    
    /**
     * Returns the group accounting type.
     * 
     * @return the group accounting type
     * 
     * @since 1.00
     */
    public GroupAccountingType getGroupAccountingType() {
        return groupAccounting;
    }
    
    /**
     * Assigns the stored monitoring semantics to the given method 
     * (<code>methodSignature</code>) in class with name <code>cls</code>.
     * 
     * @param cls the class to assign the semantics to
     * @param name the name of the method
     * @param methodSignature the signature of the method to assign the 
     *   semantics to
     * 
     * @since 1.00
     */
    void assignSemantics(String cls, String name, String methodSignature) {
        if (null != semantics && null != collector) {
            Configuration conf = Configuration.INSTANCE;
            if (ScopeType.GROUP_INHERIT == conf.getScopeType() 
                && !conf.isInstrumented(cls)) { 
                collector.assignSemantics(cls, name + methodSignature, 
                    semantics);
            }
        }
    }
    
    /**
     * Stores an annotated field.
     * 
     * @param field the annotated filed
     * @param id the annotated recorded id
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void put(IField field, String id) {
        synchronized (annotatedFields) {
            annotatedFields.put(getFieldId(field), id);
        }
    }

    /**
     * Instruments an object creation.
     * 
     * @param type the type to be created
     * @throws InstrumenterException in case that the new code does not compile
     */
    @Override
    public void editNewExpression(String type) throws InstrumenterException {
        boolean accountMemory = getMemoryAccountingType().atObjectCreation();
        String newName = null;
        if (instrumentFileIo()) {
            if (type.equals("java.io.FileInputStream")) {
                newName = InstrumentedFileInputStream.class.getName();
            } else if (type.equals("java.io.FileOutputStream")) {
                newName = InstrumentedFileOutputStream.class.getName();
            } else if (type.equals("java.io.FileReader")) {
                newName = InstrumentedFileReader.class.getName();
            } else if (type.equals("java.io.FileWriter")) {
                newName = InstrumentedFileWriter.class.getName();
            } else if (type.equals("java.io.RandomAccessFile")) {
                newName = MonitoringClassFileTransformer
                    .DELEGATING_RANDOM_ACCESS_FILE; 
            } 
        }
        if (null != newName) {
            getCodeModifier().replaceCreatedType(newName, accountMemory);
        } else {
            if (accountMemory) {
                getCodeModifier().appendMemoryAllocated(null);
            }
        }
    }

    /**
     * Instruments the creation of an array.
     * 
     * @throws InstrumenterException in case that the new code does not compile
     */
    @Override
    public void editNewArray() throws InstrumenterException {
        if (getMemoryAccountingType().atArrayCreation()) {
            getCodeModifier().appendMemoryAllocated(null);
        }
    }

    /**
     * Instruments a method call. For detailed
     * information please consider the <code>mc*</code> methods in 
     * {@link #getCodeModifier()}.
     * 
     * @param name the name of the method being called
     * @param signature the JVM signature of the method
     * @param targetClass the class being called
     * @throws InstrumenterException in case that the new code does not compile
     */
    // TODO consider JDI ThreadStartEvent/ThreadDeadEvent
    @Override
    public void editMethodCall(String name, String signature, 
        String targetClass) throws InstrumenterException {
        boolean done = true;
        IStatementModifier mod = getCodeModifier();
        String cRecId = getContextValueChangeCallRecId(mod);
        if ("start".equals(name)) {
            if (mod.mcDeclaringClassInstanceOf("java.lang.Thread")) {
                mod.notifyThreadStarted(cRecId);
            }
        } else if ("clone".equals(name)) {
            if (0 == mod.mcParameterCount() 
                && "java.lang.Object".equals(targetClass) 
                && getMemoryAccountingType().atObjectCreation()) {
                mod.appendMemoryAllocated(cRecId);
            }
        } else if ("send".equals(name) || "receive".equals(name)) {
            if (mod.mcDeclaringClassInstanceOf(
                "java.net.DatagramSocket") && instrumentNetIo()) {
                if (1 == mod.mcParameterCount() 
                    && "java.net.DatagramPacket".equals(
                        mod.mcParameterTypeName(0))) {
                    mod.notifyIoDatagramTransmission(cRecId, 
                        "send".equals(name));
                }
            }
        } else {
            if (!Configuration.INSTANCE.instrumentJavaLib()) {
                done = processNetIoMethodCall(mod, name);
            } else {
                done = processOtherIoMethodCall(mod, name);
            }
        } 
        if (!done) {
            mod.notifyContextChange(cRecId);
        }
        assignSemantics(targetClass, name, signature);
    }
    
    /**
     * Returns the context id for the current method call. If there is a last
     * field access with the same type where the method is accessed and the 
     * method call is not static, then check the last field access for the 
     * {@link ValueContext} method and return the id.
     * 
     * @param mod the statement modifier to gather information from
     * @return the context id or <b>null</b>
     * 
     * @throws InstrumenterException in case that the method call or the 
     *   declaring class cannot be found
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    private String getContextValueChangeCallRecId(IStatementModifier mod) 
        throws InstrumenterException {
        String result = null;
        // cannot tag this with @ValueContext... needs field
        if (null != lastFieldAccessType
            && null != lastFieldAccessContext
            && lastFieldAccessType.equals(mod.mcDeclaringClassName())
            && !mod.mcIsStatic()) {
            result = Helper.trimId(lastFieldAccessContext.id());
        }
        return result;
    }
    
    /**
     * Instruments a field access for notifying the recorder about value 
     * changes.
     * 
     * @param name the name of the field
     * @param type the type of the field
     * @param isWriter whether the field value is changed
     * @throws InstrumenterException in case that the new code does not compile
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public void editFieldAccess(String name, String type, boolean isWriter) 
        throws InstrumenterException {
        IStatementModifier mod = getCodeModifier();
        lastFieldAccessType = type;
        lastFieldAccessContext = mod.fAnnotation(ValueContext.class, 
            AnnotationSearchType.NONE);
        if (isWriter) {
            String fieldId = getFieldId(mod.fDeclaringClassName(), name);
            synchronized (annotatedFields) {
                if (annotatedFields.containsKey(fieldId)) {
                    getCodeModifier().notifyValueChanged(
                        annotatedFields.get(fieldId));
                }
            }
        }
    }

    /**
     * Produces an id for the given field. Avoid storing the references
     * of the bytecode framework in order to allow cleaning up references.
     * 
     * @param field the field to return the id for
     * @return the id
     * 
     * @since 1.00
     */
    private static String getFieldId(IField field) {
        // do not store field directly!
        return getFieldId(field.getDeclaringClassName(), field.getName());
    }

    /**
     * Produces an id for the given field. Avoid storing the references
     * of the bytecode framework in order to allow cleaning up references.
     * 
     * @param declaringClassName the name of the class declaring the field
     * @param name the name of the field
     * @return the id
     * 
     * @since 1.00
     */
    private static String getFieldId(String declaringClassName, String name) {
        // do not store field directly!
        return declaringClassName + "." + name;
    }

    /**
     * Clears temporary variables for instance reuse.
     * 
     * @since 1.00
     */
    public void clear() {
        monitorCpuTime = false;
        groupAccounting = GroupAccountingType.LOCAL; // TODO
        memoryAccounting = MemoryAccountingType.NONE;
        monitorNetIo = false;
        monitorFileIo = false;        
        lastFieldAccessType = null;
        lastFieldAccessContext = null;
        memRecord = 0;
    }
    
    /**
     * Defines the instrumentation record for the current method under 
     * transformation. 
     * @param record the instrumentation record (<b>null</b> clears the record)
     * 
     * @since 1.00
     */
    public void setInstrumentationRecord(MethodInstrumented record) {
        if (null != record) {
            memRecord += record.mem();
        } else {
            memRecord = 0;
        }
    }

    /**
     * Returns the instrumentation record for memory.
     * 
     * @return the instrumentation record 
     * 
     * @since 1.00
     */
    public int getMemRecord() {
        return memRecord;
    }
}