package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

import de.uni_hildesheim.sse.codeEraser.util.OnCreationJarProvider;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration
    .IObjectSizeProvider;
import de.uni_hildesheim.sse.monitoring.runtime.configuration
    .IRecordingEndListener;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.ScopeType;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.internal.
    InternalPluginRegistry;
import de.uni_hildesheim.sse.monitoring.runtime.recording.ObjectSizeProvider;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * A Java agent to instrument classes for runtime monitoring. The agent is 
 * called on a virtual machine using the following virtual machine argument
 * <br/><br/>
 * 
 * <code>-javaagent:<i>location-of-instrumentation-jar</i>=<i>parameter</i>
 *   </code>
 *   
 * <br/><br/>whereby the parameters are described in {@link Configuration here}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Agent implements IObjectSizeProvider, IRecordingEndListener {
    
    /**
     * Stores the instrumentation instance.
     */
    private static Instrumentation instrumentation;
    
    /**
     * Stores the transformer in order to provide convenient 
     * removal of the transformer.
     */
    private static MonitoringClassFileTransformer transformer;

    /**
     * Stores the additional jars to be added to a newly created class pool.
     */
    private static String[] jars = new String[0];
    
    /**
     * Stores the class names to be retransformed by default.
     */
    private static final HashMap<String, Object> RETRANSFORM 
        = new HashMap<String, Object>();
    
    /**
     * Initializes {@link #RETRANSFORM}.
     */
    static {
        RETRANSFORM.put("java.util.ArrayList", null);
        RETRANSFORM.put("java.util.Hashtable", null);
        RETRANSFORM.put("java.util.HashSet", null);
        RETRANSFORM.put("java.util.HashMap", null);
        RETRANSFORM.put("java.util.StringTokenizer", null);
        RETRANSFORM.put("java.util.Stack", null);
        RETRANSFORM.put("java.util.LinkedHashMap", null);
        RETRANSFORM.put("java.lang.Thread", null);
        RETRANSFORM.put("java.util.jar.JarVerifier", null);
        RETRANSFORM.put("java.security.cert.Certificate", null);
        RETRANSFORM.put("java.security.SecureClassLoader", null);
        
        /*        
        RETRANSFORM.add("java.io.File");
        RETRANSFORM.add("java.io.FilterInputStream");
        RETRANSFORM.add("java.net.URLClassLoader");
        RETRANSFORM.add("java.util.regex.Matcher");*/
    }
    
    /**
     * Implements an on-creation jar provider which returns {@link Agent#jars}.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class JarProvider extends OnCreationJarProvider {

        /**
         * Returns additional jars to be considered when creating a class pool.
         * Due to internal reasons, the caller must guarantee that the returned
         * array is <b>not</b> modified!
         * 
         * @return additional jars for a new class pool
         * 
         * @since 1.00
         */
        @Override
        public String[] getJars() {
            return jars;
        }
        
    }
    
    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private Agent() {
    }
    
    /**
     * JVM hook to statically load the javaagent at startup.<br/>
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     *
     * @param agentArgs the arguments for the agent
     * @param inst the instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        initialize(agentArgs, inst);
    }

    /**
     * Removes the transformer attached to this agent. May be called
     * even if the agent is not running.
     * 
     * @since 1.00
     */
    public static void removeTransformer() {
        if (null != instrumentation && null != transformer) {
            instrumentation.removeTransformer(transformer);
        }
    }
 
    /**
     * JVM hook to dynamically load javaagent at runtime.<br/>
     *
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args the arguments for the agent
     * @param inst the instrumentation instance
     * @throws Exception in case of unexpected behavior
     */
    public static void agentmain(String args, Instrumentation inst) 
        throws Exception {
        initialize(args, inst);
    }
    
    /**
     * Appends the given jar file in <code>fileName</code> either to the boot 
     * class path or to the system class path.
     * 
     * @param inst the instrumenter to append
     * @param fileName the jar file name
     * @param bootpath append to the boot class path <code>true</code> or to
     *     the system class path <code>false</code>
     * 
     * @since 1.00
     */
    private static void appendToPath(Instrumentation inst, String fileName, 
        boolean bootpath) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.err.println("installation error: " + file.toString() 
                + " not found");
            System.exit(0);
        } else {
            String targetPathName = "";
            try {
                JarFile jar = new JarFile(file);
                if (bootpath) {
                    targetPathName = "bootstrap";
                    inst.appendToBootstrapClassLoaderSearch(jar);
                } else {
                    targetPathName = "system";
                    inst.appendToSystemClassLoaderSearch(jar);
                }
                String[] tmp = new String[jars.length + 1];
                int i;
                for (i = 0; i < jars.length; i++) {
                    tmp[i] = jars[i];
                }
                tmp[i] = fileName;
                jars = tmp;
            } catch (IOException e) {
                System.err.println("cannot append " + file + " to " 
                    + targetPathName + " classpath: " + e.getMessage());
                System.exit(0);
            }
        }

    }
    
    /**
     * Initialize this agent with the given instrumentation.
     * 
     * @param args the arguments for the agent
     * @param inst the instrumentation instance
     * 
     * @since 1.00
     */
    private static void initialize(String args, Instrumentation inst) {
        String classpath = System.getProperty("java.class.path");
        if (null != classpath) {
            int posIa = classpath.indexOf("spass-meter-ia.jar");
            int posAll = classpath.indexOf("spass-meter.jar");
            if (posIa < 0 && posAll < 0) {
                System.err.println("configuration error: neither " 
                    + "spass-meter-ia.jar nor spass-meter.jar on class path");
                System.exit(0);
            } else if (posIa > 0) {
                int pos1 = classpath.lastIndexOf(File.pathSeparator, posIa);
                if (pos1 < 0) {
                    pos1 = 0; // beginning of classpath
                } else {
                    pos1++; // skip pathSeparator
                }
                String spassPath = classpath.substring(pos1, posIa);
                appendToPath(inst, spassPath + "spass-meter-boot.jar", true);
                appendToPath(inst, spassPath + "spass-meter-rt.jar", false);
            }
        }
        OnCreationJarProvider.setInstance(new JarProvider());
        Configuration conf = Configuration.INSTANCE;
        conf.readFromAgentArguments(args);
        // store instance for access to memory sizes
        instrumentation = inst;
        Agent agent = new Agent();
        ObjectSizeProvider.setInstance(agent);
        InternalPluginRegistry.attachRecordingEndListener(agent);
        // initialize instrumentation
        String bootjar = conf.getBootJar();
        if (bootjar.length() > 0) {
            Configuration.LOG.config("adding " + bootjar 
                + " to boot classpath");
            appendToPath(inst, bootjar, true);
        }
        transformer = new MonitoringClassFileTransformer();
        instrumentation.addTransformer(transformer, true);
        if (ScopeType.GROUP_INHERIT != Configuration.INSTANCE.getScopeType()) {
            retransformLoadedClasses(RETRANSFORM, true);
        }
    }
    
    /**
     * Attempts to retransform already loaded classes. 
     * 
     * @param classNames the class names to be retransformed (cleared as a 
     *   side effect)
     * @param doLibTransformation do Java library transformations if
     *   required
     * 
     * @since 1.00
     */
    public static void retransformLoadedClasses(
        HashMap<String, ?> classNames, boolean doLibTransformation) {
        Configuration conf = Configuration.INSTANCE;
        try {
            // using arrays to avoid loading additional classes
            Class<?>[] loaded = instrumentation.getAllLoadedClasses();
            int count = 0;
            for (int l = 0; l < loaded.length; l++) {
                if (classNames.containsKey(loaded[l].getName())) {
                    count++;
                } else if (doLibTransformation 
                    && instrumentation.isModifiableClass(loaded[l]) 
                    && null != loaded[l].getClassLoader() 
                    && conf.retransformJavaLib()) {
                    count++;
                } else {
                    loaded[l] = null;
                }
            }
            if (count > 0) {
                Class<?>[] retransform = new Class<?>[count];
                count = 0;
                for (int l = 0; l < loaded.length; l++) {
                    if (null != loaded[l]) {
                        retransform[count++] = loaded[l];
                    }
                }
                instrumentation.retransformClasses(retransform);
            }
        } catch (UnmodifiableClassException e) {
            System.err.println(e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Programmatic hook to dynamically load javaagent at runtime.
     * 
     * @since 1.00
     */
    public static void initialize() {
        if (instrumentation == null) {
            AgentLoader.loadAgent();
        }
    }
    
    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method is a dummy implementation.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available, 
     *         always 0)
     * 
     * @since 1.00
     */
    @Override
    public long getObjectSize(Object object) {
        return instrumentation.getObjectSize(object);
    }

    /**
     * Is called when recording ends.
     * 
     * @since 1.00
     */
    @Override
    public void notifyRecordingEnd() {
        removeTransformer();
    }

}
