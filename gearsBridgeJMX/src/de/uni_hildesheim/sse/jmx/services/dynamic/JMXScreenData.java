package de.uni_hildesheim.sse.jmx.services.dynamic;

import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.ScreenDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IScreenDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
public class JMXScreenData extends AbstractJMXServiceData implements
        IScreenDataGatherer {

    /**
     * Instance of {@link IScreenDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    private IScreenDataGatherer screenDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    public JMXScreenData() {
        screenDataGatherer = GathererFactory.getScreenDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenWidth() {
        return screenDataGatherer.getScreenWidth();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenHeight() {
        return screenDataGatherer.getScreenHeight();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenResolution() {
        return screenDataGatherer.getScreenResolution();
    }

    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(ScreenDataConstants.SCREEN_WIDTH)) {
            return getScreenWidth();
        } else if (attributeName.equals(ScreenDataConstants.SCREEN_HEIGHT)) {
            return getScreenHeight();
        } else if (attributeName.equals(ScreenDataConstants.
                SCREEN_RESOLUTION)) {
            return getScreenResolution();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "ScreenData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the "
            + "screen data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // ScreenWidth
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ScreenDataConstants.
            SCREEN_WIDTH, "int",
            "The physical screen width.", 
            READABLE, WRITEABLE, ISIS));
        // ScreenHeight
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ScreenDataConstants.
            SCREEN_HEIGHT, "int",
            "The physical screen height.", 
            READABLE, WRITEABLE, ISIS));
        // ScreenResolution
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ScreenDataConstants.
            SCREEN_RESOLUTION, "int",
            "The physical screen resolution.", 
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("ScreenData",
                "Public default constructor ...", null);

        return mBeanConstrInfo;
    }

    @Override
    public MBeanOperationInfo[] defineOperations() {
        MBeanOperationInfo[] mBeanOperInfo = new MBeanOperationInfo[0];
        return mBeanOperInfo;
    }

    @Override
    public Object invokeSpecificFunction(String operationName, Object[] params,
            String[] signature) throws ReflectionException {
        return null;
    }

    @Override
    public void setSpecificAttributes(Attribute attribute)
        throws AttributeNotFoundException {        
    }
    
}