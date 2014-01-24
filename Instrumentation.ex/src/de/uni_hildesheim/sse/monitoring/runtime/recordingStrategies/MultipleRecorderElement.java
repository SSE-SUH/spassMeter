package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;

/**
 * Defines a recording element which represents multiple recorder elements 
 * by delegating to stored monitoring groups. This class has a different
 * semantics than a {@link ContributingRecorderElement} because it passes the
 * same recorded value to all represented recorder elements.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class MultipleRecorderElement extends AbstractMultipleRecorderElement {

    /**
     * Consider the individual accountable resources
     * of the stored elements in addition to those configured for this 
     * group (<code>true</code>) or this group only (<code>false</code>).
     */
    private boolean elementResources;
    
    /**
     * Creates a recorder element.
     * 
     * @param conf the monitoring group configuration
     * @param elements the set of elements to be stored 
     * @param distributeValues account the fraction of the values according to
     *     the accountable elements or the entire value (indirect)
     * @param elementResources consider the individual accountable resources
     *     of the stored elements in addition to those configured for this 
     *     group (<code>true</code>) or this group only (<code>false</code>)
     * 
     * @since 1.00
     */
    protected MultipleRecorderElement(MonitoringGroupConfiguration conf, 
        RecorderElement[] elements, boolean distributeValues, 
        boolean elementResources) {
        super(conf, elements, distributeValues);
        this.elementResources = elementResources;
    }
    
    /**
     * Returns if this instance has the given <code>state</code> selected.
     * 
     * @param state the state to be tested
     * 
     * @return <code>true</code> if the state is selected, <code>false</code>
     *     else
     */
    public boolean hasDebugStates(DebugState state) {
        boolean has = super.hasDebugStates(state);
        int size = getElementCount();
        for (int i = 0; !has && i < size; i++) {
            RecorderElement elt = getElement(i);
            if (null != elt) {
                has = elt.hasDebugStates(state);
            }
        }
        return has;
    }

    /**
     * Returns the currently relevant recorder element (in case that 
     * multiple subelements are supported).
     * 
     * @param index the index of the sub element
     * @param factory a factory to create new elements if needed
     * @param max the (current) number of maximum required subelements (
     *     variabilities)
     * @return the subelement or this instance
     * 
     * @since 1.00
     */
    @Override
    RecorderElement getContributing(int index, RecorderElementFactory factory, 
        int max) {
        return this;
    }

    /**
     * Returns a contributing recording element, i.e. one of the elements
     * assigned to individual variants in a variability configuration.
     * 
     * @param index the element to be returned
     * @return the element assigned to the given index (always <b>this</b>)
     * @throws ArrayIndexOutOfBoundsException if 
     *     <code>index&lt;0 || index&gt;={@link #getContributingSize()}</code>
     * 
     * @since 1.00
     */
    @Override
    public RecorderElement getContributing(int index) {
        return this;
    }
    
    /**
     * Returns the number of contributing recording elements, i.e. the elements
     * recording individual variants in a variability configuration.
     * 
     * @return the number of contributing recording elements (always 0)
     * 
     * @since 1.00
     */
    @Override
    public int getContributingSize() {
        return 0;
    }
    
    /**
     * Returns whether this element is a pseudo element and should not be 
     * visible to the user.
     * 
     * @return <code>true</code> if this element is visible, <code>false</code>
     *   else
     * 
     * @since 1.00
     */
    @Override
    public boolean isVisible() {
        return false;
    }
    
    /**
     * Returns whether the given element should record the specified resource.
     * 
     * @param elt the element to be considered
     * @param resource the resource to be tested
     * @return <code>true</code> if recording should be done, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    protected boolean enableRecording(RecorderElement elt, 
        ResourceType resource) {
        if (elementResources) {
            return null != elt && elt.accountResource(resource);
        } else {
            return null != elt;
        }
    }

}
