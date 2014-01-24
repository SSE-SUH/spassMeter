package test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

import test.classLoading.MyClassLoader;
import test.classLoading.Plugin;

/**
 * Loads classes via an own class loader for testing purposes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class ClassLoaderTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ClassLoaderTest() {
    }
    
    /**
     * Starts the test. Loads two classes from a prebuild jar.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem
    public static void main(String[] args) {
        System.out.println("HERE");
        try {
            File jar = new File("bin/loader-test.jar");
            // assumption: prebuilt by ANT
            assert jar.exists();
            MyClassLoader cl = new MyClassLoader(
                new URL[]{jar.toURI().toURL()});
            Class<?> testClass = cl.loadClass("test.classLoading.test.Test");
            Plugin plug = (Plugin) testClass.newInstance();
            plug.doit();
            testClass = cl.loadClass("test.classLoading.test.StaticTest");
            testClass.newInstance();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
}
