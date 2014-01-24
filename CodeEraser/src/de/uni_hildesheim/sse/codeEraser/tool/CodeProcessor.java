package de.uni_hildesheim.sse.codeEraser.tool;

import java.io.IOException;
import java.text.Collator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.SetValue;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.codeEraser.util.Annotations;
import de.uni_hildesheim.sse.codeEraser.util.ClassPool;
import de.uni_hildesheim.sse.codeEraser.util.OnTheFlyJarProcessor;
import de.uni_hildesheim.sse.codeEraser.util.Types;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

/**
 * Implements the code eraser code processor.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CodeProcessor extends OnTheFlyJarProcessor {

    /**
     * Stores the actual bindings to be considered.
     */
    private Map<String, String> bindings;
    
    /**
     * Stores the classes being scheduled for removal.
     */
    private Set<CtClass> classesForRemoval;
    
    /**
     * Stores the elements being scheduled for removal. However, 
     * the javassist elements cannot be hashed directly. Therefore, 
     * we rely on the system identity hashcode.
     */
    private Map<Integer, ElementForRemoval> removals;
    
    /**
     * Stores the configuration read from the ANT file or the command line.
     */
    private Configuration config;
    
    /**
     * Creates a new instance based on the given configuration.
     * 
     * @param config the configuration read from the ANT file or the 
     *     command line
     * 
     * @since 1.00
     */
    public CodeProcessor(Configuration config) {
        this.config = config;
        this.config.validate();
        this.bindings = this.config.getBindings();

        javassist.ClassPool.doPruning = true;
        removals = new HashMap<Integer, ElementForRemoval>();
        classesForRemoval = new HashSet<CtClass>();

        // needed, otherways javassist will not find the annotations
        ClassPool.addClassLoader(CodeProcessor.class.getClassLoader());
    }
    
    /**
     * Processes the command in {@link Configuration#getCommand()}. This is
     * the main entry for this class.
     * 
     * @since 1.00
     */
    public void process() {
        try {
            switch (config.getCommand()) {
            case LIST_ANNOTATIONS:
                loadClasses(config.getJar());
                listAnnotations();
                break;
            case PROCESS:
                loadClasses(config.getJar());
                removeBoundElements();
                writeClasses(config.getJar(), config.getOut());
                break;
            default:
                config.addErrorMsg("Unknown command " + config.getCommand()
                    + " - stopped processing");
                break;
            }
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
    
    /**
     * Removes bound elements.
     * 
     * @throws CannotCompileException in case that the modified class code
     *     does not compile
     * @throws NotFoundException in case that any Java element cannot be found
     * 
     * @since 1.00
     */
    private void removeBoundElements() throws CannotCompileException, 
        NotFoundException {
        for (CtClass cl : loadedClasses()) {
            if (shouldRemove(Annotations.getAnnotationRec(cl, Variability.class,
                config.checkRecursively()))) {
                classesForRemoval.add(cl);
            } else {
                for (CtField field : cl.getDeclaredFields()) {
                    if (shouldRemove(Annotations.getAnnotationRec(field, 
                        Variability.class, config.checkRecursively()))) {
                        removals.put(System.identityHashCode(field), 
                            new FieldForRemoval(field));
                    }
                }
                for (CtConstructor cons : cl.getDeclaredConstructors()) {
                    if (shouldRemove(Annotations.getAnnotationRec(cons, 
                        Variability.class, config.checkRecursively()))) {
                        removals.put(System.identityHashCode(cons), 
                            new ConstructorForRemoval(cons));
                    }
                }
                for (CtMethod method : cl.getDeclaredMethods()) {
                    if (shouldRemove(Annotations.getAnnotationRec(method, 
                        Variability.class, config.checkRecursively()))) {
                        removals.put(System.identityHashCode(method), 
                            new MethodForRemoval(method));
                    }
                }
            }
        }
        RemovalEditor remEd = new RemovalEditor();
        for (CtClass cl : loadedClasses()) {
            for (CtBehavior beh : cl.getDeclaredBehaviors()) {
                beh.instrument(remEd);
            }            
        }
        for (Map.Entry<Integer, ElementForRemoval> entry 
            : removals.entrySet()) {
            entry.getValue().remove();
        }
        for (CtClass cl : classesForRemoval) {
            removeClass(cl);
            System.out.println("- removed class " + cl.getName());
        }
    }

    /**
     * A bytecode editor for removing references to individual elements.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    class RemovalEditor extends ExprEditor {

        /**
         * Is called when a cast occurs. Replaces casts to types to be
         * removed by <b>null</b>.
         * 
         * @param cast the cast to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(Cast cast) throws CannotCompileException {
            try {
                CtClass type = cast.getType();
                if (classesForRemoval.contains(type)) {
                    Variability annotation = Annotations.getAnnotationRec(type, 
                        Variability.class, config.checkRecursively());
                    String value;
                    if (null != annotation && annotation.value().length() > 0) {
                        value = "(" + annotation.value() + ") $1";
                    } else {
                        value = "null";
                    }
                    cast.replace("$_ = " + value + ";");
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when a call to a constructor such as super or this occurs. 
         * Removes calls to constructors.
         *
         * @param call the constructor call to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(ConstructorCall call) throws CannotCompileException {
            try {
                CtConstructor constructor = call.getConstructor();
                if (removals.containsKey(
                    System.identityHashCode(constructor))) {
                    call.replace("");
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when a field access occurs. Replaces field accesses to 
         * attributes scheduled for removal by the value expression specified
         * in the annotation or by the default value. Replaces initial values
         * in case of {@link SetValue}.
         *
         * @param access the field access to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */        
        @Override
        public void edit(FieldAccess access) throws CannotCompileException {
            try {
                CtField field = access.getField();
                if (removals.containsKey(System.identityHashCode(field)) 
                    || classesForRemoval.contains(field.getDeclaringClass())) {
                    Variability var = Annotations.getAnnotationRec(field, 
                        Variability.class, config.checkRecursively());
                    if (access.isWriter()) {
                        access.replace("");
                    } else {
                        String override = null;
                        if (null != var) {
                            override = var.value();
                        }
                        access.replace("$_ = " + Types.getDefaultValue(
                            field.getType(), override) + ";");
                    }
                } else {
                    if (access.isStatic() && access.isWriter()) {
                        SetValue set = Annotations.getAnnotationRec(
                            field, SetValue.class, config.checkRecursively());
                        if (null != set) {
                            String binding = bindings.get(set.id());
                            if (null != binding) {
                                access.replace(
                                    field.getDeclaringClass().getName() 
                                    + "." + field.getName() + " = " 
                                    + bindings.get(set.id()) + ";");
                                System.out.println("- modified value of " 
                                    + field.getName());
                            }
                        }
                    }
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when the instanceof operator occurs. Replaces tests to
         * classes scheduled for removal by <code>false</code> or by tests for 
         * the type mentioned in the annotation.
         * 
         * @param operator the instanceof operator
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(Instanceof operator) throws CannotCompileException {
            try {
                CtClass rhsType = operator.getType();
                if (classesForRemoval.contains(rhsType)) {
                    Variability annotation = Annotations.getAnnotationRec(
                        rhsType, Variability.class, config.checkRecursively());
                    String expression;
                    if (null != annotation && annotation.value().length() > 0) {
                        expression = "$_ = $1 instanceof " + annotation.value();
                    } else {
                        expression = "$_ = false;";
                    }
                    operator.replace(expression);
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when a method call occurs. Replaces calls to 
         * constructors or classes to be removed by the value expression
         * in the annotation or by the default value.
         *
         * @param call the method call to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(MethodCall call) throws CannotCompileException {
            try {
                CtMethod method = call.getMethod();
                if (removals.containsKey(System.identityHashCode(method))
                    || classesForRemoval.contains(method.getDeclaringClass())) {
                    if (CtClass.voidType == method.getReturnType()) {
                        call.replace("");
                    } else {
                        Variability var = Annotations.getAnnotationRec(method, 
                            Variability.class, config.checkRecursively());
                        String override = null;
                        if (null != var) {
                            override = var.value();
                        }
                        call.replace("$_ = " + Types.getDefaultValue(
                            method.getReturnType(), override) + ";");
                    }
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when an instance creation occurs. Replaces references to 
         * classes to be removed by the value expression in the annotation 
         * or by <b>null</b>.
         *
         * @param expression the instance creation expression to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(NewExpr expression) throws CannotCompileException {
            try {
                CtConstructor cons  = expression.getConstructor();
                CtClass declaring = cons.getDeclaringClass();
                if (removals.containsKey(System.identityHashCode(cons)) 
                    || classesForRemoval.contains(declaring)) {
                    Variability annotation = Annotations.getAnnotationRec(
                        declaring, Variability.class, 
                        config.checkRecursively());
                    String expr;
                    if (null != annotation && annotation.value().length() > 0) {
                        expr = "$_ = new " + annotation.value() + "($$);";
                    } else {
                        expr = "$_ = null;";
                    }
                    expression.replace(expr);
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when an exception handler occurs. Replaces references to 
         * classes to be removed by the value expression in the annotation 
         * or by <b>null</b>.
         *
         * @param handler the exception handler to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(Handler handler) throws CannotCompileException {
            try {
                CtClass type = handler.getType();
                if (!classesForRemoval.contains(type)) {
                    Variability annotation = Annotations.getAnnotationRec(type, 
                        Variability.class, config.checkRecursively());
                    String expr;
                    if (null != annotation && annotation.value().length() > 0) {
                        expr = annotation.value() + " $1";
                    } else {
                        expr = "";
                    }
                    handler.replace(expr);
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

        /**
         * Is called when an array instance creation occurs. Replaces 
         * references to classes to be removed by the value expression in 
         * the annotation or by <b>null</b>.
         *
         * @param arrayCreation the array creation expression to be processed
         * @throws CannotCompileException in case of errors in the generated 
         *    byte code
         */
        @Override
        public void edit(NewArray arrayCreation) throws CannotCompileException {
            try {
                CtClass type = arrayCreation.getComponentType(); 
                if (!classesForRemoval.contains(type)) {
                    Variability annotation = Annotations.getAnnotationRec(type, 
                        Variability.class, config.checkRecursively());
                    String expression;
                    if (null != annotation && annotation.value().length() > 0) {
                        StringBuilder buf = new StringBuilder("$_ = new ");
                        buf.append(annotation.value());
                        for (int i = 1; 
                            i <= arrayCreation.getCreatedDimensions(); i++) {
                            buf.append("[");
                            buf.append(i);
                            buf.append("]");
                        }
                        expression = buf.toString();
                    } else {
                        expression = "";
                    }
                    arrayCreation.replace(expression);
                }
            } catch (NotFoundException ex) {
                throw new CannotCompileException(ex);
            }
        }

    }
    
    /**
     * Returns if the specified annotation indicates a removal. This method
     * considers the actual bindings and the boolean operations given in the
     * annotation. 
     * 
     * @param var the annotation to be considered
     * @return <code>true</code> if the removal should be executed, 
     *   <code>false</code> if the annotated element should not be affected
     * 
     * @since 1.00
     */
    private boolean shouldRemove(Variability var) {
        boolean remove = false;
        if (null != var) {
            String[] ids = var.id();
            Operation op = var.op();
            switch (op) {
            case AND:
                remove = true;
                break;
            case OR:
                remove = false;
                break;
            case XOR:
                remove = false;
                break;
            default:
                // unknown and unspecified
                remove = false;
                break;
            }
            for (int i = 0; i < ids.length; i++) {
                boolean hasBinding = bindings.containsKey(ids[i]);
                if (var.removeIfDisabled()) {
                    hasBinding = !hasBinding;
                }
                switch (op) {
                case AND:
                    remove &= hasBinding;
                    break;
                case OR:
                    remove |= hasBinding;
                    break;
                case XOR:
                    remove ^= hasBinding;
                    break;
                default:
                    // unknown and unspecified
                    break;
                }
            }
        }
        return remove;
    }
    
    /**
     * Lists all annotations mentioned in the classes being processed. 
     * Annotation ids are listed per class and for the whole program.
     * 
     * @since 1.00
     */
    private void listAnnotations() {
        TreeSet<String> ids = new TreeSet<String>(Collator.getInstance());
        TreeSet<String> idsPerClass 
            = new TreeSet<String>(Collator.getInstance());
        System.out.println("Annotated classes:");
        for (CtClass cl : loadedClasses()) {
            appendAnnotation(idsPerClass, ids, Annotations.getAnnotationRec(
                cl, Variability.class, config.checkRecursively()));
            
            for (CtField field : cl.getDeclaredFields()) {
                appendAnnotation(idsPerClass, ids, Annotations.getAnnotationRec(
                    field, Variability.class, config.checkRecursively()));
            }
            
            for (CtBehavior behavior : cl.getDeclaredBehaviors()) {
                appendAnnotation(idsPerClass, ids, Annotations.getAnnotationRec(
                    behavior, Variability.class, config.checkRecursively()));
            }
            if (!idsPerClass.isEmpty()) {
                System.out.print(" - ");
                System.out.print(cl.getName());
                System.out.print(": ");
                Iterator<String> iter = idsPerClass.iterator();
                while (iter.hasNext()) {
                    System.out.print(iter.next());
                    if (iter.hasNext()) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("Annotation ids:");
        for (String id : ids) {
            System.out.print(" - ");
            System.out.println(id);
        }
    }
    
    /**
     * Appends the ids of a given annotation.
     * 
     * @param ids1 a list to be modified as a side effect by adding the ids
     * @param ids2 a list to be modified as a side effect by adding the ids
     * @param var the annoation to be appended (may be <b>null</b>)
     * 
     * @since 1.00
     */
    private static void appendAnnotation(Set<String> ids1, Set<String> ids2, 
        Variability var) {
        if (null != var) {
            for (String id : var.id()) {
                ids1.add(id);
                ids2.add(id);
            }
        }
    }
    
}
