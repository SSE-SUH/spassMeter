package de.uni_hildesheim.sse.monitoring.runtime.boot;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines the accountable resource types.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum ResourceType {

    /**
     * Denotes that no resources shall be accounted. In this case a set of
     * resources shall only contain {@link #ALL}.
     */
    NONE,
    
    /**
     * Denotes that all resources shall be accounted. In this case a set of
     * resources shall only contain {@link #ALL}.
     */
    ALL,

    /**
     * CPU time consumption.
     */
    @Variability(id = {AnnotationConstants.MONITOR_TIME_CPU, 
        AnnotationConstants.MONITOR_TIME_SYSTEM }, op = Operation.AND)
    CPU_TIME,
    
    /**
     * Memory allocation.
     */
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
        AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    MEMORY,

    /**
     * File I/O consumption.
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    FILE_IO,
    
    /**
     * Network I/O consumption.
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    NET_IO;
    
    // check further constants before modification

    /**
     * Defines a constant array for all resources (empty). Do not remove - used 
     * by code generation.
     */
    public static final ResourceType[] SET_ALL = { ALL };
    
    /**
     * Defines a constant array for the default resources all resources (empty).
     * Do not remove and do not unify with {@link #ALL}.
     */
    public static final ResourceType[] SET_DEFAULT = { };
    
    /**
     * Defines a constant array for no resources (empty). Do not remove - used 
     * by code generation.
     */
    public static final ResourceType[] SET_NONE = { NONE };

    /**
     * Defines a constant array resources for resources to be considered 
     * anyway for instrumentation as no dedicated SUM information is provided
     * by the JVM. Do not remove - used by code generation.
     */
    public static final ResourceType[] SET_ANYWAY = { FILE_IO, NET_IO };
    
    /**
     * Returns whether <code>resources</code> is {@link #ALL}.
     * 
     * @param resources the resources to be searched for
     * @return <code>true</code> if <code>resources</code> is {@link #ALL}, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public static final boolean isAll(ResourceType[] resources) {
        return resources == SET_ALL 
            || (resources.length == 1 && resources[0] == ALL);
    }

    /**
     * Returns whether <code>resources</code> is {@link #NONE}.
     * 
     * @param resources the resources to be searched for
     * @return <code>true</code> if <code>resources</code> is {@link #NONE}, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public static final boolean isNone(ResourceType[] resources) {
        return resources == SET_NONE 
            || (resources.length == 1 && resources[0] == NONE);
    }
    
    /**
     * Returns whether <code>resources</code> contains <code>resource</code>.
     * 
     * @param resources the resources to be searched for <code>resource</code>
     * @param resource the resource to search for
     * @return <code>true</code> if <code>resources</code> contains 
     *   <code>resource</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    public static final boolean contains(ResourceType[] resources, 
        ResourceType resource) {
        boolean found;
        if (1 == resources.length) {
            if (NONE == resources[0]) {
                found = false;
            } else if (ALL == resources[0]) {
                found = true;
            } else {
                found = resource == resources[0];
            }
        } else {
            found = false;
            for (int i = 0; !found && i < resources.length; i++) {
                found = (resources[i] == resource);
            }
        } 
        return found;
    }

    /**
     * Ensures that the elements in <code>target</code> are also in 
     * <code>source</code>. Elements which are not in <code>source</code>
     * will not be in the result.
     * 
     * @param source the source set (to be used as maximum superset)
     * @param target the target set 
     * @return <code>target</code> or a subset of <code>target</code> where
     *    all elements are also present in <code>source</code> 
     * 
     * @since 1.00
     */
    public static final ResourceType[] ensureSubset(ResourceType[] source, 
        ResourceType[] target) {
        boolean copy = false;
        // accountableResources is specified
        if (0 == target.length) {
            copy = false; // should not occur, shall be ALL or NONE -> default
        } else if (1 == target.length && target[0] == ALL) {
            copy = false;
        } else if (1 == target.length && target[0] == NONE) {
            copy = false;
        } else {
            // determine valid subset
            ResourceType[] tmp = new ResourceType[source.length];
            int tmpCount = 0;
            for (int i = 0; i < source.length; i++) {
                for (int j = 0; j < target.length; j++) {
                    if (source[i] == target[j]) {
                        tmp[tmpCount++] = source[i];
                        break;
                    }
                }
            }
            if (0 == tmpCount) {
                target = new ResourceType[1];
                target[0] = NONE;
            } else {
                target = new ResourceType[tmpCount];
                System.arraycopy(tmp, 0, target, 0, tmpCount);
            }
        }
        if (copy) {
            // all is specified
            target = new ResourceType[source.length];
            System.arraycopy(source, 0, target, 0, source.length);
        }
        return target;
    }
    
}
