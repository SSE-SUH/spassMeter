package de.uni_hildesheim.sse.codeEraser.ant;

import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;

/**
 * Changes the log level.
 * 
 * @author http://codefeed.com/blog/?p=82
 */
public class SetLogLevelTask extends Task {
    
    /**
     * Stores the current log level.
     */
    private int logLevel = -1;

    /**
     * Executes this task.
     */
    @SuppressWarnings("rawtypes")
    public void execute() {
        if (logLevel == -1) {
            throw new BuildException("You must specify a log level");
        }

        Vector listeners = this.getProject().getBuildListeners();
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            BuildListener listener = (BuildListener) i.next();

            if (listener instanceof BuildLogger) {
                BuildLogger logger = (BuildLogger) listener;
                logger.setMessageOutputLevel(logLevel);
            }
        }
    }

    /**
     * Changes the current log level.
     * 
     * @param level the current log level
     */
    public void setLevel(Echo.EchoLevel level) {
        this.logLevel = level.getLevel();
    }
}
