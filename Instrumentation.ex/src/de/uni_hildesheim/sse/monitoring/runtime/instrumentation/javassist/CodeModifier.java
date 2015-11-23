package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Iterator;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.codeEraser.util.ClassPool;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ConfigurationChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.MeasurementValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.NotifyValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Timer;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.BooleanValue;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderAccess;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
//import de.uni_hildesheim.sse.monitoring.runtime.recording.Recorder;
import de.uni_hildesheim.sse.monitoring.runtime.recording.Recorder;
import de.uni_hildesheim.sse.monitoring.runtime.recording.SynchronizedRecorder;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.ShutdownMonitor;

/**
 * Implements a concrete code modifier.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CodeModifier implements ICodeModifier {

    /**
     * Stores the class name of the recorder interface.
     */
    public static final String RECORDER_TYPE = RecorderFrontend.class.getName();
    
    /**
     * Stores the access to the recorder instance.
     */
    public static final String RECORDER_INSTANCE = RECORDER_TYPE + ".instance";
    
    /**
     * Provides the fully qualified name of the recorder class as central
     * entry point. Contents is not required to be type compilant to the 
     * contents in {@link #RECORDER_TYPE} or {@link #RECORDER_INSTANCE}.
     */
    public static final String RECORDER;
        
    /**
     * Static instrumentation needs to refer to the recorder access
     * class which needs to check first whether the recorder was 
     * instrumented.
     */
    static {
        if (Configuration.INSTANCE.isStaticInstrumentation()) {
            RECORDER = RecorderAccess.class.getName();
        } else {
            RECORDER = RECORDER_INSTANCE;
        }
    }
    
    /**
     * Defines the fully qualified name of the IoNotifier.
     */
    //public static final String IONOTIFIER = IoNotifier.class.getName();

    /**
     * Defines the recorder call in case that memory is allocated.
     */
    public static final String MEM_ALLOC_CALL;
    
    /**
     * A counter for unique elements.
     */
    private static long id = 0;

    /**
     * Stores the relationship between measurement value constants and
     * related code generation data.
     */
    private static final HashMap<MeasurementValue, MeasurementValueData> 
    MEASUREMENT_VALUE_DATA_MAP 
        = new HashMap<MeasurementValue, MeasurementValueData>();
    
    static {
        putMeasurementValueData(MeasurementValue.ALL, 
            "assignAllTo(<<recId>>, <<value>>);", "");
        
        putMeasurementValueData(MeasurementValue.VALUE,
            "notifyValueChange(<<recId>>, <<value>>);", "");
            
        putMeasurementValueData(MeasurementValue.NET_IN, 
            "readIo(<<recId>>, <<caller>>, <<value>>, " 
            + StreamType.class.getName() + ".NET);", "int");

        putMeasurementValueData(MeasurementValue.NET_OUT, 
            "writeIo(<<recId>>, <<caller>>, <<value>>, " 
            + StreamType.class.getName() + ".NET);", "int");

        putMeasurementValueData(MeasurementValue.FILE_IN, 
            "readIo(<<recId>>, <<caller>>, <<value>>, " 
            + StreamType.class.getName() + ".FILE);", "int");

        putMeasurementValueData(MeasurementValue.FILE_OUT, 
            "writeIo(<<recId>>, <<caller>>, <<value>>, " 
            + StreamType.class.getName() + ".FILE);", "int");

        putMeasurementValueData(MeasurementValue.MEM_ALLOCATED, 
            "memoryAllocated(<<tag>>, <<value>>);", "long");

        putMeasurementValueData(MeasurementValue.MEM_UNALLOCATED, 
            "memoryFreed(<<tag>>, <<value>>);", "long");
    }
    
    /**
     * Determines the instrumentation command for monitoring memory allocation.
     * This method is needed in order to provide a hook for removing
     * it as variant.
     * 
     * @return the instrumentation command
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED, value = "")
    private static final String getMemAllocCall() {
        return RECORDER + ".memoryAllocated($_);";
    }
    
    /**
     * Initializes final variables.
     */
    static {
        MEM_ALLOC_CALL = getMemAllocCall();
    }
    
    /**
     * Returns the timer notification call for the given (overriding) state.
     * 
     * @param behavior the behavior to insert around
     * @param state the overriding state
     * @param notifyTimer the original timer annotation
     * @param before insert before or after
     * @throws InstrumenterException in case of byte code problems
     * 
     * @since 1.00
     */
    @Override
    public void notifyTimerCall(IBehavior behavior, TimerState state, 
        Timer notifyTimer, boolean before) throws InstrumenterException {
        StringBuilder call = new StringBuilder(RECORDER);
        call.append(".notifyTimer(\"");
        // null as id should not happen as this is not allowed for annotations
        call.append(Helper.trimId(notifyTimer.id()));
        call.append("\" ,");
        if (null == state) {
            state = notifyTimer.state();
        }
        call.append(state.getClass().getName());
        call.append(".");
        call.append(state.name());
        call.append(",");
        call.append(notifyTimer.considerThreads());
        call.append(");");
        CtBehavior beh = ((JABehavior) behavior).getBehavior();
        try {
            if (before) {
                beh.insertBefore(call.toString());
            } else {
                beh.insertAfter(call.toString());
            }
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Returns the call to register a thread with its native implementation.
     * 
     * @param builder the string builder to use (may be <b>null</b>)
     * @param register registration or unregistration call
     * @return the registering call code appended to <code>builder</code>
     * 
     * @since 1.00
     */
    private static final StringBuilder getRegisterThreadCall(
        StringBuilder builder, boolean register) {
        if (null == builder) {
            builder = new StringBuilder();
        }
        builder.append(RECORDER);
        builder.append(".registerThisThread(");
        builder.append(register);
        builder.append(");");
        return builder;
    }
    
    /**
     * Inserts the call to register a thread with its native implementation.
     * 
     * @param cls the class to insert the notification to
     * @param behavior the behavior to modify (may be <b>null</b> in case of
     *     a not found <code>run</code> method)
     * @param isMain is this the registration call for the main method
     * @return <code>true</code> if modifications took place
     * @throws InstrumenterException in case of byte code problems
     * 
     * @since 1.00
     */
    @Override
    public boolean notifyRegisterThread(IClass cls, IBehavior behavior, 
        boolean isMain) throws InstrumenterException {
        boolean modified = false;
        try {
            boolean register = Configuration.INSTANCE.registerThreads();
            if (isMain) {
                if (register) {
                    CtBehavior beh = ((JABehavior) behavior).getBehavior();
                    String call = getRegisterThreadCall(null, true).toString();
                    beh.insertBefore(call);
                    modified = true;
                }
            } else {
                if (null == behavior) {
                    StringBuilder call = new StringBuilder(
                        "public void run() {");
                    if (register) {
                        getRegisterThreadCall(call, true);
                    }
                    if (null != cls
                        .findSuperclassWithMethodWoParameter("run")) {
                        call.append("super.run();");
                    }
                    if (register) {
                        getRegisterThreadCall(call, false);
                    }
                    appendThreadEndCall(call);
                    call.append("}");
                    CtClass cl = ((JAClass) cls).getCtClass();
                    CtMethod m = CtNewMethod.make(call.toString(), cl);
                    cl.addMethod(m);
                    modified = true;
                } else {
                    CtBehavior beh = ((JABehavior) behavior).getBehavior();
                    if (register) {
                        beh.insertBefore(
                            getRegisterThreadCall(null, true).toString());
                    }
                    StringBuilder call = new StringBuilder();
                    if (register) {
                        getRegisterThreadCall(call, false);
                    }
                    appendThreadEndCall(call);
                    beh.insertAfter(call.toString());
                    modified = true;
                }
            }
        } catch (CannotCompileException e) {
            throw new InstrumenterException(cls.getName(), e);
        }
        return modified;
    }

    /**
     * Produces a call to notify the recorder about the end of the current 
     * thread.
     * 
     * @param builder the builder to append the call to
     * 
     * @since 1.00
     */
    private static final void appendThreadEndCall(StringBuilder builder) {
        builder.append(RECORDER);
        builder.append(".notifyThreadEnd();");
    }
    
    /**
     * Processes value notifications on the given method.
     * 
     * @param behavior the method to be changed
     * @param recId the recorder identification
     * @param ann the value annotation
     * @throws InstrumenterException if any instrumented code is not valid
     * 
     * @since 1.00
     */
    @Override
    public void valueNotification(IBehavior behavior, String recId,
        NotifyValue ann) throws InstrumenterException {
        MeasurementValueData mvData = 
            MEASUREMENT_VALUE_DATA_MAP.get(ann.value());
        if (null != mvData) {
            try {
                JABehavior jaBehavior = (JABehavior) behavior;
                CtBehavior behav = jaBehavior.getBehavior();
                String clsStr = jaBehavior.getClassNameExpression();
                if (MeasurementValue.MEM_ALLOCATED == ann.value()) {
                    behav.insertBefore(mvData.getMethodCall(recId, 
                        ann.expression(), "", ann.tagExpression()));
                } else if (MeasurementValue.MEM_UNALLOCATED == ann.value()) {
                    behav.insertBefore(mvData.getMethodCall(recId, 
                        ann.expression(), "", ann.tagExpression()));
                } else if (MeasurementValue.ALL == ann.value()) {
                    behav.insertBefore(
                        mvData.getMethodCall(recId, "true", "", ""));
                    behav.insertAfter(
                        mvData.getMethodCall(recId, "false", "", ""));
                } else {
                    if (MeasurementValue.VALUE == ann.value()) {
                        behav.insertAfter(mvData.getMethodCall(
                            recId, ann.expression(), clsStr, ""));
                    } else {
                        if (ann.notifyDifference()) {
                            String varName = getUniqueVariableName();
                            CtClass typeClass = ClassPool.get(mvData.getType());
                            behav.addLocalVariable(varName, typeClass);
                            behav.insertBefore(
                                varName + " = " + ann.expression() + ";");
                            behav.insertAfter(varName + " = " 
                                + ann.expression() + "-" + varName + ";" 
                                + mvData.getMethodCall(recId, varName, 
                                    clsStr, ""));
                        } else {
                            behav.insertBefore(mvData.
                                getMethodCall(recId, ann.expression(), 
                                    clsStr, ""));
                        }
                    }
                }
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            } catch (CannotCompileException e) {
                throw new InstrumenterException(e);
            }
        } else {
            System.err.println("no code generation registered for " 
                + ann.value());
        }
    }

    
    /**
     * Creates a (hopefully) unique variable name.
     * 
     * @return a (hopefully) unique variable name
     * 
     * @since 1.00
     */
    static final String getUniqueVariableName() {
        String varName = "spassMeter" + id;
        id++; 
        if (id < 0) {
            id = 0;
        }
        return varName;
    }

    /**
     * Instrument the random access file. In this method we instrument only a
     * part because we cannot instrument native methods. The rest is done in
     * DelegatingRandomAccessFile (do not link directly).
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException if code problems occur in injected code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    @Override
    public void instrumentRandomAccessFile(IClass cls) 
        throws InstrumenterException {
        try {
            CtClass cl = ((JAClass) cls).getCtClass();
            CtField field;
            field = CtField.make("public java.lang.String recId = null;", cl);
            cl.addField(field);
            String streamTypeCode = StreamType.FILE.toCode();
            CtBehavior[] methods = cl.getDeclaredBehaviors();
            for (int i = 0; i < methods.length; i++) {
                CtBehavior method = methods[i];
                if (method.getName().equals("writeBytes")) {
                    if (1 == method.getParameterTypes().length) {
                        method.insertAfter("if (null!=" + RECORDER_INSTANCE 
                            + ") " + RECORDER_INSTANCE 
                            + ".writeIo(recId, null, $1.length(), " 
                            + streamTypeCode + ");");
                    }
                } else if (method.getName().equals("writeChars")) {
                    if (1 == method.getParameterTypes().length) {
                        method.insertAfter("if (null!=" + RECORDER_INSTANCE 
                            + ") " + RECORDER_INSTANCE + ".writeIo(recId,null,"
                            + "2*$1.length()," + streamTypeCode + ");");
                    }
                }
            }
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Instruments the socket output stream class.
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException if code problems occur in injected code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public void instrumentSocketOutputStream(IClass cls) 
        throws InstrumenterException {
        CtClass cl = ((JAClass) cls).getCtClass();
        try {
            CtField field = CtField.make(
                "public java.lang.String recId = null;", cl);
            cl.addField(field);
    
            //field = CtField.make("private boolean inWrite3 = false;", cl);
            //cl.addField(field);
    
            //field = CtField.make("private boolean inWrite1 = false;", cl);
            //cl.addField(field);
    
            CtBehavior[] methods = cl.getDeclaredBehaviors();
            for (int i = 0; i < methods.length; i++) {
                instrumentStreamWrite(methods[i], StreamType.NET);
            }
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Instruments the socket input stream class.
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException if code problems occur in injected code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public void instrumentSocketInputStream(IClass cls) 
        throws InstrumenterException {
        CtClass cl = ((JAClass) cls).getCtClass();
        try {
            CtField field = CtField.make(
                "public java.lang.String recId = null;", cl);
            cl.addField(field);
            //field = CtField.make("private boolean inRead3 = false;", cl);
            //cl.addField(field);
            //field = CtField.make("private boolean inRead1 = false;", cl);
            //cl.addField(field);
    
            CtBehavior[] methods = cl.getDeclaredBehaviors();
            for (int i = 0; i < methods.length; i++) {
                instrumentStreamRead(methods[i], StreamType.NET);
            }
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * A specific expression editor to insert code before 
     * <code>System.exit</code> if it exists.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class SystemExitExprEditor extends ExprEditor {
        
        /**
         * Stores the code to insert. Must not be <b>null</b> at the beginning
         * of the constructor, is set to <b>null</b> after successful 
         * instrumentation before <code>System.exit</code>.
         */
        private String code;

        /**
         * Creates the editor for a given behavior and instruments it. 
         * If <code>System.exit</code> cannot be found in the method, just 
         * append <code>code</code> otherways insert it before 
         * <code>System.exit</code>. 
         *
         * @param behavior the behavior to instrument
         * @param code the code to insert before or after
         * @throws CannotCompileException in case of any compile error in the 
         *   new code
         */
        public SystemExitExprEditor(CtBehavior behavior, String code) 
            throws CannotCompileException {
            this.code = code;
            if (null != code && code.length() > 0) {
                behavior.instrument(this);
                // heuristics: exact would require to analyze alternatives
                // and try-catch-blocks... if System.exit is before, than
                // the program is terminated before the duplicated insertion
                //if (null != this.code) {
                behavior.insertAfter(code);
                //}
            }
        }
        
        /**
         * Edits a method call, searches for system exit and inserts if 
         * present the given {@link #code}.
         *
         * @param mc the method call to consider
         * @throws CannotCompileException in case of any compile error in the 
         *   new code
         */
        public void edit(MethodCall mc) throws CannotCompileException {
            try {
                if (null != code) {
                    CtMethod method = mc.getMethod();
                    if ("exit".equals(method.getName()) 
                        && method.getDeclaringClass().getName().equals(
                            System.class.getName())) {
                        mc.replace(code + " $_ = $proceed($$);");
                        code = null;
                    }
                }
            } catch (NotFoundException nf) {
            }
        }

    }
    
    /**
     * Instruments the a method for ending the recorder.
     * 
     * @param behavior the behavior / method to be changed
     * @param printStatistics if statistics should be printed at the end of the 
     *     program
     * @param invoke an additional method to be invoked after monitoring (fqn 
     *     class name "." method)
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    @Override
    public void instrumentEndSystem(IBehavior behavior, 
        boolean printStatistics, String invoke) 
        throws InstrumenterException {
        try {
            JABehavior jaBehavior = (JABehavior) behavior;
            new SystemExitExprEditor(jaBehavior.getBehavior(), 
                ShutdownMonitor.class.getName() + ".endMonitoring(" 
                + printStatistics + "," 
                + jaBehavior.getClassLoaderExpression(true) 
                + ",\"" + jaBehavior.expandInvoke(invoke) 
                + "\");");
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Instruments the a method for starting the recorder.
     * 
     * @param behavior the behavior / method to be changed
     * @param shutdownHook insert a shutdown hook
     * @param invoke a method to call at the end of execution (testing)
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    @Override
    public void instrumentStartSystem(
        IBehavior behavior, boolean shutdownHook, String invoke)
        throws InstrumenterException {
        try {
            JABehavior jaBehavior = (JABehavior) behavior;
            StringBuilder startCode = new StringBuilder();
            switch (Configuration.INSTANCE.getRecordingType()) {
            case LOCAL_ASYNCHRONOUS:
                startCode.append(Recorder.class.getName());
                // TODO legacy, change
                //startCode.append(SynchronizedRecorder.class.getName()); 
                break;
            case LOCAL_SYNCHRONOUS:
            case TCP:
            default:
                startCode.append(SynchronizedRecorder.class.getName());
                break;
            }
            //startCode.append(SynchronizedRecorder.class.getName());
            startCode.append(".initialize();"); // keep this line !!!
            
            startCode.append(RECORDER);
            startCode.append(".notifyProgramStart();");
            if (shutdownHook) {
                startCode.insert(0, "));");
                startCode.insert(0, Configuration.INSTANCE.printStatistics());
                startCode.insert(0, "\",");
                startCode.insert(0, jaBehavior.expandInvoke(invoke));
                startCode.insert(0, ",\"");
                startCode.insert(0, jaBehavior.getClassLoaderExpression(false));
                startCode.insert(0, "(");
                startCode.insert(0, ShutdownMonitor.class.getName());
                startCode.insert(0, ".getRuntime().addShutdownHook(new ");
                startCode.insert(0, Runtime.class.getName());
            }
            jaBehavior.getBehavior().insertBefore(startCode.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Performs the instrumentation for a configuration change, i.e. to notify 
     * the recorder about an explicit change of 
     * 
     * @param behavior the behavior to be changed
     * @param annotation the annotation containing information on the 
     *     configuration change
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    @Override
    public void instrumentConfigurationChange(
        IBehavior behavior, ConfigurationChange annotation) 
        throws InstrumenterException {
        try {
            JABehavior jaBehavior = (JABehavior) behavior;
            StringBuilder code = new StringBuilder();
            code.append(RECORDER);
            code.append(".configurationChange(");
            if (annotation.idExpression().length() > 0) {
                code.append(annotation.idExpression());
            } else {
                code.append(annotation.valueExpression());
            }
            code.append(");");
            jaBehavior.getBehavior().insertBefore(code.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Instrument an existing finalize method in order to notify the recorder
     * about freeing this object.
     * 
     * @param method the method being considered for instrumentation
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Override
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void instrumentFinalize(IBehavior method) 
        throws InstrumenterException {
        try {
            if (method.isFinalize()) {
                ((JABehavior) method).insertAfter(
                    RECORDER + ".memoryFreed(this);");
            }
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Instruments an existing constructor in order to notify the recorder
     * about memory allocation.
     * 
     * @param method the method being considered for instrumentation
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Override
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void instrumentConstructor(IBehavior method) 
        throws InstrumenterException {
        try {
            ((JABehavior) method).insertAfter(RECORDER 
                + ".memoryAllocated(this);");
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Instruments a method for timing measurement.
     * 
     * @param behavior the method / behavior to be changed
     * @param mGroup the group annotation causing this instrumentation (must 
     *     not be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is the cause direct, e.g. in case of an annotated method
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    @Override
    public void instrumentTiming(IBehavior behavior, 
        Monitor mGroup, boolean exclude, 
        boolean directId) throws InstrumenterException {
        try {
            JABehavior jaBehavior = (JABehavior) behavior;
            String clsStr = jaBehavior.getClassNameExpression();
            String recId;
            if (null != mGroup) {
                recId = Configuration.INSTANCE.getRecId(mGroup.id());
                if (null != recId) {
                    recId = "\"" + recId + "\"";
                }
                if (null == recId) {
                    recId = clsStr;
                }
            } else {
                recId = "\"\"";
            }
            String entryCode = RECORDER + ".enter(" + clsStr + "," + recId 
                + "," + exclude + "," + directId + ");";
            jaBehavior.insertBefore(entryCode);
            String exitCode = RECORDER + ".exit(" + clsStr + "," + recId 
                + "," + exclude + "," + directId + ");";
            try {
                CtClass ctThrowable = ClassPool.get("java.lang.Throwable");
                jaBehavior.addCatch(
                    "{ " + exitCode + " throw $e; }", ctThrowable);
                // do not detach ctThrowable
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            jaBehavior.insertAfter(exitCode);
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Adds an registering initializer to <code>cls</code>.
     * 
     * @param cls the class the initializer should be added to
     * @param mGroup the related annotation
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    @Override
    public void addRegisteringInitializer(IClass cls, 
        Monitor mGroup) throws InstrumenterException {
        try {
            CtClass cl = ((JAClass) cls).getCtClass();
            CtConstructor init = cl.makeClassInitializer();
            final String paramSeparator = ", ";

            // produce code for obtaining the settings instance
            String settingsClass = MonitoringGroupSettings.class.getName();
            StringBuilder builder = new StringBuilder();
            builder.append(settingsClass);
            builder.append(" _settings=");
            builder.append(settingsClass);
            builder.append(".getFromPool();");
            
            // produce code for filling basic values
            builder.append("_settings.setBasics(");
            idAsParam(mGroup.id(), builder, cls);
            builder.append(paramSeparator);
            debugStatesAsParam(mGroup.debug(), builder);
            builder.append(paramSeparator);
            builder.append(GroupAccountingType.class.getName());
            builder.append(".");
            builder.append(mGroup.groupAccounting().name());
            builder.append(paramSeparator);
            resourcesAsParam(mGroup.resources(), builder);
            builder.append(");");

            // optional: produce code for filling multi values
            if (mGroup.id().length > 1) {
                builder.append("_settings.setMulti(");
                booleanValueAsParam(mGroup.distributeValues(), builder);
                builder.append(paramSeparator);
                booleanValueAsParam(mGroup.considerContained(), builder);
                builder.append(");");
            }
            
            // produce code for registering the group
            builder.append(RECORDER);
            builder.append(".registerForRecording(\"");
            builder.append(cl.getName());
            builder.append("\"");
            builder.append(paramSeparator);
            builder.append("_settings");
            builder.append(");");
            builder.append(settingsClass);
            builder.append(".release(_settings);");
            init.insertBefore(builder.toString());
            // do not use setBody here as it may overwrite existing initializers
        } catch (CannotCompileException e) {
            System.err.println("cannot add class initializer to " 
                + cls.getName() + " " + e.getMessage());
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Turns the ids of a monitoring group into a Java parameter and adds this
     * information to the given <code>builder</code>.
     * 
     * @param rId the ids
     * @param builder the builder to be modified as a side effect
     * @param cls the class to create the params for
     * @return <code>builder</code>
     * 
     * @since 1.00
     */
    private static final StringBuilder idAsParam(String[] rId, 
        StringBuilder builder, IClass cls) {
        if (rId.length > 0) {
            builder.append("new String[]{");
            for (int i = 0; i < rId.length; i++) {
                String id = rId[i].trim();
                if (i > 0) {
                    builder.append(",");
                }
                builder.append("\"");
                if (id.length() > 0) {
                    builder.append(id);
                } else {
                    builder.append(cls.getName());
                }
                builder.append("\"");
            }
            builder.append("}");
        } else {
            builder.append("null");
        }
        return builder;
    }

    /**
     * Turns the debug states of a monitoring group into a Java parameter and 
     * adds this information to the given <code>builder</code>.
     * 
     * @param states the debug states
     * @param builder the builder to be modified as a side effect
     * @return <code>builder</code>
     * 
     * @since 1.00
     */
    private static final StringBuilder debugStatesAsParam(DebugState[] states, 
        StringBuilder builder) {
        if (0 == states.length) {
            builder.append(DebugState.class.getName());
            builder.append(".NONE");
        } else {
            builder.append("new ");
            builder.append(DebugState.class.getName());
            builder.append("[] {");
            for (int i = 0; i < states.length; i++) {
                builder.append(DebugState.class.getName());
                builder.append(".");
                builder.append(states[i].name());
                if (i < states.length - 1) {
                    builder.append(",");
                }
            }
            builder.append("}");
        }
        return builder;
    }

    /**
     * Turns the accountable resources of a monitoring group into a Java 
     * parameter and adds this information to the given <code>builder</code>.
     * 
     * @param resources the accountable resources
     * @param builder the builder to be modified as a side effect
     * @return <code>builder</code>
     * 
     * @since 1.00
     */
    private static final StringBuilder resourcesAsParam(
        ResourceType[] resources, StringBuilder builder) {
        if (0 == resources.length) {
            builder.append(ResourceType.class.getName());
            builder.append(".SET_DEFAULT");
        } else {
            builder.append("new ");
            builder.append(ResourceType.class.getName());
            builder.append("[] {");
            for (int i = 0; i < resources.length; i++) {
                builder.append(ResourceType.class.getName());
                builder.append(".");
                builder.append(resources[i].name());
                if (i < resources.length - 1) {
                    builder.append(",");
                }
            }
            builder.append("}");
        }
        return builder;
    }

    /**
     * Turns the given boolean <code>value</code> into a Java 
     * parameter and adds this information to the given <code>builder</code>.
     * 
     * @param value the value to be converted
     * @param builder the builder to be modified as a side effect
     * @return <code>builder</code>
     * 
     * @since 1.00
     */
    private static final StringBuilder booleanValueAsParam(BooleanValue value, 
        StringBuilder builder) {
        builder.append(BooleanValue.class.getName());
        builder.append(".");
        builder.append(value.name());
        return builder;
    }
    
    /**
     * Searches the superclasses of <code>cl</code> for a final finalizer 
     * method.
     * 
     * @param cl the class to be searched for
     * @return <code>true</code> if one of the superclasses defines 
     *    a final finalizer (with a final class the entire hierarchy would
     *    be erroneous), <code>false</code> else
     * 
     * @since 1.00
     */
    /*private static final boolean hasSuperClassFinalFinalizer(CtClass cl) {
        boolean found = false;
        try {
            if (null != cl.getSuperclass()) {
                CtClass superCl = cl.getSuperclass();
                if (Configuration.INSTANCE.isInstrumented(superCl.getName())) {
                    found = true;
                } else {
                    for (CtMethod method : superCl.getMethods()) {
                        try {
                            if (Utils.isFinalize(method.getName(), 
                                method.getParameterTypes().length) 
                                && Modifier.isFinal(method.getModifiers())) {
                                found = true;
                                break;
                            }
                        } catch (NotFoundException ne) {
                        }
                    }
                }
                if (!found) {
                    found = hasSuperClassFinalFinalizer(cl.getSuperclass());
                }
            }
        } catch (NotFoundException ex) {
        }
        return found;
    }*/

    /**
     * Instruments the given method as a variability handler, i.e. disables
     * automatic variability detection.
     * 
     * @param behavior the behavior to be instrumented
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Override
    public void instrumentVariabilityHandler(IBehavior behavior) 
        throws InstrumenterException {
        try {
            JABehavior jaBehavior = (JABehavior) behavior;
            jaBehavior.insertBefore(RECORDER 
                + ".enableVariabilityDetection(false);");
            jaBehavior.insertAfter(RECORDER 
                + ".enableVariabilityDetection(true);");
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Adds a finalizer method to this class or to the finalizer method of 
     * a super class (except of object). If a finalizer method was defined
     * in a superclass (probably final) then this method is instrumented. If 
     * none is found, a new finalizer method is added.
     * 
     * @param cls the class to add the finalizer to
     * @param overhead instrument for overhead or for "normal" classes
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    @Override
    public void addFinalizer(IClass cls, boolean overhead) 
        throws InstrumenterException {
        if (!overhead) {
            try {
                CtClass cl = ((JAClass) cls).getCtClass();
                if (!cls.hasSuperClassFinalFinalizer()) {
                    CtMethod m = CtNewMethod.make(
                        "protected void finalize() throws Throwable {"
                        + "super.finalize();"
                        + RECORDER + ".memoryFreed(this);}", cl);
                    cl.addMethod(m);
                }
            } catch (CannotCompileException e) {
                throw new InstrumenterException(e);
            }
        }
    }
    
    /**
     * Instruments read methods of a stream. This method should be applied
     * to stream classes only.
     * 
     * @param method the method to be considered for instrumentation
     * @param type the type of the stream
     * @throws CannotCompileException if code problems occur in injected code
     * @throws NotFoundException if Java elements were not found
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.OR)
    private static void instrumentStreamRead(CtBehavior method, 
        StreamType type)
        throws CannotCompileException, NotFoundException {
        if (method.getName().equals("read")) {
            CtClass[] parameter = method.getParameterTypes();
            // do not instrument the other write methods as they all delegate
            // to this one
            if (3 == parameter.length) {
                /*if (!Helper.NEW_IO_HANDLING) {
                    method.insertAfter(
                        "if (null!=myRead) {myRead.charAt($_);}");
                } else {*/
                method.insertAfter("if (null!=" + RECORDER_INSTANCE + ") {"
                    + RECORDER_INSTANCE + ".readIo(recId, null, $_, " 
                    + type.toCode() + ");}");
                //}
            }
        }
    }

    /**
     * Instruments a write methods of a stream. This method should be applied
     * to stream classes only.
     * 
     * @param method the method to be considered for instrumentation
     * @param type the type of the stream
     * @throws CannotCompileException if code problems occur in injected code
     * @throws NotFoundException if Java elements were not found
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    private static void instrumentStreamWrite(CtBehavior method, 
        StreamType type) throws CannotCompileException, 
        NotFoundException {
        if (method.getName().equals("write")) {
            CtClass[] parameter = method.getParameterTypes();
            switch (parameter.length) {
            case 1:
                if (parameter[0].isArray()) {
                    method.insertAfter("if (null!=" + RECORDER_INSTANCE 
                        + ") " + RECORDER_INSTANCE 
                        + ".writeIo(recId,null,$1.length," + type.toCode()
                        + ");");
                } else {
                    method.insertAfter("if (null!=" + RECORDER_INSTANCE 
                        + ") " 
                        + RECORDER_INSTANCE + ".writeIo(recId,null,1," 
                        + type.toCode() + ");");
                }
                break;
            case 3:
                method.insertAfter("if (null!=" + RECORDER_INSTANCE 
                    + ") " + "" + RECORDER_INSTANCE 
                    + ".writeIo(recId,null,$3," + type.toCode() 
                    + ");");
                break;
            default:
                // do nothing
                break;
            }
        }
    }

    /**
     * Assigns a measurement <code>value</code> constant to the code generation 
     * data. This method is required to realize variabilities of the measurement
     * value constants.
     * 
     * @param value the measurement value constant to assign the code 
     *   generation information to
     * @param recorderMethod the (unqualified) method for instrumentation with 
     *   some parts to be replaced in the concrete context; may contain 
     *   &lt;&lt;value&gt;&gt; and &lt;&lt;caller&gt;&gt;
     * @param type the type of the expression, used for keeping the value in 
     *   a local variable during the instrumented call
     * 
     * @since 1.00
     */
    private static final void putMeasurementValueData(MeasurementValue value, 
        String recorderMethod, String type) {
        if (null != value) { // may be in case of disabled variability
            MEASUREMENT_VALUE_DATA_MAP.put(value, 
                new MeasurementValueData(recorderMethod, type));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAnnotation(IClass cls, Class<? extends Annotation> ann, 
        HashMap<String, Object> values) 
        throws InstrumenterException {
        CtClass cl = ((JAClass) cls).getCtClass();
        ClassFile ccFile = cl.getClassFile();
        ConstPool constpool = ccFile.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, 
            AnnotationsAttribute.visibleTag);
        javassist.bytecode.annotation.Annotation marker 
            = new javassist.bytecode.annotation.Annotation(
                ann.getName(), constpool);
        setValues(marker, constpool, values);
        attr.addAnnotation(marker);
        cl.getClassFile().addAttribute(attr);
    }
    
    /**
     * Sets the values for an annotation.
     * 
     * @param annotation the annotation
     * @param cp the class pool
     * @param values the values as name-value mappings
     * 
     * @since 1.00
     */
    private void setValues(javassist.bytecode.annotation.Annotation annotation, 
        ConstPool cp, HashMap<String, Object> values) {
        if (null != values) {
            Iterator<HashMap.Entry<String, Object>> iter 
                = values.entries().iterator();
            while (iter.hasNext()) {
                HashMap.Entry<String, Object> ent = iter.next();
                MemberValue mVal = createMemberValue(ent.getValue(), cp);
                if (null != mVal) {
                    annotation.addMemberValue(ent.getKey(), mVal);
                }
            }
        }
        
    }
    
    /**
     * Creates a Javassist member value for an annotation.
     * 
     * @param val the value to be turned into a member value
     * @param cp the constant pool
     * @return the member value
     * 
     * @since 1.00
     */
    private MemberValue createMemberValue(Object val, ConstPool cp) {
        MemberValue mVal = null;
        if (val instanceof Boolean) {
            mVal = new BooleanMemberValue((Boolean) val, cp);
        } else if (val instanceof Byte) {
            mVal = new ByteMemberValue((Byte) val, cp);
        } else if (val instanceof Character) {
            mVal = new CharMemberValue((Character) val, cp);
        } else if (val instanceof Short) {
            mVal = new ShortMemberValue((Short) val, cp);
        } else if (val instanceof Integer) {
            mVal = new IntegerMemberValue(cp, (Integer) val);
        }  else if (val instanceof Long) {
            mVal = new LongMemberValue((Long) val, cp);
        } else if (val instanceof Float) {
            mVal = new FloatMemberValue((Float) val, cp);
        } else if (val instanceof Double) {
            mVal = new DoubleMemberValue((Double) val, cp);
        } else if (val instanceof Class) {
            mVal = new ClassMemberValue(((Class<?>) val).getName(), cp);
        } else if (val instanceof String) {
            mVal = new StringMemberValue((String) val, cp);
        } else if (val.getClass().isArray()) {
            try {
                CtClass arrayType = ClassPool.get(val.getClass()
                    .getComponentType().getName());
                MemberValue member = javassist.bytecode.annotation.Annotation
                    .createMemberValue(cp, arrayType);
                ArrayMemberValue value = new ArrayMemberValue(member, cp);
                int len = Array.getLength(val);
                MemberValue[] valArray = new MemberValue[len];
                for (int i = 0; i < len; i++) {
                    valArray[i] = createMemberValue(Array.get(val, i), cp);
                }
                value.setValue(valArray);
                mVal = value;
            } catch (NotFoundException e) {
            }
        } else if (val.getClass().isInterface()) {
            try {
                CtClass type = ClassPool.get(val.getClass().getName());
                mVal = new AnnotationMemberValue(new javassist.bytecode.
                    annotation.Annotation(cp, type), cp);
            } catch (NotFoundException e) {
            }
        } else {
            EnumMemberValue emv = new EnumMemberValue(cp);
            emv.setType(val.getClass().getName());
            emv.setValue(val.toString());
            mVal = emv;
        }
        return mVal;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addAnnotation(IBehavior behavior, 
        Class<? extends Annotation> ann, HashMap<String, Object> values) 
        throws InstrumenterException {

        CtBehavior b = ((JABehavior) behavior).getBehavior();
        CtClass cl = ((JAClass) behavior.getDeclaringClass()).getCtClass();
        ClassFile ccFile = cl.getClassFile();
        ConstPool constpool = ccFile.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, 
            AnnotationsAttribute.visibleTag);
        javassist.bytecode.annotation.Annotation marker 
            = new javassist.bytecode.annotation.Annotation(
                ann.getName(), constpool);
        setValues(marker, constpool, values);
        attr.addAnnotation(marker);
        b.getMethodInfo().addAttribute(attr);
    }
    
    /**
     * Stores the command line configuration to the configuration class.
     * 
     * @param cls the class instance of the configuration class
     * @param args the command line arguments
     * @throws InstrumenterException in case that modifying the byte code fails
     * 
     * @since 1.00
     */
    public void storeConfiguration(IClass cls, String args) 
        throws InstrumenterException {
        try {
            CtClass cl = ((JAClass) cls).getCtClass();
            cl.instrument(new ConfigurationFieldEditor(args));
        } catch (CannotCompileException e) {
            System.err.println("cannot store configuration arguments: " 
                + e.getMessage());
            throw new InstrumenterException(e);
        } 
    }

}
