package test.agent;

import java.lang.instrument.Instrumentation;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * A second agent for testing overlapping agents.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class Agent {
    
    /**
     * Stores if this Agent is yet loaded.
     */
    private static boolean loaded = false;
    
    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private Agent() {
    }

    /**
     * JVM hook to statically load the javaagent at startup.<p>
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     *
     * @param agentArgs the arguments for the agent
     * @param inst the instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(Agent.class.getName() + ": premain called");
        loaded = true;
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.<p>
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
        System.out.println(Agent.class.getName() + ": agentmain called");
        loaded = true;
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.<p>
     *
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args the arguments for the agent
     */
    public static void agentmain(String args) {
        System.out.println(Agent.class.getName() + ": agentmain called");
        loaded = true;
    }

    /**
     * Called to load the agent at runtime (so that loads itself into the JVM).
     * 
     * @since 1.00
     */
    public static void initialize() {
        System.out.println(Agent.class.getName() + ": initialize called");
        if (!loaded) {
            System.out.println(Agent.class.getName() + ": loading agent");
            AgentLoader.loadAgent();
        }
    }

}
