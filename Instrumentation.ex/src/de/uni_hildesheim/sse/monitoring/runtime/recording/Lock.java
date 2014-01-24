package de.uni_hildesheim.sse.monitoring.runtime.recording;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.IntHashMap;

/**
 * Provides a simple global locking mechanism for the entire 
 * instrumenter / SPASS-meter. As memory allocation is currently the most 
 * pressing performance problem, this class just focuses on enabling or 
 * disabling memory as an accountable resource.
 * 
 * However, at the moment this class performs a mapping between string and
 * numeric identifiers. This shall be propagated through the framework in
 * future versions.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Lock {

    /**
     * Stores the memory resource monitoring flag for the individual threads
     * in terms of stacks. This could also be handled via ThreadLocals but 
     * this would introduce instrumented classes of lower performance here.
     */
    static final LongRecordingStackHashMap THREAD_STACKS 
        = new LongRecordingStackHashMap();
    
    /**
     * Defines basic memory recording types for executing the allocation 
     * recording.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private enum MemRecordingType {
        
        /**
         * No memory accounting.
         */
        NONE,
        
        /**
         * Direct accounting (regarding group).
         */
        DIRECT,

        /**
         * Indirect accounting (regarding group).
         */
        INDIRECT;
    }
    
    /**
     * Descriptive information about a monitoring group.
     * 
     * @author eichelbe
     * @since 1.00
     * @version 1.00
     */
    private static class GroupInfo {
        
        /**
         * Stores the recording type.
         */
        private MemRecordingType type;
        
        /**
         * Stores the numerc identifier.
         */
        private int id;
        
        /**
         * Creates a group information object.
         * 
         * @param recId the recording identifier
         * @param type the recording type
         * 
         * @since 1.00
         */
        public GroupInfo(String recId, MemRecordingType type) {
            this.type = type;
            id = nextId++;
            REGISTERED_IDS.put(id, recId);
        }
    }
    
    /**
     * Stores the registered monitoring groups (name-shall memory be monitored).
     */
    private static final HashMap<String, GroupInfo> REGISTERED_GROUPS 
        = new HashMap<String, GroupInfo>();
    
    /**
     * Stores the reverse mapping from {@link #REGISTERED_GROUPS}.
     */
    private static final IntHashMap<String> REGISTERED_IDS
        = new IntHashMap<String>();

    /**
     * Stores the next numeric id.
     */
    private static int nextId = 0;
    
    static {
        new GroupInfo(Helper.RECORDER_ID, MemRecordingType.NONE);
    }
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Lock() {
    }
    
    /**
     * Registers a monitoring group.
     * 
     * @param className the name of the class being monitored.
     * @param recId the recording identification
     * @param conf the monitoring configuration
     * 
     * @since 1.00
     */
    public static final void registerGroup(String className, String recId, 
        MonitoringGroupConfiguration conf) {
        MemRecordingType type;
        boolean isMemResource = ResourceType.contains(conf.getResources(), 
            ResourceType.MEMORY);
        if (isMemResource) {
            if (GroupAccountingType.INDIRECT == conf.getGroupAccounting()) {
                type = MemRecordingType.INDIRECT;
            } else {
                type = MemRecordingType.DIRECT;
            }
        } else {
            type = MemRecordingType.NONE;
        }
        if (null != recId && recId.length() > 0) {
            REGISTERED_GROUPS.put(recId, new GroupInfo(recId, type));
        } else if (null != className && className.length() > 0) {
            REGISTERED_GROUPS.put(className, new GroupInfo(recId, type));
        }
    }

    /**
     * Returns whether the top element on the stack enables memory accounting.
     * As a side effect, this method disables memory accounting for the top
     * element (lock). Memory accounting needs to be enabled explicitly using
     * {@link #setStackTopMemoryAccounting(long, boolean)}.
     * 
     * @param threadId the thread identification
     * @return the current value, negative if disabled
     * 
     * @since 1.00
     */
    public static final long isStackTopMemoryAccounting(long threadId) {
        long result;
        RecordingStack stack = THREAD_STACKS.get(threadId);
        if (null != stack) {
            result = stack.top(-1);
        } else {
            result = -1;
        }
        return result;
    }

    /**
     * Changes whether memory shall be accounted for the top element of the 
     * execution stack of a thread.
     * 
     * @param threadId the thread identification
     * @param account the current value, negative if disabled
     * 
     * @since 1.00
     */
    public static final void setStackTopMemoryAccounting(long threadId, 
        long account) {
        RecordingStack stack = THREAD_STACKS.get(threadId);
        if (null != stack) {
            stack.top(account);
        }
    }

    /**
     * Pushes a recording group via its <code>recId</code> to the execution
     * stack of <code>threadId</code>.
     * 
     * @param threadId the thread identification
     * @param recId the recording identification according to which the 
     *   monitoring configuration can be determined
     * 
     * @since 1.00
     */
    static final synchronized void pushToStack(long threadId, String recId) {
        if (null != recId) {
            GroupInfo gInfo = REGISTERED_GROUPS.get(recId);
            if (null != gInfo) {
                //MemoryRecordingType type = gInfo.type;
                RecordingStack stack = THREAD_STACKS.get(threadId);
                if (null == stack) {
                    stack = new RecordingStack(
                        Recorder.doUnallocationRecording()); 
                    THREAD_STACKS.put(threadId, stack);
                }
                stack.push(gInfo.id, 
                    MemRecordingType.NONE != gInfo.type ? 0 : -1, 
                    MemRecordingType.INDIRECT == gInfo.type);
            }
        }
    }

    /**
     * Pops the latest element from the stack.
     * 
     * @param threadId the thread identification
     * @param recId the recording identification which shall be on the top
     *   of the stack (currently just checked whether the recording 
     *   identification is not null)
     * 
     * @since 1.00
     */
    static final synchronized void popFromStack(long threadId, String recId) {
        if (null != recId) {
            RecordingStack stack = THREAD_STACKS.get(threadId);
            if (null != stack) {
                stack.pop();
            }
        }
    }
    
    /**
     * Returns the recorder id for the given (internal) numeric id.
     * 
     * @param id the numeric id
     * @return the recorder id (may be <b>null</b>)
     * 
     * @since 1.00
     */
    static final String getRecorderId(int id) {
        return REGISTERED_IDS.get(id);
    }

}
