package de.uni_hildesheim.sse.codeEraser.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * An ANT task for generating classes from resource files.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class FlattenTask extends MatchingTask {

    /**
     * The base directory.
     */
    private File baseDir;

    /**
     * The target directory.
     */
    private File outDir;
    
    /**
     * Stores the characters to be removed.
     */
    private String removeRegEx;
    
    /**
     * Stores a rule for handling character cases.
     */
    private String caseRule;
    
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
     * Target directory.
     * 
     * @param outDir the new target dir
     */
    public void setOutdir(File outDir) {
        this.outDir = outDir;
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
     * Defines an optional regular expression of characters to
     * be removed from target file names.
     * 
     * @param removeRegEx the regular expression
     * 
     * @since 1.00
     */
    public void setRemoveRegEx(String removeRegEx) {
        this.removeRegEx = removeRegEx;
    }
    
    /**
     * Defines an optional rule how to handle character cases.
     * 
     * @param caseRule may be <b>null</b>, <code>lower</code>, 
     *   <code>upper</code>
     * 
     * @since 1.00
     */
    public void setCaseRule(String caseRule) {
        this.caseRule = caseRule;
        if (null != this.caseRule) {
            this.caseRule = this.caseRule.toLowerCase();
        }
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
                if (null != base && file.startsWith(base)) {
                    file = file.substring(base.length());
                    while (file.startsWith(File.separator)) {
                        file = file.substring(1);
                    }
                }
                String flattenName = file.replace(File.separator, "");
                if (null != removeRegEx) {
                    try {
                        flattenName = flattenName.replaceAll(removeRegEx, "");
                    } catch (PatternSyntaxException e) {
                        throw new BuildException(
                            "illegal character removal pattern: " 
                                + e.getMessage());
                    }
                }
                if ("lower".equals(caseRule)) {
                    flattenName = flattenName.toLowerCase();
                } else if ("upper".equals(caseRule)) {
                    flattenName = flattenName.toUpperCase();
                }
                File outFile = new File(outDir, flattenName);
                try {
                    FileChannel inChannel = new FileInputStream(
                        res.getFile()).getChannel();
                    FileChannel outChannel = new FileOutputStream(
                        outFile).getChannel();
                    try {
                        inChannel.transferTo(0, inChannel.size(),
                                outChannel);
                    } catch (IOException e) {
                        throw new BuildException(e);
                    } finally {
                        if (inChannel != null) {
                            inChannel.close();
                        }
                        if (outChannel != null) {
                            outChannel.close();
                        }
                    }
                } catch (IOException e) {
                    throw new BuildException(e);
                }
                System.out.println(file + " -> " + outFile);
            }
        }
    }
}
