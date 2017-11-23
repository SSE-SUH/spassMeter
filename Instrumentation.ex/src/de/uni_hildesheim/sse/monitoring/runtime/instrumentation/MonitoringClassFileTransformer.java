package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IClass;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IFactory;
import de.uni_hildesheim.sse.monitoring.runtime.recording.Lock;
import de.uni_hildesheim.sse.system.GathererFactory;

/**
 * Implements the frontend for dynamic bytecode instrumentation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class MonitoringClassFileTransformer extends AbstractClassTransformer 
    implements ClassFileTransformer {

    /**
     * Creates a new monitoring class file transformer.
     * 
     * @since 1.00
     */
    public MonitoringClassFileTransformer() {
        super(false);
    }
    
    /**
     * The implementation of this method may transform the supplied class file 
     * and return a new replacement class file. 
     * 
     * @param loader - the defining loader of the class to be transformed, 
     *   may be null if the bootstrap loader
     * @param className - the name of the class in the internal form of 
     *   fully qualified class and interface names as defined in The Java 
     *   Virtual Machine Specification. For example, "java/util/List".
     * @param classBeingRedefined - if this is triggered by a redefine or 
     *   retransform, the class being redefined or retransformed; if this 
     *   is a class load, null
     * @param protectionDomain - the protection domain of the class being 
     *   defined or redefined
     * @param classFileBuffer - the input byte buffer in class file format - 
     *   must not be modified 
     * @return a well-formed class file buffer (the result of the transform), 
     *   or null if no transform is performed.
     *   
     * @throws IllegalClassFormatException in case that illegal byte code is
     *     produced
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, 
        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, 
        byte[] classFileBuffer) throws IllegalClassFormatException {
        byte[] result;
        if (shouldInstrument(className)) {
            long tid = Thread.currentThread().getId();
            long accMem = Lock.isStackTopMemoryAccounting(tid);
            try {
                // synchronized due to parallel cleanup
                result = doClass(loader, className, classFileBuffer, 
                    null != classBeingRedefined);
            } catch (Throwable t) {
                // e.g. ClassCircularityError
                result = null;
            }
            Lock.setStackTopMemoryAccounting(tid, accMem);
        } else {
            deleteSemantics(className);
            result = null; // classFileBuffer
        }
        return result;
    }
    
    /**
     * Processes the given class.
     * 
     * @param loader the class loader
     * @param name the name of the class given in in the internal form of 
     *   fully qualified class and interface names as defined in The Java 
     *   Virtual Machine Specification, i.e. slashes instead of dots.
     * @param classBytes the loaded class as a byte array
     * @param isRedefinition <code>true</code> in case that the class is being
     *   redefined / retransformed, <code>false</code> else
     * @return the new bytecode for the class in case that it was instrumented,
     *   <b>null</b> if the class was not modified
     * 
     * @since 1.00
     */
    private final byte[] doClass(ClassLoader loader, String name, 
        byte[] classBytes, boolean isRedefinition) {
        byte[] result = null;
        if (!Configuration.INSTANCE.isInstrumented(name.replace('/', '.'))) {
            synchronized (IFactory.LOCK) {
            //IFactory.aquireReentrantLock();
                try {
                    IFactory factory = IFactory.getInstance();
                    IClass cl = factory.obtainClass(
                        loader, name, classBytes, isRedefinition);
                    TransformationType type = isRedefinition 
                        ? TransformationType.REDEFINITION 
                        : TransformationType.ON_LOAD;
                    result = transform0(name, cl, type);
                } catch (InstrumenterException e) {
                    log("Error instrumenting" + name, e, Level.SEVERE);
                } //finally {
               // IFactory.releaseReentrantLock();            
            //}
            }
        }
        return result;
    }
    
    /**
     * Top-level call of {@link #transform(String, IClass, TransformationType)}.
     * 
     * @param name the name of the class given in the 
     *   internal form of fully qualified class and interface names as defined 
     *   in The Java Virtual Machine Specification. For example, 
     *   "java/util/List".
     * @param cl the class
     * @param type the type of the transformation
     * @return the new bytecode for the class in case that it was instrumented,
     *   <b>null</b> if the class was not modified
     * 
     * @since 1.00
     */
    private byte[] transform0(String name, IClass cl, TransformationType type) {
        byte[] result = null;
        try {
            if (transform(name, cl, type)) {
                result = cl.toBytecode();
            }
        } catch (InstrumenterException e) {
            log("Cannot instrument " + name, e, Level.SEVERE);
        } catch (Throwable e) {
            log("Cannot instrument " + name, e, Level.SEVERE);
        } finally {
            cl.release();
        }
        return result;
    }
    
    /**
     * Logs the message and attaches the first stack trace element (seen from 
     * the top of the stack) which belongs to <b>this package</b>.
     * 
     * @param text the message to be logged
     * @param th the throwable to be considered
     * @param level the logging level
     *
     * @since 1.00
     */
    private static final void log(String text, Throwable th, Level level) {
        StringBuilder result = new StringBuilder(text);
        String separator = System.getProperty("line.separator", "\n");
        StackTraceElement[] e = th.getStackTrace();
        String pkg = MonitoringClassFileTransformer.class.getPackage().
            getName();
        StackTraceElement closest = getClosest(e, pkg);
        if (null != th.getMessage()) {
            result.append(" Msg: ");
            result.append(th.getClass().getName());
            result.append(":");
            result.append(th.getMessage());
            result.append(separator);
        }
        if (null != closest) {
            result.append(" @ ");
            result.append(closest.toString());
            result.append(separator);
        }
        if (null != th.getCause()) {
            closest = getClosest(th.getCause().getStackTrace(), pkg);
            if (null != closest) {
                result.append(" @ ");
                result.append(closest.toString());
                result.append(separator);
            }
        }
        for (int i = 0; i < e.length; i++) {
            result.append(e[i].toString());
            result.append(separator);
        }
        if (null != th.getCause()) {
            result.append("caused by ");
            result.append(th.getCause().getMessage());
            result.append(separator);
            e = th.getCause().getStackTrace();
            for (int i = 0; i < e.length; i++) {
                result.append(e[i].toString());
                result.append(separator);
            }
        }
        Configuration.LOG.log(level, result.toString());
    }

    /**
     * Returns the first stack trace element in <code>trace</code> which 
     * relates to the given package <code>pkg</code>.
     * 
     * @param trace the trace to be analyzed
     * @param pkg the package
     * @return the closest (first) stack trace element, <b>null</b> if not found
     * 
     * @since 1.00
     */
    private static StackTraceElement getClosest(StackTraceElement[] trace, 
        String pkg) {
        StackTraceElement found = null;
        for (int i = 0; null == found && i < trace.length; i++) {
            if (trace[i].getClassName().startsWith(pkg)) {
                found = trace[i];
            }
        }
        return found;
    }
    
    /**
     * Retransforms the specified classes.
     * 
     * @param classNames the class names, values are ignored
     * 
     * @since 1.00
     */
    protected void retransformAssigned(String[] classNames) {
        IFactory factory = IFactory.getInstance();
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        ArrayList<byte[]> transformed = new ArrayList<byte[]>();
        for (int c = 0; c < classNames.length; c++) {
            try {
                if (null != classNames[c] 
                    && !Configuration.INSTANCE.isInstrumented(classNames[c])) {
                    String internalName = classNames[c].replace('.', '/');
                    if (shouldInstrument(internalName)) {
                        IClass cl = factory.obtainClass(classNames[c], true);
                        if (null != cl) {
                            byte[] tResult = transform0(internalName, cl, 
                                TransformationType.REDEFINITION);
                            if (null != tResult) {
                                classes.add(
                                    factory.getLoadedClass(classNames[c]));
                                transformed.add(tResult);
                            }
                        }
                    }
                }
            } catch (InstrumenterException e) {
                log("while retransformation", e, Level.SEVERE);
            }
        }
        if (!transformed.isEmpty()) {
            /*
            Class<?>[] aClasses = new Class<?>[1];
            byte[][] bytecode = new byte[1][];
            for (int t = 0; t < transformed.size(); t++) {
                aClasses[0] = classes.get(t);
                bytecode[0] = transformed.get(t);
                GathererFactory.getDataGatherer().redefineClass(
                    classes.get(t), transformed.get(t));
            }
            */
            Class<?>[] aClasses = new Class<?>[classes.size()];
            classes.toArray(aClasses);
            byte[][] bytecode = new byte[classes.size()][];
            for (int t = 0; t < transformed.size(); t++) {
                bytecode[t] = transformed.get(t);
            }
            // unclear whether this may cause circular link problems
            GathererFactory.getDataGatherer().redefineClasses(aClasses, 
                bytecode);
        }
    }

}
