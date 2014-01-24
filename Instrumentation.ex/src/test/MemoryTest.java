package test;

import java.util.HashMap;
import java.util.Map;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.configuration
    .IObjectSizeProvider;
import de.uni_hildesheim.sse.monitoring.runtime.recording.ObjectSizeProvider;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;

/**
 * This class aims at evaluating the estimation of object sizes by 
 * {@link ObjectSizeProvider}. Therefore, various classes are defined, 
 * instantiated and compared with the native value delivered by the JVM.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class MemoryTest {

    // checkstyle: stop javadoc check
    
    private static class ByteTest {

        @SuppressWarnings("unused")
        private byte val = 100;
    }
    
    private static class ShortTest {

        @SuppressWarnings("unused")
        private short val = 100;
    }

    private static class CharacterTest {

        @SuppressWarnings("unused")
        private char val = 100;
    }

    private static class BooleanTest {

        @SuppressWarnings("unused")
        private boolean val = true;
    }
    
    private static class IntTest {

        @SuppressWarnings("unused")
        private int val = 100;
    }
    
    private static class FloatTest {

        @SuppressWarnings("unused")
        private float val = 0.25F;
    }
    
    private static class ObjectTest {

        @SuppressWarnings("unused")
        private Object val = null;
    }

    private static class DoubleTest {

        @SuppressWarnings("unused")
        private double val = 0.33F;
    }

    private static class EmptyTest {
    }
    
    @SuppressWarnings("unused")
    private static class MixTest {
        
        private int iVal = 100;
        private float fVal = 0.25F;
    }

    @SuppressWarnings("unused")
    private static class MixTest2 {

        private int iVal = 100;
        private float fVal = 0.25F;
        private Object oVal = null;
    }

    @SuppressWarnings("unused")
    private static class MixTest3 {
    
        private int iVal1 = 100;
        private int iVal2 = 100;
        private int iVal3 = 100;
        private int iVal4 = 100;
        private int iVal5 = 100;
    }

    // checkstyle: resume javadoc check

    /**
     * Prevents this class from being instantiated from outside.
     */
    private MemoryTest() {
    }
    
    /**
     * Performs the test by creating instances and comparing them to the sizes
     * delivered by the JVM.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        IObjectSizeProvider osp = ObjectSizeProvider.getInstance();
        IMemoryDataGatherer mdf = GathererFactory.getMemoryDataGatherer();

        Map<String, Object> tests = new HashMap<String, Object>();
        tests.put("empty", new EmptyTest());
        tests.put("boolean", new BooleanTest());
        tests.put("char", new CharacterTest());
        tests.put("byte", new ByteTest());
        tests.put("short", new ShortTest());
        tests.put("int", new IntTest());
        tests.put("float", new FloatTest());
        tests.put("double", new DoubleTest());
        tests.put("Object", new ObjectTest());
        tests.put("Mix", new MixTest());
        tests.put("Mix2", new MixTest2());
        tests.put("Mix3", new MixTest3());
        
        for (Map.Entry<String, Object> entry : tests.entrySet()) {
            Object o = entry.getValue();
            System.out.println(entry.getKey() + " " + osp.getObjectSize(o) 
                + " " + mdf.getObjectSize(o));
        }
    }
    
}
