package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import java.lang.management.ManagementFactory;
import com.sun.tools.attach.VirtualMachine;

import de.uni_hildesheim.sse.monitoring.runtime.ElschaLogger;

/**
 * A class for automatically loading the agent loader (currently untested 
 * / unused).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class AgentLoader {

    /**
     * Defines the JAR for monitoring.
     */
    // TODO make configurable
    private static final String JARFILEPATH = "./Monitoring.jar";

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private AgentLoader() {
      ElschaLogger.info("AgentLoader instance created.");
    }
    
    /**
     * Loads the agent.
     * 
     * @since 1.00
     */
    public static void loadAgent() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
    
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(JARFILEPATH, "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
