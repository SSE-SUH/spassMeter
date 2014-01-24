package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

import java.io.IOException;
import java.io.InputStream;
//import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.xml.IResolver;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.internal.*;

/**
 * Provides a factory to the instrumentation library classes. A 
 * factory must define a static initializer registering itself.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class IFactory implements IResolver, Cleanup {
    
    /**
     * Defines a general lock on the byte code modifier. Use this lock
     * to shield the modifier from parallel cleanup operations.
     */
    public static final Object LOCK = new Object();
//    private static final ReentrantReadWriteLock LOCK 
//        = new ReentrantReadWriteLock(true);
    
    /**
     * Stores the factory instance.
     */
    private static IFactory instance;
    
    /**
     * Dynamically creates the factory upon the information in 
     * {@link Configuration}.
     * 
     * @since 1.00
     */
    static {
        String clsName = Configuration.INSTANCE.getInstrumenterFactory();
        try {
            Class.forName(clsName);
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("using default factory: " + e.getMessage());
            try {
                Class.forName(Configuration.DEFAULT_INSTRUMENTER);
            } catch (Throwable e1) {
                System.err.println("default factory failed: "
                    + e1.getMessage());
                System.exit(-1);
            }
        }
    }

    /**
     * Register a factory. To be used in the static initializer.
     * Can be called only once.
     * 
     * @param factory the new factory
     * 
     * @since 1.00
     */
    protected static final void setInstance(IFactory factory) {
        if (null == instance) {
            instance = factory;
            InternalPluginRegistry.attachInstrumenterCleanup(instance);
        }
    }
    
    /**
     * Returns the factory instance.
     * 
     * @return the factory instance
     * 
     * @since 1.00
     */
    public static final IFactory getInstance() {
        return instance;
    }
    
    /**
     * Acquires a reentrant lock for byte code manipulation. Multiple byte code
     * manipulations may run in parallel.
     * 
     * @since 1.00
     */
    //public static final void aquireReentrantLock() {
    //    LOCK.readLock().lock();
   // }

    /**
     * Releases a lock for byte code manipulation acquired 
     * by {@link #aquireReentrantLock()}.
     * 
     * @since 1.00
     */
    //public static final void releaseReentrantLock() {
    //    LOCK.readLock().unlock();
    //}

    /**
     * Acquires an exclusive lock for byte code manipulation, e.g. for
     * parallel memory cleanup.
     * 
     * @since 1.00
     */
    //protected static final void acquireExclusiveLock() {
    //    LOCK.writeLock().lock();
    //}
    
    /**
     * Releases an exclusive lock for byte code manipulation acquired 
     * by {@link #acquireExclusiveLock()}.
     * 
     * @since 1.00
     */
    //protected static final void releaseExclusiveLock() {
    //    LOCK.writeLock().unlock();
   // }

    /**
     * Obtains a class instance. Must released explicitly
     * by {@link IClass#release()}.
     * 
     * @param name the name of the class given in Java notation
     * @param isRedefinition <code>true</code> in case that the class is being
     *   redefined / retransformed, <code>false</code> else
     * @return the class instance
     * @throws InstrumenterException in case that reading the class fails
     * 
     * @since 1.00
     */
    public abstract IClass obtainClass(String name, boolean isRedefinition) 
        throws InstrumenterException;
    
    /**
     * Returns the already loaded class object (for classes 
     * returned by {@link #obtainClass(String, boolean)}.
     * 
     * @param name the name of the class given Java notation
     * @return the class (may be <b>null</b> if not found)
     * 
     * @since 1.00
     */
    public abstract Class<?> getLoadedClass(String name);
    
    /**
     * Obtains a class instance. Must released explicitly
     * by {@link IClass#release()}.
     * 
     * @param loader the class loader
     * @param name the name of the class given in in the internal form of 
     *   fully qualified class and interface names as defined in The Java 
     *   Virtual Machine Specification, i.e. slashes instead of dots.
     * @param classBytes the loaded class as a byte array
     * @param isRedefinition <code>true</code> in case that the class is being
     *   redefined / retransformed, <code>false</code> else
     * @return the class instance
     * @throws InstrumenterException in case that reading the class fails
     * 
     * @since 1.00
     */
    public abstract IClass obtainClass(ClassLoader loader, String name, 
        byte[] classBytes, boolean isRedefinition) throws InstrumenterException;
    
    /**
     * Obtains a class instance from a stream. Must released explicity
     * by {@link IClass#release()} in case that <code>fromPool</code> is true.
     * 
     * @param in the input stream to obtain the class instance from
     * @param fromPool explicit memory management
     * @return the class instance
     * @throws IOException in case of reading errors
     * 
     * @since 1.00
     */
    public abstract IClass obtainClass(InputStream in, boolean fromPool) 
        throws IOException;
    
    /**
     * Returns the global code modifier.
     * 
     * @return the global code modifier
     * 
     * @since 1.00
     */
    public abstract ICodeModifier getCodeModifier();

    /**
     * Adds a class loader for resolving classes.
     * 
     * @param loader the class loader to be added
     * 
     * @since 1.00
     */
    public abstract void addClassLoader(ClassLoader loader);

    /**
     * Appends the given path to the class path.
     * 
     * @param pathname the path name to be appended
     * @throws InstrumenterException in case that the path cannot be found
     * 
     * @since 1.00
     */
    public abstract void addToClassPath(String pathname) 
        throws InstrumenterException;

    /**
     * Enables or disables class pruning (optimization).
     * 
     * @param pruning enable or disable
     * 
     * @since 1.00
     */
    public abstract void doPruning(boolean pruning);
    
    /**
     * Do memory cleanup if required.
     * 
     * @since 1.00
     */
    public abstract void cleanupIfRequired();

    /**
     * Returns if the given <code>type</code> is equal or a subclass of to the 
     * class object represented by <code>class</code>. We use Object instead of 
     * <code>java.lang.Class</code> here, because we do not know the concrete
     * type used by the instrumenter.
     * 
     * @param cls the class object
     * @param type the type to be checked for
     * @return <code>true</code> if <code>type</code> is of the type of 
     *     <code>cls</code> or a subclass of <code>cls</code>
     * 
     * @since 1.00
     */
    public boolean isInstanceOf(Object cls, String type) {
        boolean result = false;
        if (cls instanceof IClass) {
            result = ((IClass) cls).isInstanceOf(type);
        }
        return result;
    }

}
