package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines constantes for the different types of events.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class Constants {

    // sonst sieht man nicht, ob man Duplikate hat ;)
    /**
     * Stores the identification for {@link EnterConfigurationElement}.
     */
    public static final int ENTERCONFIGURATION = 0;
    
    /**
     * Stores the identification for {@link EnterElement}+.
     */
    public static final int ENTER = 1;
    
    /**
     * Stores the identification for {@link ExitElement}.
     */
    public static final int EXIT = 2;
    
    /**
     * Stores the identification for {@link FinishRecordingElement}.
     */
    public static final int FINISHRECORDING = 3;
    
    /**
     * Stores the identification for {@link MemoryAllocatedElement}.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public static final int MEMORYALLOCATED = 4;
    
    /**
     * Stores the identification for {@link MemoryFreedByRecIdElement}.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public static final int MEMORYFREEDBYRECID = 5;
    
    /**
     * Stores the identification for {@link PrintStatisticsElement}.
     */
    public static final int PRINT_STATISTICS = 8;
    
    /**
     * Stores the identification for {@link IoElement}.
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
        AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public static final int IO = 9;
    
    /**
     * Stores the identification for {@link StartRecordingElement}.
     */
    public static final int STARTRECORDING = 10;
    
    /**
     * Stores the identification for {@link StopTimeRecordingElement}.
     */
    public static final int STOP_TIME_RECORDING = 11;
    
    /**
     * Stores the identification for {@link EndSystemElement}.
     */
    public static final int ENDSYSTEM = 13;

    /**
     * Stores the identification for {@link RegisterElement}.
     */
    public static final int REGISTER = 14;
 
    /**
     * Stores the identification for {@link AssignToAllElement}.
     */
    public static final int ASSIGNTOALL = 15;

    /**
     * Stores the identification for {@link PrintCurrentState}.
     */
    public static final int PRINTCURRENTSTATE = 16;

    /**
     * Stores the identification for {@link NotifyTimerElement}.
     */
    public static final int NOTIFYTIMER = 17;
 
    /**
     * Stores the identification for {@link NotifyValueChangeElement}.
     */
    public static final int NOTIFYVALUE = 18;

    /**
     * Stores the identification for {@link NotifyProgramRecordCreationElement}.
     */
    public static final int PROGRAMRECORDCREATION = 19;

    /**
     * Stores the identification for {@link ClearTemporaryDataElement}.
     */
    public static final int CLEARTEMPORARYDATA = 20;
    
    /**
     * Constructor.
     */
    private Constants() {
        
    }
}