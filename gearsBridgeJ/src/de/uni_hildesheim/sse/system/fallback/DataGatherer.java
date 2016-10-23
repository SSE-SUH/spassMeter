package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AccessPointData;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IDataGatherer;

/**
 * The central class for gathering system specific information.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = {AnnotationConstants.VAR_GATHER_DATA, 
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)    
class DataGatherer implements IDataGatherer {

    @Override
    @Variability(id = AnnotationConstants.VAR_WIFI_DATA)
    public AccessPointData[] gatherWifiSignals(int timeout) {
        return new AccessPointData[0];
    }
    
    @Override
    public boolean supportsJVMTI() {
        return false;
    }
   
    @Override
    public void registerThisThread(boolean register) {
    }
    
    @Override
    public boolean needsThreadRegistration() {
        return false;
    }

    @Override
    public int redefineMultiClasses(Class<?>[] classes, byte[][] bytecode) {
        return -1;
    }

    @Override
    public int redefineClass(Class<?> clazz, byte[] bytecode) {
        return -1;
    }

    @Override
    public int redefineClasses(Class<?>[] classes, byte[][] bytecode) {
        return -1;
    }

}
