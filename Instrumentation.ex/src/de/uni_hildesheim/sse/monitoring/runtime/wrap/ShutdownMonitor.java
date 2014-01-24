package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;

/**
 * Provides code to shutdown the monitoring framework. This is needed because
 * javassist does not support inner or local classes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ShutdownMonitor extends Thread {
    
    /**
     * Stores whether the end code shall be executed. <b>null</b> means
     * ignore, <code>false</code> wait, <code>true</code> execute.
     */
    private static Boolean executeEndCode = null;
    
    /**
     * Stores the static method to be invoked at the end of monitoring.
     */
    private String invoke;
    
    /**
     * Stores the class loader for invoking {@link #invoke}.
     */
    private ClassLoader loader;
    
    /**
     * Creates a new monitor.
     * 
     * @param invoke the static method to be invoked
     * @param loader the related class loader for invocation
     * 
     * @since 1.00
     */
    public ShutdownMonitor(ClassLoader loader, String invoke) {
        this.invoke = invoke;
        this.loader = loader;
    }

    /**
     * Enables explicit waiting for end system notification (by
     * {@link #endSystemNotification()}). This mechanism should not be used
     * when this class is used as a JVM shutdown hook!
     * 
     * @param wait if <code>true</code> end system notification is required
     *   to shut down monitoring, if <code>false</code> the system just exists
     * 
     * @since 1.00
     */
    public static void setWaitEndSystemNotification(boolean wait) {
        if (wait) {
            executeEndCode = Boolean.TRUE;
        }
    }

    /**
     * Notifies that final end of monitoring occured.
     * 
     * @since 1.00
     */
    public static void endSystemNotification() {
        executeEndCode = Boolean.TRUE;
    }

    /**
     * Ends the monitoring system properly.
     * 
     * @param printStatistics determines if statistics should be printed
     * @param loader the class loader for {@link #invoke}
     * @param invoke a static method to be invoked after finishing monitoring 
     *    (in the form fqn class name "." method name, no parameter)
     * 
     * @since 1.00
     */
    public static void endMonitoring(boolean printStatistics, 
        ClassLoader loader, String invoke) {
        try {
            RecorderFrontend frontend = RecorderFrontend.instance;
            if (null != frontend) {
                frontend.notifyProgramEnd();
                if (printStatistics) {
                    frontend.printStatistics();
                }
                frontend.endSystem();
            }
        } catch (NullPointerException npe) {
            // happens sometimes
            npe.printStackTrace(System.out);
        }
        while (Boolean.FALSE == executeEndCode) {
            Thread.yield();
        }        
        if (null != invoke && invoke.length() > 0) {
            int pos = invoke.lastIndexOf('.');
            if (pos > 0 && pos < invoke.length()) {
                String cls = invoke.substring(0, pos);
                String meth = invoke.substring(pos + 1);
                if (meth.endsWith(")")) {
                    meth = meth.substring(0, meth.length() - 1);
                    if (meth.endsWith("(")) {
                        meth = meth.substring(0, meth.length() - 1);
                    }
                }
                try {
                    Class<?> clazz = loader.loadClass(cls);
                    Method method = clazz.getMethod(meth);
                    method.setAccessible(true); // run it in any case ;)
                    method.invoke(null);
                } catch (InvocationTargetException e) {
                    handleException(invoke, e);
                    e.getCause().printStackTrace();
                } catch (Exception e) {
                    handleException(invoke, e);
                }
            }
        }
    }
    
    /**
     * Emits an exception.
     * 
     * @param invoke the method to invoke
     * @param exception the caught exception
     * 
     * @since 1.00
     */
    private static void handleException(String invoke, Exception exception) {
        System.err.println("SPASS-meter: cannot call " + invoke 
            + ": " + exception.getClass().getName() + " " 
            + exception.getMessage());
    }
    
    /**
     * Ends the monitoring.
     */
    public void run() {
        endMonitoring(true, loader, invoke);
    }

}
