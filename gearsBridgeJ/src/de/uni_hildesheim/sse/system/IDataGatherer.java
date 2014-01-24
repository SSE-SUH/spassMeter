package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Provides methods to gather system specific data.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = {AnnotationConstants.VAR_GATHER_DATA, 
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)    
public interface IDataGatherer {

    /**
     * Gathers WiFi signal measurements using the external native 
     * library.
     * 
     * @param timeout in milli seconds when data gathering should be stopped 
     *        if no data was provided by the operating system
     * @return access point data (or an empty array)
     * 
     * @since 1.00
     */
    public AccessPointData[] gatherWifiSignals(int timeout);
    
    /**
     * Returns if (memory) functions relying on JVMTI are supported.
     * 
     * @return <code>true</code> if native JVMTI support is available, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean supportsJVMTI();

    /**
     * Registers the thread id of this thread with the related native thread in 
     * order to provide time monitoring on thread level.
     * This method should only be implemented in case that JMX is not supported
     * by the VM, e.g. on Android. This method provides functionality only if 
     * {@link #needsThreadRegistration()} returns <code>true</code>.
     * 
     * @param register register this thread if <code>true</code>, unregister
     *   it if <code>false</code>
     *
     * @since 1.00
     */
    public void registerThisThread(boolean register);
    
    /**
     * Returns weather the underlying (JVM) implementation needs to register
     * SUM threads with native threads. The background for this functionality
     * is that time monitoring of thread execution time is only provided
     * as JMX functionality which is not provided in all JVMs, e.g. prior to 
     * 1.4 or in Android. 
     * 
     * @return <code>true</code> if thread registration is required, 
     *   <code>false</code> if not (default)
     * 
     * @since 1.00
     */
    public boolean needsThreadRegistration();

    /**
     * Redefines the given classes at once (interdependencies). However, this 
     * method is only functional if {@link #supportsJVMTI()}.
     * 
     * @param classes the classes to be redefined
     * @param bytecode the new bytecode related to <code>classes</code>
     * @return the related JVMTI error code, negative is no attempt, 
     *   0 is successful
     * 
     * @since 1.00
     */
    public int redefineMultiClasses(Class<?>[] classes, byte[][] bytecode);
    
    /**
     * Redefines the given classes one-by-one. However, this method is only 
     * functional if {@link #supportsJVMTI()}.
     * 
     * @param classes the classes to be redefined
     * @param bytecode the new bytecode related to <code>classes</code>
     * @return the related JVMTI error code, negative is no attempt, 
     *   0 is successful
     * 
     * @since 1.00
     */
    public int redefineClasses(Class<?>[] classes, byte[][] bytecode);

    /**
     * Redefines the given class. However, this method is only functional
     * if {@link #supportsJVMTI()}.
     * 
     * @param clazz the class to be redefined
     * @param bytecode the new bytecode related to <code>class</code>
     * @return the related JVMTI error code, negative is no attempt, 
     *   0 is successful
     * 
     * @since 1.00
     */
    public int redefineClass(Class<?> clazz, byte[] bytecode);

}
