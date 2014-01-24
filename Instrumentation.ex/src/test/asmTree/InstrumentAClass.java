package test.asmTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.ValueChange;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.MethodEditor;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.*;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.preprocess.OnTheFlyProcessor;

/**
 * Instruments a given class by executing the asm transformations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InstrumentAClass {

    /**
     * Instrument the class internals for testing only or the class based
     * on its annotations.
     */
    private static final boolean CLASS_INTERNALS_ONLY = false;
    
    /**
     * Controls whether the resulting bytecode should be printed for debugging.
     */
    private static final boolean PRINT_CODE = true;
    
    /**
     * Prevents this class from being instantiated from outside.
    * 
     * @since 1.00
     */
    private InstrumentAClass() {
    }
    
    /**
     * Executes the test.
     * 
     * @param args may be empty then <code>test.asmTree.Test</code> is 
     *   transformed to <code>test.asmTree.TestGeneratedAsm</code> in 
     *   <code>bin</code>, if one argument is given then the denoted class 
     *   (given as file name) is transformed to the class given above, if 
     *   two arguments are given, the first one denotes the class to be 
     *   transformed, the second the out file location (and implicitly the fqn) 
     *   of the output class 
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        System.setProperty("spass-meter.iFactory", de.uni_hildesheim.
            sse.monitoring.runtime.instrumentation.asmTree.
            Factory.class.getName());
        Configuration.INSTANCE.setStaticInstrumentation(true);
        String clsName;
        File outFile;
        if (0 == args.length) {
            if (CLASS_INTERNALS_ONLY) {
                clsName = "test.asmTree.ClassInternalTest";
            } else {
                clsName = "test.asmTree.ClassAnnotationTest";
            }
        } else {
            clsName = args[0];
        }
        String outPath;
        if (args.length > 1) {
            outPath = args[1].replace('\\', '/'); 
            // irrelevant for OS but now prepared for internal JVM format
            outFile = new File(args[1]);
        } else {
            outPath = "test/asmTree/TestGeneratedAsm.class"; // leave as /
            outFile = new File("bin"); // as base directory
            outFile = new File(outFile, outPath);
        }
        String outClassName = outPath;
        int pos = outPath.lastIndexOf('.');
        if (pos > 0) {
            outClassName = outClassName.substring(0, pos);
        }
        
        Factory factory = Factory.getLocalFactory();
        try {
            String inClassName = Factory.toInternalName(clsName);
            AClass.setCheckCode(true);
            AClass.setPrintCode(PRINT_CODE);

            AClass cls = (AClass) factory.obtainClass(
                inClassName, 
                ReflectionTest.class.getClassLoader());
            instrument(cls);
            
            // rename class
            cls.setName(outClassName);
            int behavCount = cls.getDeclaredBehaviorCount();
            for (int b = 0; b < behavCount; b++) {
                ABehavior behav = (ABehavior) cls.getDeclaredBehavior(b);

                // rename references
                MethodNode mn = behav.getNode();
                ListIterator<AbstractInsnNode> i = mn.instructions.iterator();
                while (i.hasNext()) {
                    AbstractInsnNode instruction = i.next();
                    int opcode = instruction.getOpcode();
                    int type = instruction.getType();
                    if (Opcodes.INVOKESTATIC == opcode) {
                        MethodInsnNode miNode = (MethodInsnNode) instruction;
                        if (miNode.owner.equals(inClassName)) {
                            miNode.owner = outClassName;
                        }
                    } else if (Opcodes.LDC == opcode) {
                        LdcInsnNode lNode = (LdcInsnNode) instruction;
                        if (lNode.cst instanceof String) {
                            if (inClassName.equals(lNode.cst)) {
                                lNode.cst = outClassName;
                            }
                        }
                    } else if (AbstractInsnNode.FIELD_INSN == type) {
                        FieldInsnNode fNode = (FieldInsnNode) instruction;
                        if (fNode.owner.equals(inClassName)) {
                            fNode.owner = outClassName;
                        }
                    }
                }
                behav.release();
            }
                        
            FileOutputStream out = new FileOutputStream(outFile);
            out.write(cls.toBytecode());
            out.close();
            System.out.println("instrumented " + clsName);
        } catch (InstrumenterException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * Simulates the instrumentation of the given class for testing.
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException in case that anything goes wrong while
     *   instrumenting
     * 
     * @since 1.00
     */
    static void instrument(IClass cls) throws InstrumenterException {
        if (CLASS_INTERNALS_ONLY) {
            // register value fields (emulate full instrumentation process)
            int fieldCount = cls.getDeclaredFieldCount();
            for (int f = 0; f < fieldCount; f++) {
                IField field = cls.getDeclaredField(f);
                ValueChange vc = field.getAnnotation(ValueChange.class, false);
                if (vc != null) {
                    MethodEditor.put(field, vc.id());
                }
                field.release();
            }
            
            // instrument methods
            int behavCount = cls.getDeclaredBehaviorCount();
            for (int b = 0; b < behavCount; b++) {
                ABehavior behav = (ABehavior) cls.getDeclaredBehavior(b);
                // aim at instrumenting all for tests
                MethodEditor editor 
                    = MethodEditor.getFromPool(null, true, null, false);
                behav.instrument(editor);
                MethodEditor.release(editor);
                behav.release();
            }
        } else {
            // set null to real arguments to test modifications to 
            // Configuration class
            OnTheFlyProcessor otfp = new OnTheFlyProcessor(null);
            otfp.process(cls);
        }
    }
    
}
