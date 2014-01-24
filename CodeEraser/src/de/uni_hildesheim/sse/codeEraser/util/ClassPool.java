package de.uni_hildesheim.sse.codeEraser.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Realizes a class pool frontend for a cleanable class pool.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ClassPool {

    /**
     * Stores the javassist class pool.
     */
    private static javassist.ClassPool classPool;
        
    /**
     * Stores the class loaders added at runtime and used while cleanup.
     */
    private static Map<ClassLoader, ClassPath> loaders 
        = new HashMap<ClassLoader, ClassPath>();

    /**
     * Stores class loading oaths added at runtime and used while cleanup.
     */
    private static Map<String, ClassPath> paths 
        = new HashMap<String, ClassPath>();

    /**
     * Stores whether auto cleanup should be done ({@value}).
     */
    private static boolean doAutoCleanup = true;
    
    /**
     * Stores the cleanup count, i.e. a counter how often
     * {@link #cleanup()} was called.
     */
    private static int cleanupCount = 0;

    /**
     * Stores the internal threshold for the cleanup count.
     */
    private static int cleanupCountThreshold = 200;
    
    /**
     * Stores the internal threshold for the class pool size.
     */
    private static int classPoolSizeThreshold = 100;
    
    /**
     *  Stores a reference to the classes hashtable in the class pool.
     */
    @SuppressWarnings("rawtypes")
    private static Hashtable classes;
    
    /**
     * The field containing the classes hashtable.
     */
    private static Field classPoolClasses = null;

    /**
     * Stores the constructor instance of ctClassType.
     */
    private static Constructor<?> ctClassConstructor;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ClassPool() {
    }
        
    /**
     * Creates a configured class pool instance. Takes into account 
     * {@link OnCreationJarProvider#getInstance()}.
     * 
     * @return a configured ready-for-use class pool instance
     * 
     * @since 1.00
     */
    private static final javassist.ClassPool createClassPoolInstance() {
        javassist.ClassPool classPool = new javassist.ClassPool(null); 
        classPool.childFirstLookup = true;
        classPool.appendSystemPath();
        OnCreationJarProvider additionalJars = 
            OnCreationJarProvider.getInstance();
        if (null != additionalJars) {
            for (String jar : additionalJars.getJars()) {
                try {
                    classPool.appendClassPath(jar);
                } catch (NotFoundException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        return classPool;
    }

    /**
     * Obtain access to internal classes attribute of class pool on class load.
     */
    static {
        // TODO check if needed anymore (regular cleanup)
        Class<?> jaClassPool = javassist.ClassPool.class;
        classPool = createClassPoolInstance(); 
        try {
            classPoolClasses = jaClassPool.getDeclaredField("classes");
            classPoolClasses.setAccessible(true);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
            System.err.println("Javaassist Classpool attribute " 
                + "\"classes\" not found");
        }
    }
    
    /**
     * Creates a (new) class pool.
     * 
     * @since 1.00
     */
    @SuppressWarnings("rawtypes")
    private static final synchronized void createClassPool() {
        classPool = createClassPoolInstance();
        if (null != classPoolClasses) {
            try {
                classes = (Hashtable) classPoolClasses.get(classPool);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }
    
    /**
     * Initializes the classpool on class loading.
     */
    static {
        // required to enforce classloading (otherwise cleanup may overleave
        // with instrumentation activities in parallel settings)
        clean(); //calls createClassPool();
    }

    /**
     * Creates a new class (or interface) from the given class file.
     * If there already exists a class with the same name, the new class
     * overwrites that previous class.
     *
     * @param in class file.
     * @return the create class proxy
     * @throws RuntimeException if there is a frozen class with the
     *                          the same name.
     * @throws IOException in case that any I/O problem occurred
     */
    public static synchronized CtClass makeClass(InputStream in) 
        throws IOException {
        cleanupCount++;
        if (doAutoCleanup) {
            cleanup();
        }
        return classPool.makeClass(in);
    }
    
    /**
     * Creates a new class for the given name.
     * 
     * @param name the name of the class
     * @return the created class
     * 
     * @since 1.00
     */
    public static synchronized CtClass makeClass(String name) {
        return classPool.makeClass(name);
    }

    /**
     * Creates a new class for the given name.
     * 
     * @param name the name of the class
     * @param superclass the superclass of the class to be created
     * @return the created class
     * 
     * @since 1.00
     */
    public static synchronized CtClass makeClass(String name, 
        CtClass superclass) {
        return classPool.makeClass(name, superclass);
    }
    
    /**
     * Reads a class file from the source and returns a reference
     * to the <code>CtClass</code>
     * object representing that class file.  If that class file has been
     * already read, this method returns a reference to the
     * <code>CtClass</code> created when that class file was read at the
     * first time.
     *
     * <p>If <code>name</code> ends with "[]", then this method
     * returns a <code>CtClass</code> object for that array type.
     *
     * <p>To obtain an inner class, use "$" instead of "." for separating
     * the enclosing class name and the inner class name.
     *
     * @param name         a fully-qualified class name.
     * @return the class proxy
     * @throws NotFoundException in case that any Java element was not found
     */
    public static synchronized CtClass get(String name) 
        throws NotFoundException {
        cleanupCount++;
        if (doAutoCleanup) {
            cleanup();
        }
        return classPool.get(name);
    }
    
    /**
     * Adds a class loader to the internal class pool.
     * 
     * @param loader the class loader to be added
     * 
     * @since 1.00
     */
    public static final synchronized void addClassLoader(ClassLoader loader) {
        if (!loaders.containsKey(loader)) {
            ClassPath cp = new LoaderClassPath(loader);
            loaders.put(loader, cp);
            classPool.appendClassPath(cp);
        }
    }
    
    /**
     * Returns a loaded class via the registered class loaders.
     * 
     * @param name the name of the class
     * @return the loaded class
     * 
     * @since 1.00
     */
    public static final synchronized Class<?> getLoadedClass(String name) {
        Class<?> result = null;
        Iterator<ClassLoader> iter = loaders.keySet().iterator();
        while (null == result && iter.hasNext()) {
            try {
                result = iter.next().loadClass(name);
            } catch (ClassNotFoundException e) {
            }
        }
        return result;
    }
    
    /**
     * Appends the given path to the class path.
     * 
     * @param pathname the path name to be appended
     * @throws NotFoundException in case that the path cannot be found
     * 
     * @since 1.00
     */
    public static final synchronized void addToClassPath(String pathname) 
        throws NotFoundException {
        if (!paths.containsKey(pathname)) {
            ClassPath cp = classPool.appendClassPath(pathname);
            paths.put(pathname, cp);
        }
    }
    
    /**
     * Removes the given class loader from the internal class pool.
     * 
     * @param loader the loader to be removed
     * 
     * @since 1.00
     */
    public static final synchronized void removeClassLoader(
        ClassLoader loader) {
        if (loaders.containsKey(loader)) {
            ClassPath cp = loaders.remove(loader);
            classPool.removeClassPath(cp);
        }
    }

    /**
     * Removes the given path from the internal class pool.
     * 
     * @param pathname the path to be removed
     * 
     * @since 1.00
     */
    public static final synchronized void removeClassPath(String pathname) {
        ClassPath path = paths.remove(pathname);
        if (null != path) {
            classPool.removeClassPath(path);
        }
    }
    
    /**
     * Cleans the internal pool.
     * 
     * @since 1.00
     */
    public static synchronized void clean() {
        createClassPool();
        for (ClassLoader loader : loaders.keySet()) {
            classPool.appendClassPath(loaders.get(loader));
        }
        for (ClassPath path : paths.values()) {
            classPool.appendClassPath(path);
        }
    }

    /**
     * Cleans the internal pool if internal thresholds are not valid.
     * 
     * @since 1.00
     */
    public static synchronized void cleanup() {
        if ((null != classes && classes.size() > classPoolSizeThreshold) 
            || cleanupCount > cleanupCountThreshold) {
            cleanupCount = 0;
            clean();
        }
    }
    
    /**
     * Enables or disables auto cleanup behavior. 
     * 
     * @param clean enable or disable auto cleanup
     * 
     * @since 1.00
     */
    public static void setAutocleanup(boolean clean) {
        doAutoCleanup = clean;
    }
    
    /**
     * Returns the current size of the class pool.
     * 
     * @return the current size of the class pool
     * 
     * @since 1.00
     */
    public static int getSize() {
        return null != classes ? classes.size() : 0;
    }
    
    /**
     * Create a local class instance not cached in the class pool.
     * 
     * @param className the name of the class
     * @return a local copy of the class
     * @throws NotFoundException in case that <code>className</code> cannot be
     *   resolved or cannot be made local
     * 
     * @since 1.00
     */
    public static CtClass makeLocal(String className) 
        throws NotFoundException {
        CtClass result = null;
        if (null == ctClassConstructor) {
            try {
                Class<?> cls = Class.forName("javassist.CtClassType");
                ctClassConstructor = cls.getDeclaredConstructor(
                    InputStream.class, javassist.ClassPool.class);
                ctClassConstructor.setAccessible(true);
            } catch (ClassNotFoundException e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                throw new NotFoundException(e.getMessage(), e);
            }
        }
        if (null != ctClassConstructor) {
            try {
                CtClass cls = ClassPool.get(className);
                InputStream in = new ByteArrayInputStream(cls.toBytecode());
                result = 
                    (CtClass) ctClassConstructor.newInstance(in, classPool);
            } catch (CannotCompileException e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (IOException e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (InstantiationException e) {
                throw new NotFoundException(e.getMessage(), e);
            }
        }
        return result;
    }

}
