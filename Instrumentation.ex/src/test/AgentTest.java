package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import test.threadedTest.Data;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Execute some interleaving threads with different 
 * resource consumption.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class AgentTest {
    
    /**
     * Stores the data instance(s).
     */
    @SuppressWarnings("unused")
    private static Data data;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private AgentTest() {
    }
        
    /**
     * Executes the test.
     * 
     * @param args an arbitrary argument leads to dynamically loading the 
     *    instrumentation agent
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem
    public static void main(String[] args) {
        System.out.println("start ------  " + AgentTest.class.getName());
        if (args.length > 0) {
            try {
                Class<?> agentClass = Class.forName("test.agent.Agent");
                Method method = agentClass.getDeclaredMethod(
                    "initialize", new Class<?>[]{});
                method.invoke(null, (Object[]) null);
            } catch (ClassNotFoundException e) {
                System.err.println("Agent not found");
            } catch (NoSuchMethodException e) {
                System.err.println("Agent.initialize not found");
            } catch (InvocationTargetException e) {
                System.err.println(e.getMessage());
            } catch (IllegalAccessException e) {
                System.err.println(e.getMessage());
            }
        }
        
        for (int i = 0; i < 10000; i++) {
            data = new Data();
            if (i % 50 == 0) {
                System.gc();
            }
        }
        System.out.println("stop ------  " + AgentTest.class.getName());
    }

}
