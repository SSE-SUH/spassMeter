package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AccessPointData;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
public class Data implements DataMBean {

    /**
     * Instance of {@link IDataGatherer} for gathering required informations.
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
    private IDataGatherer dataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
    public Data() {
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

}
