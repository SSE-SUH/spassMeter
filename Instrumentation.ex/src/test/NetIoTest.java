package test;

import java.io.IOException;

import test.testing.TestEnvironment;
import test.testing.MonitoringGroupValue;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Perform simple network IO testing of the monitoring infrastructure. This
 * class realizes a simple TCP client as well as a simple server.<p>
 * Monitoring must be enabled via the agent JVM parameter!<p>
 * Note that this class just reuses {@link AbstractNetIoTest} so that the inner
 * classes defined there are not annotated and not accounted to the defined 
 * recording id but to the program.<p>
 * This class may cause fileIO as it reads internal Java security policies.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_NET_IO)
public class NetIoTest extends AbstractNetIoTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    protected NetIoTest() {
    }
    
    /**
     * Starts the server part, executes the client part and prints out the 
     * results. Currently to be compared manually with the output of the 
     * monitoring infrastructure.
     *
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    protected void executeClient() throws IOException {
        // force accounting
        super.executeClient();
    }
    
    /**
     * Starts the test.
     * 
     * @param args command line arguments (ignored)
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) throws IOException {
        AbstractNetIoTest.main(args, new NetIoTest());
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // client resources are accounted to ID_NET_IO, server resources are not
        // considered
        TestEnvironment.assertEquals(AnnotationId.ID_NET_IO, 
            MonitoringGroupValue.NET_WRITE, AbstractNetIoTest.getClientWrite());
        TestEnvironment.assertEquals(AnnotationId.ID_NET_IO, 
            MonitoringGroupValue.NET_READ, AbstractNetIoTest.getClientRead());

        // client an server accounted to program
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_WRITE, AbstractNetIoTest.getAllWrite(), 1);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_READ, AbstractNetIoTest.getAllRead());

        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_WRITE, 0);
        // reads internal security policy
        TestEnvironment.assertGreater(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_READ, 0);
        
        TestEnvironment.success(AnnotationId.ID_NET_IO);
    }

}
