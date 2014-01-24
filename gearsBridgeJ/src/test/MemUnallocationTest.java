package test;

import java.util.HashMap;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;

/**
 * Tests the memory unallocation recording functionality (intended for 
 * SPASS-meter).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class MemUnallocationTest implements IMemoryUnallocationReceiver {

    /**
     * Stores the expected results for one subtest. Cleared automatically
     * while receiving unallocations.
     * 
     * @see #addExpected(String, long)
     */
    private HashMap<Integer, Long> expected = new HashMap<Integer, Long>();
    
    /**
     * Stores whether all expected values are met so far.
     */
    private boolean ok = true;
    
    /**
     * Executes the tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        
        MemUnallocationTest rec = new MemUnallocationTest();
        IMemoryDataGatherer mdg = GathererFactory.getMemoryDataGatherer();
        long tag = 1234;
        long tag1 = 1235;
        
        final int myRec = 1;
        final int myRec1 = 2;
        
        mdg.recordUnallocationByTag(tag, 500, myRec);
        mdg.recordUnallocationByTag(tag, 500, myRec);
        mdg.recordUnallocationByTag(tag);
        mdg.recordUnallocationByTag(tag1, 500, myRec);
        mdg.recordUnallocationByTag(tag1);
        rec.addExpected(myRec, 1500);
        mdg.receiveUnallocations(rec);

        mdg.recordUnallocationByTag(tag, 500, myRec1);
        mdg.recordUnallocationByTag(tag1, 500, myRec);
        mdg.recordUnallocationByTag(tag);
        mdg.recordUnallocationByTag(tag1);
        rec.addExpected(myRec1, 500);
        rec.addExpected(myRec, 500);
        mdg.receiveUnallocations(rec);
        
        if (rec.isOk()) {
            System.out.println("done");
        } else {
            System.out.println("problem " + rec.ok + " " + rec.expected);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unallocated(int recId, long size) {
        Long exSize = expected.remove(recId);
        if (null == exSize) {
            System.err.println("recId '" + recId + "' unexpected");
            ok = false;
        } else {
            if (exSize.longValue() != size) {
                System.err.println("expected size '" + exSize 
                    + " does not match '" + size + " for recId 'recId '" 
                    + recId + "'");
                ok = false;
            } else {
                ok |= true;
            }
        }
    }
    
    /**
     * Adds an expected result, i.e. a given <code>size</code> for a specified 
     * <code>recId</code>.
     * 
     * @param recId the recording identifier
     * @param size the expected size for <code>recId</code>
     * 
     * @since 1.00
     */
    private void addExpected(int recId, long size) {
        expected.put(recId, size);
    }
    
    /**
     * Returns whether all tests so far were successful and also the
     * {@link #expected} is empty.
     * 
     * @return <code>true</code> if successful, <code>false</code> else
     * 
     * @since 1.00
     */
    private boolean isOk() {
        return ok && expected.isEmpty();
    }
}
