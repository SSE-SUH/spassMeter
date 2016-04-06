package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.lang.annotation.Annotation;
import java.util.ListIterator;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    AnnotationSearchType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.xml.
    XMLConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.DelegatingInputStream;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.DelegatingOutputStream;

import static de.uni_hildesheim.sse.monitoring.runtime.instrumentation.
    asmTree.Utils.*;

/**
 * Implements the statement modifier for method bodies. Stack size modifications
 * in this class are calculated based on the max stack size before modification,
 * the stack requirements of the new fragment(s) and the actual stack height
 * where the fragment(s) shall be inserted ({@link #maxStack(int)}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class StatementModifier extends MethodVisitor implements IStatementModifier, 
    Opcodes {
    
    /**
     * Stores the instance pool.
     */
    private static final ArrayList<StatementModifier> POOL 
        = new ArrayList<StatementModifier>(1);
    
    /**
     * Defines the internal name of {@link StreamType}.
     */
    private static final String STREAM_TYPE = 
        Factory.toInternalName(StreamType.class.getName());

    /**
     * Stores the descriptor of {@link StreamType}.
     */
    private static final String STREAM_TYPE_DESCR = 
        getClassTypeDescriptor(STREAM_TYPE, true);
    
    /**
     * Defines the internal name of {@link DelegatingInputStream}.
     */
    private static final String DELEGATING_INPUTSTREAM = 
        Factory.toInternalName(DelegatingInputStream.class.getName());

    /**
     * Defines the internal name of {@link DelegatingOutputStream}.
     */
    private static final String DELEGATING_OUTPUTSTREAM = 
        Factory.toInternalName(DelegatingOutputStream.class.getName());

    /**
     * Stores the descriptor of the read/write IO methods in the recorder.
     */
    private static final String READ_WRITE_DESCR = "(" + STRING_DESCR
        + STRING_DESCR + "I" + STREAM_TYPE_DESCR + ")I";
    
    /**
     * Stores the available additional local variables and whether a local 
     * variable is used (negative value) or free (positive value).
     */
    private int[] usedLocals = new int[4];
    
    /**
     * The maximum stack size of this method.
     */
    private int maxStack;
    
    /**
     * Stores the actual stack size.
     */
    private int curStack;
    
    /**
     * Stores the node to be instrumented.
     */
    private MethodNode nodeToBeInstrumented;
    
    /**
     * Stores the editor containing the high level instrumentation logic.
     */
    //private BehaviorEditor editor;
    
    /**
     * Stores the first / latest exception.
     */
    //private InstrumenterException exception;
    
    /**
     * Stores the class loader of the declaring class (may be <b>null</b>).
     */
    private ClassLoader loader;
    
    /**
     * Stores the instruction set of the method being modified.
     */
    private InsnList instructions;
    
    /**
     * Stores the current instruction.
     */
    private AbstractInsnNode instruction;

    /**
     * Stores the last visited new instruction.
     */
    private ArrayList<TypeInsnNode> lastNew = new ArrayList<TypeInsnNode>();
    
    /**
     * Stores a temporary set of instructions.
     */
    private InsnList tempInstructions = new InsnList();
    
    /**
     * Stores whether the declaring class of the current method call. This is 
     * only valid during {@link BehaviorEditor#editMethodCall(String, String)}.
     */
    private String mcDeclaringClass;
    
    /**
     * Stores whether the current method call is static. This is only valid
     * during {@link BehaviorEditor#editMethodCall(String, String)}.
     */
    private boolean mcIsStatic;

    /**
     * Stores whether the current method descriptor. This is only valid
     * during {@link BehaviorEditor#editMethodCall(String, String)}.
     */
    private String mcDescriptor;
    
    /**
     * Stores the parameter list of the method. This is only valid
     * during {@link BehaviorEditor#editMethodCall(String, String)}.
     */
    private ArrayList<String> mcParameter;
    
    /**
     * Stores whether the declaring class of the current field. This is 
     * only valid during {@link BehaviorEditor#editFieldAccess(String, 
     * String, boolean)}.
     */
    private String fDeclaringClass;
    
    /**
     * Creates a new statement modifier 
     * (via {@link #getModifierFromPool(BehaviorEditor)}.
     * 
     * @since 1.00
     */
    private StatementModifier() {
        super(Opcodes.ASM4);
        //Testing only
        //Configuration.INSTANCE.setStaticInstrumentation(true);
    }
    
    /**
     * Returns a modifier from the pool.
     *
     * @return a modifier instance
     * 
     * @since 1.00
     */
    static final synchronized StatementModifier getModifierFromPool() {
        StatementModifier result;
        int size = POOL.size();
        if (size > 0) {
            result = POOL.remove(size - 1);
        } else {
            result = new StatementModifier();
        }
        return result;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param editor the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseModifier(StatementModifier editor) {
        POOL.add(editor);
    }
    
    /**
     * Replace the created type in a new array or new object expression.
     * 
     * @param newType the type to be created
     * @param accountMemory should the allocated memory be accounted
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    @Override
    public void replaceCreatedType(String newType, boolean accountMemory)
        throws InstrumenterException {
        newType = Factory.toInternalName(newType);
        if (!lastNew.isEmpty()) {
            int lastIndex = lastNew.size() - 1;
            TypeInsnNode nn = lastNew.get(lastIndex);
            nn.desc = newType;
            MethodInsnNode mNode = (MethodInsnNode) instruction;
            mNode.owner = newType;
            // does not change maxStack or maxLocals
        }
        if (accountMemory) {
            appendMemoryAllocated(null);
        }
    }

    /**
     * Appends the context notification code.
     * 
     * @param contextId the context id (may be <b>null</b>)
     * @param before code for inserting before the actual notification
     * 
     * @since 1.00
     */
    private void appendContextNotification(String contextId, boolean before) {
        if (null != contextId) {
            appendRecorderCallProlog(tempInstructions, false);
            tempInstructions.add(new LdcInsnNode(contextId)); 
            int instr = (before ? ICONST_1 : ICONST_0);
            tempInstructions.add(new InsnNode(instr));
            appendRecorderCall(tempInstructions, "changeValueContext", 
                "(Ljava/lang/String;Z)V");
            maxStack(2 + 1); // does not change maxLocals
        }
    }

    /**
     * Inserts the current instructions in {@link #tempInstructions}
     * before <code>instruction</code>. The code to be inserted must not change
     * the stack until the actual position.
     * 
     * @param instruction the instruction to insert before
     * @throws InstrumenterException in case that the beginning of the 
     *     statement cannot be found
     * 
     * @since 1.00
     */
    private void insertBefore(AbstractInsnNode instruction) 
        throws InstrumenterException {
        if (tempInstructions.size() > 0) {
            instructions.insertBefore(instruction, tempInstructions);
        }        
    }
    
    /**
     * Finds the next instruction representing the return of a current
     *(method call).
     * 
     * @param instruction the instruction to start at
     * @return the found statement (may be <b>null</b>)
     * 
     * @since 1.00
     */
    private AbstractInsnNode findNextInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode next = instruction.getNext();
        while (null != next 
            && (isPseudo(next))) { // ignore pseudo 
            next = next.getNext();
        }
        return next;
    }
    
    /**
     * Returns whether the given instruction is a pseudo instruction.
     * 
     * @param instruction the instruction to test
     * @return <code>true</code> if it is pseudo, <code>false</code> else
     * 
     * @since 1.00
     */
    private static boolean isPseudo(AbstractInsnNode instruction) {
        int type = instruction.getType();
        return AbstractInsnNode.LABEL == type 
            || AbstractInsnNode.LINE == type 
            || AbstractInsnNode.FRAME == type;
    }
    
    /**
     * Appends a memory allocation to the current statement and uses the result
     * of the current statement as input for the notification of the memory 
     * allocation. Insert value context change notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    @Override
    public void appendMemoryAllocated(String contextId)
        throws InstrumenterException {
        appendMemoryAllocated(contextId, "memoryAllocated");
    }

    /**
     * Returns the next free local variable. This method updates the 
     * <code>maxLocals</code> variable in {@link #nodeToBeInstrumented}.
     * 
     * @return the index of the next free local variable.
     * 
     * @since 1.00
     */
    private int getFreeLocalVariable() {
        int free = 0;
        // number = maxLocals + i
        for (int i = 0; 0 == free && i < usedLocals.length; i++) {
            int local = usedLocals[i];
            if (local > 0) {
                free = usedLocals[i];
                usedLocals[i] = -free;
            } else if (0 == local) {
                free = nodeToBeInstrumented.maxLocals;
                nodeToBeInstrumented.maxLocals++;
                usedLocals[i] = -free;
            } // local < 0 -> in use
        }
        if (0 == free) {
            // insufficient space
            int[] tmp = new int[usedLocals.length + 1];
            System.arraycopy(usedLocals, 0, tmp, 0, usedLocals.length);
            usedLocals = tmp;
            free = nodeToBeInstrumented.maxLocals;
            nodeToBeInstrumented.maxLocals++;
            usedLocals[usedLocals.length - 1] = -free;
        }
        return free;
    }
    
    /**
     * Releases a local variable obtained by {@link #getFreeLocalVariable()}.
     * 
     * @param index the index of the variable to release
     * 
     * @since 1.00
     */
    private void releaseLocalVariable(int index) {
        for (int i = 0; i < usedLocals.length; i++) {
            if (usedLocals[i] == -index) {
                usedLocals[i] = index;
                break;
            }
        }
    }

    /**
     * Identifies operations which save a result of a memory allocation.
     * 
     * @param op the op code
     * @return <code>true</code> if it saves a result, <code>false</code> else
     * 
     * @since 1.00
     */
    private static boolean savesResult(int op) {
        return ASTORE == op /*|| PUTFIELD == op || PUTSTATIC == op*/;
    }
    
    /**
     * Returns the closest NEW node before <code>mNode</code> which fits
     * to the type of <code>mNode</code>.
     * 
     * @param node the node to start the search at
     * @return the closest NEW node or <b>null</b>
     * 
     * @since 1.00
     */
    private TypeInsnNode findMatchingNew(MethodInsnNode node) {
        // this may require a stack analysis in case of nested un-DUPped NEWs
        TypeInsnNode result = null;
        AbstractInsnNode prev = node;
        int sameTypeCount = 0;
        do {
            prev = prev.getPrevious();
            if (null != prev) {
                switch (prev.getOpcode()) {
                case INVOKESPECIAL:
                    MethodInsnNode mNode = (MethodInsnNode) prev;
                    if (mNode.owner.equals(node.owner)) {
                        sameTypeCount++;
                    }
                    break;
                case NEW:
                    TypeInsnNode tNode = (TypeInsnNode) prev;
                    if (tNode.desc.equals(node.owner)) {
                        if (sameTypeCount > 0) {
                            sameTypeCount--;
                        } else {
                            result = tNode;
                            break;
                        }
                    }
                    break;
                default:
                    // other nodes are not relevant
                    break;
                }
            }
        } while (null != prev);
        return result;
    }
    
    /**
     * Appends a memory allocation to the current statement and uses the result
     * of the current statement as input for the notification of the memory 
     * allocation. Insert value context change notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @param operation the recorder operation to call
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    private void appendMemoryAllocated(String contextId, String operation)
        throws InstrumenterException {
        AbstractInsnNode next = findNextInstruction(instruction);
        if (null != next) {
            appendContextNotification(contextId, true);
            insertBefore(instruction);

            int stack = 0;
            AbstractInsnNode insertAt = instruction;
            AbstractInsnNode insertAfter = null;
            int[] varNr = null;
            if (!savesResult(next.getOpcode())) {
                if (INVOKESPECIAL == instruction.getOpcode()) {
                    MethodInsnNode mNode = (MethodInsnNode) instruction;
                    if (ABehavior.CONSTRUCTOR_NAME.equals(mNode.name)) {
                        // special case: no DUP after new
                        // just saving all parameters does not work :(
                        AbstractInsnNode newNode = findMatchingNew(mNode);
                        if (null != newNode 
                            && DUP != newNode.getNext().getOpcode()) {
                            // DUP2?
                            instructions.insert(newNode, new InsnNode(DUP));
                        }
                    }
                }
                if (null == varNr) {
                    // no astore, chained instructions
                    // store result temporarily and restore after
                    varNr = new int[1];
                    varNr[0] = getFreeLocalVariable();
                    tempInstructions.add(new VarInsnNode(ASTORE, varNr[0]));
                }
                insertAfter = new VarInsnNode(ALOAD, varNr[0]);
                stack = 2;
            }
            appendRecorderCallProlog(tempInstructions, false);
            // push new instance as param for recorder
            if (savesResult(next.getOpcode())) {
                copy(next, ALOAD, null, tempInstructions);
                stack = 2;
                insertAt = next;
            } else {
                copy(insertAfter, NO_NEW_OP, null, tempInstructions);
            }
            if (stack > 0) {
                appendRecorderCall(tempInstructions, operation, 
                    "(Ljava/lang/Object;)V");
                if (mcIsStatic) {
                    maxStack += stack;
                } else {
                    stack--; // implicit object, new param
                    if (stack > 0) {
                        maxStack += stack;
                    }
                }
                appendContextNotification(contextId, false);
                if (null != insertAfter) {
                    // chained instructions, restore result
                    tempInstructions.add(insertAfter);
                }
                if (null != varNr) {
                    for (int i = 0; i < varNr.length; i++) {
                        releaseLocalVariable(varNr[i]);
                    }
                }
                instructions.insert(insertAt, tempInstructions);
            } else {
                throw new InstrumenterException("previous instruction " 
                    + "opcode " + next.getOpcode() + " not recognized");
            }
        }
    }
    
    /**
     * Add a attribute value notification call for the given recorder id.
     * 
     * @param recId the recorder id to notify (may be <b>null</b>)
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    @Override
    public void notifyValueChanged(String recId)
        throws InstrumenterException {
        
        FieldInsnNode fNode = (FieldInsnNode) instruction;
        int opcode = fNode.getOpcode();
        appendRecorderCallProlog(tempInstructions, false);
        if (null == recId) {
            tempInstructions.add(new InsnNode(ACONST_NULL));
        } else {
            tempInstructions.add(new LdcInsnNode(recId)); 
        }
        if (GETSTATIC == opcode || PUTSTATIC == opcode) {
            tempInstructions.add(new FieldInsnNode(GETSTATIC, fNode.owner, 
                fNode.name, fNode.desc));
        } else {
            tempInstructions.add(new VarInsnNode(ALOAD, 0));
            tempInstructions.add(new FieldInsnNode(GETFIELD, fNode.owner, 
                fNode.name, fNode.desc));
        }
        appendRecorderCall(tempInstructions, "notifyValueChange", 
            "(Ljava/lang/String;" + fNode.desc + ")V");
        instructions.insert(instruction, tempInstructions);
        maxStack(4); // wc, no change to maxLocals
    }
        
    /**
     * Rechains a stream creation by inserting a delegating stream.
     * 
     * @param type the type of the stream (may be <b>null</b> in case that
     *   <code>typeExpression</code> specifies the type of the stream as a
     *   Java expression)
     * @param typeExpression an alternative expression taking precedence over
     *   <code>type</code>, may be <b>null</b>
     * @param input if <code>true</code> return an expression for an input 
     *   stream, for an output stream else
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    @Override
    public void rechainStreamCreation(StreamType type,
        TypeExpressions typeExpression, boolean input)
        throws InstrumenterException {
        int stack = -1;
        
        // do not care about the return value of instruction - after executing
        // instruction it is on the stack. In case that it should be stored
        // to some variable, this will happen anyway in this method, either
        // as static method call or as constructor call to the delegating 
        // stream
        
        // insert parameter dependent on typeExpression or given type
        if (null != typeExpression) {
            switch (typeExpression) {
            case URL_TYPE:
                if (input) {
                    int urlVarNr = getFreeLocalVariable();
                    // save implicit parameter, i.e. the URL
                    tempInstructions.add(new VarInsnNode(ASTORE, urlVarNr));
                    tempInstructions.add(new VarInsnNode(ALOAD, urlVarNr));
                    instructions.insertBefore(instruction, tempInstructions);
                    tempInstructions.clear();

                    // first param is on stack as result of <instruction>
                    tempInstructions.add(new VarInsnNode(ALOAD, urlVarNr));
                    tempInstructions.add(new InsnNode(ACONST_NULL));
                    tempInstructions.add(createMethodInsnNode(INVOKESTATIC, 
                        DELEGATING_INPUTSTREAM, "createFrom", 
                        "(Ljava/io/InputStream;Ljava/net/URL;" 
                        + "Ljava/lang/String;)Ljava/io/InputStream;"));
                    releaseLocalVariable(urlVarNr);
                    stack = 2;
                } else {
                    throw new InstrumenterException("no output code for " 
                        + typeExpression);
                }
                break;
            default:
                // future alternatives set stack at least to zero
                break;
            }
        }
        if (stack < 0) {
            // TODO validate
            // first param is on stack as result of <instruction>
            tempInstructions.add(new FieldInsnNode(GETSTATIC, STREAM_TYPE, 
                type.name(), STREAM_TYPE_DESCR));
            tempInstructions.add(new InsnNode(ACONST_NULL));
            String delegate;
            if (input) {
                delegate = DELEGATING_INPUTSTREAM;
            } else {
                delegate = DELEGATING_OUTPUTSTREAM;
            }
            tempInstructions.add(new TypeInsnNode(NEW, delegate));
            tempInstructions.add(new InsnNode(DUP));
            tempInstructions.add(createMethodInsnNode(INVOKESPECIAL, 
                delegate, ABehavior.CONSTRUCTOR_NAME, 
                "(Ljava/io/InputStream; " + STREAM_TYPE_DESCR 
                + "Ljava/lang/String;)V"));
            stack = 2;
        }

        if (tempInstructions.size() > 0) {
            instructions.insert(instruction, tempInstructions);
            if (stack > 0) {
                maxStack += stack; // wc, no change to maxLocals
            }
        }
    }

    /**
     * Notifies about a thread start and insert value context change 
     * notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    @Override
    public void notifyThreadStarted(String contextId)
        throws InstrumenterException {

        int varNr = getFreeLocalVariable();
        // save implicit parameter
        tempInstructions.add(new VarInsnNode(ASTORE, varNr));

        appendContextNotification(contextId, true);
        appendRecorderCallProlog(tempInstructions, false);

        // obtain parameter for recorder call
        tempInstructions.add(new VarInsnNode(ALOAD, varNr));
        // use the same implicit object here ;)
        maxStack++;
        
        appendRecorderCall(tempInstructions, "notifyThreadStart", 
            "(Ljava/lang/Thread;)V");
        appendContextNotification(contextId, false);
        // restore parameter
        tempInstructions.add(new VarInsnNode(ALOAD, varNr));
        releaseLocalVariable(varNr);
        insertBefore(instruction);
    }

    /**
     * Notifies about an individual data transmission call and insert value 
     * context change notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @param write is this a write or a read operation
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    @Override
    public void notifyIoDatagramTransmission(String contextId, boolean write)
        throws InstrumenterException {
        int stack = 0;
        appendContextNotification(contextId, true);

        // save first and only parameter
        int varNr = getFreeLocalVariable();
        tempInstructions.add(new VarInsnNode(ASTORE, varNr));
        tempInstructions.add(new VarInsnNode(ALOAD, varNr));
        maxStack(1);
        insertBefore(instruction);
        
        appendRecorderCallProlog(tempInstructions, false);
        tempInstructions.add(new InsnNode(ACONST_NULL));
        stack++;
        tempInstructions.add(new InsnNode(ACONST_NULL));
        stack++;
        tempInstructions.add(new VarInsnNode(ALOAD, varNr));
        stack++;
        tempInstructions.add(createMethodInsnNode(INVOKEVIRTUAL, 
            "java/net/DatagramPacket", "getLength", "()I"));
        tempInstructions.add(new FieldInsnNode(GETSTATIC, STREAM_TYPE, 
            "NET", STREAM_TYPE_DESCR));
        String method;
        if (write) {
            method = "writeIo";
        } else {
            method = "readIo";
        }
        appendRecorderCall(tempInstructions, method, READ_WRITE_DESCR);

        // result unused
        tempInstructions.add(new InsnNode(POP));
        maxStack(stack);
        appendContextNotification(contextId, false);        
        // restore parameter
        //tempInstructions.add(new VarInsnNode(ALOAD, varNr));
        releaseLocalVariable(varNr);
        instructions.insert(instruction, tempInstructions);
    }

    /**
     * Leave the underlying code as it is and just insert value context change
     * notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    @Override
    public void notifyContextChange(String contextId)
        throws InstrumenterException {
        appendContextNotification(contextId, true);
        insertBefore(instruction);
        appendContextNotification(contextId, false);
        instructions.insert(instruction, tempInstructions);
    }

    /**
     * Returns the name of the declaring class of the current method.
     * This method works properly only if the statement being processed is a 
     * method call. [performance]
     * 
     * @return the name of the declaring class.
     * @throws InstrumenterException in case that the declaring class is 
     *   not found
     * 
     * @since 1.00
     */
    @Override
    public String mcDeclaringClassName() throws InstrumenterException {
        return mcDeclaringClass;
    }

    /**
     * Returns the declaring class of the current method is instance of the 
     * given <code>type</code>, whereby <code>type</code> may be a superclass.
     * This method works properly only if the statement being processed is a 
     * method call. [performance]
     * 
     * @param type the type the declaring class should be a subclass of
     * @return <code>true</code> if this class is a subclass of 
     *     <code>type</code>, <code>false</code> else
     *     
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     * 
     * @since 1.00
     */
    @Override
    public boolean mcDeclaringClassInstanceOf(String type)
        throws InstrumenterException {
        IClass cls = Factory.getLocalFactory().obtainClass(
            Factory.toInternalName(mcDeclaringClass), loader);
        return cls.isInstanceOf(type);
    }

    /**
     * Returns the number of parameters of the current method call. This method 
     * works properly only if the statement being processed is a method call.
     * [performance]
     * 
     * @return the number of parameters of the current method call
     * 
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     *     
     * @since 1.00
     */
    @Override
    public int mcParameterCount() throws InstrumenterException {
        if (null == mcParameter) {
            mcParameter = ABehavior.scanParameter(mcDescriptor, true);
        }
        return mcParameter.size();
    }

    /**
     * Returns the type name of the given parameter of the current method call. 
     * This method works properly only if the statement being processed is a 
     * method call. [performance]
     * 
     * @param index the index of the parameter, 
     *     <code>0&lt;=index&lt;{@link #mcParameterCount()}</code>
     * @return the type name 
     * 
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     * @throws IndexOutOfBoundsException in case that <code>index</code> is 
     *     wrong
     * 
     * @since 1.00
     */
    @Override
    public String mcParameterTypeName(int index)
        throws InstrumenterException {
        if (null == mcParameter) {
            mcParameter = ABehavior.scanParameter(mcDescriptor, true);
        }
        return mcParameter.get(index);
    }

    /**
     * Returns whether the current method call goes to a static 
     * method. This method works properly only if the statement being 
     * processed is a method call. [performance]
     * 
     * @return <code>true</code> if the called method is static, 
     *   <code>false</code> else
     *   
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     * 
     * @since 1.00
     */
    @Override
    public boolean mcIsStatic() throws InstrumenterException {
        return mcIsStatic;
    }
    
    /**
     * Adjusts the value of {@link #maxStack} based on the stack requirement
     * of the inserted fragment, the current value of {@link #maxStack} and
     * the current stack usage {@link #curStack}.
     * 
     * @param increment the increment required by the actual fragment created
     *   by the instrumentation
     * 
     * @since 1.00
     */
    private void maxStack(int increment) {
        maxStack = Math.max(maxStack, 
            Math.max(increment, curStack + increment));
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for the current field access. This method does not
     * support pruning of annotations. This method works properly only if the 
     * statement being processed is a field access. [performance]
     * 
     * @param <T> the type of the annotation
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param search the annotation search type
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    @Override
    public <T extends Annotation> T fAnnotation(Class<T> annotation,
        AnnotationSearchType search) {
        T result = null;
        try {
            Factory factory = Factory.getLocalFactory();
            IClass cls = factory.obtainClass(fDeclaringClass, loader);
            int count = cls.getDeclaredFieldCount();
            IField field = null;
            for (int i = 0; i < count; i++) {
                field = cls.getDeclaredField(i);
            }
            if (null != field) {
                boolean done = false;
                XMLConfiguration config = Configuration.INSTANCE.getXMLConfig();
                if (null != config) {
                    result = config.getAnnotation(field.getSignature(), 
                        cls, annotation, factory);
                    done = config.isExclusive();
                } 
                if (!done && null == result) {
                    result = field.getAnnotation(annotation, false);
                }
            }
        } catch (InstrumenterException e) {
            // exception = e;
        }
        return result;
    }

    /**
     * Returns the name of the declaring class of the current field.
     * This method works properly only if the statement being processed is a 
     * field access. [performance]
     * 
     * @return the name of the declaring class.
     * @throws InstrumenterException in case that the declaring class is 
     *   not found
     * 
     * @since 1.00
     */
    @Override
    public String fDeclaringClassName() throws InstrumenterException {
        return Factory.toCodeName(fDeclaringClass, true);
    }
    
    /**
     * Is called in case of a field access.
     * 
     * @param editor the behavior editor
     * @param fNode the field access instruction node
     * @param isWriter whether this call is a writing call
     * @throws InstrumenterException in case of any code problem
     * 
     * @since 1.00
     */
    private final void fieldAccess(BehaviorEditor editor, 
        FieldInsnNode fNode, boolean isWriter) throws InstrumenterException {
        // true unclear here
        fDeclaringClass = fNode.owner;
        editor.editFieldAccess(fNode.name, 
            Factory.toCodeName(fNode.desc, true), isWriter);
        fDeclaringClass = null;
    }
    
    /**
     * Loops over the given <code>behavior</code>.
     * 
     * @param behavior the behavior to modify
     * @param editor the editor to be notified which contains high-level 
     *   transformation logic
     * @throws InstrumenterException in case of any instrumentation error
     * 
     * @since 1.00
     */
    public void loop(ABehavior behavior, BehaviorEditor editor) 
        throws InstrumenterException {
        nodeToBeInstrumented = behavior.getNode();
        try {
            maxStack = nodeToBeInstrumented.maxStack;
            loader = behavior.getDeclaringAClass().getClassLoader();
            instructions = behavior.getNode().instructions;
            lastNew.clear();
            ListIterator<AbstractInsnNode> i = instructions.iterator();
            while (i.hasNext()) {
                instruction = i.next();
                int opcode = instruction.getOpcode();
                if (PUTFIELD == opcode || PUTSTATIC == opcode) {
                    fieldAccess(editor, (FieldInsnNode) instruction, true);
                } else if (GETFIELD == opcode || GETSTATIC == opcode) {
                    fieldAccess(editor, (FieldInsnNode) instruction, false);
                } else if (NEWARRAY == opcode || MULTIANEWARRAY == opcode 
                    || ANEWARRAY == opcode) {
                    editor.editNewArray();
                } else if (NEW == opcode) {
                    //avoids this()/super()
                    lastNew.add((TypeInsnNode) instruction); 
                } else if (ANEWARRAY == opcode) {
                    editor.editNewArray();
                } else if (INVOKEVIRTUAL == opcode || INVOKESPECIAL == opcode 
                    || INVOKESTATIC == opcode || INVOKEINTERFACE == opcode) {
                    MethodInsnNode mNode = (MethodInsnNode) instruction;
                    if (INVOKESPECIAL == opcode && !lastNew.isEmpty() 
                        && ABehavior.CONSTRUCTOR_NAME.equals(mNode.name)) {
                        int topIndex = lastNew.size() - 1;
                        TypeInsnNode top = lastNew.get(topIndex);
                        // catches <init> of correct type
                        if (top.desc.equals(mNode.owner)) {
                            editor.editNewExpression(
                                Factory.toCodeName(top.desc, true));
                            lastNew.remove(topIndex);
                            mNode = null;
                        }
                    } 
                    if (null != mNode && !ABehavior.CONSTRUCTOR_NAME.equals(
                        mNode.name)) {
                        mcIsStatic = (INVOKESTATIC == opcode);
                        mcDescriptor = mNode.desc;
                        mcDeclaringClass 
                            = Factory.toCodeName(mNode.owner, true);
                        editor.editMethodCall(mNode.name, mNode.desc, 
                            mcDeclaringClass);
                        mcDeclaringClass = null;
                        mcDescriptor = null;
                        mcIsStatic = false;
                        mcParameter = null;
                    } 
                } //else if (Opcodes.INVOKEDYNAMIC == opcode) {
                    //InvokeDynamicInsnNode idNode = 
                    //    (InvokeDynamicInsnNode) instruction;
                    //TODO jdk 1.7
                //}
                int tmp = Utils.stackModification(instruction);
                int push = Utils.getLSB(tmp);
                int pop = Utils.getMSB(tmp);
                curStack += (push - pop);
            }
        } catch (InstrumenterException e) {
            throw e;
        } finally {
            nodeToBeInstrumented.maxStack = maxStack;
            tempInstructions.clear();
            instruction = null;
            instructions = null;
            loader = null;
            for (int i = 0; i < usedLocals.length; i++) {
                usedLocals[i] = 0;
            }
            curStack = 0;
            maxStack = 0;
            nodeToBeInstrumented = null;
            lastNew.clear();
        }
    }
    
}

/**
 * Creates instructions to save all parameters of the method invocation
 * <code>mNode</code> into local variables and to restore the stack 
 * immediately. The new operations are inserted into {@link #instructions}
 * before {@link #instruction}.
 * 
 * @param mNode the node representing the method cell
 * @return the numbers of the local variables; <b>null</b> if no local 
 *   variables were used, all local variables else (if <code>mNode</code> 
 *   has an implicit object reference, the variable number will be
 *   stored in the first position of the array)
 * 
 * @since 1.00
 */
/*private int[] saveAllParameters(MethodInsnNode mNode) {
    return saveAllParameters(mNode, 
        hasImplicitFirstArgument(mNode.getOpcode()));
}*/

/**
 * Creates instructions to save all parameters of the method invocation
 * <code>mNode</code> into local variables and to restore the stack 
 * immediately. The new operations are inserted into {@link #instructions}
 * before {@link #instruction}.
 * 
 * @param mNode the node representing the method cell
 * @param includingObjectReference shall also the implicit object reference
 *   be saved
 * @return the numbers of the local variables; <b>null</b> if no local 
 *   variables were used, all local variables else (if 
 *   <code>includingObjectReference</code> is enabled the variable number 
 *   will be stored in the first position of the array)
 * 
 * @since 1.00
 */
/*private int[] saveAllParameters(MethodInsnNode mNode, 
    boolean includingObjectReference) {
    ArrayList<String> param = ABehavior.scanParameter(
        mNode.desc, false);
    int pCount = param.size();
    int[] varNr;
    if (0 == pCount && !includingObjectReference) {
        varNr = null;
    } else {
        varNr = new int[pCount + 1];
        InsnList instr = new InsnList();
        ArrayList<AbstractInsnNode> recreate 
            = new ArrayList<AbstractInsnNode>();
        int offset;
        if (includingObjectReference) {
            offset = 1;
        } else {
            offset = 0;
        }
        for (int v = pCount - 1; v >= 0; v--) {
            int varIndex = v + offset;
            varNr[varIndex] = getFreeLocalVariable();
            String type = param.get(v);
            char typeC;
            
            if (type.length() > 1) {
                typeC = Factory.INTERNAL_OBJECT_PREFIX;
            } else {
                typeC = type.charAt(0);
            }

            int loadOp;
            int storeOp;

            // determine load and store operations
            switch (typeC) {
            case Factory.INTERNAL_BOOLEAN:
            case Factory.INTERNAL_INT:
            case Factory.INTERNAL_BYTE:
            case Factory.INTERNAL_CHAR:
            case Factory.INTERNAL_SHORT:
                loadOp = ILOAD;
                storeOp = ISTORE;
                break;
            case Factory.INTERNAL_DOUBLE:
                loadOp = DLOAD;
                storeOp = DSTORE;
                break;
            case Factory.INTERNAL_FLOAT:
                loadOp = FLOAD;
                storeOp = FSTORE;
                break;
            case Factory.INTERNAL_LONG:
                loadOp = LLOAD;
                storeOp = LSTORE;
                break;
            default:
                loadOp = ALOAD;
                storeOp = ASTORE;
                break;
            }
            // store stack top
            instr.add(new VarInsnNode(storeOp, varNr[varIndex]));
            recreate.add(new VarInsnNode(loadOp, varNr[varIndex]));
        }
        if (includingObjectReference) {
            varNr[0] = getFreeLocalVariable();
            instr.add(new VarInsnNode(ASTORE, varNr[0]));
            instr.add(new VarInsnNode(ALOAD, varNr[0]));
        }
        for (int r = recreate.size() - 1; r >= 0; r--) {
            instr.add(recreate.get(r));
        }
        instructions.insertBefore(instruction, instr);
    }
    return varNr;
}*/

/**
 * Copies the stack instructions to for the specified parameter. The
 * resulting instruction objects are added to {@link #tempInstructions}.
 * This method currently works only with object references and integer
 * values (as other types are not used by the recorder interface).
 * 
 * @param instruction the instruction node to start from
 * @param desc the descriptor of the method call
 * @param param the 0-based parameter number to copy
 * @return worst case stack usage in the MSB, the local variable number
 *     used to store the value and to be released by 
 *     {@link #releaseLocalVariable(int)} in the LSB
 * @throws InstrumenterException in case that anything goes wrong
 * 
 * @since 1.00
 */
/*    private int copyParameter(AbstractInsnNode instruction, String desc, 
    int param) throws InstrumenterException {
    int stack = 0;
    int varNr = 0;
    AbstractInsnNode previous = findParameter(instruction, desc, param);
    if (null != previous) {
        int opcode = previous.getOpcode();
        if (ALOAD == opcode) {
            copy(previous, NO_NEW_OP, null, tempInstructions);
            stack = 2;
        } else if (ILOAD == opcode) {
            copy(previous, NO_NEW_OP, null, tempInstructions);
            stack = 2;
        } else if (AALOAD == opcode) {
            varNr = handleReturnResult(previous, ";");
            stack = 2;
        } else if (IALOAD == opcode) {
            varNr = handleReturnResult(previous, "I");
            stack = 2;
        } else if (GETSTATIC == opcode) {
            copy(previous, NO_NEW_OP, null, tempInstructions);
            stack = 2;
        } else if (GETFIELD == opcode) {
            AbstractInsnNode prev = previous.getPrevious();
            if (null != prev) {
                copy(prev, NO_NEW_OP, null, tempInstructions);
                copy(previous, NO_NEW_OP, null, tempInstructions);
                stack = 2 + 2;
            } else {
                throw new InstrumenterException(toString(instruction) 
                    + "no previous instruction");
            }
        } else if (CHECKCAST == opcode) {
            AbstractInsnNode prev = previous.getPrevious();
            if (null != prev && ALOAD == prev.getOpcode()) {
                // might fail as checkcast follows...
                copy(prev, NO_NEW_OP, null, tempInstructions);
            } else {
                TypeInsnNode cNode = (TypeInsnNode) previous;
                varNr = handleReturnResult(previous, cNode.desc);
            }                
        } else if (AbstractInsnNode.METHOD_INSN == previous.getType()) {
            MethodInsnNode mNode = (MethodInsnNode) previous;
            varNr = handleReturnResult(previous, mNode.desc);
        } else {
            throw new InstrumenterException(toString(instruction) 
                + ":parameter instruction " + opcode);
        }
    } else {
        throw new InstrumenterException(toString(instruction) 
            + ": no parameter " + param);
    }
    return buildInt(stack, varNr); 
}*/

/**
 * Returns the operations needed to load/store a value of the given type 
 * (see Factory constants).
 * 
 * @param type the type the operations shall be returned for
 * @return the operations (store in MSB, load in LSB)
 * 
 * @since 1.00
 */
/*private long ops(char type) {
    int loadOp;
    int storeOp;

    // determine load and store operations dependent on the concrete type
    switch (type) {
    case Factory.INTERNAL_BOOLEAN:
    case Factory.INTERNAL_INT:
    case Factory.INTERNAL_BYTE:
    case Factory.INTERNAL_CHAR:
    case Factory.INTERNAL_SHORT:
        loadOp = ILOAD;
        storeOp = ISTORE;
        break;
    case Factory.INTERNAL_DOUBLE:
        loadOp = DLOAD;
        storeOp = DSTORE;
        break;
    case Factory.INTERNAL_FLOAT:
        loadOp = FLOAD;
        storeOp = FSTORE;
        break;
    case Factory.INTERNAL_LONG:
        loadOp = LLOAD;
        storeOp = LSTORE;
        break;
    default:
        loadOp = ALOAD;
        storeOp = ASTORE;
        break;
    }
    return buildLong(storeOp, loadOp);
}*/

/**
 * Handles a return result, i.e. produces code to provide access to the 
 * return result of <code>instruction</code>, regardless whether 
 * <code>instruction</code> is a method call or a type cast. 
 * 
 * @param instruction the instruction to handle the return result for
 * @param descriptor the type descriptor of the return result
 * @return the local variable used (<code>0</code> if none)
 * 
 * @since 1.00
 */
/*private int handleReturnResult(AbstractInsnNode instruction, 
    String descriptor) {
    // following two are reversed as directly inserted 
    // after previous
    char type;
    if (1 == descriptor.length()) {
        type = descriptor.charAt(0);
    } else {
        type = Factory.INTERNAL_OBJECT_SUFFIX;
    }

    long tmp = ops(type);
    int loadOp = getLongLSB(tmp);
    int storeOp = getLongMSB(tmp);

    // determine (or aquire) a free local variable, store the value, load
    // it back to the stack for normal operations and add to the set of
    // instructions to be inserted during instrumentation the appropriate
    // load operation
    int varNr = getFreeLocalVariable();
    instructions.insert(instruction, new VarInsnNode(loadOp, varNr));
    instructions.insert(instruction, new VarInsnNode(storeOp, varNr));
    tempInstructions.add(new VarInsnNode(loadOp, varNr));
    return varNr;
}*/

/**
 * Returns a textual description of <code>instruction</code> for debugging.
 * 
 * @param instruction the instruction to be turned into a String
 * @return the textual description
 * 
 * @since 1.00
 */
/*private static String toString(AbstractInsnNode instruction) {
    String result;
    if (AbstractInsnNode.METHOD_INSN == instruction.getType()) {
        MethodInsnNode mNode = (MethodInsnNode) instruction;
        result = mNode.owner + "." + mNode.name;
    } else {
        result = "<unrecognized> " + instruction.getOpcode();
    }
    return result;
}*/

/*        
OLD: insert before     
 
AbstractInsnNode insertBefore = instruction;
if (AbstractInsnNode.METHOD_INSN == instruction.getType()) {
    MethodInsnNode mNode = (MethodInsnNode) instruction;
    List<String> param = ABehavior.scanParameter(mNode.desc);
    if (hasImplicitFirstArgument(mNode.getOpcode())) {
        param.add(0, Factory.JAVA_LANG_OBJECT);
    }
    int count = param.size();
    int[] vars = new int[count];
    for (int v = 0; v < count; v++) {
        vars[v] = getFreeLocalVariable();
        String type = param.get(v);
        long tmp;
        if (type.length() > 1) {
            tmp = ops((char) 0);
        } else {
            tmp = ops(type.charAt(0));
        }
        int loadOp = getLongLSB(tmp);
        int storeOp = getLongMSB(tmp);
        AbstractInsnNode first = tempInstructions.getFirst();
        tempInstructions.insert(instruction, 
            new VarInsnNode(storeOp, vars[v]));
        tempInstructions.add(new VarInsnNode(loadOp, vars[v]));
    }
    for (int v = 0; v < count; v++) {
        releaseLocalVariable(vars[v]);
    }
    mNode.desc
    // save param
    instructions.insertBefore(instruction, tempInstructions);
    // restore param
}
if (null != insertBefore) {
} else {
    throw new InstrumenterException(
        "beginning of statement not found");
}
}*/
