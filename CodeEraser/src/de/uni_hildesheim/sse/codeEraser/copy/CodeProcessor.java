package de.uni_hildesheim.sse.codeEraser.copy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;
import de.uni_hildesheim.sse.codeEraser.util.ClassPool;
import de.uni_hildesheim.sse.codeEraser.util.OnTheFlyJarProcessor;

/**
 * Implements a code processor for replacing classes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CodeProcessor extends OnTheFlyJarProcessor {

    /**
     * Stores the configuration read from the ANT file or the command line.
     */
    private Configuration config;

    /**
     * Creates a code processor for replacing class names.
     * 
     * @param config the configuration
     * 
     * @since 1.00
     */
    public CodeProcessor(Configuration config) {
        this.config = config;
        this.config.validate();
        
        javassist.ClassPool.doPruning = true;
        
        // needed, otherways javassist will not find the annotations
        ClassPool.addClassLoader(CodeProcessor.class.getClassLoader());
    }
    
    /**
     * Adds the type represented by <code>cls</code> to <code>inner</code> in
     * case that the (outer class name) <code>prefix</code> maps to 
     * <code>cls</code>. The remainder of the class name is prepended to 
     * <code>targetClassName</code> including a "$".
     * 
     * @param prefix the prefix representing the outer class name
     * @param cls the class representing the type
     * @param targetClassName the target class name of the outer class 
     *     (excluding the trailing "$")
     * @param inner a map containing original and target class names for
     *   inner classes (to be modified as a side effect in case if the 
     *   <code>prefix</code> matches)
     * 
     * @since 1.00
     */
    private void addTypeIf(String prefix, CtClass cls, String targetClassName, 
        Map<String, String> inner) {
        if (cls.isArray()) {
            try {
                addTypeIf(prefix, cls.getComponentType(), targetClassName, 
                    inner);
            } catch (NotFoundException e) {
            }
        } else {
            String type = cls.getName();
            if (type.startsWith(prefix)) {
                String suffix = type.substring(prefix.length());
                inner.put(type, targetClassName + "$" + suffix);
            }
        }
    }
    
    /**
     * Adds a class to the set of classes to be processed.
     * 
     * @param originalClassName the original name of the class used for 
     *   loading it
     * @param targetClassName the target output name
     * @param inner a map containing original and target class names for
     *   inner classes (to be modified as a side effect)
     * @throws NotFoundException in case that a class cannot be found
     * 
     * @since 1.00
     */
    private void addClass(String originalClassName, String targetClassName, 
        Map<String, String> inner) throws NotFoundException {
        String targetFileName = classNameToJarFileName(targetClassName);

        CtClass cls = ClassPool.makeLocal(originalClassName);
        addClass(cls, targetFileName);
        String innerClassPrefix = originalClassName + "$";
        for (CtField field : cls.getFields()) {
            addTypeIf(innerClassPrefix, field.getType(), 
                targetClassName, inner);
        }
        ReplicatingExpressionEditor editor 
            = new ReplicatingExpressionEditor(innerClassPrefix, 
                targetClassName, inner);
        for (CtBehavior behavior : cls.getDeclaredBehaviors()) {
            if (behavior instanceof CtMethod) {
                addTypeIf(innerClassPrefix, 
                    ((CtMethod) behavior).getReturnType(), 
                    targetClassName, inner);
            }
            for (CtClass param : behavior.getParameterTypes()) {
                addTypeIf(innerClassPrefix, param, targetClassName, inner);
            }
            try {
                behavior.instrument(editor);
            } catch (CannotCompileException e) {
                throw new NotFoundException(e.getMessage(), e);
            }
        }
    }
    
    /**
     * An expression analyzer to find dependent use of inner classes.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class ReplicatingExpressionEditor extends ExprEditor {

        /**
         * Stores the prefix for identifying relevant inner
         *   classes (including "$").
         */
        private String innerClassPrefix;

        /**
         * Stores the target name of the new outer class 
         *   (excluding "$").
         */
        private String targetClassName;
        
        /**
         * Stores the inner class mapping (current name - target name).
         */
        private Map<String, String> inner;

        /**
         * Creates a new replicating (analyzing) expression editor. No code
         * is modified in this class.
         * 
         * @param innerClassPrefix the prefix for identifying relevant inner
         *   classes (including "$")
         * @param targetClassName the target name of the new outer class 
         *   (excluding "$")
         * @param inner the inner class map (current name - target name)
         * 
         * @since 1.00
         */
        public ReplicatingExpressionEditor(String innerClassPrefix, 
            String targetClassName, Map<String, String> inner) {
            this.innerClassPrefix = innerClassPrefix;
            this.targetClassName = targetClassName;
            this.inner = inner;
        }
        
        /**
         * {@inheritDoc}
         */
        public void edit(Cast ca) throws CannotCompileException {
            try {
                addTypeIf(innerClassPrefix, ca.getType(), targetClassName, 
                    inner);
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void edit(Instanceof in) throws CannotCompileException {
            try {
                addTypeIf(innerClassPrefix, in.getType(), 
                    targetClassName, inner);            
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void edit(FieldAccess fi) throws CannotCompileException {
            try {
                addTypeIf(innerClassPrefix, fi.getField().getType(), 
                    targetClassName, inner);
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public void edit(MethodCall me) throws CannotCompileException {
            try {
                CtMethod method = me.getMethod();
                addTypeIf(innerClassPrefix, method.getReturnType(), 
                    targetClassName, inner);
                for (CtClass param : method.getParameterTypes()) {
                    addTypeIf(innerClassPrefix, param, targetClassName, inner);
                }    
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void edit(NewArray ar) throws CannotCompileException {
            try {
                addTypeIf(innerClassPrefix, ar.getComponentType(), 
                    targetClassName, inner);
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
            
        }

        /**
         * {@inheritDoc}
         */
        public void edit(NewExpr ex) throws CannotCompileException {
            try {
                addTypeIf(innerClassPrefix, 
                    ex.getConstructor().getDeclaringClass(), 
                    targetClassName, inner);
            } catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }
        
    }
    
    /**
     * Processes the class replacement. This is
     * the main entry for this class.
     * 
     * @since 1.00
     */
    public void process() {
        try {
            initializeClasses();
            ClassMap classMap = config.getMapping();
            @SuppressWarnings("unchecked")
            Set<Map.Entry<Object, Object>> allMappings 
                = (Set<Map.Entry<Object, Object>>) classMap.entrySet();
            Iterator<Map.Entry<Object, Object>> iter = allMappings.iterator();
            
            Map<String, String> mapping = new HashMap<String, String>();
            while (iter.hasNext()) {
                Map.Entry<Object, Object> entry = iter.next();
                mapping.put(entry.getKey().toString(), 
                    entry.getValue().toString());
            }
            
            while (!mapping.isEmpty()) {
                Map<String, String> inner = new HashMap<String, String>();
                for (Map.Entry<String, String> entry : mapping.entrySet()) {
                    String oldName = entry.getKey().replace('/', '.');
                    String newName = entry.getValue().replace('/', '.');
                    addClass(oldName, newName, inner);
                    if (!classMap.containsKey(oldName)) {
                        classMap.put(oldName, newName);
                    }
                }
                mapping.clear();
                mapping.putAll(inner);
            }
            for (CtClass cl : loadedClasses()) {
                String oldName = cl.getName();
                cl.replaceClassName(classMap);
                if (!cl.getName().equals(oldName)) {
                    replaceClass(cl);
                }
                System.out.println("replicated " + oldName + " -> " 
                    + cl.getName());
            }
            writeClasses(config.getOut());
        } catch (IOException e) {
            config.addErrorMsg("I/O error - stopped processing: " 
                + e.getMessage());
        } catch (CannotCompileException e) {
            config.addErrorMsg("Class compilation error - stopped processing: "
                + e.getMessage());
            e.printStackTrace();
        } catch (NotFoundException e) {
            config.addErrorMsg("Class structure error - stopped processing: "
                + e.getMessage());
        }
    }
    
}
