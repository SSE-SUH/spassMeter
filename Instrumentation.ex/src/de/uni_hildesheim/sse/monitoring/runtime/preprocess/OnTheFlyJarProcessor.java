package de.uni_hildesheim.sse.monitoring.runtime.preprocess;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Realizes an on-the-fly JAR processor, i.e. a processor for transforming 
 * class files. The processor may operate in two different modes of operation.
 * <ol>
 *   <li>Load all relevant classes into memory and process them afterwards.
 *       Call first {@link #loadClasses(String)}, process the classes in 
 *       {@link #loadedClasses()} and call 
 *       {@link #writeClasses(String, String)}.
 *       You may want to select the classes to be loaded by overriding
 *       {@link #loadClass(String)}.</li>
 *   <li>Open the input JAR file and transform the classes before writing
 *       them back by implementing {@link ClassProcessor} and passing
 *       it to {@link #writeClasses(String, String, ClassProcessor)}.</li>
 *   <li>You may combine both alternatives, by selectively loading classes
 *       and process others afterwards.</li>
 * </ol>
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class OnTheFlyJarProcessor {

    /**
     * Defines the file suffix for bytecode files.
     */
    private static final String CLASS_SUFFIX = ".class";

    /**
     * Stores weather the processor should run in lazy mode, i.e. if it should
     * ignore class not found exceptions during instrumentation.
     */
    private boolean lazy = false;
    
    /**
     * Stores all loaded classes with related JAR entries (for writing back).
     * This attribute is initialized only if {@link #loadClasses(String)} 
     * was called before.
     */
    private HashMap<IClass, String> classes;
    
    /**
     * Stores the optional bin path.
     */
    private String binPath;

    /**
     * Creates a new on-the-fly processor.
     */
    public OnTheFlyJarProcessor() {
        this(null);
    }
    
    /**
     * Creates a new on-the-fly processor.
     * 
     * @param binPath an optional binary prefix path prepended to all classes
     */
    public OnTheFlyJarProcessor(String binPath) {
        this.binPath = binPath;
    }

    /**
     * Changes weather the processor should run in lazy mode, i.e. if it should
     * ignore class not found exceptions during instrumentation.
     * 
     * @param lazy ignore errors or not
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }
    
    /**
     * Returns the set of all classes loaded by {@link #loadClasses(String)}.
     * 
     * @return all loaded classes
     * 
     * @since 1.00
     */
    protected Iterable<IClass> loadedClasses() {
        return classes.keys();
    }
    
    /**
     * Replaces the given class <code>cls</code> in the internal class-filename
     * mapping.
     * 
     * @param cls the class to be replaced
     * 
     * @since 1.00
     */
    protected void replaceClass(IClass cls) {
        String fileName = cls.getName().replace('.', '/') + ".class";
        classes.put(cls, fileName);
    }
    
    /**
     * Removes a specified class from the set of loaded classes in order to
     * avoid writing it back to the out JAR.
     * 
     * @param cl the class to be removed
     * 
     * @since 1.00
     */
    protected void removeClass(IClass cl) {
        if (null != classes) {
            classes.remove(cl);
        }
    }
    
    /**
     * Adds a new class to be considered when writing it to the out JAR.
     * 
     * @param cl the class
     * @param fileName the file name to be used for the JAR entry
     * @return <code>true</code> if the new class was added, <code>false</code>
     *   if it was registered before (use {@link #removeClass(CtClass)} before
     *   calling this method)
     * 
     * @since 1.00
     */
    protected boolean addClass(IClass cl, String fileName) {
        boolean done = false;
        if (null != classes && !classes.containsKey(cl)) {
            classes.put(cl, fileName);
            done = true;
        }
        return done;
    }

    /**
     * Returns if the specified class was loaded (or added manually by 
     * {@link #addClass(CtClass, String)}).
     * 
     * @param cl the class to be queried for
     * @return <code>true</code> if the class was loaded, <code>false</code> 
     *     else
     * 
     * @since 1.00
     */
    public boolean wasLoaded(IClass cl) {
        return null != classes && classes.containsKey(cl);
    }
    
    /**
     * Loads all classes (with respect to {@link #loadClass(String)}) in the 
     * given <code>in</code> JAR file.
     * 
     * @param in the input JAR file
     * @throws IOException in case of any I/O problem or error
     * 
     * @since 1.00
     */
    protected void loadClasses(String in) throws IOException {
        classes = new HashMap<IClass, String>();
        JarFile jf = new JarFile(in);
        Enumeration<JarEntry> iter = jf.entries();
        IFactory factory = IFactory.getInstance();
        while (iter.hasMoreElements()) {
            JarEntry entry = iter.nextElement();
            if (entry.getName().endsWith(CLASS_SUFFIX)) {
                if (loadClass(jarEntryToClassName(entry))) {
                    classes.put(factory.obtainClass(
                        jf.getInputStream(entry), false), entry.getName());
                }
            }
        }
        jf.close();
    }

    /**
     * Is called to find out if the specified class should be loaded in 
     * {@link #loadClasses(String)}.
     * 
     * @param className the name of the class being considered for loading
     * @return <code>true</code> if the class should be loaded, 
     *     <code>false</code> else (default)
     * 
     * @since 1.00
     */
    protected boolean loadClass(String className) {
        return true;
    }

    /**
     * Write the classes processed to <code>out</code>. Consider 
     * <code>in</code> as a jar file to read non-classes from and transfer 
     * them to <code>out</code>.
     * 
     * @param in the input jar file
     * @param out the output jar file to be created
     * @throws IOException in case that any I/O related problem or error occurs
     * @throws InstrumenterException in case that new code cannot be compiled
     * 
     * @since 1.00
     */
    protected void writeClasses(String in, String out) 
        throws IOException, InstrumenterException {
        writeClasses(in, out, null);
    }
    
    /**
     * Write the classes processed to <code>out</code>. Consider 
     * <code>in</code> as a jar file to read non-classes from and transfer 
     * them to <code>out</code>.
     * 
     * @param in the input jar file
     * @param out the output jar file to be created
     * @param processor an optional class processor for on-the-fly processing
     * @throws IOException in case that any I/O related problem or error occurs
     * @throws InstrumenterException in case that new code cannot be compiled
     * 
     * @since 1.00
     */
    protected void writeClasses(String in, String out, ClassProcessor processor)
        throws IOException, InstrumenterException {
        JarFile jf = null;
        JarOutputStream jos = null;
        try {
            jf = new JarFile(in);
            jos = new JarOutputStream(new FileOutputStream(
                out), jf.getManifest());
            if (null != classes) {
                for (HashMap.Entry<IClass, String> entry : classes.entries()) {
                    writeBackClass(entry.getKey(), entry.getValue(), jos);
                }
            }
    
            byte[] buf = new byte[1024];
            IFactory factory = IFactory.getInstance();
            Enumeration<JarEntry> iter = jf.entries();
            while (iter.hasMoreElements()) {
                JarEntry entry = iter.nextElement();
                String name = entry.getName();
                if (name.endsWith(CLASS_SUFFIX)) {
                    if (null != processor) {
                        Boolean process = processor.doProcess(
                            jarEntryToClassName(entry));
                        if (null != process) {
                            if (process) {
                                IClass cl = factory.obtainClass(
                                    jf.getInputStream(entry), false);
                                try {
                                    processor.process(cl);
                                } catch (InstrumenterException nfe) {
                                    if (!lazy) {
                                        throw nfe;
                                    }  else {
                                        System.err.println("Warning: " + nfe);
                                    }
                                }
                                writeBackClass(cl, entry.getName(), jos);
                            } else {
                                writeBackJarEntry(entry, jf, jos, buf);
                            }
                        }
                    }
                } else if (!"META-INF".equals(name)
                    && !"META-INF/MANIFEST.MF".equals(name)) {
                    writeBackJarEntry(entry, jf, jos, buf);
                }
            }
            jf.close();
            jos.close();
        } catch (IOException e) {
            if (null != jf) {
                jf.close();
            }
            if (null != jos) {
                jos.close();
            }
            throw e;
        } catch (InstrumenterException e) {
            if (null != jf) {
                jf.close();
            }
            if (null != jos) {
                jos.close();
            }
            throw e;
        }
    }

    /**
     * Writes back a given class to the given output stream.
     * 
     * @param cl the class to be written
     * @param entryName the name of the JAR entry to be created
     * @param jos the JAR output stream where to write the entry to
     * @throws IOException in case of any I/O error or problem
     * @throws InstrumenterException in case that new code in classes
     *     cannot be compiled
     * 
     * @since 1.00
     */
    private void writeBackClass(IClass cl, String entryName, 
        JarOutputStream jos) throws IOException, InstrumenterException {
        JarEntry jEntry = new JarEntry(entryName);
        jos.putNextEntry(jEntry);
        jos.write(cl.toBytecode());
        jos.closeEntry();
    }

    /**
     * Writes back a given JAR entry to the given output stream.
     * 
     * @param entry the entry (from <code>jf</code>) to be written to 
     *     <code>jos</code>
     * @param jf the JAR file which provides the contents for <code>entry</code>
     * @param jos the JAR output stream where to write the entry to
     * @param buf a buffer for byte writing operations
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    private void writeBackJarEntry(JarEntry entry, JarFile jf, 
        JarOutputStream jos, byte[] buf) throws IOException {
        JarEntry jEntry = new JarEntry(entry.getName());
        jos.putNextEntry(jEntry);
        InputStream is = jf.getInputStream(entry);
        int read;
        do {
            read = is.read(buf);
            if (read >= 0) {
                jos.write(buf, 0, read);
            }
        } while (read >= 0);
        jos.closeEntry();
    }

    /**
     * Returns the class name for the given JAR entry (assuming that
     * the entry points to a class file.
     * 
     * @param entry the JAR entry
     * @return the corresponding class name
     * 
     * @since 1.00
     */
    private String jarEntryToClassName(JarEntry entry) {
        String clName = entry.getName();
        assert clName.endsWith(CLASS_SUFFIX);
        clName = clName.substring(0, clName.length() 
            - CLASS_SUFFIX.length());
        if (null != binPath && clName.startsWith(binPath)) {
            clName = clName.substring(binPath.length());
        }
        return clName.replace('/', '.');
    }

}
