package de.uni_hildesheim.sse.monitoring.runtime.preprocess;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.Path;

/**
 * Implements a specific java task for running the preprocessor. This task
 * accepts the same arguments than the ANT Java task and the following 
 * additional arguments:
 * <ul>
 *   <li><code>in</code> the input jar file(s), separated by ","</li>
 *   <li><code>out</code> either the output jar file (in case that 
 *       <code>in</code> names only one file or the target directory in case
 *       of multiple jars in <code>in</code></li>
 *   <li><code>params</code> the instrumentation parameters, see
 *       {@link de.uni_hildesheim.sse.monitoring.runtime.instrumentation.Agent}
 *       </li>
 * </ul>
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class SpassMeterTask extends Java {
    
    /**
     * Stores the (usually relative) path of the input jar file.
     */
    private String in = null;
    
    /**
     * Stores the (usually relative) path of the output jar file.
     */
    private File out = null;
    
    /**
     * Stores the instrumentation parameter.
     */
    private String params = null;
    
    /**
     * Stores the base dir.
     */
    private File dir = null;
    
    /**
     * Changes the (usually relative) path of the output jar file.
     * 
     * @param outFile the new path of the output jar file
     */
    public void setOut(File outFile) {
        this.out = outFile;
    }
    
    /**
     * Changes the (usually relative) path of the input jar file(s).
     * 
     * @param in the new path of the input jar file (s)
     */
    public void setIn(String in) {
        this.in = in;
    }
    
    /**
     * Set the working directory of the process.
     *
     * @param dir working directory.
     *
     */
    public void setDir(File dir) {
        this.dir = dir;
        super.setDir(dir);
    }
    
    /**
     * Defines the instrumentation parameters.
     * 
     * @param params the instrumentation parameters (see 
     * {@link de.uni_hildesheim.sse.monitoring.runtime.instrumentation.Agent}).
     * 
     * @since 1.00
     */
    public void setParams(String params) {
        // TODO allow individual params
        this.params = params;
    }

    /**
     * Do the execution.
     * @throws BuildException if failOnError is set to true and the application
     * returns a nonzero result code.
     */
    public void execute() throws BuildException {
        ClassLoader loader = SpassMeterTask.class.getClassLoader();
        if (loader instanceof AntClassLoader) {
            AntClassLoader aLoader = (AntClassLoader) loader;
            StringTokenizer tokens = new StringTokenizer(
                aLoader.getClasspath(), File.pathSeparator);
            boolean found = false;
            while (tokens.hasMoreTokens()) {
                String path = tokens.nextToken();
                if (path.endsWith("spass-meter-ant.jar")) {
                    found = true;
                    getCommandLine().getClasspath().add(
                        new Path(getProject(), path));
                }
            }
            if (!found) {
                System.err.println(
                    "Cannot find SPASS-meter (ANT distribution)");
            }
        } else {
            System.err.println("Unexpected class loader: " 
                + loader.getClass().getName());
        }
//        getCommandLine().getClasspath().add(new );
        setClassname(de.uni_hildesheim.sse.monitoring.runtime.preprocess.
            Preprocess.class.getName());

        if (null == in) {
            throw new BuildException("in file(s) must be provided");
        }

        if (null == out) {
            throw new BuildException("out file or directory must be provided");
        }

        StringBuilder inArg = new StringBuilder();
        StringTokenizer token = new StringTokenizer(in, ",");
        File f;
        while (token.hasMoreTokens()) {
            f = new File(token.nextToken().trim());
            if (null != dir && !f.isAbsolute()) {
                f = new File(dir, f.toString());
            }
            inArg.append(f.getAbsolutePath());
            if (token.hasMoreTokens()) {
                inArg.append(",");
            }
        }
        
        Argument arg = createArg();
        arg.setValue(inArg.toString());

        f = out;
        if (null != dir && !f.isAbsolute()) {
            f = new File(dir, f.toString());
        }
        arg = createArg();
        arg.setValue(f.getAbsolutePath());
        if (null != params) {
            arg = createArg();
            arg.setValue(params);
        }
        super.execute();
    }

}
