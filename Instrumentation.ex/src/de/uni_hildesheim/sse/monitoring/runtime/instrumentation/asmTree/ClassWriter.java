package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import org.objectweb.asm.ClassReader;

/**
 * Overrides the original ASM class writer in order to correct class resolution
 * problems.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ClassWriter extends org.objectweb.asm.ClassWriter {

    /**
     * Constructs a new {@link ClassWriter} object.
     *
     * @param flags option flags that can be used to modify the default behavior
     *        of this class. See {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
     */
    public ClassWriter(final int flags) {
        super(flags);
    }
    
    /**
     * Constructs a new {@link ClassWriter} object and enables optimizations for
     * "mostly add" bytecode transformations. These optimizations are the
     * following:
     *
     * <ul> <li>The constant pool from the original class is copied as is in the
     * new class, which saves time. New constant pool entries will be added at
     * the end if necessary, but unused constant pool entries <i>won't be
     * removed</i>.</li> <li>Methods that are not transformed are copied as is
     * in the new class, directly from the original class bytecode (i.e. without
     * emitting visit events for all the method instructions), which saves a
     * <i>lot</i> of time. Untransformed methods are detected by the fact that
     * the {@link ClassReader} receives {@link org.objectweb.asm.MethodVisitor} 
     * objects that come from a {@link ClassWriter} (and not from any 
     * other {@link org.objectweb.asm.ClassVisitor} instance).</li> </ul>
     *
     * @param classReader the {@link ClassReader} used to read the original
     *        class. It will be used to copy the entire constant pool from the
     *        original class and also to copy other fragments of original
     *        bytecode where applicable.
     * @param flags option flags that can be used to modify the default behavior
     *        of this class. <i>These option flags do not affect methods that
     *        are copied as is in the new class. This means that the maximum
     *        stack size nor the stack frames will be computed for these
     *        methods</i>. See {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
     */
    public ClassWriter(final ClassReader classReader, final int flags) {
        super(classReader, flags);
    }
    
    /**
     * Returns the common super type of the two given types. The default
     * implementation of this method <i>loads</i> the two given classes and uses
     * the java.lang.Class methods to find the common super class. 
     *
     * @param type1 the internal name of a class.
     * @param type2 the internal name of another class.
     * @return the internal name of the common super class of the two given
     *         classes.
     */
    @Override
    protected String getCommonSuperClass(final String type1, 
        final String type2) {
        return Factory.getLocalFactory().getCommonSuperclass(type1, type2);
    }

}
