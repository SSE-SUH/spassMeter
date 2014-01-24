package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;

/**
 * Defines a recording element which represents a variability configuration and
 * therefore contains sub elements for recording individual contributions to the
 * configuration by individual variants. In fact, the recording strategy will
 * call {@link #getContributing(int, RecorderElementFactory, int)} or 
 * {@link #getContributing(int)} to directly access the subelements and thus, 
 * only at the end of recording, the remaining methods in this class will be 
 * called. These methods aim at evenly distributing the values to be recorded
 * to the subelements.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.CALIBRATION)
public class ContributingRecorderElement 
    extends AbstractMultipleRecorderElement {
    
    /**
     * Stores the number of variants (elements in {@link #elements} may be 
     * <b>null</b>, particularly during initialization or when automatic
     * variant detection is enabled).
     */
    private int count = 0;
    
    /**
     * Creates a recorder element.
     * 
     * @param conf the monitoring group configuration
     * 
     * @since 1.00
     */
    protected ContributingRecorderElement(MonitoringGroupConfiguration conf) {
        super(conf);
    }
    
    /**
     * Copies the data stored in <code>from</code> into this instance. (Deep 
     * copy)
     * 
     * @param from the instance from where to copy (may also be a subclass)
     * 
     * @since 1.00
     */
    @Override
    public void copy(RecorderElement from) {
        super.copy(from);
        int size = getElementCount();
        for (int i = 0; i < size; i++) {
            RecorderElement elt = getElement(i);
            if (null != elt) {
                RecorderElement elt2 = createElement();
                elt2.copy(elt);
                setElement(i, elt2);
            }
        }
    }
    
    /**
     * Creates a new subelement to be stored within this element (factory 
     * method).
     * 
     * @return the created element
     */
    protected RecorderElement createElement() {
        return new DefaultRecorderElement(getConfiguration());
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
        boolean result = false;
        if (count > 0) {
            result = firstElement().hasDebugStates(state);
        }
        return result;
    }

    /**
     * Returns the currently relevant recorder element (in case that 
     * multiple subelements are supported).
     * 
     * @param index the index of the sub element (may be negative if not known)
     * @param factory a factory to create new elements if needed
     * @param max the (current) number of maximum required subelements 
     *     (variabilities)
     * @return the subelement or this instance
     * 
     * @since 1.00
     */
    @Override
    RecorderElement getContributing(int index, RecorderElementFactory factory, 
        int max) {
        RecorderElement result = this;
        if (index >= 0 && null != factory) {
            ensureSize(index, max);
            if (null == getElement(index)) {
                setElement(index, factory.create(getConfiguration(), true));
                count++;
            }
            result = getElement(index);
        } else {
            result = this;
        }
        return result;
    }
    
    /**
     * Returns a contributing recording element, i.e. one of the elements
     * assigned to individual variants in a variability configuration.
     * 
     * @param index the element to be returned
     * @return the element assigned to the given index
     * @throws ArrayIndexOutOfBoundsException if 
     *     <code>index&lt;0 || index&gt;={@link #getContributingSize()}</code>
     * 
     * @since 1.00
     */
    @Override
    public RecorderElement getContributing(int index) {
        return getElement(index);
    }

    /**
     * Returns the number of contributing recording elements, i.e. the elements
     * recording individual variants in a variability configuration.
     * 
     * @return the number of contributing recording elements
     * 
     * @since 1.00
     */
    @Override
    public int getContributingSize() {
        return getElementCount();
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
        return null != elt;
    }

}
