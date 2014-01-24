package test.classLoading;

import java.net.URL;
import java.net.URLClassLoader;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Implements a simple (delegating) class loader.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class MyClassLoader extends URLClassLoader {

    /**
     * Instantiates this class loader with the URLs or jar files.
     * 
     * @param urls the URLs to consider
     * 
     * @since 1.00
     */
    public MyClassLoader(URL[] urls) {
        super(urls);
        System.out.println("Class loader initialized");
    }

}
