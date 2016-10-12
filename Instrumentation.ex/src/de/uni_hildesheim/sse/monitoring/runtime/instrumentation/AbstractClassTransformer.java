package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import java.lang.annotation.Annotation;
import java.util.HashSet;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Instrumented;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Timer;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.NotifyValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ConfigurationChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerPosition;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ValueChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.VariabilityHandler;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.internal.
    MethodInstrumented;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MainDefaultType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.
    InstrumentedFileOutputStream;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    AnnotationSearchType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MemoryAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.ScopeType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.xml.
    XMLConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.recording.ElschaLogger;
import de.uni_hildesheim.sse.monitoring.runtime.recording.Recorder;

/**
 * Implements the class file transformer introducing additional code
 * for instrumentation. Note that some methods in this class require the class
 * name to be given in in the internal form of fully qualified class and 
 * interface names as defined in The Java Virtual Machine Specification. This is
 * due to performance reasons for runtime instrumentation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.13
 */
public class AbstractClassTransformer implements ISemanticsCollector {

    /**
     * The fully qualified name of the delegating random access file.
     * Do not refer this class directly because otherwise it is loaded
     * before self-instrumentation.
     */
    static final String DELEGATING_RANDOM_ACCESS_FILE =
        InstrumentedFileOutputStream.class.getPackage().getName() 
        + ".DelegatingRandomAccessFile";
    
    // otherways loaded here before instrumentation

    /**
     * Stores if static instrumentation should be done.
     */
    private boolean isStatic;
    
    /**
     * Stores registered classes.
     */
    private HashSet<String> registeredClasses = new HashSet<String>(); 
    
    /**
     * Counts the number of main methods (usually only the first one 
     * is relevant).
     */
    private int mainCount = 0;

    /**
     * Stores classes to be excluded.
     * @since 1.13
     */
    private String[] excludeClasses = null;
    
    /**
     * Analyze the members of the individual classes.
     * @since 1.13
     */
    private HashMap<String, Boolean> analyzeMembers;
    
    /**
     * Stores the assignments.
     */
    private final HashMap<String, HashMap<String, Monitor>> assignments 
        = new HashMap<String, HashMap<String, Monitor>>();
    
    /**
     * Creates a new monitoring class file transformer.
     *
     * @param isStatic denotes if static instrumentation should be done or 
     *   dynamic
     *
     * @since 1.00
     */
    public AbstractClassTransformer(boolean isStatic) {
        Recorder.initialize();
        this.isStatic = isStatic;
        Configuration cfg = Configuration.INSTANCE;
        excludeClasses = cfg.getExcludeClasses();
        XMLConfiguration xmlCfg = cfg.getXMLConfig();
        if (null != xmlCfg && !cfg.allClassMembers()) {
            analyzeMembers = xmlCfg.getAnalyzeMembers();
        }
    }

    /**
     * Returns if the given class name denotes the random access file.
     * 
     * @param className the class name to be tested given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @return <code>false</code> if it is not the random access file, 
     *    <code>true</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO, value = "false")
    private final boolean isRandomAccessFile(String className) {
        return className.equals("java/io/RandomAccessFile");
    }

    /**
     * Returns if the given class name denotes the socket input stream.
     * 
     * @param className the class name to be tested given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @return <code>false</code> if it is not the socket input stream, 
     *    <code>true</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO, value = "false")
    private final boolean isSocketInputStream(String className) {
        return className.equals("java/net/SocketInputStream");
    }

    /**
     * Returns if the given class name denotes the socket output stream.
     * 
     * @param className the class name to be tested given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @return <code>false</code> if it is not the socket output stream, 
     *    <code>true</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO, value = "false")
    private final boolean isSocketOutputStream(String className) {
        return className.equals("java/net/SocketOutputStream");
    }
    
    /**
     * Returns if the given class name denotes a recorder class.
     * 
     * @param className the class name to be tested given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @return <code>false</code> if it is not a recorder class, 
     *    <code>true</code> else
     * 
     * @since 1.00
     */
    private final boolean isRecorderClass(String className) {
        return className.startsWith(
            "de/uni_hildesheim/sse/monitoring/runtime/");
    }
    
    /**
     * Converts a given fully qualified class name in the 
     * internal form of fully qualified class and interface names as defined 
     * in The Java Virtual Machine Specification (slash separated) to a Java
     * fully qualified name (dot separated).
     * 
     * @param vmFqn the fully qualified name according to the Java Virtual 
     *   Machine Specification
     * @return the Java typical name
     * 
     * @since 1.00
     */
    public static final String internalVmFqnToJavaFqn(String vmFqn) {
        return vmFqn.replace('/', '.');
    }

    /**
     * Converts a given Java fully qualified class name (dot separated) to the 
     * internal form of fully qualified class and interface names as defined 
     * in The Java Virtual Machine Specification (slash separated).
     * 
     * @param fqn the fully qualified Java name 
     * @return the fully qualified name according to the Java Virtual 
     *   Machine Specification
     * 
     * @since 1.00
     */
    public static final String javaFqnToInternalVmFqnTo(String fqn) {
        return fqn.replace('.', '/');
    }
    
    /**
     * Returns if the class given by its class name should be instrumented.
     * 
     * @param className the name of the class to be checked given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @return <code>true</code> if the class should be instrumented, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    public final boolean shouldInstrument(String className) {
        if (Configuration.INSTANCE.instrumentJavaLib() 
            && (isSocketInputStream(className) 
                || isSocketOutputStream(className) 
                || isRandomAccessFile(className))) {
            return true;
        }
        if (isRecorderClass(className)) {
            return false;
        }
        boolean isJavaClass = className.equals("sun/misc/Cleaner");
        if (!Configuration.INSTANCE.instrumentJavaLib()) {
            // be more precise
            isJavaClass |= className.startsWith("javax/");
            isJavaClass |= className.startsWith("com/sun/");
            isJavaClass |= className.startsWith("com/oracle/");
            isJavaClass |= className.startsWith("sun/");
            isJavaClass |= className.startsWith("sunw/");
            isJavaClass |= className.startsWith("org/xml");
            isJavaClass |= className.startsWith("org/w3c");
            isJavaClass |= className.startsWith("org/omg");
            isJavaClass |= className.startsWith("org/jcp");
            isJavaClass |= className.startsWith("org/ietf");
        }
        boolean additional = false;
        if (Configuration.INSTANCE.instrumentInstrumenter()) {
            // TODO: be careful with existing results but should be excluded
            // there as it is overhead
            additional |= className.startsWith("javassist/");
        }
        additional |= className.startsWith("com/sun/jmx/remote/internal/");
        // accesses private constructors in other classes
        additional |= className.startsWith("sun/reflect/Generated");
        
        
        boolean instr = !(className.startsWith("de/uni_hildesheim/sse/system")
            || isJavaClass || additional);
        if (instr && null != excludeClasses) {
            for (int i = 0; instr && i < excludeClasses.length; i++) {
                instr &= !className.startsWith(excludeClasses[i]);
            }
        }
        return instr;
    }
    
    /**
     * Returns the {@link Monitor} annotation for <code>cl</code>. This method 
     * considers whether <code>cl</code> or its (outer) declaring class has a 
     * {@link Monitor} annotation and returns it or stops at a 
     * {@link ExcludeFromMonitoring} annotation.
     * 
     * @param cl the class to be queried for the annotation
     * @return the related annotation or <b>null</b> in case that none was found
     *     or the class is explicitly excluded from monitoring.
     * @throws InstrumenterException in case that some class cannot be found
     * 
     * @since 1.00
     */
    private static final Monitor getMonitorAnnotation(IClass cl) 
        throws InstrumenterException {
        AnnotationSearchType asType = 
            Configuration.INSTANCE.getAnnotationSearchType();
        Monitor result = getAnnotation(cl, Monitor.class, asType);
        if (null == result) {
            if (null == getAnnotation(cl, 
                ExcludeFromMonitoring.class, asType)) {
                IClass outer = cl;
                do {
                    IClass decl = outer.getDeclaringClass();
                    if (outer != cl) {
                        // not our responsiblity
                        outer.release();
                    }
                    outer = decl;
                    if (null != outer) {
                        result = getAnnotation(outer, Monitor.class, asType);
                        if (null == result) {
                            if (null != getAnnotation(outer, 
                                ExcludeFromMonitoring.class, asType)) {
                                outer.release();
                                outer = null;
                            }
                        }
                    }
                } while (null != outer && null == result);
                if (null != outer) {
                    outer.release();
                }
            }
        }
        return result;
    }

    // below:
    /*        if (!mayAlterStructure) {
    CtConstructor[] methods = cl.getDeclaredConstructors();
    for (int i = 0; i < methods.length; i++) {
        methods[i].insertAfter("System.out.println(\"**\");");
    }
    /*
    MemoryAccountingType memAccounting = Configuration.INSTANCE
        .getMemoryAccountingType(); 
    if (memAccounting.atFinalizer()) {
        CtMethod[] methods = cl.getMethods();
        boolean hasFinalize = false;
        for (int m = 0; m < methods.length; m++) {
            CtMethod method = methods[m];
            if (InstrumentationFragments.isFinalize(method)) {
                hasFinalize = true;
                InstrumentationFragments.instrumentFinalize(method);
            }
        }
        if (!hasFinalize) {
            InstrumentationFragments.addFinalizer(cl, false);
        }
    }
    return;
}*/
    
    /**
     * Returns whether the members of a class shall be analyzed. Prerequisite 
     * is that {@link #analyzeMembers} is not <b>null</b>.
     * 
     * @param clName the class name
     * @param cl the class itself for further inspection
     * @return <code>true</code> if the
     * @throws InstrumenterException in case of problems with the bytecode
     * 
     * @since 1.00
     */
    private boolean analyzeMembers(String clName, IClass cl) 
        throws InstrumenterException {
        boolean analyze;
        if (null != analyzeMembers) {
            analyze = analyzeMembers.containsKey(clName);
            if (!analyze) {
                // for inner classes
                String decl = cl.getDeclaringClassName();
                if (null != decl) {
                    analyze = analyzeMembers.containsKey(decl);
                }
            }
        } else {
            analyze = true;
        }
        return analyze;
    }

    /**
     * Processes a class of the system under monitoring.
     * 
     * @param name the name of the class given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @param cl the class
     * @param type the type of the transformation
     * @return <code>true</code> if the class was modified, 
     *   <code>false</code> else
     *
     * @throws InstrumenterException in case that any error occurs 
     *   while processing bytecode
     * 
     * @since 1.00
     */
    public final boolean transform(String name, IClass cl, 
        TransformationType type) throws InstrumenterException {
        boolean transformed = false;
        HashMap<String, Monitor> assignedSemantics;
        Configuration config = Configuration.INSTANCE;
        String clName = cl.getName();
        if (ScopeType.GROUP_INHERIT == config.getScopeType()) { 
            assignedSemantics = assignments.remove(clName);
        } else {
            assignedSemantics = null;
        }
        boolean mayAlterStructure = type.mayAlterStructure();
        ICodeModifier modifier = IFactory.getInstance().getCodeModifier();
        if (null == getAnnotation(cl, Instrumented.class, 
            AnnotationSearchType.NONE)) {
            boolean done = false;
            if (isSocketInputStream(name) && ResourceType.contains(
                config.getAccountableResources(), ResourceType.NET_IO)) {
                modifier.instrumentSocketInputStream(cl);
                transformed = true;
                done = true;
            } else if (isSocketOutputStream(name) && ResourceType.contains(
                config.getAccountableResources(), ResourceType.NET_IO)) {
                modifier.instrumentSocketOutputStream(cl);
                transformed = true;
                done = true;
            } else if (isRandomAccessFile(name) && ResourceType.contains(
                config.getAccountableResources(), ResourceType.FILE_IO)) {
                modifier.instrumentRandomAccessFile(cl);
                transformed = true;
                done = true;
            } else if (isRecorderClass(name)) {
                done = true;
            }

            boolean inherited = false;
            Monitor mGroup = null;
            if (!done && TransformationType.REDEFINITION != type) {
                mGroup = getMonitorAnnotation(cl);
            }
            if (!done && !cl.isInterface() && (null != assignedSemantics 
                || analyzeMembers(clName, cl))) {
//                long memSize = ObjectSizeCache.INSTANCE.getSize(
//                    cl.getSuperclass().getName(), false);
                int fCount = cl.getDeclaredFieldCount();
                for (int f = 0; f < fCount; f++) {
                    IField field = cl.getDeclaredField(f);
//                    memSize = ObjectSizeEstimator.getTypeSize(
//                        field.getTypeName());
                    ValueChange change = getAnnotation(field, 
                        ValueChange.class, AnnotationSearchType.NONE);
                    if (null != change) {
                        MethodEditor.put(field, Helper.trimId(change.id()));
                    }
                    field.release();
                }
//                ObjectSizeCache.INSTANCE.setSize(cl.getName(),
//                    ObjectSizeEstimator.getClassSize(memSize));
                int bCount = cl.getDeclaredBehaviorCount();
                boolean hasFinalizer = false;
                MethodEditor methodEditor = 
                    MethodEditor.getFromPool(mGroup, false, this, false);
               ElschaLogger.info("Meditor for class " + cl.getName() + " is " + methodEditor);
                for (int m = 0; m < bCount; m++) {
                    IBehavior behavior = cl.getDeclaredBehavior(m);
                    if (!behavior.isAbstract() && !behavior.isNative()) {
                        Monitor mSem = mGroup;
                        if (null != assignedSemantics) {
                            Monitor iSem = assignedSemantics.get(
                                behavior.getName() 
                                + behavior.getJavaSignature());
                            inherited = (null != iSem);
                            mSem = iSem;
                            methodEditor.setGroup(mSem, false, this, inherited);
                        }
                        try {
                            transformed |= doMethod(behavior, mSem, inherited, 
                                type, methodEditor);
                            if (0 == mainCount && "main".equals(
                                behavior.getName())) {
                                int pCount = behavior.getParameterCount();
                                if (pCount == 1 
                                    && Constants.JAVA_LANG_STRING_ARRAY1.equals(
                                        behavior.getParameterTypeName(0))) {
                                    transformed |= doFirstMain(cl, behavior, 
                                        mSem, type);
                                    mainCount++;
                                }
                            }
                        } catch (InstrumenterException e) {
                           ElschaLogger.info("Caught Exception: " + e.getMessage());
                            if (!handleMethodInstrumentationException(e)) {
                                throw e;
                            }
                        }
                        if (mSem != mGroup) {
                            methodEditor.setGroup(mGroup, false, this, false);
                        }
                    }
                    hasFinalizer |= behavior.isFinalize();
                    behavior.release();
                }
                MethodEditor.release(methodEditor);
                if (mayAlterStructure && !hasFinalizer && Configuration.
                    INSTANCE.getMemoryAccountingType().atFinalizer()) {
                    modifier.addFinalizer(cl, false);
                    transformed = true;
                }
                // nested classes are considered by default
            }
            if (!done) {
                if (cl.isInstanceOf(Constants.JAVA_LANG_THREAD) 
                    || cl.isInstanceOf(Constants.JAVA_LANG_RUNNABLE)) {
                    transformed |= instrumentThreadRun(cl, type);
                }
                if (!cl.isInterface() && null == assignedSemantics) {
                    transformed |= registerRecorderId(mGroup, cl);
                }
            }
        } // has Instrumented annotation
        Configuration.INSTANCE.instrumented(clName);
        assignments.remove(clName);

        // trigger retransformation
        if (mayAlterStructure) {
            // concurrent modification!!!
            int size = assignments.keySize();
            if (size > 0) {
                String[] classes = new String[size];
                assignments.keysToArray(classes);
                retransformAssigned(classes);
            }
        }
        //assignments.clear();
        return transformed;
    }
    
    /**
     * Retransforms the specified classes.
     * 
     * @param classNames the class names
     * 
     * @since 1.00
     */
    protected void retransformAssigned(String[] classNames) {
    }

    /**
     * Instruments the run method of a thread. This method is called only
     * if JMX is not supported, e.g. on Android.
     * 
     * @param cl the class to analyze
     * @param type the type of the transformation
     * @return <code>true</code> if the class was modified, 
     *   <code>false</code> else
     * @throws InstrumenterException in case that the new code cannot be 
     *   compiled
     * 
     * @since 1.00
     */
    private final boolean instrumentThreadRun(IClass cl, 
        TransformationType type) throws InstrumenterException {
        boolean modified = false;
        if (!cl.isInterface() && !cl.isAbstract()) {
            // sufficient for instantiable classes, avoid problems with 
            // TODO serializable classes without SerialVersionUID
            int bCount = cl.getDeclaredBehaviorCount();
            boolean found = false;
            for (int m = 0; m < bCount; m++) {
                IBehavior behavior = cl.getDeclaredBehavior(m);
                if ("run".equals(behavior.getName())) {
                    if (0 == behavior.getParameterCount()) {
                        if (!behavior.isAbstract()) {
                            modified = IFactory.getInstance().getCodeModifier().
                                notifyRegisterThread(cl, behavior, false);
                        }
                        found = true;
                    }
                }
                behavior.release();
            }
            if (!found && type.mayAlterStructure()) {
                boolean doit = true;
                if (cl.isInstanceOf(Constants.JAVA_IO_SERIALIZABLE)) {
                    // this is a fix... alternative - calculate original uid
                    doit = false;
                    for (int f = 0; !doit && f < cl.getDeclaredFieldCount(); 
                        f++) {
                        IField field = cl.getDeclaredField(f);
                        doit = "long".equals(field.getTypeName()) 
                            && Constants.SERIAL_VERSION_FIELD_NAME.equals(
                                field.getName());
                    }
                }
                if (doit) {
                    modified = IFactory.getInstance().getCodeModifier().
                        notifyRegisterThread(cl, null, false);
                }
            }
        }
        return modified;
    }
    
    /**
     * Registers a monitoring element (class or method) for monitoring.
     * 
     * @param mGroup the monitoring annotation
     * @param cl the containing class
     * @return <code>true</code> if the class was modified, 
     *   <code>false</code> else
     * @throws InstrumenterException in case of any byte code error
     * 
     * @since 1.00
     */
    private final synchronized boolean registerRecorderId(
        Monitor mGroup, IClass cl) throws InstrumenterException {
        boolean modified = false;
        if (null != mGroup) {
            if (Helper.isId(mGroup, Helper.PROGRAM_ID) 
                || Helper.isId(mGroup, Helper.RECORDER_ID)) {
                Configuration.LOG.warning("@Monitor for " + cl.getName() 
                    + "uses internal recorder id - ignored");
            } else if (!Helper.ignore(mGroup)) {
                if (isStatic) {
                    String className = cl.getName();
                    if (!registeredClasses.contains(className)) {
                        // avoid duplicated registering initializers
                        IFactory.getInstance().getCodeModifier().
                            addRegisteringInitializer(cl, mGroup);
                        registeredClasses.add(className);
                        modified = true;
                    }
                } else {
                    MonitoringGroupSettings settings 
                        = MonitoringGroupSettings.getFromPool();
                    String[] ids = mGroup.id();
                    for (int i = 0; i < ids.length; i++) {
                        // usually arrays returned by annotations (in our case)
                        // should not be modified, because in the XML case
                        // they are generate dynamically an values are 
                        // overwritten in memory
                        ids[i] = ids[i].trim();
                        if (0 == ids[i].length()) {
                            ids[i] = cl.getName();
                        }
                    }
                    settings.setBasics(ids, mGroup.debug(), mGroup.groupAccounting(), 
                        mGroup.resources(), mGroup.instanceIdentifierKind());
                    if (ids.length > 1) {
                        settings.setMulti(mGroup.distributeValues(), 
                            mGroup.considerContained());
                    }
                    RecorderFrontend.instance.registerForRecording(
                        cl.getName(), settings);
                    MonitoringGroupSettings.release(settings);
                }
            }
        }
        return modified;
    }
        
    /**
     * Returns whether the specified annotation may be pruned, i.e. removed
     * from resulting bytecode. The results depends on the current configuration
     * and the concrete type of the annotation, i.e. no pruning happens, if 
     * pruning is disabled by default. If pruning is enabled, it is enabled for
     * all annotations except of the {@link Instrumented} annotation, which
     * is enabled only in case of dynamic instrumentation (for staged static
     * instrumentations).
     * 
     * @param <T> the type of the annotation
     * @param annotation the type of the annotation (meta class)
     * @return <code>true</code> if the given annotation (type) may be pruned,
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    protected static final <T extends Annotation> boolean pruneAnnotation(
        Class<T> annotation) {
        boolean result = false;
        // pruning would lead to problems finding the value contexts
        Configuration config = Configuration.INSTANCE;
        if (config.pruneAnnotations()) {
            if (annotation == Instrumented.class) {
                result = !config.isStaticInstrumentation();
            } else {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>member</code>.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param search the annotation search type
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    protected static final <T extends Annotation> T getAnnotation(
        IField member, Class<T> annotation, AnnotationSearchType search) {
        // check AllDelegatingEditor
        T result =  null;
        boolean done = false;
        XMLConfiguration config = Configuration.INSTANCE.getXMLConfig();
        if (null != config) {
            IClass declaring = member.getDeclaringClass();
            result = config.getAnnotation(member.getSignature(), 
                declaring, annotation, IFactory.getInstance());
            declaring.release();
            done = config.isExclusive();
        } 
        if (!done && null == result) {
            result = member.getAnnotation(annotation, 
                pruneAnnotation(annotation));
        }
        return result;
    }

    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>member</code>.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param search the annotation search type
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    protected static final <T extends Annotation> T getAnnotation(
        IBehavior member, Class<T> annotation, AnnotationSearchType search) {
        IClass declaring = member.getDeclaringClass();
        T result =  getAnnotation(member, declaring, annotation, search, true);
        declaring.release();
        return result;
    }
    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>member</code>.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param searchIn the class / interface to search in
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param search the annotation search type
     * @param topCall is this method called from the top of the current 
     *   recursion or from within
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    private static final <T extends Annotation> T getAnnotation(
        IBehavior member, IClass searchIn, Class<T> annotation, 
        AnnotationSearchType search, boolean topCall) {
        T result =  null;
        IBehavior sMember = member;
        try {
            boolean done = false;
            XMLConfiguration config = Configuration.INSTANCE.getXMLConfig();
            if (!topCall) {
                sMember = searchIn.findSignature(member);
            }
            if (null != config) {
                result = config.getAnnotation(sMember.getSignature(), 
                    searchIn, annotation, IFactory.getInstance());
                done = config.isExclusive();
            } 
            if (!done && null == result) {
                result = sMember.getAnnotation(annotation, 
                    pruneAnnotation(annotation));
            }
            if (AnnotationSearchType.NONE != search && null == result) {
                if (search.considerSuperclass()) {
                    IClass su = searchIn.getSuperclass();
                    if (null != su) {
                        IBehavior mem = su.findSignature(member);
                        if (null != mem) {
                            result = getAnnotation(mem, su, annotation, 
                                search, false);
                            mem.release();
                        }
                        su.release();
                    } 
                }
                if (null == result && search.considerInterface()) {
                    int iCount = searchIn.getInterfaceCount();
                    if (iCount > 0) {
                        for (int i = 0; null == result && i < iCount; i++) {
                            IClass iface = searchIn.getInterface(i);
                            IBehavior mem = iface.findSignature(member);
                            if (null != mem) {
                                result = getAnnotation(mem, iface, annotation, 
                                    search, false);
                                mem.release();
                            }
                            iface.release();
                        }
                    }
                }
            }
        } catch (InstrumenterException e) {
        } finally {
            if (!topCall && null != sMember) {
                sMember.release();
            }
        }
        return result;
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>cls</code>.
     * 
     * @param <T> the type of the annotation
     * @param cls the class to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param search the annotation search type
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    protected static final <T extends Annotation> T getAnnotation(
        IClass cls, Class<T> annotation, AnnotationSearchType search) {
        T result =  null;
        XMLConfiguration config = Configuration.INSTANCE.getXMLConfig();
        boolean done = false;
        if (null != config) {
            result = config.getAnnotation(cls.getName(), cls, 
                annotation, IFactory.getInstance());
            done = config.isExclusive();
        } 
        if (!done && null == result) {
            result = cls.getAnnotation(annotation, 
                pruneAnnotation(annotation));
        }
        if (AnnotationSearchType.NONE != search && null == result) {
            try {
                if (search.considerSuperclass()) {
                    IClass su = cls.getSuperclass();
                    if (null != su) {
                        result = getAnnotation(su, annotation, search);
                        su.release();
                    } 
                }
                if (null == result && search.considerInterface()) {
                    int ifCount = cls.getInterfaceCount();
                    if (ifCount > 0) {
                        for (int i = 0; null == result && i < ifCount; i++) {
                            IClass iface = cls.getInterface(i);
                            result = getAnnotation(iface, annotation, search);
                            iface.release();
                        }
                    }
                }
            } catch (InstrumenterException e) {
            }
        }
        return result;
    }

    /**
     * Processes the first main method which occurs during instrumentation of a 
     * program.
     * 
     * @param cls the class
     * @param behavior the behavior to be modified
     * @param mGroupClass the annotation on class level as cause for 
     *     instrumenting this method (may be <b>null</b>)
     * @param type the type of the transformation
     * @return <code>true</code> if the class was modified, 
     *   <code>false</code> else
     * @throws InstrumenterException in case that the code added by 
     *     instrumentation does not compile
     * 
     * @since 1.00
     */
    private final boolean doFirstMain(IClass cls, IBehavior behavior, 
        Monitor mGroupClass, TransformationType type) 
        throws InstrumenterException {
        boolean modified = false;
        MainDefaultType dType = Configuration.INSTANCE.getMainDefault();
        ICodeModifier modifier = IFactory.getInstance().getCodeModifier();
        if (MainDefaultType.NONE != dType) { 
            if (dType.atStart() || dType.atShutdown()) {
                StartSystem startSystem = getAnnotation(behavior, 
                    StartSystem.class, AnnotationSearchType.NONE);
                if (null == startSystem) {
                    modifier.instrumentStartSystem(behavior, 
                        dType.atShutdown(), "");
                    modified = true;
                }
            }
            if (dType.atStop()) {
                EndSystem endSystem = getAnnotation(behavior, 
                    EndSystem.class, AnnotationSearchType.NONE);
                if (null == endSystem) {
                    modifier.instrumentEndSystem(behavior, 
                        Configuration.INSTANCE.printStatistics(), "");
                    modified = true;
                }
            }
        }
        if (cls.isInstanceOf(Constants.JAVA_LANG_THREAD)) {
            modified = modifier.notifyRegisterThread(cls, null, false);
        }
        return modified;
    }

    /**
     * Processes a method of a regular class (system under measurement).
     * 
     * @param behavior the behavior to be modified
     * @param mGroupClass the annotation on class level as cause for 
     *     instrumenting this method (may be <b>null</b>)
     * @param inherited whether <code>mGroupClass</code> is inherited
     * @param type the type of the transformation
     * @param editor the editor for modifications
     * @return <code>true</code> if the behavior was modified, 
     *   <code>false</code> else
     * @throws InstrumenterException in case that the code added by 
     *     instrumentation does not compile
     * 
     * @since 1.00
     */
    private final boolean doMethod(IBehavior behavior, Monitor mGroupClass, 
        boolean inherited, TransformationType type, MethodEditor editor)
        throws InstrumenterException {
        boolean modified = false;
        ICodeModifier modifier = IFactory.getInstance().getCodeModifier();
        AnnotationSearchType asType = Configuration.INSTANCE.
            getAnnotationSearchType();
        EndSystem endSystem = 
            getAnnotation(behavior, EndSystem.class, AnnotationSearchType.NONE);
        ConfigurationChange configurationChangeAnnotation = 
            getAnnotation(behavior, ConfigurationChange.class, asType);
        Monitor mGroup = getAnnotation(behavior, Monitor.class, asType);
        Timer notifyTimer = getAnnotation(behavior, Timer.class, asType);
        VariabilityHandler varHandler = getAnnotation(behavior, 
            VariabilityHandler.class, asType);
        boolean isExcluded = (null != getAnnotation(behavior, 
            ExcludeFromMonitoring.class, asType));
        boolean manualDetectionWithConfigChange = 
            !Configuration.INSTANCE.configurationDetection() 
            && null != configurationChangeAnnotation;
        
        if (null != mGroup && !isExcluded) {
            // local annotation has higher priority, excluded even higher
            mGroupClass = mGroup;
            IClass decl = behavior.getDeclaringClass();
            // TODO may happen in multiple cases during static initialization!
            registerRecorderId(mGroup, decl);
            decl.release();
        }
        StartSystem startSystem = getAnnotation(behavior, 
            StartSystem.class, AnnotationSearchType.NONE);

        if (null != notifyTimer && !inherited) {
            modified |= instrumentNotifyTimer(behavior, notifyTimer);
        }
        MemoryAccountingType memAccounting = editor.getMemoryAccountingType(); 
        if (memAccounting.atFinalizer()) {
            modifier.instrumentFinalize(behavior);
            modified = true;
        }
        if (memAccounting.atConstructor() && behavior.isConstructor()) {
            modifier.instrumentConstructor(behavior);
            modified = true;
        }
        if (!isExcluded) {
            if (null != mGroupClass && !inherited) {
                modifier.instrumentTiming(behavior, mGroupClass, false, 
                    null != mGroup);
                modified = true;
            }
            boolean configLocal = (GroupAccountingType.LOCAL 
                == editor.getGroupAccountingType());
            if (!configLocal || (configLocal && null != mGroup)) {
                boolean isRedefinition = 
                    TransformationType.REDEFINITION == type;
                // do deeper instrumentation only if non-local or local with 
                // appropriate annotation
                if (isRedefinition) {
                    MethodInstrumented instrumented = getAnnotation(behavior, 
                        MethodInstrumented.class, AnnotationSearchType.NONE);
                    if (null != instrumented) {
                        editor.setInstrumentationRecord(instrumented);
                    }
                }
                behavior.instrument(editor);
                if (isRedefinition) {
                    int record = editor.getMemRecord();
                    if (record > 0) {
                        HashMap<String, Object> map 
                            = new HashMap<String, Object>();
                        map.put("mem", record);
                        modifier.addAnnotation(behavior, 
                            MethodInstrumented.class, map);
                        modified = true;
                    }
                    editor.setInstrumentationRecord(null);
                }
            }
            if (null != varHandler && !inherited 
                && !manualDetectionWithConfigChange) {
                modifier.instrumentVariabilityHandler(behavior);
                modified = true;
            }
        } else {
            if (null != mGroupClass && !inherited) {
                modifier.instrumentTiming(behavior, mGroupClass, true, true);
                modified = true;
            }
        }
        
        if (null != configurationChangeAnnotation && !inherited) {
            modifier.instrumentConfigurationChange(behavior, 
                configurationChangeAnnotation);
            modified = true;
        }

        modified |= processNotifications(behavior, mGroupClass, modifier);
        if (null != startSystem) {
            modifier.notifyRegisterThread(behavior.getDeclaringClass(), 
                behavior, true);
            modifier.instrumentStartSystem(behavior, 
                startSystem.shutdownHook(), startSystem.invoke());
            modified = true;
        } 

        if (null != endSystem) {
            modifier.instrumentEndSystem(behavior, 
                Configuration.INSTANCE.printStatistics(), endSystem.invoke());
            modified = true;
        }
        return modified;
    }
    
    /**
     * Processes value notifications on the given method.
     * 
     * @param behavior the method to be changed
     * @param mGroup the annotation which indicated recording (may be 
     *     <b>null</b>)
     * @param modifier the code modifier to be used
     * @return <code>true</code> if the behavior was modified, 
     *   <code>false</code> else
     * @throws InstrumenterException if any instrumented code is not valid
     * 
     * @since 1.00
     */
    private boolean processNotifications(IBehavior behavior, 
        Monitor mGroup, ICodeModifier modifier) throws InstrumenterException {
        boolean modified = false;
        NotifyValue ann = getAnnotation(behavior, 
            NotifyValue.class, AnnotationSearchType.NONE);
        if (null != ann) {
            String recId = null;
            String annId = Helper.trimId(ann.id());
            if (annId.length() > 0) {
                recId = annId;
            } else if (null != mGroup) {
                recId = Configuration.INSTANCE.getRecId(mGroup.id());
            }
            modifier.valueNotification(behavior, recId, ann);
            modified = true;
        }
        return modified;
    }

    /**
     * Performs the instrumentation for the timer notification. This method
     * determines the point within the method / constructor to affect existing
     * code at, constructs the call to the recorder and inserts the call
     * accordingly.
     * 
     * @param behavior the method / constructor being processed
     * @param notifyTimer the timer annotation appended to 
     *     <code>behaviorInfo</code>
     * @return <code>true</code> if the behavior was modified, 
     *   <code>false</code> else
     * @throws InstrumenterException in case that new code cannot be compiled
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    private boolean instrumentNotifyTimer(
        IBehavior behavior, Timer notifyTimer) 
        throws InstrumenterException {
        boolean modified = false;
        TimerPosition position = notifyTimer.affectAt();
        if (TimerPosition.DEFAULT == position 
            || TimerPosition.BOTH == notifyTimer.state().getDefaultPosition()) {
            position = notifyTimer.state().getDefaultPosition();
        }

        TimerState beginningState = null;
        TimerState endState = null;
        switch (position) {
        case BOTH:
            if (TimerState.RESUME_SUSPEND == notifyTimer.state()) {
                beginningState = TimerState.RESUME;
                endState = TimerState.SUSPEND;
            }
            if (TimerState.SUSPEND_RESUME == notifyTimer.state()) {
                beginningState = TimerState.SUSPEND;
                endState = TimerState.RESUME;
            }
            if (TimerState.START_FINISH == notifyTimer.state()) {
                beginningState = TimerState.START;
                endState = TimerState.FINISH;
            }
            break;
        case BEGINNING:
            beginningState = notifyTimer.state();
            break;
        case END:
            endState = notifyTimer.state();
            break;
        default:
            assert false;
            break;
        }
        if (null != beginningState) {
            IFactory.getInstance().getCodeModifier().notifyTimerCall(behavior, 
                beginningState, notifyTimer, true);
            modified = true;
        }
        if (null != endState) {
            IFactory.getInstance().getCodeModifier().notifyTimerCall(behavior, 
                endState, notifyTimer, false);
            modified = true;
        }
        return modified;
    }
    
    /**
     * Is called to handle an instrumentation which occurred during 
     * instrumenting a method. By default, this method does not handle this
     * exception and causes throwing for further handling
     * 
     * @param exception the exception occurred
     * @return <code>true</code> if the exception was handled and 
     *     instrumentation can continue with the next method, <code>false</code>
     *     if the exception should be thrown by the caller for further handling
     * 
     * @since 1.00
     */
    protected boolean handleMethodInstrumentationException(
        InstrumenterException exception) {
        return false;
    }

    /**
     * Assigns the monitoring semantics for the method <code>methodName</code> 
     * in class <code>cls</code>.
     * 
     * @param cls the name of the class the monitoring semantics is defined for
     * @param methodSignature the name of the method to assign the semantics to
     * @param semantics the monitoring semantics
     * 
     * @since 1.00
     */
    public void assignSemantics(String cls, String methodSignature, 
        Monitor semantics) {
        if (null != semantics && !Configuration.INSTANCE.isInstrumented(cls)) { 
            boolean exclude = false;
            exclude |= cls.equals("int");
            exclude |= cls.equals("boolean");
            exclude |= cls.equals("byte");
            exclude |= cls.equals("short");
            exclude |= cls.equals("float");
            exclude |= cls.equals("double");
            exclude |= cls.equals("long");
            exclude |= cls.startsWith(
                "de.uni_hildesheim.sse.monitoring.runtime.");
            exclude |= cls.equals("java.lang.Object");
            exclude |= cls.equals("java.lang.Class");

            // ---- EXPERIMENT
            exclude |= cls.equals("java.lang.String");
            exclude |= cls.equals("java.lang.StringBuilder");
            exclude |= cls.equals("java.lang.StringBuffer");
            exclude |= cls.equals("java.io.PrintStream");
            // ---- EXPERIMENT

            if (!exclude) {
                HashMap<String, Monitor> sem = assignments.get(cls);
                if (null == sem) {
                    sem = new HashMap<String, Monitor>();
                    assignments.put(cls, sem);
                }
                sem.put(methodSignature, semantics);
            }
        }
    }
    
    /**
     * Removes the assigned semantics for <code>cls</code>.
     * 
     * @param cls the name of the class the monitoring semantics shall be 
     *     removed for
     * 
     * @since 1.00
     */
    public void deleteSemantics(String cls) {
        assignments.remove(cls);
    }

}
