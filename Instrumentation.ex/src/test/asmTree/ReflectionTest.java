package test.asmTree;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.*;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Tests the asm class reflection integration. Annotations are included for 
 * testing. The interface is attached for testing only.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@SuppressWarnings("serial")
@Monitor
public class ReflectionTest implements Serializable {

    /**
     * Just for testing.
     */
    @ValueChange(id = "me")
    public static final int CONSTANT = 0;
    
    /**
     * Just for testing.
     */
    public static final String[][] TESTARRAY = null;
    
    /**
     * Prevents this class from being initialized from outside.
     * 
     * @since 1.00
     */
    private ReflectionTest() {
    }
    
    /**
     * Executes the test.
     * 
     * @param args command line arguments, ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        Factory factory = Factory.getLocalFactory();
        try {
            IClass cls = factory.obtainClass(
                Factory.toInternalName(ReflectionTest.class.getName()), 
                ReflectionTest.class.getClassLoader());
            System.out.println("is object " 
                + cls.isInstanceOf("java.lang.Object"));
            System.out.println("is reflectionTest " 
                + cls.isInstanceOf(ReflectionTest.class.getName()));
            System.out.println("is Serializable " 
                + cls.isInstanceOf(Serializable.class.getName()));
            System.out.println("is String[] " 
                + cls.isInstanceOf("java.lang.String[]"));
            printClass(cls, "", new HashSet<IClass>());
            cls = factory.obtainClass(
                Factory.toInternalName("java.lang.String[]"), 
                ReflectionTest.class.getClassLoader());
            printClass(cls, "", new HashSet<IClass>());
            
        } catch (InstrumenterException e) {
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * Prints <code>cls</code> and recursively classes and interfaces 
     * <code>cls</code> depends on.
     * 
     * @param cls the class to be printed
     * @param prefix the line prefix (start with an empty string)
     * @param done list of currently visited classes (prevent endless loops)
     * @throws InstrumenterException in case that anything goes wrong
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    private static void printClass(IClass cls, String prefix, Set<IClass> done) 
        throws InstrumenterException {
        if (!done.contains(cls)) {
            done.add(cls);
            Annotation annot = cls.getAnnotation(Monitor.class, false);
            if (null != annot) {
                System.out.print(prefix);
                System.out.println(annot);
            }
            System.out.print(prefix);
            if (cls.isInterface()) {
                System.out.print("interface");
            } else {
                System.out.print("class");
            }
            System.out.print(" ");
            System.out.print(cls.getName());
            IClass superClass = cls.getSuperclass();
            if (null != superClass) {
                System.out.print(" extends ");
                System.out.print(superClass.getName());
            }
            int ifCount = cls.getInterfaceCount();
            IClass[] interfaces = new IClass[ifCount];
            if (ifCount > 0) {
                System.out.print(" implements ");
                for (int i = 0; i < ifCount; i++) {
                    interfaces[i] = cls.getInterface(i);
                    System.out.print(interfaces[i].getName());
                    if (i < ifCount - 1) {
                        System.out.print(", ");
                    }
                }
            }
            System.out.println(" {");
            String localPrefix = prefix + "  ";
            int fieldCount = cls.getDeclaredFieldCount();
            for (int f = 0; f < fieldCount; f++) {
                IField field = cls.getDeclaredField(f);
                annot = field.getAnnotation(ValueChange.class, false);
                if (null != annot) {
                    System.out.print(localPrefix);
                    System.out.println(annot);
                }
                System.out.print(localPrefix);
                System.out.print(field.getTypeName());
                System.out.print(" ");
                System.out.println(field.getName());
                field.release();
            }
            int behavCount = cls.getDeclaredBehaviorCount();
            for (int b = 0; b < behavCount; b++) {
                IBehavior behav = cls.getDeclaredBehavior(b);
                annot = behav.getAnnotation(ExcludeFromMonitoring.class, false);
                if (null != annot) {
                    System.out.print(localPrefix);
                    System.out.println(annot);
                }
                annot = behav.getAnnotation(Monitor.class, false);
                if (null != annot) {
                    System.out.print(localPrefix);
                    System.out.println(annot);
                }
                System.out.print(localPrefix);
                System.out.print(behav.getName());
                System.out.print("(");
                int pCount = behav.getParameterCount();
                for (int p = 0; p < pCount; p++) {
                    System.out.print(behav.getParameterTypeName(p));
                    if (p < pCount - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println(")");
                behav.release();
            }
            
            System.out.print(prefix);
            System.out.println("}");
            
            if (null != superClass) {
                System.out.println("SUPERCLASS");
                printClass(superClass, localPrefix, done);
                superClass.release();
            }
            if (ifCount > 0) {
                System.out.println("INTERFACES");
                for (int i = 0; i < ifCount; i++) {
                    printClass(interfaces[i], localPrefix, done);
                    interfaces[i].release();
                }
            }
            done.remove(cls);
        }
    }
    
}
