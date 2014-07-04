package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.AnnotationBuilder;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Annotations;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.*;

/**
 * Implements the class abstraction for asm.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class AClass extends AType {

    /**
     * Stores whether the code should be checked when producing bytecode.
     */
    private static boolean checkCode = false;

    /**
     * Stores whether the code should be printed before (possibly) checked when 
     * producing bytecode.
     */
    private static boolean printCode = false;

    /**
     * Stores the class pool.
     */
    private static final ArrayList<AClass> CLASS_POOL 
        = new ArrayList<AClass>(3);

    /**
     * Stores the field pool.
     */
    private static final ArrayList<AField> FIELD_POOL 
        = new ArrayList<AField>(1);

    /**
     * Stores the behavior pool.
     */
    private static final ArrayList<ABehavior> BEHAVIOR_POOL 
        = new ArrayList<ABehavior>(1);

    /**
     * Stores the class node represented by this class.
     */
    private ClassNode node;
    
    /**
     * Stores the class loader used to load the represented class.
     */
    private ClassLoader loader;
    
    /**
     * Stores the component type in case of arrays (may be <b>null</b>).
     */
    private AType componentType;
    
    /**
     * Stores the (external) usage count in order to avoid duplicate 
     * allocations.
     */
    private int usageCount;
    
    /**
     * Notifies this instance that one reference has been passed outside
     * this package and that the {@link #release()} shall be deferred.
     * 
     * @since 1.00
     */
    void notifyExternalUsage() {
        usageCount++;
    }
    
    /**
     * Returns an {@link AClass} from the pool.
     * 
     * @param node the class to attach
     * @param loader the loader used to load <code>node</code>, may be 
     *   <b>null</b>
     * @param componentType the component type in case of arrays
     * @param internalName the JVM internal name (only for arrays as they 
     *   cannot be read from resources, then <code>node</code> must point to 
     *   <code>java.lang.Object</code>), <b>null</b> else
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized AClass getClassFromPool(ClassNode node, 
        ClassLoader loader, AType componentType, String internalName) {
        AClass cl;
        int size = CLASS_POOL.size();
        if (size > 0) {
            cl = CLASS_POOL.remove(size - 1);
        } else {
            cl = new AClass();
        }
        cl.attach(node, loader, componentType, internalName);
        return cl;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param cl the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseClass(AClass cl) {
        CLASS_POOL.add(cl);
    }

    /**
     * Returns a {@link AField} from the pool.
     * 
     * @param field the field to attach
     * @param cls the declaring class
     * @param name in case of fixed fields such as arrays
     * @param desc descriptor in case of fixed fields such as array lengths
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized AField getFieldFromPool(FieldNode field, 
        AClass cls, String name, String desc) {
        AField result;
        int size = FIELD_POOL.size();
        if (size > 0) {
            result = FIELD_POOL.remove(size - 1);
        } else {
            result = new AField();
        }
        result.attach(field, cls, name, desc);
        return result;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param field the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseField(AField field) {
        FIELD_POOL.add(field);
    }

    /**
     * Returns a {@link ABehavior} from the pool.
     * 
     * @param behavior the behavior to attach
     * @param cls the declaring class
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized ABehavior getBehaviorFromPool(
        MethodNode behavior, AClass cls) {
        ABehavior result;
        int size = BEHAVIOR_POOL.size();
        if (size > 0) {
            result = BEHAVIOR_POOL.remove(size - 1);
        } else {
            result = new ABehavior();
        }
        result.attach(behavior, cls);
        return result;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param be the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseBehavior(ABehavior be) {
        BEHAVIOR_POOL.add(be);
    }
    
    /**
     * Attaches the given class node.
     * 
     * @param node the class node
     * @param loader the loader used to load <code>node</code> (may be 
     *   <b>null</b>)
     * @param componentType the component type in case of arrays (may 
     *   be <b>null</b>)
     * @param internalName the JVM internal name (only for arrays as they 
     *   cannot be read from resources, then <code>node</code> must point to 
     *   <code>java.lang.Object</code>), <b>null</b> else
     * 
     * @since 1.00
     */
    void attach(ClassNode node, ClassLoader loader, AType componentType, 
        String internalName) {
        this.node = node;
        this.loader = loader;
        this.componentType = componentType;
        if (null == internalName) {
            internalName = node.name;
        } 
        setInternalName(internalName);
    }
    
    /**
     * Returns the number of declared field.
     * 
     * @return the number of declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getDeclaredFieldCount() throws InstrumenterException {
        return node.fields.size() + (componentType != null ? 1 : 0);
    }

    /**
     * Returns the declared field at given <code>index</code>.
     * 
     * @param index the declared field at the given index
     * @return the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IField getDeclaredField(int index) throws InstrumenterException {
        IField result;
        if (null != componentType && index == node.fields.size()) {
            result = getFieldFromPool(null, this, "length", 
                String.valueOf(Factory.INTERNAL_INT));
        } else {
            result = getFieldFromPool(
                (FieldNode) node.fields.get(index), this, null, null);
        }
        return result;
    }

    /**
     * Returns the name of the declared field at given <code>index</code>.
     * 
     * @param index the declared field at the given index
     * @return the name of the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public String getDeclaredFieldName(int index) throws InstrumenterException {
        String result;
        if (null != componentType && index == node.fields.size()) {
            result = "length";
        } else {
            result = node.fields.get(index).name;
        }
        return result;
    }
    
    /**
     * Returns the number of declared behaviors.
     * 
     * @return the number of declared behaviors
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getDeclaredBehaviorCount() throws InstrumenterException {
        return node.methods.size();
    }

    /**
     * Returns the declared behavior at given <code>index</code>.
     * 
     * @param index the declared behavior at the given index
     * @return the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IBehavior getDeclaredBehavior(int index)
        throws InstrumenterException {
        return getBehaviorFromPool((MethodNode) node.methods.get(index), this);
    }

    /**
     * Returns the number of interfaces of this class.
     * 
     * @return the number of interfaces
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getInterfaceCount() throws InstrumenterException {
        return node.interfaces.size();
    }

    /**
     * Returns the interface at the specific <code>index</code>.
     * 
     * @param index the index of the interface
     * @return the interface (to be released by 
     *   {@link #release()}).
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */    
    @Override
    public IClass getInterface(int index) throws InstrumenterException {
        return Factory.getLocalFactory().obtainClass(
            node.interfaces.get(index).toString(), loader);
    }

    /**
     * Returns the superclass of this class. Call 
     * {@link #release()} when not used anymore.
     * 
     * @return the super class of this class (may be <b>null</b>)
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IClass getSuperclass() throws InstrumenterException {
        IClass result;
        String superName = node.superName;
        if (null == superName && !node.name.equals(Factory.JAVA_LANG_OBJECT)) {
            superName = Factory.JAVA_LANG_OBJECT;
        }
        if (null == superName) {
            result = null;
        } else {
            result = Factory.getLocalFactory().obtainClass(superName, loader);
        }
        return result;
    }

    /**
     * Returns the declaring class of this class. Call 
     * {@link #release()} when not used anymore.
     * 
     * @return the declaring class of this class (may be <b>null</b>)
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IClass getDeclaringClass() throws InstrumenterException {
        IClass result = null;
        if (null != node.outerClass) {
            result = Factory.getLocalFactory().obtainClass(
                node.outerClass, loader);
        }
        return result;
    }

    @Override
    public boolean isInterface() {
        return Flags.isSet(node.access, Opcodes.ACC_INTERFACE);
    }
    
    @Override
    public boolean isAbstract() {
        return Flags.isSet(node.access, Opcodes.ACC_ABSTRACT);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotation,
        boolean remove) {
        return (T) getAnnotation(node.visibleAnnotations, 
            annotation, remove, loader);
    }

    @Override
    public byte[] toBytecode() throws InstrumenterException {
        // ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
        ClassWriter writer = new ClassWriter(0);
        if (checkCode) {
            try {
                ClassVisitor cv;
                if (printCode) {
                    cv = new TraceClassVisitor(new PrintWriter(
                        new OutputStreamWriter(System.out))); 
                    node.accept(cv);
                }
                cv = new CheckClassAdapter(writer, true);
                node.accept(cv);        
            } catch (Throwable e) {
                System.err.println("Error while instrumenting " 
                    + getName());
                e.printStackTrace(System.err);
            }
        } else {
            try {
                node.accept(writer);
            } catch (RuntimeException e) {
                throw new InstrumenterException(e);
            }
        }
        return writer.toByteArray();
    }

    /**
     * Returns whether this type is an instance of <code>type</code>.
     * 
     * @param type the type to check for
     * @return <code>true</code> if this type is an instance of 
     *   <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    boolean isInstanceOf(AType type) {
        boolean result = false;
        try {
            if (getInternalName().equals(type.getInternalName())) {
                result = true;
            } else {
                AType tmp = (AType) getSuperclass();
                if (null != tmp) {
                    result = tmp.isInstanceOf(type);
                }
                int size = getInterfaceCount();
                for (int i = 0; !result && i < size; i++) {
                    tmp = (AType) getInterface(i);
                    if (null != tmp) {
                        result = tmp.isInstanceOf(type);
                    }
                }
            }
        } catch (InstrumenterException e) {
            System.out.println("WARNING: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Returns if the given class is instance of the given <code>type</code>, 
     * whereby <code>type</code> may be a superclass.
     * 
     * @param type the type this class should be a subclass of
     * @return <code>true</code> if this class is a subclass of 
     *     <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isInstanceOf(String type) {
        boolean found = false;
        found = getName().equals(type);
        if (!found) {
            try {
                IClass sup = getSuperclass();
                if (null != sup) {
                    found = sup.isInstanceOf(type);
                    sup.release();
                }
            } catch (InstrumenterException e) {
            }
            if (!found) {
                try {
                    int iCount = getInterfaceCount();
                    for (int i = 0; !found && i < iCount; i++) {
                        IClass iface = getInterface(i);
                        if (null != iface) {
                            found = iface.isInstanceOf(type);
                            iface.release();
                        }
                    }
                } catch (InstrumenterException e) {
                }
            }
        }
        return found;
    }

    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        if (!Factory.getLocalFactory().isCached(this)) {
            if (usageCount > 0) {
                usageCount--;
            } else {
                super.release();
                node = null;
                componentType = null;
                if (null != loader) {
                    Factory.getLocalFactory().removeClassLoader(loader);
                    loader = null;
                }
                releaseClass(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBehavior findSignature(IBehavior member)
        throws InstrumenterException {
        IBehavior found = null;
        ABehavior bMember = (ABehavior) member;
        int count = getDeclaredBehaviorCount();
        for (int i  = 0; null == found && i < count; i++) {
            ABehavior behav = (ABehavior) getDeclaredBehavior(i);
            if (member.getName().equals(behav.getName()) 
                && bMember.checkParameter(behav)) {
                found = behav;
            }
            if (null == found) {
                behav.release();
            }
        }
        return found;
    }
        
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined in <code>annotations</code>.
     * 
     * @param <T> the type of the annotation
     * @param annotations the annotations to search for
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @param loader the class loader to be used for resolving values
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    static final <T extends Annotation> T getAnnotation(
        List<AnnotationNode> annotations, Class<T> annotation,
        boolean remove, ClassLoader loader) {
        T result = null;
        AnnotationNode annNode = null;
        if (null != annotations) {
            String internal = Type.getDescriptor(annotation);
            int len = annotations.size();
            for (int i = 0; null == annNode && i < len; i++) {
                AnnotationNode ann = annotations.get(i);
                if (ann.desc.equals(internal)) {
                    annNode = ann;
                    if (remove) {
                        annotations.remove(i);
                    }
                }
            }
        }
        if (null != annNode) {
            AnnotationBuilder<?> builder = Annotations.getTemplate(
                annotation.getName());
            if (null != builder) {
                builder = builder.prepareForUse();
                if (null != annNode.values) {
                    int vLen = annNode.values.size();
                    for (int v = 0; v < vLen; v += 2) {
                        Object val = annNode.values.get(v + 1);
                        if (val instanceof String[]) {
                            val = getAnnotationValueInstance((String[]) val, 
                                loader);
                        }
                        builder.setData(annNode.values.get(v).toString(), 
                            val);
                    }
                }
                result = annotation.cast(builder.create());
            } else {
                HashMap<String, Object> values 
                    = new HashMap<String, Object>();
                if (null != annNode.values) {
                    int vLen = annNode.values.size();
                    for (int v = 0; v < vLen; v += 2) {
                        values.put(annNode.values.get(v).toString(), 
                            annNode.values.get(v + 1)); 
                    }
                }
                Method[] methods = annotation.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    values.put(methods[i].getName(), 
                        methods[i].getDefaultValue());
                }
                result = AnnotationInstanceProvider.INSTANCE
                    .get(annotation, values);
            }
        }
        return result;
    }
    
    /**
     * Returns an annotation value from a bytecode string array, i.e. this
     * may start with a type descriptor as first entry, contain 1 or further 
     * values (array).
     * 
     * @param array the array to parse
     * @param loader the class loader to use
     * @return the value or array
     * 
     * @since 1.00
     */
    private static Object getAnnotationValueInstance(String[] array, 
        ClassLoader loader) {
        Object result = null;
        try {
            if (array.length > 1) {
                String type = array[0];
                if (type.length() == 1) {
                    // primitive
                    switch (type.charAt(0)) {
                    case Factory.INTERNAL_BOOLEAN:
                        if (2 == array.length) {
                            result = Boolean.valueOf(array[1]);
                        } else {
                            boolean[] res = new boolean[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = Boolean.parseBoolean(array[i + 1]);
                            }
                        }
                        break;
                    case Factory.INTERNAL_BYTE:
                        if (2 == array.length) {
                            result = Byte.valueOf(array[1]);
                        } else {
                            byte[] res = new byte[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = Byte.parseByte(array[i + 1]);
                            }
                        }
                        break;
                    case Factory.INTERNAL_CHAR:
                        if (2 == array.length) {
                            result = array[1].charAt(0);
                        } else {
                            char[] res = new char[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = array[i + 1].charAt(0);
                            }
                        }
                        break;
                    case Factory.INTERNAL_DOUBLE:
                        if (2 == array.length) {
                            result = Double.valueOf(array[1]);
                        } else {
                            double[] res = new double[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = Double.parseDouble(array[i + 1]);
                            }
                        }
                        break;
                    case Factory.INTERNAL_FLOAT:
                        if (2 == array.length) {
                            result = Float.valueOf(array[1]);
                        } else {
                            float[] res = new float[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = Float.parseFloat(array[i + 1]);
                            }
                        }
                        break;
                    case Factory.INTERNAL_INT:
                        if (2 == array.length) {
                            result = Integer.valueOf(array[1]);
                        } else {
                            int[] res = new int[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = Integer.parseInt(array[i + 1]);
                            }
                        }
                        break;
                    case Factory.INTERNAL_LONG:
                        if (2 == array.length) {
                            result = Long.valueOf(array[1]);
                        } else {
                            long[] res = new long[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = Long.parseLong(array[i + 1]);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                } else if (type.startsWith("L") && type.endsWith(";")) {
                    type = type.substring(1, type.length() - 1)
                        .replace('/', '.');
                    Class<?> cls;
                    if (null != loader) {
                        cls = loader.loadClass(type);
                    } else {
                        cls = Class.forName(type);
                    }
                    if (cls.isEnum()) {
                        if (2 == array.length) {
                            result = retrieveEnumValue(cls, array[1]);
                        } else {
                            Object[] res = new Object[array.length - 1];
                            for (int i = 0; i < res.length; i++) {
                                res[i] = retrieveEnumValue(cls, array[i + 1]);
                            }
                        }
                    } else {
                        System.err.println("type is no enum: " 
                            + type);
                    }
                }
            } 
            if (null == result) {
                // fallback
                result = array;
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
        return result;
    }
    
    /**
     * Retrieves an enum value from the given class.
     * 
     * @param cls the class to take as basis
     * @param name the name of the enum value
     * @return the enum object
     * 
     * @since 1.00
     */
    private static Object retrieveEnumValue(Class<?> cls, String name) {
        Object result = null;
        try {
            result = cls.getField(name).get(null);
        } catch (NoSuchFieldException e) {
            System.err.println("enum " + cls.getName() 
                + " does not have a value " + name);
        } catch (IllegalAccessException e) {
            System.err.println("enum value " + name + " in " 
                + cls.getName() + " cannot be accessed" + e.getMessage());
        }
        return result;
    }
    
    /**
     * Returns the class loader used for loading this class.
     * 
     * @return may be <b>null</b> if the system class loader should be used
     * 
     * @since 1.00
     */
    ClassLoader getClassLoader() {
        return loader;
    }

    /**
     * Returns whether this type is primitive.
     * 
     * @return <code>true</code> if this type is primitive, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    /**
     * Returns the class node attached to this type.
     * 
     * @return the class node (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public ClassNode getNode() {
        return node;
    }
    
    /**
     * Returns the component type in case of arrays.
     * 
     * @return the component type (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public AType getComponentType() {
        return componentType;
    }
    
    /**
     * Changes the name of this class. (inofficial for testing).
     * 
     * @param name the new name of this class
     * 
     * @since 1.00
     */
    public void setName(String name) {
        node.name = name;
    }
    
    /**
     * Controls whether generated code should be checked.
     * 
     * @param check whether generated code should be checked
     * 
     * @since 1.00
     */
    public static void setCheckCode(boolean check) {
        checkCode = check;
    }

    /**
     * Controls whether generated code should be printed before being checked.
     * @param print whether generated code should be printed
     * 
     * @since 1.00
     */
    public static void setPrintCode(boolean print) {
        printCode = print;
    }

}
