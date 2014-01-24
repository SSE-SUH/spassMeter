package de.uni_hildesheim.sse.codeEraser.replace;

import java.io.IOException;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.CtClass;
import javassist.NotFoundException;
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
        try {
            // allow self-resolution for declaring classes
            ClassPool.addToClassPath(config.getJar());
        } catch (NotFoundException e) {
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
            loadClasses(config.getJar());
            ClassMap classMap = config.getMapping();
            if (config.hasPatterns()) {
                for (CtClass cl : loadedClasses()) {
                    String name = cl.getName();
                    String subst = config.getSubstitute(name);
                    if (null != subst) {
                        classMap.put(name, subst);
                    }
                }
            }
            ArrayList<CtClass> classesForRemoval = new ArrayList<CtClass>();
            for (CtClass cl : loadedClasses()) {
                String oldName = cl.getName();
/*                if (classMap.containsKey(ClassMap.toJvmName(oldName))) {
                    classesForRemoval.add(cl);
                } else {
                    boolean found = false;
                    try {
                        CtClass declaring = cl.getDeclaringClass();
                        do {
                            if (null != declaring) {
                                if (classMap.containsKey(ClassMap.toJvmName(
                                    declaring.getName()))) {
                                    classesForRemoval.add(cl);
                                    found = true;
                                } else {
                                    declaring = declaring.getDeclaringClass();
                                }
                            }
                        } while (!found && null != declaring);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!found) {*/
                cl.replaceClassName(classMap);
                if (!cl.getName().equals(oldName)) {
                    replaceClass(cl);
                }
//                    }
//                }
            }
            for (CtClass cl : classesForRemoval) {
                removeClass(cl);
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
