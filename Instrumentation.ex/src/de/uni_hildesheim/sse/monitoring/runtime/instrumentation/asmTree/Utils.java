package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javassist.bytecode.Opcode;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderAccess;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IBehavior;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Some utility methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
class Utils implements Opcode {

    /**
     * Defines a pseudo instruction which signals 
     * {@link #copy(AbstractInsnNode, int, boolean)} to take the original
     * opcode.
     */
    public static final int NO_NEW_OP = Integer.MIN_VALUE;

    /**
     * Stores the descriptor of <code>java.lang.ClassLoader</code>.
     */
    public static final String CLASSLOADER_DESCR = 
        getClassTypeDescriptor(ClassLoader.class.getName(), false);

    /**
     * Stores the internal name of <code>java.lang.String</code>.
     */
    public static final String STRING 
        = Factory.toInternalName(String.class.getName());
    
    /**
     * Stores the descriptor of <code>java.lang.String</code>.
     */
    public static final String STRING_DESCR = 
        getClassTypeDescriptor(String.class.getName(), false);

    /**
     * Stores the internal name of <code>java.lang.Throwable</code>.
     */
    public static final String THROWABLE 
        = Factory.toInternalName(Throwable.class.getName());
    
    /**
     * Stores the internal name of <code>java.lang.Class</code>.
     */
    public static final String CLASS 
        = Factory.toInternalName(Class.class.getName());

    /**
     * Stores the internal name of <code>java.lang.System</code>.
     */
    public static final String SYSTEM 
        = Factory.toInternalName(System.class.getName());

    /**
     * Stores the descriptor of <code>java.lang.Class</code>.
     */
    public static final String CLASS_DESCR 
        = getClassTypeDescriptor(Class.class.getName(), false);

    /**
     * Stores the descriptor of <code>java.lang.Object</code>.
     */
    public static final String OBJECT_DESCR 
        = getClassTypeDescriptor(Object.class.getName(), false);
    
    /**
     * Defines the internal name of the recorder frontend class.
     */
    public static final String RECORDER_FRONTEND 
        = Factory.toInternalName(RecorderFrontend.class.getName());
    
    /**
     * Defines the internal name of the recorder access class.
     */
    public static final String RECORDER_ACCESS 
        = Factory.toInternalName(RecorderAccess.class.getName());

    /**
     * Defines the descriptor of the recorder frontend class.
     */
    public static final String RECORDER_FRONTEND_DESCR 
        = getClassTypeDescriptor(RECORDER_FRONTEND, true);
    
    /**
     * Zero operand instructions of category 2 (long or double result).
     */
    private static final int[] CATEGORY_2 = {
        DALOAD, LALOAD, LSTORE, LADD, DADD, LMUL, DMUL, LDIV, DDIV, LREM, LNEG, 
        DNEG, LSHL, LSHR, LUSHR, LAND, LOR, LXOR, I2L, I2D, L2D, F2L, F2D, D2L,
        DREM, DSTORE, DCONST_0, DCONST_1, LCONST_0, LCONST_1, I2L, I2D, L2D, 
        F2L, F2D, D2L, 
        DRETURN, LRETURN
    };

    /**
     * Zero operand instructions of category 1 (neither long or double result).
     */
    private static final int[] CATEGORY_1 = {
        ACONST_NULL, ASTORE, IALOAD, ISTORE, ICONST_M1, ICONST_0, ICONST_1, 
        ICONST_2, ICONST_3, ICONST_4, ICONST_5, FCONST_0, FCONST_1, FCONST_2, 
        I2F, L2I, L2F, F2I, D2I, D2F, I2B, I2C, I2S, LSUB, DSUB, IADD, FADD, 
        ISUB, FSUB, IMUL, FMUL, IDIV, FDIV, IREM, FREM, FALOAD, AALOAD, 
        BALOAD, CALOAD, SALOAD, FSTORE
    };
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Utils() {
    }

    /**
     * Finds the given parameter node.
     * 
     * @param instruction the instruction node to start from
     * @param desc the descriptor of the method call
     * @param param the 0-based parameter number to return
     * @return the parameter instruction found or <b>null</b>
     * 
     * @since 1.00
     */
    /*public static AbstractInsnNode findParameter(
        AbstractInsnNode instruction, String desc, int param) {
        return findParameter(instruction, param, Factory.countParameter(desc));
    }*/
    
    /**
     * Finds the given parameter node. Regularly, this method is called with
     * the correct number of parameters of a method call. For the dup 
     * instructions, this number may not be known while analyzing the stack in
     * reverse direction. Then <code>param = 0</code> and 
     * <code>pCount = 1</code> will deliver the next parameter starting at 
     * <code>instruction</code>.
     * 
     * @param instruction the instruction node to start from
     * @param param the 0-based parameter number to return
     * @param pCount the number of parameters
     * @return the parameter instruction found or <b>null</b>
     * 
     * @since 1.00
     */
    private static AbstractInsnNode findParameter(
        AbstractInsnNode instruction, int param, int pCount) {
        int opcode = instruction.getOpcode();
        if (hasImplicitFirstArgument(opcode)) {
            pCount++;
        }
        AbstractInsnNode result = null;
        if (pCount > 0) {
            if (param >= pCount) {
                param = pCount - 1;
            }
            if (pCount > 0) {
                int offsetCount = 0;
                result = instruction.getPrevious();
                while (null != result 
                    && (pCount - 1 != param || offsetCount > 0)) {
                    int mod = stackModification(result);
                    int push = getLSB(mod);
                    int pop = getMSB(mod);
                    if (0 == offsetCount) {
                        // this is a parameter of the originating instruction
                        pCount -= push;
                        offsetCount = pop; 
                    } else {
                        // this is somewhere in calculating the arguments
                        offsetCount += pop - push;
                    }
                    //pCount--;
                    result = result.getPrevious();
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the least significant bits.
     * 
     * @param value the value to return the LSB for
     * @return the least significant bits
     * 
     * @since 1.00
     */
    public static int getLSB(int value) {
        return value & 0xff;
    }

    /**
     * Returns the most significant bits.
     * 
     * @param value the value to return the MSB for
     * @return the least most bits
     * 
     * @since 1.00
     */
    public static int getMSB(int value) {
        return (value & 0xff00) >> 8;
    }

    /**
     * Constructs and integer from its most and least significant bits.
     * 
     * @param msb the most significant bits
     * @param lsb the least significant bits
     * @return the constructed integer containing both
     * 
     * @since 1.00
     */
    public static int buildInt(int msb, int lsb) {
        return (msb << 8) + lsb; 
    }

    /**
     * Constructs and integer from its most and least significant bits.
     * 
     * @param msb the most significant bits
     * @param lsb the least significant bits
     * @return the constructed integer containing both
     * 
     * @since 1.00
     */
    public static long buildLong(int msb, int lsb) {
        return ((long) msb << 32) + ((long) lsb ); 
    }

    /**
     * Returns the least significant bits.
     * 
     * @param value the value to return the LSB for
     * @return the least significant bits
     * 
     * @since 1.00
     */
    public static int getLongLSB(long value) {
        return (int) value;
    }

    /**
     * Returns the most significant bits.
     * 
     * @param value the value to return the MSB for
     * @return the least most bits
     * 
     * @since 1.00
     */
    public static int getLongMSB(long value) {
        return (int) (value >> 32);
    }

    /**
     * Returns the actual stack modification of the given instruction.
     * 
     * @param instruction the instruction to return the stack modification for
     * @return the number of instructions (not bytes) pushed in the LSB, the 
     *   number of popped instructions the MSB
     * 
     * @since 1.00
     */
    public static int stackModification(AbstractInsnNode instruction) {
        int push = 0;
        int pop = 0;
        int opcode = instruction.getOpcode();
        switch (instruction.getType()) {
        case AbstractInsnNode.FIELD_INSN:
            int tmpF = stackModificationFieldInsn(instruction);
            push = getLSB(tmpF);
            pop = getMSB(tmpF);
            break;
        case AbstractInsnNode.FRAME:
            // pseudo-instruction -> no change to stack
            break;
        case AbstractInsnNode.IINC_INSN:
            // changes local variable -> no change to stack
            break;
        case AbstractInsnNode.INSN:
            int tmpR = stackModificationInsn(instruction);
            push = getLSB(tmpR);
            pop = getMSB(tmpR);
            break;
        case AbstractInsnNode.INT_INSN:
            switch (opcode) {
            case BIPUSH:
            case SIPUSH:
                push = 1;
                break;
            case NEWARRAY:
                pop = 1;
                push = 1;
                break;
            default:
                break;
            }
            break;
        case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
            InvokeDynamicInsnNode idNode = (InvokeDynamicInsnNode) instruction;
            pop = Factory.countParameter(idNode.desc);
            if (Factory.INTERNAL_VOID 
                != idNode.desc.charAt(idNode.desc.length() - 1)) {
                push = 1;
            }
            break;
        case AbstractInsnNode.JUMP_INSN:
            if (IFEQ <= opcode && opcode <= IFLE) {
                pop = 1;
            } else if (IF_ICMPEQ <= opcode && opcode <= IF_ACMPNE) {
                pop = 2;
            } else if (IFNONNULL == opcode || IFNULL == opcode) {
                pop = 1;
            } else if (JSR == opcode) {
                push = 1;
            } // goto does not change
            break;
        case AbstractInsnNode.LABEL:
            // pseudo-argument
            break;
        case AbstractInsnNode.LDC_INSN:
            push = 1;
            break;
        case AbstractInsnNode.LINE:
            // pseudo-argument
            break;
        case AbstractInsnNode.LOOKUPSWITCH_INSN:
            pop = 1;
            break;
        case AbstractInsnNode.METHOD_INSN:
            MethodInsnNode mNode = (MethodInsnNode) instruction;
            pop = Factory.countParameter(mNode.desc);
            if (hasImplicitFirstArgument(opcode)) {
                pop++;
            }
            if (Factory.INTERNAL_VOID 
                != mNode.desc.charAt(mNode.desc.length() - 1)) {
                push = 1;
            }
            break;
        case AbstractInsnNode.MULTIANEWARRAY_INSN:
            MultiANewArrayInsnNode maNode = 
                (MultiANewArrayInsnNode) instruction;
            pop = maNode.dims;
            push = 1;
            break;
        case AbstractInsnNode.TABLESWITCH_INSN:
            pop = 1;
            break;
        case AbstractInsnNode.TYPE_INSN:
            switch (opcode) {
            case NEW:
                push = 1;
                break;
            case ANEWARRAY:
                pop = 1;
                push = 1;
                break;
            case CHECKCAST:
                // remains unchanged if valid
                break;
            case INSTANCEOF:
                pop = 1;
                push = 1;
                break;
            default:
                break;
            }
            break;
        case AbstractInsnNode.VAR_INSN:
            if (ILOAD <= opcode && opcode < IALOAD) {
                // including ILOAD_*, FLOAD_*, DLOAD_*, ALOAD_*
                push = 1;
                
            } else if (ISTORE <= opcode && opcode < IASTORE) {
                // including ISTORE_*, FSTORE_*, DSTORE_*, ASTORE_*
                pop = 1;
            } // no change by RET
            break;
        default:
            break;
        }
        return buildInt(pop, push); 
    }

    /**
     * Returns the stack modification of the given field instruction.
     * 
     * @param instruction the instruction to return the stack modification for
     * @return the number of instructions (not bytes) pushed in the LSB, the 
     *   number of popped instructions the MSB
     * 
     * @since 1.00
     */
    private static int stackModificationFieldInsn(
        AbstractInsnNode instruction) {
        int opcode = instruction.getOpcode();
        int push = 0;
        int pop = 0;
        switch (opcode) {
        case PUTFIELD:
            push = 0;
            pop = 2;
            break;
        case GETFIELD:
            push = 1;
            pop = 1;
            break;
        case PUTSTATIC:
            push = 0;
            pop = 1;
            break;
        case GETSTATIC:
            push = 1;
            pop = 0;
            break;
        default:
            break;
        }
        return buildInt(pop, push); 
    }
    
    /**
     * Returns the stack modification of the given zero operand instruction.
     * 
     * @param instruction the instruction to return the stack modification for
     * @return the number of instructions (not bytes) pushed in the LSB, the 
     *   number of popped instructions the MSB
     * 
     * @since 1.00
     */
    private static int stackModificationInsn(AbstractInsnNode instruction) {
        int opcode = instruction.getOpcode();
        AbstractInsnNode temp;
        int tempCategory;
        int push = 0;
        int pop = 0;
        if (ACONST_NULL <= opcode && opcode < BIPUSH) {
            push = 1;
        } else if (IALOAD <= opcode && opcode < ISTORE) {
            pop = 2;
            push = 1;
        } else if (IADD <= opcode && opcode <= DREM) {
            pop = 2;
            push = 1;
        } else if (INEG <= opcode && opcode <= DNEG) {
            pop = 1;
            push = 1;
        } else if (ISHL <= opcode && opcode <= LUSHR) {
            pop = 2;
            push = 1;
        } else if (IAND <= opcode && opcode <= LXOR) {
            pop = 2;
            push = 1;
        } else if (LCMP <= opcode && opcode <= DCMPG) {
            pop = 2;
            push = 1;
        } else if (IRETURN <= opcode && opcode <= ARETURN) {
            pop = 1;
        } else if (I2L <= opcode && opcode <= I2S) {
            pop = 1;
            push = 1;
        } else {
            switch (opcode) {
            case POP:
                pop = 1;
                break;
            case POP2:
                temp = findParameter(instruction, 0, 1);
                tempCategory = getComputationalCategory(temp);
                if (2 == tempCategory) {
                    // 1 value category 2 comp type pop = 1
                    push = 1;
                    pop = 1;
                } else {
                    // 2 values category 1 comp type pop = 2
                    push = 2;
                    pop = 2;
                }
                break;
            case DUP:
                pop = 1;
                push = 2;
                break;
            case DUP_X1:
                pop = 2;
                push = 3;
                break;
            case DUP_X2:
                temp = findParameter(instruction, 0, 1);
                tempCategory = getComputationalCategory(temp);
                if (2 == tempCategory) {
                    // 2 value category 2 computational type push = 3;
                    push = 3;
                    pop = 2;
                } else {
                    // 3 values category 1 computational type push = 4;
                    push = 4;
                    pop = 3;
                }
                break;
            case DUP2:
                temp = findParameter(instruction, 0, 1);
                tempCategory = getComputationalCategory(temp);
                if (2 == tempCategory) {
                    // 1 value category 2 computational type push = 2;
                    push = 2;
                    pop = 1;
                } else {
                    // 2 values category 1 computational type push = 4;
                    push = 4;
                    pop = 2;
                }
                break;
            case DUP2_X1:
                temp = findParameter(instruction, 0, 1);
                tempCategory = getComputationalCategory(temp);
                if (2 == tempCategory) {
                    // 2 value category 2 computational type push = 3;
                    push = 3;
                    pop = 2;
                } else {
                    // 3 values category 1 computational type push = 5;
                    push = 5;
                    pop = 3;
                }
                break;
            case DUP2_X2:
                temp = findParameter(instruction, 0, 1);
                tempCategory = getComputationalCategory(temp);
                AbstractInsnNode temp1 = findParameter(temp, 0, 1);
                int temp1Category = getComputationalCategory(temp1);
                if (2 == tempCategory) {
                    if (2 == temp1Category) {
                        // 2 value cat 2 push = 3;
                        push = 3;
                        pop = 2;
                    } else {
                        // 2 value cat 1 1 values cat 2 comp type push = 5;
                        push = 5;
                        pop = 3;
                    }
                } else {
                    if (2 == temp1Category) {
                        // 1 value cat 2 2 values cat 1 comp type push = 4;
                        push = 4;
                        pop = 3;
                    } else {
                        // 4 values category 1 comp type push = 6;
                        push = 6;
                        pop = 4;
                    }
                }
                break;
            case SWAP:
                pop = 2;
                push = 2;
                break;
            case ARRAYLENGTH:
            case ATHROW:
                pop = 1;
                push = 1;
                break;
            case MONITORENTER:
            case MONITOREXIT:
                pop = 1;
                break;
            default:
                break;
            }
        } // no change for NOP, RETURN
        return buildInt(pop, push); 
    }

    /**
     * Returns the computational category for the given instruction. In some
     * cases this may involve searching the instructions recursively.
     * 
     * @param instruction the instruction to return the category for
     * @return the computational category, i.e. 2, 1 or 0 if none
     * 
     * @since 1.00
     */
    private static int getComputationalCategory(
        AbstractInsnNode instruction) {
        int category = 0;
        int opcode = instruction.getOpcode();
        AbstractInsnNode temp;

        switch (instruction.getType()) {
        case AbstractInsnNode.FIELD_INSN:
            if (GETFIELD == opcode || GETSTATIC == opcode) {
                FieldInsnNode fNode = (FieldInsnNode) instruction;
                category = getComputationalCategory(fNode.desc);
            }
            break;
        case AbstractInsnNode.FRAME:
            // pseudo-instruction -> no change to stack
            break;
        case AbstractInsnNode.IINC_INSN:
            // changes local variable -> no change to stack
            break;
        case AbstractInsnNode.INSN:
            for (int i = 0; 0 == category && i < CATEGORY_2.length; i++) {
                if (CATEGORY_2[i] == opcode) {
                    category = 2;
                }
            }
    
            for (int i = 0; 0 == category && i < CATEGORY_1.length; i++) {
                if (CATEGORY_1[i] == opcode) {
                    category = 1;
                }
            }
            if (0 == category) {
                switch (opcode) {
                case POP:
                    // no category, just pop
                    break;
                case POP2:
                    // no category, just pop
                    break;
                case DUP:
                case DUP_X1:
                case DUP_X2:
                    category = 1;
                    break;
                case DUP2:
                case DUP2_X1:
                case DUP2_X2:
                    // cat 1 -> cat 1
                    // cat 2 -> cat 2
                    temp = findParameter(instruction, 0, 1);
                    category = getComputationalCategory(temp);
                    break;
                case SWAP:
                    break;
                case ARRAYLENGTH:
                case ATHROW:
                    category = 1;
                    break;
                case MONITORENTER:
                case MONITOREXIT:
                    // no category
                    break;
                default:
                    break;
                }
            } // no change for NOP, RETURN
            break;
        case AbstractInsnNode.INT_INSN:
            category = 1;
            break;
        case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
            InvokeDynamicInsnNode idNode = (InvokeDynamicInsnNode) instruction;
            category = getComputationalCategory(idNode.desc);
            break;
        case AbstractInsnNode.JUMP_INSN:
            // no category, all jumps
            break;
        case AbstractInsnNode.LABEL:
            // pseudo-argument
            break;
        case AbstractInsnNode.LDC_INSN:
            LdcInsnNode lNode = (LdcInsnNode) instruction;
            if (Double.TYPE == lNode.cst.getClass() 
                || Long.TYPE == lNode.cst.getClass()) {
                category = 2;
            } else {
                category = 1;
            }
            break;
        case AbstractInsnNode.LINE:
            // pseudo-argument
            break;
        case AbstractInsnNode.LOOKUPSWITCH_INSN:
            // no real result as execution continues at address
            break;
        case AbstractInsnNode.METHOD_INSN:
            MethodInsnNode mNode = (MethodInsnNode) instruction;
            category = getComputationalCategory(mNode.desc);
            break;
        case AbstractInsnNode.MULTIANEWARRAY_INSN:
            category = 1;
            break;
        case AbstractInsnNode.TABLESWITCH_INSN:
            // no real result as execution continues at address
            break;
        case AbstractInsnNode.TYPE_INSN:
            category = 1;
            break;
        case AbstractInsnNode.VAR_INSN:
            if (DLOAD == opcode || LLOAD == opcode) {
                category = 2;
            } else if (ILOAD <= opcode && opcode < IALOAD) {
                category = 1;
            } // no category for ISTORE...IASTORE, RET
            break;
        default:
            break;
        }
        return category; 
    }

    /**
     * Returns the computational category for the given descriptor.
     * 
     * @param desc the descriptor
     * @return the computational category, i.e. 2, 1 or 0 if none
     * 
     * @since 1.00
     */
    private static int getComputationalCategory(String desc) {
        int category = 0;
        char type = desc.charAt(desc.length() - 1);
        if (Factory.INTERNAL_DOUBLE == type 
            || Factory.INTERNAL_LONG == type) {
            category = 2;
        } else if (Factory.INTERNAL_VOID != type) {
            category = 1;
        }
        return category;
    }

    /**
     * Returns whether the method invocation operation determined by 
     * <code>opcode</code> requires an implicit first (object) argument.
     * 
     * @param opcode the opcode of the operation
     * @return <code>true</code> if it has an implicit first argument, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public static final boolean hasImplicitFirstArgument(int opcode) {
        // invokedynamic does not go here
        return (INVOKEINTERFACE == opcode || INVOKEVIRTUAL == opcode 
            || INVOKESPECIAL == opcode);
    }

    
    /**
     * Appends the call prolog for getting static access to the 
     * instance attribute of the recorder frontend dependent on static or 
     * dynamic instrumentation mode to <code>insn</code>. This method puts a 
     * getStatic onto the stack.
     * 
     * @param insn the instruction set to be modified
     * @param anyway insert the code anyway
     * @return the changes to maxStack in the LSB, the 
     *   changes to maxLocals in the MSB
     * 
     * @since 1.00
     */
    public static int appendRecorderCallProlog(InsnList insn, boolean anyway) {
        int stackMax = 0;
        if (anyway || !Configuration.INSTANCE.isStaticInstrumentation()) {
            insn.add(new FieldInsnNode(GETSTATIC, 
                RECORDER_FRONTEND, "instance", 
                RECORDER_FRONTEND_DESCR));
            stackMax = 1;
        }
        return buildInt(0, stackMax);
    }

    /**
     * Appends the recorder call dependent on static or dynamic instrumentation
     * mode to <code>insn</code>.
     * 
     * @param insn the instruction set to be modified
     * @param name the name of the method
     * @param desc the method descriptor
     * @return the changes to maxStack in the LSB, the 
     *   changes to maxLocals in the MSB
     * 
     * @since 1.00
     */
    public static int appendRecorderCall(InsnList insn, String name, 
        String desc) {
        if (Configuration.INSTANCE.isStaticInstrumentation()) {
            insn.add(createMethodInsnNode(INVOKESTATIC, 
                RECORDER_ACCESS, name, desc));
        } else {
            insn.add(createMethodInsnNode(INVOKEVIRTUAL, 
                RECORDER_FRONTEND, name, desc));
        }
        return buildInt(0, 0);
    }

    /**
     * Old style MethodInsnNode creation after update of ASM to JDK 8.
     * 
     * @param opcode the opcode of the type instruction to be constructed
     * @param owner the internal name of the method's owner class
     * @param name the method's name
     * @param desc the method's descriptor
     * @return the instance
     * 
     * @since 1.00
     */
    static MethodInsnNode createMethodInsnNode(final int opcode, 
        final String owner, final String name, final String desc) {
        return new MethodInsnNode(opcode, owner, name, desc, 
            opcode == Opcodes.INVOKEINTERFACE);
    }
    
    /**
     * Returns the class type descriptor.
     * 
     * @param name the name of the class
     * @param isInternalName whether <code>name</code> is yet an internal
     *   name or a code name
     * @return the class type descriptor
     * 
     * @since 1.00
     */
    public static final String getClassTypeDescriptor(String name, 
        boolean isInternalName) {
        if (!isInternalName) {
            name = Factory.toInternalName(name);
        }
        return "L" + name + ";";
    }

    /**
     * Returns the internal name contained in the class type descriptor.
     * 
     * @param desc the descriptor
     * @return the internal name
     * 
     * @since 1.00
     */
    public static final String getInternalNameFromDescriptor(String desc) {
        String result;
        if (null != desc) {
            int last = desc.length() - 1;
            if (desc.length() > 2 && 'L' == desc.charAt(0) 
                && ';' == desc.charAt(last)) {
                result = desc.substring(1, last);
            } else {
                result = desc;
            }
        } else {
            result = null;
        }
        return result;
    }

    
    /**
     * Inserts the statements in <code>source</code> before the first statement 
     * of <code>target</code>. The statements in <code>source</code> must not 
     * alter any local variables and keep the stack as it is.
     * 
     * @param source the source instructions to be inserted
     * @param target the target instructions to be inserted
     * @throws InstrumenterException if anything goes wrong
     * 
     * @since 1.00
     */
    public static final void insertAtBeginning(InsnList source, 
        InsnList target) throws InstrumenterException {
        target.insertBefore(target.getFirst(), source);
    }

    
    /**
     * Inserts the statements in <code>source</code> at the end of 
     * <code>target</code> but before return (if there is any). The
     * statements in <code>source</code> must not alter any local variables and
     * keep the stack as it is.
     * 
     * @param source the source instructions to be inserted
     * @param target the target instructions to be inserted
     * @param maxLocals the current number of local variables in case that NOPs
     *   should be replaced by the return value, no further code modification 
     *   if negative
     * @param method the target method in case to adjust labels
     * @return the increment to the maxLocals if required in MSB, the maxStack
     *   in LSB 
     * @throws InstrumenterException if anything goes wrong
     * 
     * @since 1.00
     */
    public static final int insertBeforeReturn(InsnList source, 
        InsnList target, int maxLocals, MethodNode method) 
        throws InstrumenterException {
        int incMaxLocals = 0;
        int maxStack = 0;
        if (0 == target.size()) {
            target.add(source);
        } else {
            // InsnList.get does the same
            AbstractInsnNode[] targetInsn = target.toArray(); 
            int ret = previousReturn(targetInsn, target.size() - 1);
            InsnList copy = null;
            while (ret >= 0) {
                AbstractInsnNode retNode = targetInsn[ret];
                int prevRet = previousReturn(targetInsn, ret - 1);
                if (prevRet >= 0) {
                    if (null == copy) {
                        copy = new InsnList();
                    } else {
                        copy.clear();
                    }
                    copy(source, copy, method);
                } else {
                    copy = source;
                }
                if (maxLocals >= 0) { // -> replace NOPs by return value
                    boolean insert = true;
                    int loadOp = 0;
                    int storeOp =  0;
                    switch (retNode.getOpcode()) {
                    case ARETURN:
                        loadOp = ALOAD;
                        storeOp = ASTORE;
                        break;
                    case IRETURN:
                        loadOp = ILOAD;
                        storeOp = ISTORE;
                        break;
                    case LRETURN:
                        loadOp = LLOAD;
                        storeOp = LSTORE;
                        break;
                    case FRETURN:
                        loadOp = FLOAD;
                        storeOp = FSTORE;
                        break;
                    case DRETURN:
                        loadOp = DLOAD;
                        storeOp = DSTORE;
                        break;
                    default:
                        insert = false;
                        break;
                    }
                    if (insert) {
                        copy.insertBefore(copy.getFirst(), new VarInsnNode(
                            storeOp, maxLocals));
                        copy.add(new VarInsnNode(loadOp, maxLocals));
                        for (int i = copy.size() - 1; i >= 0; i--) {
                            AbstractInsnNode node = copy.get(i);
                            if (NOP == node.getOpcode()) {
                                copy.set(node, 
                                    new VarInsnNode(loadOp, maxLocals));
                            }
                        }
                        incMaxLocals = 1;
                    }
                }
                target.insertBefore(retNode, copy);
                // now ret is shifted but not prevRet (if it exists)
                ret = prevRet;
            }
        }
        return buildInt(incMaxLocals, maxStack);
    }

    /**
     * Finds the next return starting at <code>pos</code> in <code>insn</code>.
     * 
     * @param insn the instruction list to search for
     * @param pos the start position
     * @return the position of the previous return or <code>-1</code> if there 
     *   is none
     * 
     * @since 1.00
     */
    private static int previousReturn(AbstractInsnNode[] insn, int pos) {
        int result = -1;
        while (pos >= 0) {
            int opc = insn[pos].getOpcode();
            boolean isReturn = IRETURN == opc || LRETURN == opc 
                || FRETURN == opc;
            isReturn |= DRETURN == opc || ARETURN == opc || RETURN == opc;
            if (isReturn) {
                result = pos;
                break;
            }
            pos--;
        }
        return result;
    }

    /**
     * Copies the given instruction and replaces (if needed and possible) 
     * the opcode.
     * 
     * @param instruction the instruction to copy
     * @param newOpcode the new opcode (or {@link #NO_NEW_OP} if the original
     *   one should be used)
     * @param labelMap the labels to be replaced, otherwise labels are ignored
     * @param insn the instruction list to be modified as a side effect
     * @throws InstrumenterException if anything goes wrong
     * 
     * @since 1.00
     */
    public static void copy(AbstractInsnNode instruction, int newOpcode, 
        HashMap<LabelNode, LabelNode> labelMap, InsnList insn) 
        throws InstrumenterException {
        AbstractInsnNode result;
        int opcode;
        if (newOpcode != NO_NEW_OP) {
            opcode = newOpcode;
        } else {
            opcode = instruction.getOpcode();
        }
        switch (instruction.getType()) {
        case AbstractInsnNode.FIELD_INSN:
            FieldInsnNode fNode = (FieldInsnNode) instruction;
            result = new FieldInsnNode(opcode, fNode.owner, fNode.name, 
                fNode.desc);
            break;
        case AbstractInsnNode.FRAME:
            FrameNode frNode = (FrameNode) instruction;
            result = new FrameNode(frNode.type, frNode.local.size(), 
                frNode.local.toArray(), frNode.stack.size(), 
                frNode.stack.toArray());
            break;
        case AbstractInsnNode.IINC_INSN:
            IincInsnNode i1Node = (IincInsnNode) instruction;
            result = new IincInsnNode(i1Node.var, i1Node.incr);
            break;
        case AbstractInsnNode.INSN:
            result = new InsnNode(opcode);
            break;
        case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
            InvokeDynamicInsnNode i3Node = (InvokeDynamicInsnNode) instruction;
            result = new InvokeDynamicInsnNode(i3Node.name, i3Node.desc, 
                i3Node.bsm, i3Node.bsmArgs);
            break;
        case AbstractInsnNode.JUMP_INSN:
            JumpInsnNode jNode = (JumpInsnNode) instruction;
            result = new JumpInsnNode(opcode, 
                replaceLabel(jNode.label, labelMap));
            break;
        case AbstractInsnNode.LABEL:
            if (null != labelMap) {
                result = replaceLabel((LabelNode) instruction, labelMap);
            } else {
                result = null;
            }
            break;
        case AbstractInsnNode.LDC_INSN:
            LdcInsnNode lNode = (LdcInsnNode) instruction;
            result = new LdcInsnNode(lNode.cst);
            break;
        case AbstractInsnNode.LINE:
            result = null;
            break;
        case AbstractInsnNode.LOOKUPSWITCH_INSN:
            LookupSwitchInsnNode l2Node = (LookupSwitchInsnNode) instruction;
            int size = l2Node.keys.size();
            int[] keys = new int[l2Node.keys.size()];
            for (int i = 0; i < size; i++) {
                keys[i] = l2Node.keys.get(i);
            }
            result = new LookupSwitchInsnNode(
                replaceLabel(l2Node.dflt, labelMap), keys, 
                replaceLabels(l2Node.labels, labelMap));
            result = null;
            break;
        case AbstractInsnNode.METHOD_INSN:
            MethodInsnNode m1Node = (MethodInsnNode) instruction;
            result = createMethodInsnNode(opcode, m1Node.owner, m1Node.name, 
                m1Node.desc);
            break;
        case AbstractInsnNode.MULTIANEWARRAY_INSN:
            MultiANewArrayInsnNode m2Node = 
                (MultiANewArrayInsnNode) instruction;
            result = new MultiANewArrayInsnNode(m2Node.desc, m2Node.dims);
            break;
        case AbstractInsnNode.TABLESWITCH_INSN:
            TableSwitchInsnNode tsNode = (TableSwitchInsnNode) instruction;
            result = new TableSwitchInsnNode(tsNode.min, tsNode.max, 
                replaceLabel(tsNode.dflt, labelMap), 
                replaceLabels(tsNode.labels, labelMap));
            result = null;
            break;
        case AbstractInsnNode.TYPE_INSN:
            TypeInsnNode tNode = (TypeInsnNode) instruction;
            result = new TypeInsnNode(opcode, tNode.desc);
            break;
        case AbstractInsnNode.VAR_INSN:
            VarInsnNode vNode = (VarInsnNode) instruction;
            result = new VarInsnNode(opcode, vNode.var);
            break;
        default:
            throw new InstrumenterException("unknown instruction type " 
                + instruction.getType());
        }
        if (null != result) {
            insn.add(result);
        }
    }

    /**
     * Replaces all labels in <code>labels</code>.
     * 
     * @param labels the labels to be replaced
     * @param labelMap the map containing the replacements, may be <b>null</b>
     * @return the replaced <code>labels</code>, <b>null</b> if 
     *     <code>labels == <b>null</b></code>
     * 
     * @since 1.00
     */
    private static LabelNode[] replaceLabels(List<LabelNode> labels, 
        HashMap<LabelNode, LabelNode> labelMap) {
        LabelNode[] result;
        if (null == labels) {
            result = null;
        } else {
            int size = labels.size();
            result = new LabelNode[size];
            for (int i = 0; i < size; i++) {
                result[i] = replaceLabel(labels.get(i), labelMap);
            }
        }
        return result;
    }
    
    /**
     * Returns either <code>label</code> or the replacement specified in 
     * <code>labelMap</code>.
     * 
     * @param label the label to be replaced
     * @param labelMap the map containing the replacements, may be <b>null</b>
     * @return the replaced or original label
     * 
     * @since 1.00
     */
    private static LabelNode replaceLabel(LabelNode label, 
        HashMap<LabelNode, LabelNode> labelMap) {
        LabelNode result = label;
        if (null != labelMap) {
            result = labelMap.get(label);
            if (null == result) {
                result = label;
            }
        }
        return result;
    }

    /**
     * Copies the instructions from <code>source</code> to <code>target</code>
     * by creating new instruction nodes.
     * 
     * @param source the source instructions to be copied
     * @param target the list to copy the instructions to
     * @param method the source / target method, if given labels are recreated 
     *        and mapped and the exception table is adjusted accordingly
     * @throws InstrumenterException if anything goes wrong
     * 
     * @since 1.00
     */
    public static void copy(InsnList source, InsnList target, 
        MethodNode method) 
        throws InstrumenterException {
        int size = source.size();
        HashMap<LabelNode, LabelNode> labelMap 
            = new HashMap<LabelNode, LabelNode>();
        for (int i = 0; i < size; i++) {
            AbstractInsnNode node = source.get(i);
            if (AbstractInsnNode.LABEL == node.getType()) {
                labelMap.put((LabelNode) node, new LabelNode());
            }
        }
        for (int i = 0; i < size; i++) {
            copy(source.get(i), NO_NEW_OP, labelMap, target);
        }
        if (null != method && null != method.tryCatchBlocks) {
            size = method.tryCatchBlocks.size();
            // size() must not be part of loop condition!
            for (int i = 0; i < size; i++) {
                TryCatchBlockNode tcbNode = method.tryCatchBlocks.get(i);
                LabelNode start = replaceLabel(tcbNode.start, labelMap);
                LabelNode end = replaceLabel(tcbNode.end, labelMap);
                LabelNode handler = replaceLabel(tcbNode.handler, labelMap);
                if (start != tcbNode.start || end != tcbNode.end 
                    || handler != tcbNode.handler) {
                    TryCatchBlockNode addition = new TryCatchBlockNode(
                        start, end, handler, tcbNode.type);
                    // add after size
                    method.tryCatchBlocks.add(addition);
                } 
            }
        }
    }
    
    /**
     * Returns an instruction node representing a boolean constant.
     * 
     * @param bool the boolean for representing the constant
     * @return the instruction node
     * 
     * @since 1.00
     */
    public static AbstractInsnNode booleanToNode(boolean bool) {
        int val = (bool ? ICONST_1 : ICONST_0);
        return new InsnNode(val);
    }
    
    /**
     * Returns an instruction node representing an integer constant.
     * 
     * @param val the integer value for representing the constant
     * @return the instruction node
     * 
     * @since 1.00
     */
    public static AbstractInsnNode integerToNode(int val) {
        AbstractInsnNode result;
        switch (val) {
        case 0:
            result = new InsnNode(ICONST_0);
            break;
        case 1:
            result = new InsnNode(ICONST_1);
            break;
        case 2:
            result = new InsnNode(ICONST_2);
            break;
        case 3:
            result = new InsnNode(ICONST_3);
            break;
        case 4:
            result = new InsnNode(ICONST_4);
            break;
        case 5:
            result = new InsnNode(ICONST_5);
            break;
        default:
            result = new VarInsnNode(BIPUSH, val);
            break;
        }
        return result;
    }

    /**
     * Returns an instruction node representing an long constant.
     * 
     * @param val the long value for representing the constant
     * @return the instruction node
     * 
     * @since 1.20
     */
    public static AbstractInsnNode longToNode(long val) {
        AbstractInsnNode result;
        if (0 == val) {
            result = new InsnNode(LCONST_0);
        } else if (1 == val) {
            result = new InsnNode(LCONST_1);
        } else {
            result = new LdcInsnNode(val);
        }
        return result;
    }

    /**
     * Adds instructions to the stack which loads the current class.
     * 
     * @param instr the instruction list to be modified
     * @param className the class name for a static call, <b>null</b> in case
     *   of a dynamic call
     * @return the influence to the stack
     * 
     * @since 1.00
     */
    public static int classToStack(InsnList instr, String className) {
        int maxStack;
        if (null != className) {
            // .class.getName()
            instr.add(new LdcInsnNode(Type.getType(
                getClassTypeDescriptor(className, false)))); // <Class>.class
            maxStack = 1;
        } else {
            // getClass().getName()
            instr.add(new VarInsnNode(ALOAD, 0));
            instr.add(createMethodInsnNode(INVOKEVIRTUAL, 
                Factory.JAVA_LANG_OBJECT, "getClass", 
                "()" + Utils.CLASS_DESCR));                    
            maxStack = 2;
        }
        return maxStack;
    }

    /**
     * Adds instructions to the stack which loads the current class name.
     * 
     * @param instr the instruction list to be modified
     * @param className the class name for a static call, <b>null</b> in case
     *   of a dynamic call
     * @return the influence to the stack
     * 
     * @since 1.00
     */
    public static int classNameToStack(InsnList instr, String className) {
        int maxStack = classToStack(instr, className);
        instr.add(createMethodInsnNode(INVOKEVIRTUAL, Utils.CLASS, 
            "getName", "()" + Utils.STRING_DESCR));
        return maxStack + 1;
    }

    /**
     * Adds instructions to the stack which loads the current class name.
     * 
     * @param instr the instruction list to be modified
     * @param className the class name for a static call, <b>null</b> in case
     *   of a dynamic call
     * @return the influence to the stack
     * 
     * @since 1.00
     */
    public static int classLoaderToStack(InsnList instr, String className) {
        int maxStack = classToStack(instr, className);
        instr.add(createMethodInsnNode(INVOKEVIRTUAL, Utils.CLASS, 
            "getClassLoader", "()" + Utils.CLASSLOADER_DESCR));
        return maxStack;
    }
    
    /**
     * Prints an individual behavior. [debugging]
     * 
     * @param behavior the behavior to print
     * 
     * @since 1.00
     */
    public static void print(IBehavior behavior) {
        MethodNode mNode = ((ABehavior) behavior).getNode();
        System.out.println(behavior.getDeclaringClassName() + "." 
            + behavior.getName() + " " + behavior.getJavaSignature());
        print(mNode);
    }

    /**
     * Prints an individual method behavior. [debugging]
     * 
     * @param node the node to print
     * 
     * @since 1.00
     */
    public static void print(MethodNode node) {
        Textifier tf = new Textifier();
        node.accept(new TraceMethodVisitor(tf));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
        tf.print(pw);
        pw.flush();
    }

    /**
     * Prints an instruction list.
     * 
     * @param insn instructions
     * 
     * @since 1.00
     */
    public static void print(InsnList insn) {
        Textifier tf = new Textifier();
        MethodVisitor vis = new TraceMethodVisitor(tf);
        for (int i = 0; i < insn.size(); i++) {
            insn.get(i).accept(vis);
        }
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
        tf.print(pw);
        pw.flush();
    }
    
    /**
     * Prints an entire class.
     * 
     * @param cl the class to print
     * 
     * @since 1.00
     */
    public static void print(ClassNode cl) {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
        ClassVisitor cv = new TraceClassVisitor(pw); 
        cl.accept(cv);
        pw.flush();
    }

}
