package test;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IProcessDataGatherer;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * A simple process monitor that samples value for a given process 
 * identification and stops when the process ends.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ProcessMonitor {

    /**
     * Stores the number of samples.
     */
    private static int count;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * 
     * @since 1.00
     */
    private ProcessMonitor() {
    }
        
    /**
     * Defines a class maintaining minimum, average and maximum values.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class MinMaxAvg {
        
        /**
         * Stores the minimum value.
         */
        private double min = Double.MAX_VALUE;

        /**
         * Stores the maximum value.
         */
        private double max = Double.MIN_VALUE;
        
        /**
         * Stores the average value.
         */
        private double avg = 0;
        
        /**
         * Adds a value, i.e. considers it for minimum, maximum and average.
         * 
         * @param val the new value
         * 
         * @since 1.00
         */
        public void addValue(double val) {
            min = Math.min(min, val);
            max = Math.max(max, val);
            avg += val;
        }
        
        /**
         * Returns the minimum value.
         * 
         * @return the minimum value
         * 
         * @since 1.00
         */
        public double getMin() {
            return min;
        }

        /**
         * Returns the maximum value.
         * 
         * @return the maximum value
         * 
         * @since 1.00
         */
        public double getMax() {
            return max;
        }

        /**
         * Returns the average value.
         * 
         * @return the average value
         * 
         * @since 1.00
         */
        public double getAvg() {
            if (0 == count) {
                return 0;
            } else {
                return avg / count;
            }
        }
        
        /**
         * Returns formatted output on minimum, average and maximum.
         * 
         * @return the formatted output
         */
        public String toString() {
            return String.format("%.2f\t%.2f\t%.2f", 
                getMin(), getAvg(), getMax());
        }
        
    }
    
    /**
     * The timer task.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class Task extends TimerTask {

        /**
         * Stores the memory data gatherer instance.
         */
        private IMemoryDataGatherer memDg 
            = GathererFactory.getMemoryDataGatherer();

        /**
         * Stores the processor data gatherer instance.
         */
        private IProcessorDataGatherer cpuDg 
            = GathererFactory.getProcessorDataGatherer();
        
        /**
         * Stores the process data gatherer instance.
         */
        private IProcessDataGatherer pDg 
            = GathererFactory.getProcessDataGatherer();
        
        /**
         * Stores minimum, average and maximum for the system load.
         */
        private MinMaxAvg sysLoad = new MinMaxAvg();

        /**
         * Stores minimum, average and maximum for the system memory usage.
         */
        private MinMaxAvg sysMem = new MinMaxAvg();
        
        /**
         * Stores minimum, average and maximum for the process load.
         */
        private MinMaxAvg procLoad = new MinMaxAvg();

        /**
         * Stores minimum, average and maximum for the process memory usage.
         */
        private MinMaxAvg procMem = new MinMaxAvg();

        /**
         * Stores the process identification to sample for.
         */
        private int pid;
        
        /**
         * Creates a new task to sample for the given process identification.
         * 
         * @param pid the process identification
         * 
         * @since 1.00
         */
        private Task(int pid) {
            this.pid = pid;
        }
        
        /**
         * Executes the task.
         */
        @Override
        public void run() {
            if (!pDg.isProcessAlive(pid)) {
                System.out.println(pid + " is dead!");
                cancel();
            } else {
                sysLoad.addValue(cpuDg.getCurrentSystemLoad());
                sysMem.addValue(memDg.getCurrentMemoryUse());
                procLoad.addValue(pDg.getProcessProcessorLoad(pid));
                procMem.addValue(pDg.getProcessMemoryUse(pid));
                count++;
                if (count % 10 == 0) {
                    System.out.println(
                        "sysload\t" + sysLoad + "\tsysmem\t" + sysMem
                        + "\tprocload\t" + procLoad + "\tprocMem\t" + procMem);
                }
            }
        }
        
    }

    /**
     * Executes the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.print("Process identifier: ");
        int pid = s.nextInt();
        Timer timer = new Timer();
        timer.schedule(new Task(pid), 0, 500);
        System.out.println("monitoring " + pid);
    }
}
