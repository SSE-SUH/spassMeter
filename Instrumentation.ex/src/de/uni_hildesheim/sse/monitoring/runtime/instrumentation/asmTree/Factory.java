package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import de.uni_hildesheim.sse.codeEraser.util.OnCreationJarProvider;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Implements the factory interface for the tree API of asm.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public class Factory extends IFactory {

    /**
     * Stores the internal name of "java.lang.Object".
     */
    static final String JAVA_LANG_OBJECT = "java/lang/Object";

    /**
     * Stores the internal name of "java.lang.System".
     */
    static final String JAVA_LANG_SYSTEM = "java/lang/System";

    /**
     * Stores the internal name of "java.lang.Thread".
     */
    static final String JAVA_LANG_THREAD = "java/lang/Thread";

    /**
     * Defines the identifier for the internal type <code>void</code>.
     */
    static final char INTERNAL_VOID = 'V';

    /**
     * Defines the identifier for the internal type <code>int</code>.
     */
    static final char INTERNAL_INT = 'I';
    
    /**
     * Defines the identifier for the internal type <code>boolean</code>.
     */
    static final char INTERNAL_BOOLEAN = 'Z';

    /**
     * Defines the identifier for the internal type <code>char</code>.
     */
    static final char INTERNAL_CHAR = 'C';
    
    /**
     * Defines the identifier for the internal type <code>byte</code>.
     */
    static final char INTERNAL_BYTE = 'B';
    
    /**
     * Defines the identifier for the internal type <code>short</code>.
     */
    static final char INTERNAL_SHORT = 'S';
    
    /**
     * Defines the identifier for the internal type <code>float</code>.
     */
    static final char INTERNAL_FLOAT = 'F';
    
    /**
     * Defines the identifier for the internal type <code>long</code>.
     */
    static final char INTERNAL_LONG = 'J';
    
    /**
     * Defines the identifier for the internal type <code>double</code>.
     */
    static final char INTERNAL_DOUBLE = 'D';

    /**
     * Defines the identifier for the internal type <code>int</code>.
     */
    static final String INTERNAL_INT_STRING = String.valueOf(INTERNAL_INT);
    
    /**
     * Defines the identifier for the internal type <code>boolean</code>.
     */
    static final String INTERNAL_BOOLEAN_STRING 
        = String.valueOf(INTERNAL_BOOLEAN);

    /**
     * Defines the identifier for the internal type <code>char</code>.
     */
    static final String INTERNAL_CHAR_STRING = String.valueOf(INTERNAL_CHAR);
    
    /**
     * Defines the identifier for the internal type <code>byte</code>.
     */
    static final String INTERNAL_BYTE_STRING = String.valueOf(INTERNAL_BYTE);
    
    /**
     * Defines the identifier for the internal type <code>short</code>.
     */
    static final String INTERNAL_SHORT_STRING = String.valueOf(INTERNAL_SHORT);
    
    /**
     * Defines the identifier for the internal type <code>float</code>.
     */
    static final String INTERNAL_FLOAT_STRING = String.valueOf(INTERNAL_FLOAT);
    
    /**
     * Defines the identifier for the internal type <code>long</code>.
     */
    static final String INTERNAL_LONG_STRING = String.valueOf(INTERNAL_LONG);
    
    /**
     * Defines the identifier for the internal type <code>double</code>.
     */
    static final String INTERNAL_DOUBLE_STRING 
        = String.valueOf(INTERNAL_DOUBLE);
    
    /**
     * Defines the prefix for the internal array type names.
     */
    static final char INTERNAL_ARRAY_PREFIX = '[';
    
    /**
     * Defines the prefix for the internal object type names.
     */
    static final char INTERNAL_OBJECT_PREFIX = 'L';

    /**
     * Defines the postfix for the internal object type names.
     */
    static final char INTERNAL_OBJECT_SUFFIX = ';';
    
    /**
     * Stores the internal threshold for the class pool size.
     */
    private static int classPoolSizeThreshold = 100;
    
    /**
     * Stores the singleton of this class.
     */
    private static Factory instance;
    
    /**
     * Stores the class loader.
     */
    private ArrayList<ClassLoader> loader = new ArrayList<ClassLoader>();

    /**
     * Stores the local cache of class representants. Maps from JVM 
     * internal names to representant instances.
     */
    private HashMap<String, AType> cache = new HashMap<String, AType>();

    /**
     * Stores the representant for "java.lang.Object".
     */
    private AType object;
    
    /**
     * Stores the asm node for {@link #object}.
     */
    private ClassNode objectNode;

    /**
     * Stores the code modifier.
     */
    private CodeModifier codeModifier;
    
    /**
     * Returns the (local) singleton instance.
     * 
     * @return the local singleton instance
     * 
     * @since 1.00
     */
    public static Factory getLocalFactory() {
        return instance;
    }
    
    /**
     * Creates the factory.
     * 
     * @since 1.00
     */
    static {
        if (null == instance) {
            instance = new Factory();
            instance.codeModifier = new CodeModifier();
            instance.populate();
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
        AClass result;
        try {
            ClassNode node = new ClassNode();
            ClassReader reader = new ClassReader(classBytes);
            reader.accept(node, 0);
            result = AClass.getClassFromPool(node, null, null, null);
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
        ClassReader reader = new ClassReader(in);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return AClass.getClassFromPool(node, null, null, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public IClass obtainClass(String name, boolean isRedefinition) 
        throws InstrumenterException {
        name = name.replace('.', '/') + ".class";
        AClass result = null;
        InputStream stream = null;
        for (int l = 0; null == stream && l < loader.size(); l++) {
            stream = loader.get(l).getResourceAsStream(name);
        }
        if (null != stream) {
            try {
                ClassReader reader = new ClassReader(stream);
                ClassNode node = new ClassNode();
                reader.accept(node, 0);
                result = AClass.getClassFromPool(node, null, null, null);
            } catch (Exception e) {
                throw new InstrumenterException(e);
            }
        } else {
            throw new InstrumenterException("class not found: " + name);
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
        Class<?> result = null;
        for (int l = 0; null == result && l < loader.size(); l++) {
            try {
                result = loader.get(l).loadClass(name);
            } catch (ClassNotFoundException e) {
                // handled below
            }
        }
        return result;
    }
    
    /**
     * Loads a class.
     * 
     * @param internalName the internal name of the class
     * @param loader the class loader to use
     * @return the loaded class in terms of a class abstraction
     * @throws InstrumenterException in case of class resolution errors
     * 
     * @since 1.00
     */
    public IClass obtainClass(String internalName, ClassLoader loader) 
        throws InstrumenterException {
        return obtainType(internalName, loader);
    }

    /**
     * Loads a class.
     * 
     * @param internalName the internal name of the class
     * @param loader the primary class loader to use (tried first, may be 
     *     <b>null</b> then the loaders stored in this factory and finally
     *     the loader of this factory are used)
     * @return the loaded class in terms of a class abstraction
     * @throws InstrumenterException in case of class resolution errors
     * 
     * @since 1.00
     */
    public AType obtainType(String internalName, ClassLoader loader) 
        throws InstrumenterException {
        AType result = cache.get(internalName);
        int len = internalName.length();
        if (null == result && len > 0) {
            String nonArrayName;
            AType nonArrayType;
            int arrayDimension = 0;
            while (arrayDimension < len && internalName
                .charAt(arrayDimension) == INTERNAL_ARRAY_PREFIX) {
                arrayDimension++;
            }
            if (arrayDimension > 0) {
                nonArrayName = internalName.substring(arrayDimension);
                nonArrayType = cache.get(nonArrayName);
            } else {
                nonArrayName = internalName;
                nonArrayType = null;
            }
            if (null == nonArrayType) {
                InputStream in = null;
                String fileName = nonArrayName + ".class";
                // keep in line with forName
                if (null != loader) {
                    in = loader.getResourceAsStream(fileName);
                }
                int size = this.loader.size();
                for (int l = 0; null == in && l < size; l++) {
                    in = this.loader.get(l).getResourceAsStream(fileName);
                }
                if (null == in) {
                    in = getClass().getClassLoader().getResourceAsStream(
                        fileName);
                }
                if (null != in) {
                    try {
                        ClassNode node = new ClassNode();
                        ClassReader reader = new ClassReader(in);
                        reader.accept(node, 0);
                        nonArrayType = AClass.getClassFromPool(
                            node, loader, null, null);
                        cache(nonArrayType);
                    } catch (IOException e) {
                        throw new InstrumenterException(e);
                    }
                }
            }
            if (arrayDimension > 0) {
                result = AClass.getClassFromPool(
                    objectNode, loader, nonArrayType, internalName);
                cache(result);
            } else {
                result = nonArrayType;
            }
        }
        if (null == result) {
            throw new InstrumenterException("Not found " 
                + toCodeName(internalName, true));
        }
        return result;
    }

    /**
     * Loads a class. Note that classes returned by this method may not be 
     * initialized.
     * 
     * @param className the code name of the class
     * @param loader the primary class loader to use (tried first, may be 
     *     <b>null</b> then the loaders stored in this factory and finally
     *     the loader of this factory are used)
     * @return the loaded class (may be <b>null</b> if not found)
     * 
     * @since 1.00
     */
    private Class<?> forName(String className, ClassLoader loader) {
        Class<?> result = null;
        
        // keep in line with obtainType
        if (null != loader) {
            try {
                result = Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
            }
        }
        int size = this.loader.size();
        for (int l = 0; null == result && l < size; l++) {
            try {
                result = Class.forName(className, false, this.loader.get(l));
            } catch (ClassNotFoundException e) {
            }
        }
        if (null == result) {
            try {
                result = Class.forName(className, false, 
                    getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
            }
        }
        return result;
    }
    
    /**
     * Returns the common super type of the two given types.
     *
     * @param type1 the internal name of a class.
     * @param type2 the internal name of another class.
     * @return the internal name of the common super class of the two given
     *         classes.
     */
    String getCommonSuperclass(String type1, String type2) {
        int cause = 0;
        type1 = type1.replace('.', '/'); // bug in ASM
        type2 = type2.replace('.', '/'); // bug in ASM
        String t1 = toCodeName(type1, true); 
        String t2 = toCodeName(type2, true); 
        String result = null;
        try {
            AType c1 = obtainType(type1, null);
            if (null != c1) {
                AType c2 = obtainType(type2, null);
                if (null != c2) {
                    if (c1.isInstanceOf(c2)) {
                        // c1 is same or subtype of c2
                        // c2.isAssignableFrom(c1) -> c1 is same/subtype of c2
                        //  --> type2
                        result = type2;
                    } else if (c2.isInstanceOf(c1)) {
                        // c2 is same or subtype of c1
                        // c1.isAssignableFrom(c2) -> c2 is same/subtype of c1
                        //  --> type1
                        result = type1;
                    } else if (c1.isInterface() || c2.isInterface()) {
                        return "java/lang/Object";
                    } else {
                        do {
                            c1 = (AType) c1.getSuperclass();
                            // while c1.isAssignableFrom(c2)
                        } while (null != c1 && !c2.isInstanceOf(c1));
                        if (null == c1) {
                            result = "java/lang/Object";
                        } else {
                            result = c1.getName();    
                        }
                    }
                } else {
                    cause = 2;
                }
            } else {
                cause = 1;
            }
        } catch (InstrumenterException e) { 
            // hope on fallback
        }
        if (null == result) {
            cause = 0;
            // fallback, adapted original implementation from ASM
            Class<?> c = forName(t1, null);
            if (null != c) {
                Class<?> d = forName(t1, null);
                if (null != d) {
                    if (c.isAssignableFrom(d)) {
                        result = type1;
                    }
                    if (d.isAssignableFrom(c)) {
                        result = type2;
                    }
                    if (c.isInterface() || d.isInterface()) {
                        result = "java/lang/Object";
                    } else {
                        do {
                            c = c.getSuperclass();
                        } while (null != c && !c.isAssignableFrom(d));
                        if (null == c) {
                            result = "java/lang/Object";
                        } else {
                            result = c.getName().replace('.', '/');
                        }
                    }
                } else {
                    cause = 2;
                }
            } else {
                cause = 1;
            }
        }
        switch (cause) {
        case 1:
            throw new RuntimeException("class " + t1 + " not found");
        case 2:
            throw new RuntimeException("class " + t2 + " not found");
        default:
            break;
        }
        return result;
    }

    /**
     * Converts a fqn code name to an internal name.
     * 
     * @param name the name of the class
     * @return the code name
     * 
     * @since 1.00
     */
    public static String toInternalName(String name) {
        int pos = name.length() - 1;
        StringBuilder result = new StringBuilder();
        while (pos - 1 > 0) {
            if ('[' == name.charAt(pos - 1) && ']' == name.charAt(pos)) {
                result.append("[");
                pos -= 2;
            } else {
                break;
            }
        }
        result.append(name.substring(0, pos + 1).replace('.', '/'));
        return result.toString();
    }

    /**
     * Converts an internal name to a code name.
     * 
     * @param name the name of the class
     * @param acceptClassName accept an individual class name in internal JVM
     *   notation
     * @return the code name
     * 
     * @since 1.00
     */
    public static String toCodeName(String name, boolean acceptClassName) {
        return toCodeName(name, 0, name.length() - 1, acceptClassName);
    }
    
    /**
     * Converts an internal name to a code name (as part of a signature).
     * 
     * @param desc the descriptor (or type name)
     * @param start the start position
     * @param end the inclusive end position (from 
     *   @{link #scanNextInternalName(String,int)})
     * @param acceptClassName accept an individual class name in internal JVM
     *   notation
     * @return the code name
     * 
     * @since 1.00
     */
    static String toCodeName(String desc, int start, int end, 
        boolean acceptClassName) {
        String result = null;
        int i = start;
        while (i <= end && INTERNAL_ARRAY_PREFIX == desc.charAt(i)) {
            i++;
        }
        int dimensionCount = i - start;
        if (i <= end) {
            switch (desc.charAt(i)) {
            case INTERNAL_VOID: // for behavior signatures
                result = "void";
                break;
            case INTERNAL_INT:
                result = "int";
                break;
            case INTERNAL_BOOLEAN:
                result = "boolean";
                break;
            case INTERNAL_CHAR:
                result = "char";
                break;
            case INTERNAL_BYTE:
                result = "byte";
                break;
            case INTERNAL_SHORT:
                result = "short";
                break;
            case INTERNAL_FLOAT:
                result = "float";
                break;
            case INTERNAL_LONG:
                result = "long";
                break;
            case INTERNAL_DOUBLE:
                result = "double";
                break;
            case INTERNAL_OBJECT_PREFIX:
                if (INTERNAL_OBJECT_SUFFIX == desc.charAt(end)) {
                    result = desc.substring(i + 1, end).replace('/', '.');
                }
                break;
            default:
                break;
            }
            if (null == result && acceptClassName) {
                result = desc.replace('/', '.');
            }
        }
        if (dimensionCount > 0) {
            StringBuilder tmp = new StringBuilder(result);
            while (dimensionCount > 0) {
                tmp.append("[]");
                dimensionCount--;
            }
            result = tmp.toString();
        }
        return result;
    }

    /**
     * Returns the number of parameter in the given method descriptor.
     * 
     * @param desc the method descriptor
     * @return the number of parameter
     * 
     * @since 1.00
     */
    static int countParameter(String desc) {
        int start = 0;
        int count = 0;
        int len = desc.length();
        while (start < len && '(' == desc.charAt(start)) {
            // should be exactly one
            start++;
        }
        while (')' != desc.charAt(start) && start < len) {
            int end = Factory.scanNextDescriptorName(desc, start);
            count++;
            start = end + 1;
        }
        return count;
    }

    /**
     * Scans for the end of the next type descriptor starting at 
     * <code>pos</code>.
     * 
     * @param desc the descriptor to parse
     * @param pos the start position
     * @return <code>&gt;pos</code> if a valid internal type name was found, 
     *   <code>pos</code> if none was found
     * 
     * @since 1.00
     */
    static int scanNextDescriptorName(String desc, int pos) {
        int result = pos;
        while (result < desc.length() 
            && INTERNAL_ARRAY_PREFIX == desc.charAt(result)) {
            result++;
        }
        if (result < desc.length()) {
            switch (desc.charAt(result)) {
            case INTERNAL_VOID: // void (for behavior signatures)
            case INTERNAL_INT:
            case INTERNAL_BOOLEAN:
            case INTERNAL_CHAR:
            case INTERNAL_BYTE:
            case INTERNAL_SHORT:
            case INTERNAL_FLOAT:
            case INTERNAL_LONG:
            case INTERNAL_DOUBLE:
                break;
            case INTERNAL_OBJECT_PREFIX:
                while (result < desc.length() 
                    && INTERNAL_OBJECT_SUFFIX != desc.charAt(result)) {
                    result++;
                }
                break;
            default:
                break;
            }
        } 
        return result;
    }

    /**
     * Clears temporary memory if required. This is the internal unsynchronized
     * version.
     * 
     * @since 1.00
     */
    private void doCleanup() {
        // TODO check performance vs. cleanup attribute in AClass
        Iterator<HashMap.Entry<String, AType>> iter 
            = cache.entries().iterator();
        while (iter.hasNext()) {
            HashMap.Entry<String, AType> entry = iter.next();
            if (!entry.getValue().isPrimitive() && entry.getValue() != object) {
                iter.remove();
                entry.getValue().release();
            }
        }
    }
    
    /**
     * Clears temporary memory if required. This method is synchronized by
     * {@link #acquireExclusiveLock()} and {@link #releaseReentrantLock()}.
     * 
     * @since 1.00
     */
    @Override
    public void cleanup() {
        synchronized (LOCK) {
        //acquireExclusiveLock();
            doCleanup();
        //releaseExclusiveLock();
        }
    }

    /**
     * Do memory cleanup if required. This method is synchronized by
     * {@link #acquireExclusiveLock()} and {@link #releaseReentrantLock()}.
     * 
     * @since 1.00
     */
    @Override
    public void cleanupIfRequired() {
        synchronized (LOCK) {
        //acquireExclusiveLock();
            if ((cache.size() > classPoolSizeThreshold)) {
                doCleanup();
            }
        //releaseExclusiveLock();
        }
    }

    /**
     * Returns the global code modifier.
     * 
     * @return the global code modifier
     * 
     * @since 1.00
     */
    @Override
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
    @Override
    public void addClassLoader(ClassLoader loader) {
        this.loader.add(loader);
    }

    /**
     * Appends the given path to the class path.
     * 
     * @param pathname the path name to be appended
     * @throws InstrumenterException in case that the path cannot be found
     * 
     * @since 1.00
     */
    @Override
    public void addToClassPath(String pathname) throws InstrumenterException {
        try {
            File file = new File(pathname);
            URL[] url = new URL[1];
            url[0] = file.toURI().toURL();
            this.loader.add(new URLClassLoader(url));
        } catch (MalformedURLException e) {
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Removes the given class loader.
     * 
     * @param loader the loader to remove
     * 
     * @since 1.00
     */
    public void removeClassLoader(ClassLoader loader) {
        this.loader.remove(loader);
    }
    
    /**
     * Initial population of the cache.
     * 
     * @since 1.00
     */
    private void populate() {
        if (null == object) {
            try {
                object = obtainType(JAVA_LANG_OBJECT, null);
                objectNode = object.getNode();
            } catch (InstrumenterException e) {
                // should not happen
            }
            cache(new APrimitive(INTERNAL_BOOLEAN));
            cache(new APrimitive(INTERNAL_CHAR));
            cache(new APrimitive(INTERNAL_BYTE));
            cache(new APrimitive(INTERNAL_SHORT));
            cache(new APrimitive(INTERNAL_INT));
            cache(new APrimitive(INTERNAL_FLOAT));
            cache(new APrimitive(INTERNAL_LONG));
            cache(new APrimitive(INTERNAL_DOUBLE));

            addClassLoader(ClassLoader.getSystemClassLoader());
            OnCreationJarProvider additionalJars = 
                OnCreationJarProvider.getInstance();
            if (null != additionalJars) {
                try {
                    String[] add = additionalJars.getJars();
                    URL[] url = new URL[add.length];
                    for (int u = 0; u < add.length; u++) {
                        File file = new File(add[u]);
                        url[u] = file.toURI().toURL();
                    }
                    this.loader.add(new URLClassLoader(url));
                } catch (MalformedURLException e) {
                    System.err.println("while adding additional jars: " + e);
                }
            }
        }
    }

    /**
     * Caches a type.
     * 
     * @param type the type to be cached
     * 
     * @since 1.00
     */
    private void cache(APrimitive type) {
        cache.put(type.getName(), type);
        cache.put(type.getInternalName(), type);
    }
    
    /**
     * Caches a type.
     * 
     * @param type the type to be cached
     * 
     * @since 1.00
     */
    private void cache(AType type) {
        cache.put(type.getInternalName(), type);
    }

    /**
     * Enables or disables class pruning (optimization).
     * This optimization is not required in ASM.
     * 
     * @param pruning enable or disable
     * 
     * @since 1.00
     */
    @Override
    public void doPruning(boolean pruning) {
        // not needed here
    }
    
    /**
     * Returns whether the given <code>type</code> is cached.
     * 
     * @param type the type to check for
     * @return <code>true</code> if cached, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isCached(AType type) {
        return cache.containsKey(type.getInternalName());
    }

}
