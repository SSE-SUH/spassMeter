package de.uni_hildesheim.sse.codeEraser.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import de.uni_hildesheim.sse.codeEraser.genResource.
    PropertyToListResourceBundle;

/**
 * An ANT task for generating classes from resource files.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class PropertyGenTask extends MatchingTask {

    /**
     * The base directory.
     */
    private File baseDir;
    
    /**
     * The file sets collected.
     */
    private List<FileSet> filesets = new ArrayList<FileSet>();
    
    /**
     * Directory from which to use files.
     * 
     * @param baseDir the new base dir
     */
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }
    
    /**
     * Adds a set of files.
     * 
     * @param set the file set to add
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }
    
    /**
     * Validate and build.
     * 
     * @throws BuildException in case that a build exception occurred
     */
    public void execute() throws BuildException {
        if (filesets.isEmpty()) {
            throw new BuildException("No resources specified");
        }
        String base = null;
        if (null != baseDir) {
            base = baseDir.getAbsolutePath();
        }
        for (int i = 0; i < filesets.size(); i++) {
            FileSet set = filesets.get(i);
            Iterator<?> iter = set.iterator();
            while (iter.hasNext()) {
                FileResource res = (FileResource) iter.next();
                String file = res.getFile().getAbsolutePath();
                if (file.endsWith(
                    PropertyToListResourceBundle.RESOURCE_FILE_SUFFIX)) {
                    if (null != base && file.startsWith(base)) {
                        file = file.substring(base.length());
                        while (file.startsWith(File.separator)) {
                            file = file.substring(1);
                        }
                    }
                    try {
                        String className = PropertyToListResourceBundle
                            .generateClass(baseDir, file);
                        if (null != className) {
                            System.out.println("generated " + className);
                        } else {
                            System.out.println("cannot generate for " + file);
                        }
                    } catch (IOException e) {
                        throw new BuildException(e.getMessage());
                    } catch (MissingResourceException e) {
                        throw new BuildException(e.getMessage());
                    }
                } else {
                    System.out.println("Ignoring " + file);
                }
            }
        }
    }
}
