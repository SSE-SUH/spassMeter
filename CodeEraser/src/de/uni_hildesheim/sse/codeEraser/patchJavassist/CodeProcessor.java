package de.uni_hildesheim.sse.codeEraser.patchJavassist;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import de.uni_hildesheim.sse.codeEraser.Configuration;
import de.uni_hildesheim.sse.codeEraser.util.ClassPool;
import de.uni_hildesheim.sse.codeEraser.util.OnTheFlyJarProcessor;

/**
 * Implements a code processor for patching Javassist. The functionality is to 
 * add three public Boolean attributes to <code>ExprEditor</code>, namely 
 * <code>disableInstanceof</code>, <code>disableCast</code> and 
 * <code>disableHandler</code> which all are initialized as <code>false</code>
 * to be modified on demand.
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
     * Specific editor for the doit-method. Adds code to disable the instanceof
     * and cast handler editor calls.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class LoopBodyEditor extends ExprEditor {
        
        /**
         * Edits a method call.
         * 
         * @param mc the method call
         * @throws CannotCompileException in case that compile errors occur
         */
        public void edit(MethodCall mc) throws CannotCompileException {
            if (mc.getMethodName().equals("byteAt")) {
                try {
                    String opcode = Opcode.class.getName();
                    CtMethod method = mc.getMethod();
                    CtClass declaring = method.getDeclaringClass();
                    if (declaring.getName().equals(
                        CodeIterator.class.getName())) {
                        mc.replace("$_ = $proceed($$); " 
                            + "if (disableInstanceof && $_==" 
                            + opcode + ".INSTANCEOF)" 
                            + "{return false;}" 
                            + "if (disableCast && $_==" 
                            + opcode + ".CHECKCAST)" 
                            + "{return false;}");
                    }
                } catch (NotFoundException e) {
                    throw new CannotCompileException(e);
                }
            }
        }
        
    }

    /**
     * Specific editor for the doit-method. Adds code to disable the exception
     * handler editor calls.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class DoitEditor extends ExprEditor {

        /**
         * Edits a method call.
         * 
         * @param mc the method call
         * @throws CannotCompileException in case that compile errors occur
         */
        public void edit(MethodCall mc) throws CannotCompileException {
            if (mc.getMethodName().equals("size")) {
                try {
                    CtMethod method = mc.getMethod();
                    CtClass declaring = method.getDeclaringClass();
                    if (declaring.getName().equals(
                        ExceptionTable.class.getName())) {
                        mc.replace("$_ = $proceed($$); if (disableHandler) " 
                            + "{$_ = 0;}");
                    }
                } catch (NotFoundException e) {
                    throw new CannotCompileException(e);
                }
            }
        }
    }

    /**
     * Processes the class replacement. This is
     * the main entry for this class. Adds three boolean configuration
     * attributes the expression editor class and modifies the internal
     * methods accordingly.
     * 
     * @since 1.00
     */
    public void process() {
        try {
            loadClasses(config.getJar());
            for (CtClass cl : loadedClasses()) {
                if (cl.getName().equals(ExprEditor.class.getName())) {
                    
                    CtField f = new CtField(CtClass.booleanType, 
                        "disableInstanceof", cl);
                    f.setModifiers(Modifier.PUBLIC);
                    cl.addField(f, "false");

                    f = new CtField(CtClass.booleanType, 
                        "disableCast", cl);
                    f.setModifiers(Modifier.PUBLIC);
                    cl.addField(f, "false");

                    f = new CtField(CtClass.booleanType, 
                        "disableHandler", cl);
                    f.setModifiers(Modifier.PUBLIC);
                    cl.addField(f, "false");
                    
                    CtMethod method = cl.getDeclaredMethod("loopBody");
                    method.instrument(new LoopBodyEditor());
                    CtClass[] param = {
                        ClassPool.get(CtClass.class.getName()), 
                        ClassPool.get(MethodInfo.class.getName())};
                    method = cl.getDeclaredMethod("doit", param);
                    method.instrument(new DoitEditor());
                }
            }
            writeClasses(config.getJar(), config.getOut());
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
