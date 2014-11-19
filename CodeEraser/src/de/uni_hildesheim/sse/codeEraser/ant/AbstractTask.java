package de.uni_hildesheim.sse.codeEraser.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import de.uni_hildesheim.sse.codeEraser.Configuration;

/**
 * Realizes a base task class corresponding to 
 * {@link de.uni_hildesheim.sse.codeEraser.Configuration}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractTask extends Task {

    /**
     * Stores the base directory (used for deriving the 
     * absolute path of relative paths).
     */
    private File baseDir = null;
    
    /**
     * Stores the (usually relative) path of the input jar file.
     */
    private File jarFile = null;
    
    /**
     * Stores the (usually relative) path of the output jar file.
     */
    private File outFile = null;

    /**
     * Stores the classpath.
     */
    private Path classpath;

    /**
     * Changes the base directory (used for deriving the 
     * absolute path of relative paths).
     * 
     * @param baseDir the new base directory
     */
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Changes the (usually relative) path of the input jar file.
     * 
     * @param jarFile the new path to the input jar file
     */
    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * Changes the (usually relative) path of the output jar file.
     * 
     * @param outFile the new path of the output jar file
     */
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    /**
     * Returns the base directory (used for deriving the 
     * absolute path of relative paths).
     * 
     * @return the new directory
     */
    protected File getBaseDir() {
        return baseDir;
    }

    /**
     * Returns the (usually relative) path of the input jar file.
     * 
     * @return the path to the input jar file
     */
    protected File getJarFile() {
        return jarFile;
    }

    /**
     * Returns the (usually relative) path of the output jar file.
     * 
     * @return the path of the output jar file
     */
    protected File getOutFile() {
        return outFile;
    }

    /**
     * Relocates <code>file</code> to {@link #baseDir}
     * if necessary.
     * 
     * @param file the file to be relocated if not absolute
     * @return the relocated absolute file
     * 
     * @since 1.00
     */
    protected File getAbsoluteFile(File file) {
        File result = file;
        if (null != file && !file.isAbsolute()) {
            result = new File(baseDir, file.toString());
        }
        return result;
    }
    
    /**
     * Set the classpath to be used for this modification.
     *
     * @param classpath an Ant Path object containing the compilation classpath.
     */
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    /**
     * Gets the classpath to be used for this modification.
     * 
     * @return the class path
     */
    public Path getClasspath() {
        return classpath;
    }

    /**
     * Adds a path to the classpath.
     * 
     * @return a class path to be configured
     */
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    /**
     * Adds a reference to a classpath defined elsewhere.
     * 
     * @param ref a reference to a classpath
     */
    public void setClasspathRef(Reference ref) {
        createClasspath().setRefid(ref);
    }
    
    /**
     * Initializes this task.
     * 
     * @since 1.00
     */
    public void init() {
        super.init();
    }
    
    /**
     * Initializes the given configuration based on the data gathered in this
     * task.
     * 
     * @param config the configuration to be modified as a side effect
     * 
     * @since 1.00
     */
    protected void initConfiguration(Configuration config) {
        config.setJar(getAbsoluteFile(jarFile));
        config.setOut(getAbsoluteFile(outFile));
        if (null != classpath) {
            List<URL> urls = new ArrayList<URL>();
            String[] paths = classpath.list();
            for (String path : paths) {
                try {
                    File tmp = new File(path);
                    urls.add(tmp.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new BuildException(path 
                        + " cannot be turned into a classpath URL!",
                        getLocation());
                }
            }
            config.setClasspath(urls);
        }
    }

}
