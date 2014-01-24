package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import java.io.IOException;
import java.io.InputStream;

import javassist.CtClass;
import javassist.NotFoundException;

import de.uni_hildesheim.sse.codeEraser.util.ClassPool;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements the factory interface for javassist.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Factory extends IFactory {

    /**
     * Stores the singleton of this class.
     */
    private static Factory instance;
    
    /**
     * Stores the singleton instance of the code modifier.
     */
    private static ICodeModifier codeModifier;
    
    /**
     * Returns the (local) singleton instance.
     * 
     * @return the local singleton instance
     * 
     * @since 1.00
     */
    static Factory getLocalFactory() {
        return instance;
    }
    
    /**
     * Creates the factory.
     * 
     * @since 1.00
     */
    static {
        // clashes otherwise with parallel instrumentation
        ClassPool.setAutocleanup(false); 
        if (null == instance) {
            instance = new Factory();
            codeModifier = new CodeModifier();
            setInstance(instance);
        }
    }
    
    /**
     * Obtains a class instance.
     * 
     * @param loader the class loader
     * @param name the name of the class given in in the internal form of 
     *   fully qualified class and interface names as defined in The Java 
     *   Virtual Machine Specification, i.e. slashes instead of dots.
     * @param classBytes the loaded class as a byte array
     * @param isRedefinition <code>true</code> in case that the class is being
     *   redefined / retransformed, <code>false</code> else
     * @return the class interface
     * @throws InstrumenterException in case that reading the class fails
     * 
     * @since 1.00
     */
    @Override
    public IClass obtainClass(ClassLoader loader, String name, 
        byte[] classBytes, boolean isRedefinition) 
        throws InstrumenterException {
        if (null != loader) {
            ClassPool.addClassLoader(loader);
        }
        JAClass result;
        try {
            CtClass cl = ClassPool.makeClass(
                new java.io.ByteArrayInputStream(classBytes));
            result = JAClass.getClassFromPool(null);
            result.attach(cl, loader);
        } catch (Exception e) {
            throw new InstrumenterException(e);
        }
        return result;
    }
    
    /**
     * Obtains a class instance from a stream. Must released explicitly
     * by {@link IClass#release()} in case that <code>fromPool</code> is true.
     * 
     * @param in the input stream to obtain the class instance from
     * @param fromPool explicit memory management
     * @return the class instance
     * @throws IOException in case of reading errors
     * 
     * @since 1.00
     */
    @Override
    public IClass obtainClass(InputStream in, boolean fromPool) 
        throws IOException {
        JAClass result;
        if (fromPool) {
            result = JAClass.getClassFromPool(null);
        } else {
            result = new JAClass();
        }
        result.attach(ClassPool.makeClass(in));
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public IClass obtainClass(String name, boolean isRedefinition) {
        CtClass cls = null;
        try {
            cls = ClassPool.get(name);
            if (isRedefinition && cls.isFrozen()) {
                cls.stopPruning(true);
                cls.defrost();
            }
        } catch (NotFoundException e) {
            cls = null;
        } catch (RuntimeException e) {
            cls = null;
        }
        JAClass result = null;
        if (null != cls)  {
            result = JAClass.getClassFromPool(null);
            result.attach(cls);
        }
        return result;
    }

    /**
     * Returns the already loaded class object (for classes 
     * returned by {@link #obtainClass(String, boolean)}.
     * 
     * @param name the name of the class given Java notation
     * @return the class (may be <b>null</b> if not found)
     * 
     * @since 1.00
     */
    public Class<?> getLoadedClass(String name) {
        return ClassPool.getLoadedClass(name);
    }
    
    /**
     * Returns the global code modifier.
     * 
     * @return the global code modifier
     * 
     * @since 1.00
     */
    public ICodeModifier getCodeModifier() {
        return codeModifier;
    }

    /**
     * Adds a class loader for resolving classes.
     * 
     * @param loader the class loader to be added
     * 
     * @since 1.00
     */
    public void addClassLoader(ClassLoader loader) {
        ClassPool.addClassLoader(loader);
    }

    /**
     * Appends the given path to the class path.
     * 
     * @param pathname the path name to be appended
     * @throws InstrumenterException in case that the path cannot be found
     * 
     * @since 1.00
     */
    public void addToClassPath(String pathname) throws InstrumenterException {
        try {
            ClassPool.addToClassPath(pathname);
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        }
    }

    /**
     * Enables or disables class pruning (optimization).
     * 
     * @param pruning enable or disable
     * 
     * @since 1.00
     */
    public void doPruning(boolean pruning) {
        javassist.ClassPool.doPruning = pruning;
    }

    /**
     * Clears temporary memory if required.
     * 
     * @since 1.00
     */
    public void cleanup() {
        synchronized (LOCK) { 
            //acquireExclusiveLock();
            ClassPool.clean();
            //releaseExclusiveLock();
        }
    }
    
    /**
     * Do memory cleanup if required.
     * 
     * @since 1.00
     */
    public void cleanupIfRequired() {
        synchronized (LOCK) {
            //acquireExclusiveLock();
            ClassPool.cleanup();
            //releaseExclusiveLock();
        }
    }

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
        if (cls instanceof javassist.CtClass) {
            result = JAClass.isInstanceOf(
                (javassist.CtClass) cls, type);
        } else {
            result = super.isInstanceOf(cls, type);
        }
        return result;
    }

}
