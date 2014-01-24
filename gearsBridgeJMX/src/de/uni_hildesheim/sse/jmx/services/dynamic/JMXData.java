package de.uni_hildesheim.sse.jmx.services.dynamic;

import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.DataConstants;
import de.uni_hildesheim.sse.system.AccessPointData;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
public class JMXData extends AbstractJMXServiceData implements IDataGatherer {

    /**
     * Instance of {@link IDataGatherer} for gathering required informations.
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
    private IDataGatherer dataGatherer;

    /**
     * Creates an instance of {@link JMXData}.
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
    public JMXData() {
        dataGatherer = GathererFactory.getDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_WIFI_DATA)
    public AccessPointData[] gatherWifiSignals(int timeout) {
        return dataGatherer.gatherWifiSignals(timeout);
    }

    @Override
    public boolean supportsJVMTI() {
        return dataGatherer.supportsJVMTI();
    }

    @Override
    public boolean needsThreadRegistration() {
        return dataGatherer.needsThreadRegistration();
    }

    @Override
    public void registerThisThread(boolean arg0) {
        dataGatherer.registerThisThread(arg0);
    }


    @Override
    public int redefineClass(Class<?> arg0, byte[] arg1) {
        return dataGatherer.redefineClass(arg0, arg1);
    }

    @Override
    public int redefineClasses(Class<?>[] arg0, byte[][] arg1) {
        return dataGatherer.redefineClasses(arg0, arg1);
    }

    @Override
    public int redefineMultiClasses(Class<?>[] arg0, byte[][] arg1) {
        return dataGatherer.redefineMultiClasses(arg0, arg1);
    }

    
    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(DataConstants.SUPPORTS_JVMTI)) {
            return supportsJVMTI();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "Data";
        mBeanInfo[1] = "Dynamic MBean for getting system specific data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // SupportsJVMTI
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            DataConstants.SUPPORTS_JVMTI, "java.lang.Boolean",
            "Returns if (memory) functions relying on JVMTI are supported.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("Data",
                "Public default constructor ...", null);

        return mBeanConstrInfo;
    }

    @Override
    public MBeanOperationInfo[] defineOperations() {
        MBeanOperationInfo[] mBeanOperInfo = new MBeanOperationInfo[1];
        MBeanParameterInfo[] signature = new MBeanParameterInfo[1];
        signature[0] = new MBeanParameterInfo(
            "timeout",
            "int",
            "timeout in milli seconds when data gathering should be stopped if "
            + "no data was provided by the operating system");
        mBeanOperInfo[0] = new MBeanOperationInfo(
            "gatherWifiSignals",
            "Gathers WiFi signal measurements using the external native "
            + "library.",
            signature, "java.lang.String", MBeanOperationInfo.INFO);
        return mBeanOperInfo;
    }

    @Override
    public Object invokeSpecificFunction(String operationName, Object[] params,
            String[] signature) throws ReflectionException {
        if (operationName.equals("gatherWifiSignals")) {
            return gatherWifiSignalsString(Integer
                    .valueOf(params[0].toString()));
        } else {
            throw new ReflectionException(new NoSuchMethodException(
                    operationName), "Invalid operation name: " + operationName);
        }
    }

    /**
     * Utility method for gathering wifi signals.
     * 
     * @param timeout in milli seconds when data gathering should be stopped 
     *        if no data was provided by the operating system.
     *        
     * @return A string representation of the wifi signals. 
     */
    public String gatherWifiSignalsString(int timeout) {
        StringBuilder sb = new StringBuilder();
        AccessPointData[] wifiSignals = gatherWifiSignals(timeout);
        for (AccessPointData apd : wifiSignals) {
            sb.append(apd.toString() + ";");
        }
        return sb.toString();
    }

    @Override
    public void setSpecificAttributes(Attribute attribute)
        throws AttributeNotFoundException {        
    }

}
