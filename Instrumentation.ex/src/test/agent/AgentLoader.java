package test.agent;

import java.lang.management.ManagementFactory;

import test.AnnotationId;

import com.sun.tools.attach.VirtualMachine;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * A loader for dynamically loading an Agent.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class AgentLoader {

    /**
     * Defines the (fixed) path of the JAR containing the agent with 
     * appropriate manifest tags (Agent-Class).
     */
    private static final String JAR_FILE_PATH = "./bin/test-agent.jar";

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private AgentLoader() {
        
    }
    
    /**
     * Loads the agent. Called by the agent :o
     * 
     * @since 1.00
     */
    public static void loadAgent() {
        System.out.println("dynamically loading javaagent");
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(JAR_FILE_PATH, "");
            vm.detach();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
