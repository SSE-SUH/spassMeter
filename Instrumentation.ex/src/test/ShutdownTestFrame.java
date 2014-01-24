package test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import test.testing.TestEnvironment;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Test the automatically generated shutdown hook for a JFrame.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@SuppressWarnings("serial")
public class ShutdownTestFrame extends JFrame implements ActionListener {

    /**
     * Stores the internal timer used for closing the window.
     */
    private Timer timer;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @param delay milliseconds for showing the frame
     * 
     * @since 1.00
     */
    private ShutdownTestFrame(int delay) {
        super("Test");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        timer = new Timer(delay, this);
        timer.start();
        
        setVisible(true);
    }
    
    /**
     * Start the test, monitoring but do not explicitly stop it. ;)
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem(shutdownHook = true, invoke = "asserts")
    public static void main(String[] args) {
        TestEnvironment.notice(ShutdownTestFrame.class.getName());
        new ShutdownTestFrame(1000);
        TestEnvironment.notice("------------------ done: ShutdownTestFrame");
    }

    /**
     * Called from timer. For automated tests only.
     * 
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        timer.stop();
        processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>StartSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // this should just be called from the instrumenter
        TestEnvironment.success(ShutdownTestFrame.class.getName());
    }

}
