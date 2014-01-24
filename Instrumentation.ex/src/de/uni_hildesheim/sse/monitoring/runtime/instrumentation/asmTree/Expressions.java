package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

import static de.uni_hildesheim.sse.monitoring.runtime.instrumentation.
    asmTree.Utils.*;

/**
 * A class for parsing (method call) expressions and turning them into bytecode.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Expressions {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Expressions() {
    }
    
    /**
     * Defines internal identifier types.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public enum IdentifierType {
        
        /**
         * A (qualified) Java identifier.
         */
        IDENTIFIER,
        
        /**
         * A string constant.
         */
        STRING,
        
        /**
         * A character constant.
         */
        CHARACTER, 
        
        /**
         * An integral number.
         */
        INTEGRAL_NUMBER,
        
        /**
         * A floating point number.
         */
        FLOAT_NUMBER,
        
        /**
         * An illegal (non-matching) identifier.
         */
        ILLEGAL;
    }
    
    /**
     * Combines the information resulting from creating code for a (method)
     * expression.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class Result {
        
        /**
         * Stores the increment to the maximum stack.
         */
        private int stackInc;
        
        /**
         * Stores the increment to the number of local variables.
         */
        private int localsInc;
        
        /**
         * Stores the (current) result type of an expression being created.
         */
        private IClass type;

        /**
         * A mapping between pseudo parameter names and required data.
         */
        private HashMap<String, ParamMapping> paramConsidered;
        
        /**
         * Prevents this class from being instantiated from outside.
         * 
         * @since 1.00
         */
        private Result() {
        }
        
        /**
         * Returns the type.
         * 
         * @return the type
         * 
         * @since 1.00
         */
        public IClass getType() {
            return type;
        }
        
        /**
         * Returns the increment to the maximum stack size.
         * 
         * @return the increment (nonnegative)
         * 
         * @since 1.00
         */
        public int getStackInc() {
            return stackInc;
        }

        /**
         * Returns the increment to the number of local variables.
         *  
         * @return the increment (nonnegative)
         * 
         * @since 1.00
         */
        public int getLocalsInc() {
            return localsInc;
        }
        
        /**
         * Releases this instance, i.e. the type instance.
         * 
         * @since 1.00
         */
        public void release() {
            releaseType();
        }

        /**
         * Releases the type instance.
         * 
         * @since 1.00
         */
        public void releaseType() {
            if (null != type) {
                type.release();
                
            }
        }
        
        /**
         * Changes the type stored in this instance (including proper release). 
         * 
         * @param type the new type
         * 
         * @since 1.00
         */
        public void setType(IClass type) {
            releaseType();
            this.type = type;
        }
        
        /**
         * Returns the type descriptor of the type stored in this instance.
         * 
         * @return the type descriptor
         * 
         * @since 1.00
         */
        public String getTypeDescriptor() {
            return Expressions.getTypeDescriptor((AType) type);
        } 
    }
    
    /**
     * Returns the type descriptor of <code>type</code>.
     * 
     * @param type the type to be taken into account
     * @return the type descriptor of <code>type</code>
     * 
     * @since 1.00
     */
    private static String getTypeDescriptor(AType type) {
        String result;
        if (null == type) {
            result = "V";
        } else {
            if (type.isPrimitive()) {
                result = type.getInternalName();
            } else {
                result = Utils.getClassTypeDescriptor(type.getName(), 
                    false);
            }
        }
        return result;
    } 
    
    /**
     * Stores the internal data about parameter mappings.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class ParamMapping {
        
        /**
         * The local variable number holding the initial value of the parameter.
         */
        private int var;
        
        /**
         * The opcode used to read the value.
         */
        private int readOpcode;
        
        /**
         * The type of the parameter.
         */
        private IClass type;
    }
    
    /**
     * Internal information about a method to be modified.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class MethodCode {
        
        /**
         * The behavior storing the method.
         */
        private ABehavior behav;
        
        /**
         * The initialization code to be inserted, e.g. to store local copies
         * of parameters.
         */
        private InsnList init;
        
        /**
         * The instruction set being built up.
         */
        private InsnList instr;
        
        /**
         * A mapping between pseudo parameter names and required data.
         */
        private HashMap<String, ParamMapping> paramConsidered;
    }
    
    /**
     * Returns the identifier type for a number (double, float, integer, etc.).
     * 
     * @param text the text to be analyzed
     * @return the identifier type inferred
     * 
     * @since 1.00
     */
    private static IdentifierType getNumberType(String text) {
        boolean ok = true;
        int dotCount = 0;
        int len = text.length();
        char lastNonNumChar = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if ('.' == c) {
                dotCount++;
            } else if (!Character.isDigit(c)) {
                if (i < len - 1) {
                    ok = false;
                } else {
                    lastNonNumChar = c;
                }
            }
        }
        IdentifierType result;
        if (ok && dotCount < 0) {
            result = IdentifierType.INTEGRAL_NUMBER;
            if (0 != lastNonNumChar) {
                if ('L' != lastNonNumChar) {
                    result = IdentifierType.ILLEGAL;
                }
            } 
        } else if (ok && 1 == dotCount) {
            result = IdentifierType.FLOAT_NUMBER;
            if ('D' != lastNonNumChar && 'F' != lastNonNumChar) {
                result = IdentifierType.ILLEGAL;
            }
        } else {
            result = IdentifierType.ILLEGAL;
        }
        return result;
    }

    /**
     * Analyzes text and returns the type of identifier.
     * 
     * @param text the text to be analyzed
     * @return the type of identifier
     * 
     * @since 1.00
     */
    public static IdentifierType getIdentifierType(String text) {
        IdentifierType result;
        text = text.trim();
        if (0 == text.length()) {
            result = IdentifierType.ILLEGAL;
        } else {
            result = getNumberType(text);
            if (IdentifierType.ILLEGAL == result) {
                char firstChar = text.charAt(0);
                char lastChar = text.charAt(text.length() - 1);
                if ('"' == firstChar && '"' == lastChar) {
                    result = IdentifierType.STRING;
                } else if ('\'' == firstChar && '\'' == lastChar) {
                    result = IdentifierType.CHARACTER;
                } else if ('.' == firstChar || '.' == lastChar) {
                    result = IdentifierType.ILLEGAL;
                } else {
                    boolean checkForStart = false;
                    boolean ok = true;
                    boolean isPseudo = false;
                    for (int i = 0; ok && i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (checkForStart) {
                            if (c == '$') {
                                isPseudo = true;
                            } else {
                                ok = Character.isJavaIdentifierStart(c);
                            }
                            checkForStart = false;
                        } else if ('.' == c) {
                            checkForStart = true;
                            isPseudo = false;
                        } else {
                            if (isPseudo) {
                                ok = Character.isDigit(c);
                            } else {
                                ok = Character.isJavaIdentifierPart(c);
                            }
                        }
                    }
                    if (ok) {
                        result = IdentifierType.IDENTIFIER;
                    } else {
                        result = IdentifierType.ILLEGAL;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates code for handling a method parameter.
     * 
     * @param valEx the value expression to create code for
     * @param code information on the method to insert code to
     * @param incLocals the current maxLocals
     * @return the type of the param, implies maxStack = 1 and maxLocals +1 if
     *   <code>code.paramConsidered</code> increases in size
     * @throws InstrumenterException in case of byte code / name resolution 
     *   problems
     * 
     * @since 1.00
     */
    private static IClass handlePseudoParameter(String valEx, MethodCode code, 
        int incLocals) throws InstrumenterException {
        IClass result = null;
        ParamMapping mapping = code.paramConsidered.get(valEx);
        if (null == mapping) {
            String tmp = valEx.substring(1, valEx.length());
            int pos = Integer.valueOf(tmp);
            if (0 <= pos && pos <= code.behav.getParameterCount()) {
                if (0 == pos) {
                    if (code.behav.isStatic()) {
                        code.instr.add(new InsnNode(ACONST_NULL));
                        result = null;
                    } else {
                        code.instr.add(new VarInsnNode(ALOAD, 0));
                        result = code.behav.getDeclaringClass();
                    }
                } else {
                    int offset = 0;
                    if (!code.behav.isStatic()) {
                        offset = 0;
                    } else {
                        offset = 1; // param 0 does not exist
                    }
                    AType pType = (AType) code.behav.getParameterType(
                        pos - 1);
                    int writeOpcode;
                    int readOpcode;
                    if (pType.isPrimitive()) {
                        String typeName = pType.getInternalName();
                        if (1 != typeName.length()) {
                            throw new InstrumenterException(typeName 
                                + " is not primitive!"); 
                        }
                        char typeC = typeName.charAt(0);
                        if (Factory.INTERNAL_DOUBLE == typeC) {
                            writeOpcode = DSTORE;
                            readOpcode = DLOAD;
                        } else if (Factory.INTERNAL_FLOAT == typeC) {
                            writeOpcode = FSTORE;
                            readOpcode = FLOAD;
                        } else if (Factory.INTERNAL_LONG == typeC) {
                            writeOpcode = LSTORE;
                            readOpcode = LLOAD;
                        } else {
                            writeOpcode = ISTORE;
                            readOpcode = ILOAD;
                        }
                    } else {
                        writeOpcode = ASTORE;
                        readOpcode = ALOAD;
                    }
                    mapping = new ParamMapping();
                    mapping.var = code.behav.getNode().maxLocals + incLocals++;
                    mapping.readOpcode = readOpcode;
                    mapping.type = pType;
                    code.init.add(new VarInsnNode(readOpcode, pos - offset));
                    code.init.add(new VarInsnNode(writeOpcode, mapping.var));
                    code.instr.add(new VarInsnNode(readOpcode, mapping.var));
                    code.paramConsidered.put(valEx, mapping);
                    result = pType;
                }
            } else {
                throw new InstrumenterException("illegal parameter number " 
                    + pos);
            }
        } else {
            code.instr.add(new VarInsnNode(mapping.readOpcode, mapping.var));
            result = mapping.type;
        }
        return result;
    }
    
    /**
     * Creates code for an identifier expression.
     * 
     * @param valEx the value expression to create code for
     * @param code information on the method to insert code to
     * @param result result information to be modified as a side effect
     * @return the type of the expression (do not release this instance)
     * @throws InstrumenterException in case of byte code / name resolution 
     *   problems
     * 
     * @since 1.00
     */
    private static IClass createIdentifierExpressionCode(
        String valEx, MethodCode code, Result result) 
        throws InstrumenterException {
        // variable, attribute, etc.
        IClass resultType = null;
        String resultTypeName = null;
        IdentifierType type = getIdentifierType(valEx);
        Object tmpO;
        switch (type) {
        case STRING:
            code.instr.add(new LdcInsnNode(
                valEx.substring(1, valEx.length() - 1)));
            resultTypeName = Utils.STRING;
            break;
        case CHARACTER:
            code.instr.add(integerToNode(valEx.charAt(1)));
            resultTypeName = Factory.INTERNAL_CHAR_STRING;
            break;
        case FLOAT_NUMBER:
            if (valEx.endsWith("D")) {
                tmpO = Double.valueOf(valEx.substring(0, valEx.length() - 1));
            } else {
                if (valEx.endsWith("F")) {
                    tmpO = Float.valueOf(
                        valEx.substring(0, valEx.length() - 1));
                    resultTypeName = Factory.INTERNAL_FLOAT_STRING;
                } else {
                    tmpO = Float.valueOf(valEx);
                    resultTypeName = Factory.INTERNAL_DOUBLE_STRING;
                }
            }
            code.instr.add(new LdcInsnNode(tmpO));
            break;
        case INTEGRAL_NUMBER:
            if (valEx.endsWith("L")) {
                tmpO = Long.valueOf(valEx.substring(0, valEx.length() - 1));
                resultTypeName = Factory.INTERNAL_LONG_STRING;
            } else {
                tmpO = Integer.valueOf(valEx);
                resultTypeName = Factory.INTERNAL_INT_STRING;
            }
            code.instr.add(new LdcInsnNode(tmpO));
            break;
        case IDENTIFIER:
            resultType = resolveIdentifier(valEx, code, result);
            break;
        default:
            break;
        }
        if (null != resultTypeName) {
            resultType = Factory.getLocalFactory().obtainType(
                resultTypeName, null);
        }
        return resultType;
    }

    /**
     * Resolves a (qualified) identifier.
     * 
     * @param valEx the expression to be resolved
     * @param code information on the code to be modified
     * @param result result information to be modified as a side effect
     * @return the type of the identifier (do not release this instance)
     * @throws InstrumenterException in case of byte code / name resolution 
     *   problems
     * 
     * @since 1.00
     */
    private static IClass resolveIdentifier(
        String valEx, MethodCode code, 
        Result result) throws InstrumenterException {
        
        IClass resultType = null;
        IClass owner = code.behav.getDeclaringClass();
        String simpleOwnerName = owner.getName();
        int pos = simpleOwnerName.lastIndexOf('.');
        if (pos > 0) {
            simpleOwnerName = simpleOwnerName.substring(pos + 1);
        }

        int lastPos = -1;
        int startPos = -1;
        pos = startPos;
        boolean attributeResolved = false;
        do {
            pos = valEx.indexOf('.', pos + 1);
            String part;
            if (pos < 0) {
                part = valEx.substring(startPos + 1);
            } else {
                part = valEx.substring(startPos + 1, pos);
            }
            IClass type = null;
            if ('$' == part.charAt(0)) {
                int mappingSize = code.paramConsidered.size();
                type = handlePseudoParameter(valEx, code, result.localsInc);
                resultType = type;
                if (mappingSize != code.paramConsidered.size()) {
                    result.localsInc++;
                }
                result.stackInc++;
                attributeResolved = (null != type);
            }
            // here param name resolution may take place but this is enabled
            // in debug mode only, thus we do not rely on this
            if (null == type) {
                // resolve attribute
                IField field = findField(owner, part);
                if (null != field) {
                    int opcode;
                    if (field.isStatic()) {
                        opcode = GETSTATIC;
                    } else {
                        opcode = GETFIELD;
                        if (startPos < 0) {
                            code.instr.add(new VarInsnNode(ALOAD, 0));
                        } else if (!attributeResolved) {
                            throw new InstrumenterException(
                                "owning object unknown " + valEx);
                        }
                    }
                    ClassLoader loader = 
                        code.behav.getDeclaringAClass().getClassLoader();
                    IClass fieldType = Factory.getLocalFactory().obtainClass(
                        Factory.toInternalName(field.getTypeName()), loader);
                    code.instr.add(new FieldInsnNode(opcode, 
                        Factory.toInternalName(field.getDeclaringClassName()),
                        part, getTypeDescriptor((AType) fieldType)));
                    type = Factory.getLocalFactory().obtainClass(
                        Factory.toInternalName(field.getDeclaringClassName()), 
                        loader);
                    resultType = fieldType;
                    result.stackInc++;
                    attributeResolved = true;
                }
            }
            if (null == type) {
                if (startPos < 0 && part.equals(simpleOwnerName)) {
                    type = owner;
                } else {
                    try {
                        type = Factory.getLocalFactory().obtainClass(
                            Factory.toInternalName(part), 
                            code.behav.getDeclaringAClass().getClassLoader());
                    } catch (InstrumenterException e) {
                        // thrown below, may be partial FQN
                    }
                }
            }
            if (null != type) {
                owner.release();
                owner = type;
                startPos = pos;
            } else {
                if (pos < 0) {
                    throw new InstrumenterException("cannot resolve " + valEx);
                } // else chance to find, keep startPos
            }
            lastPos = pos;
        } while (lastPos >= 0);
        result.setType(resultType);
        return resultType;
    }
    
    /**
     * Creates the byte code for the given expression.
     * 
     * @param behav the behavior the expression is attached to
     * @param valEx the expression
     * @param instr the instruction list to be modified (at place of insertion)
     * @param init the initializing instructions (to be inserted at beginning of
     *   <code>behav</code>)
     * @param chain reuse result information from previous call, 
     *   <b>null</b> no information is availabe / shouild be ignored
     * @return the modifications to stack and locals (maxStack in LSB, increment
     *   to locals in MSB)
     * @throws InstrumenterException in case of any errors
     * 
     * @since 1.00
     */
    static Result createExpressionCode(ABehavior behav, String valEx, 
        InsnList instr, InsnList init, Result chain) 
        throws InstrumenterException {
        MethodCode methodCode = new MethodCode();
        Result result = new Result();
        methodCode.behav = behav;
        methodCode.init = init;
        methodCode.instr = instr;
        if (null != chain) {
            methodCode.paramConsidered = chain.paramConsidered;
            result.localsInc = chain.localsInc;
            chain.release();
        } else {
            methodCode.paramConsidered = new HashMap<String, ParamMapping>();
        }
        createExpressionCode(valEx, null, methodCode, result);
        for (ParamMapping mapping : methodCode.paramConsidered.values()) {
            if (null != mapping.type) {
                mapping.type.release();
            }
        }
        result.paramConsidered = methodCode.paramConsidered;
        return result;
    }

    /**
     * Finds the field given by <code>name</code> in <code>owner</code> or any
     * superclass or superinterface. This method considers access restrictions.
     * 
     * @param owner the class where to start
     * @param name the name of the field to search for
     * @return the field (to be released)
     * @throws InstrumenterException in case of any code resolution errors
     * 
     * @since 1.00
     */
    static IField findField(IClass owner, String name) 
        throws InstrumenterException {
        return findField(owner, name, false);
    }
    
    /**
     * Returns the path of the given qualified name.
     * 
     * @param name the name to return the path for
     * @return the path
     * 
     * @since 1.00
     */
    private static String getPath(String name) {
        String result;
        int pos = name.lastIndexOf('.');
        if (pos > 0) {
            result = name.substring(0, pos);
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Finds the field given by <code>name</code> in <code>owner</code> or any
     * superclass or superinterface.
     * 
     * @param owner the class where to start
     * @param name the name of the field to search for
     * @param upper whether <code>owner</code> is the starting class for the 
     *   search or whether we have to consider access restrictions
     * @return the field (to be released)
     * @throws InstrumenterException in case of any code resolution errors
     * 
     * @since 1.00
     */
    private static IField findField(IClass owner, String name, boolean upper) 
        throws InstrumenterException {
// TODO consider inner classes        
        IField result = null;
        int fCount = owner.getDeclaredFieldCount();
        for (int i = 0; null == result && i < fCount; i++) {
            if (owner.getDeclaredFieldName(i).equals(name)) {
                result = owner.getDeclaredField(i);
                if (upper) {
                    if (result.isPrivate()) {
                        result.release();
                        result = null;
                    } else if (result.isPackageLocal()) {
                        String p1 = getPath(owner.getName());
                        String p2 = getPath(result.getDeclaringClassName());
                        if (!p1.equals(p2)) {
                            result.release();
                            result = null;
                        }
                    }
                }
            }
        }
        if (null == result) {
            IClass tmp = owner.getSuperclass();
            if (null != tmp) {
                result = findField(tmp, name, true);
                tmp.release();
                if (null == result) {
                    int iCount = owner.getInterfaceCount();
                    for (int i = 0; null == result && i < iCount; i++) {
                        result = findField(owner.getInterface(iCount), 
                            name, true);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds a behavior in <code>owner</code> (or in superclasses/interfaces of 
     * <code>owner</code> matching the given specifications) by first checking
     * for identity conversion then for allowed conversions.
     * 
     * @param owner the owning class (where to start the search)
     * @param name the name of the behavior to search for
     * @param paramTypes the parameter types
     * @return the matching behavior (or <b>null</b>), to be released
     * @throws InstrumenterException in case of any error
     * 
     * @since 1.00
     */
    static ABehavior findBehavior(IClass owner, String name, 
        ArrayList<IClass> paramTypes) throws InstrumenterException {
        ABehavior result = findBehavior(owner, name, paramTypes, true);
        if (null == result) {
            result = findBehavior(owner, name, paramTypes, true);
        }
        return result;
    }

    /**
     * Finds a behavior in <code>owner</code> (or in superclasses/interfaces of 
     * <code>owner</code> matching the given specifications).
     * 
     * @param owner the owning class (where to start the search)
     * @param name the name of the behavior to search for
     * @param paramTypes the parameter types
     * @param strict search strict (equal parameter types) or allow conversions
     * @return the matching behavior (or <b>null</b>), to be released
     * @throws InstrumenterException in case of any error
     * 
     * @since 1.00
     */
    static ABehavior findBehavior(IClass owner, String name, 
        ArrayList<IClass> paramTypes, boolean strict) 
        throws InstrumenterException {
        ABehavior result = null;
        int bSize = owner.getDeclaredBehaviorCount();
        for (int b = 0; null == result && b < bSize; b++) {
            IBehavior behav = owner.getDeclaredBehavior(b);
            if (behav.getName().equals(name)) { 
                if (checkParameter(behav, paramTypes, strict)) {
                    result = (ABehavior) behav;
                }
            }
            if (null == result) {
                behav.release();
            }
        }
        if (null == result) {
            IClass tmp = owner.getSuperclass();
            if (null != tmp) {
                result = findBehavior(tmp, name, paramTypes, strict);
                tmp.release();
            }
            for (int i = 0; null == result 
                && i < owner.getInterfaceCount(); i++) {
                IClass iface = owner.getInterface(i);
                result = findBehavior(iface, name, paramTypes, strict);
                iface.release();
            }
        }
        return result;
    }

    /**
     * Checks the parameter types of <code>behav</code> for sequence and type
     * compatibility with <code>paramTypes</code>.
     * 
     * @param behav the behavior to check
     * @param paramTypes the parameter types to check
     * @param strict check for equality only or for conversion
     * @return <code>true</code> if the parameter types match, 
     *   <code>false</code> else
     * @throws InstrumenterException in case of any error
     * 
     * @since 1.00
     */
    private static boolean checkParameter(IBehavior behav, 
        ArrayList<IClass> paramTypes, boolean strict) 
        throws InstrumenterException {
        boolean ok = true;
        int pCount = behav.getParameterCount();
        if (pCount == paramTypes.size()) {
            for (int p = 0; ok && p < pCount; p++) {
                if (strict) {
                    ok = behav.getParameterTypeName(p).equals(
                        paramTypes.get(p).getName());
                } else {
                    IClass pType = behav.getParameterType(p);
// TODO todo check widening conversions for primitives
                    ok = pType.isInstanceOf(paramTypes.get(p).getName());
                    pType.release();
                }
            }
        }
        return ok;
    }
    
    /**
     * Creates the byte code for the given expression.
     * 
     * @param valEx the expression
     * @param paramTypes the parameter types to be modified successively for 
     *   each processed parameter (in case that the call should produce 
     *   parameter code), may be <b>null</b>
     * @param code information on the code to be modified
     *   <code>behav</code>)
     * @param result information on the result to be modified as a side
     *   effect
     * @throws InstrumenterException in case of any errors
     * 
     * @since 1.00
     */
    private static void createExpressionCode(String valEx, 
        ArrayList<IClass> paramTypes, MethodCode code, Result result) 
        throws InstrumenterException {
        int startArgListPos = valEx.indexOf('(');
        int endArgListPos = valEx.lastIndexOf(')');
        boolean error = false;
        ArrayList<IClass> pTypes = new ArrayList<IClass>();
        if (startArgListPos < 0 && endArgListPos < 0) {
            createIdentifierExpressionCode(valEx, code, result);
        } else if (startArgListPos < endArgListPos) {
            String identifier = valEx.substring(0, startArgListPos);
            IdentifierType type = getIdentifierType(identifier);
            if (IdentifierType.ILLEGAL == type) {
                error = true;
            } else {
                int methodPos = identifier.lastIndexOf('.');
                IClass methodOwner;
                String methodName;
                if (methodPos > 0) {
                    // resolve qualified call
                    methodName = identifier.substring(methodPos + 1);
                    methodOwner = createIdentifierExpressionCode(
                        identifier.substring(0, methodPos), code, result);
                    if (null == result.type) {
                        throw new InstrumenterException(
                            "empty type / not found");
                    }
                } else {
                    methodOwner = (AClass) code.behav.getDeclaringClass();
                    methodName = identifier;
                }
                StringTokenizer tokens = new StringTokenizer(
                    valEx.substring(startArgListPos + 1, endArgListPos));
                while (tokens.hasMoreTokens()) {
                    createExpressionCode(tokens.nextToken(), 
                        pTypes, code, result);
                }
                result.stackInc = Math.max(result.stackInc, pTypes.size());
                ABehavior behavior = findBehavior(methodOwner, methodName, 
                    pTypes);
                release(pTypes, 0, -1);
                pTypes.clear();
                if (null == behavior) {
                    methodOwner.release();
                    throw new InstrumenterException("method not found");
                }
                int opcode;
                String ownerName = Factory.toInternalName(
                    behavior.getDeclaringClassName());
                if (behavior.isStatic()) {
                    opcode = INVOKESTATIC;
                } else if (behavior.isConstructor()) {
                    code.instr.add(new TypeInsnNode(NEW, ownerName));
                    opcode = INVOKESPECIAL;
                } else {
                    opcode = INVOKEVIRTUAL;
                }
                code.instr.add(new MethodInsnNode(opcode, ownerName, 
                    methodName, behavior.getNode().desc));
                result.setType(Factory.getLocalFactory().obtainClass(
                    behavior.getResultTypeNameI(), null));
                behavior.release();
                methodOwner.release();
            }
        } else {
            error = true;
        }
        if (error) {
            throw new InstrumenterException("illegal expression (only " 
                + "method calls are allowed): " + valEx);
        }
    }
    
    /**
     * Releases the class information instances stored in <code>classes</code>.
     * 
     * @param classes the list of classes to be released
     * @param start the first index to be released (inclusive)
     * @param end the last index to be released (exclusive), set to the size of
     *   <code>classes</code> if negative
     * 
     * @since 1.00
     */
    private static void release(ArrayList<IClass> classes, int start, int end) {
        if (end < 0) {
            end = classes.size();
        }
        for (int i = start; i < end; i++) {
            classes.get(i).release();
        }
    }

}
