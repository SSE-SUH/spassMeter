package test;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Implements a test class which calls all relevant (native) methods.
 * 
 * @author eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Test {

    /**
     * Stores the timer instance.
     */
    private static Timer timer;
    
    /**
     * Stores the maximum number of runs for the test task.
     */
    private static int maxNetCount = -1;
    
    /**
     * A repetitive test task.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class TestTask extends TimerTask {

        /**
         * Executes the task.
         */
        @Override
        public void run() {
            double cur = GathererFactory.
                getNetworkDataGatherer().getCurrentNetSpeed();
            System.out.println("cur net speed " + cur);
            double max = GathererFactory.
                getNetworkDataGatherer().getMaxNetSpeed();
            System.out.println("max net speed " 
                + max);
            System.out.println("net utilization " 
                + (max > 0 ? (cur / (double) max * 100) : 0.0));
            System.out.println("Thread CPU time " 
                + GathererFactory.getThreadDataGatherer().getCpuTime(
                    Thread.currentThread().getId()));
            System.out.println();
            if (maxNetCount > 0) {
                maxNetCount--;
            } else if (0 == maxNetCount) {
                cancel();
                timer.purge();
                timer.cancel();
            }
        }
        
    }
    
    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private Test() {
    }

    /**
     * Executes the tests.
     * 
     * @param args the first param may be the number of repeated tests (network
     *   utilization)
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                maxNetCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Param is no number (ignored).");
            }
        }
        System.out.println("Factory: " + GathererFactory.getInstanceName());
        System.out.println("Environment: " + System.getProperties());
        System.out.println("initial process load");
        
        GathererFactory.getThisProcessDataGatherer()
            .getCurrentProcessProcessorLoad();
        System.out.println("gathering data ...");
        //AccessPointData[] aps = GathererFactory.
//            getDataGatherer().gatherWifiSignals(1000);
        
  //      for (AccessPointData ap : aps) {
  //          System.out.println(ap);
  //      }
        
        System.out.println();
        
        IThisProcessDataGatherer pdg = 
            GathererFactory.getThisProcessDataGatherer();
        IoStatistics io = pdg.getCurrentProcessIo();
        if (null != io) {
            System.out.println("JVM read   " + io.read);
            System.out.println("JVM write  " + io.write);
        } else {
            System.out.println("JVM read/write N/A");
        }
        System.out.println("JVM user   " 
            + pdg.getCurrentProcessUserTimeTicks());
        System.out.println("JVM kernel " 
            + pdg.getCurrentProcessKernelTimeTicks());
        System.out.println("JVM mem    " 
            + pdg.getCurrentProcessMemoryUse());
        System.out.println("JVM system " 
            + pdg.getCurrentProcessSystemTimeTicks());
        System.out.println();
        
        System.out.println("battery life percent " 
            + GathererFactory.getBatteryDataGatherer().getBatteryLifePercent());
        System.out.println("battery life time " 
            + GathererFactory.getBatteryDataGatherer().getBatteryLifeTime());
        System.out.println("battery power plug " 
            + GathererFactory.getBatteryDataGatherer().getPowerPlugStatus());
        System.out.println("battery present? " 
            + GathererFactory.getBatteryDataGatherer().hasSystemBattery());
        System.out.println();

        System.out.println("cur mem use " 
            + GathererFactory.getMemoryDataGatherer().getCurrentMemoryUse());
        System.out.println("cur mem avail " 
            + GathererFactory.getMemoryDataGatherer().getCurrentMemoryAvail());
        System.out.println("mem capacity " 
            + GathererFactory.getMemoryDataGatherer().getMemoryCapacity());
        Object object = new Object();
        System.out.println("object size " + GathererFactory.
            getMemoryDataGatherer().getObjectSize(object));
        System.out.println();
        System.out.println("JVMTI support: " 
            + GathererFactory.getDataGatherer().supportsJVMTI()); 

        System.out.println("cur net speed " 
            + GathererFactory.getNetworkDataGatherer().getCurrentNetSpeed());
        System.out.println("max net speed " 
            + GathererFactory.getNetworkDataGatherer().getMaxNetSpeed());
        System.out.println();

        System.out.println("cur processor speed " + GathererFactory.
            getProcessorDataGatherer().getCurrentProcessorSpeed());
        System.out.println("sys load " + GathererFactory.
            getProcessorDataGatherer().getCurrentSystemLoad());
        System.out.println("max processor speed " + GathererFactory.
            getProcessorDataGatherer().getMaxProcessorSpeed());
        System.out.println("processor number " + GathererFactory.
            getProcessorDataGatherer().getNumberOfProcessors());
        System.out.println();

        System.out.println("screen resolution " 
            + GathererFactory.getScreenDataGatherer().getScreenResolution());
        System.out.println("screen resolution " 
            + GathererFactory.getScreenDataGatherer().getScreenWidth());
        System.out.println("screen resolution " 
            + GathererFactory.getScreenDataGatherer().getScreenHeight());
        System.out.println();

        System.out.println("volume use " 
            + GathererFactory.getVolumeDataGatherer().getCurrentVolumeUse());
        System.out.println("volume avail " 
            + GathererFactory.getVolumeDataGatherer().getCurrentVolumeAvail());
        System.out.println("volume capacity " 
            + GathererFactory.getVolumeDataGatherer().getVolumeCapacity());
        System.out.println();

        System.out.println("JVM kernel time " 
            + pdg.getCurrentProcessKernelTimeTicks());
        System.out.println("JVM mem " 
            + pdg.getCurrentProcessMemoryUse());
        System.out.println("JVM load " 
            + pdg.getCurrentProcessProcessorLoad());
        System.out.println("JVM sys time " 
            + pdg.getCurrentProcessSystemTimeTicks());
        System.out.println("JVM sys time " 
            + pdg.getCurrentProcessUserTimeTicks());
        IoStatistics stat = pdg.getCurrentProcessIo();
        if (null != stat) {
            System.out.println("VM in " + stat.read + " out " + stat.write 
                + " incl file " + pdg.isFileIoDataIncluded(false)
                + " incl net " + pdg.isNetworkIoDataIncluded(false));
        } else {
            System.out.println("VM I/O N/A");
        }
        stat = GathererFactory.getThisProcessDataGatherer().getAllProcessesIo();
        if (null != stat) {
            System.out.println("all in " + stat.read + " out " + stat.write
                + " incl file " + pdg.isFileIoDataIncluded(true)
                + " incl net " + pdg.isNetworkIoDataIncluded(true));
        } else {
            System.out.println("all I/O N/A");
        }
        
        System.out.println(Arrays.toString(
            GathererFactory.getThreadDataGatherer().getAllThreadIds()));
        
        if (maxNetCount > 0) {
            timer = new Timer();
            timer.schedule(new TestTask(), 0, 1200);
        }
    }

}
