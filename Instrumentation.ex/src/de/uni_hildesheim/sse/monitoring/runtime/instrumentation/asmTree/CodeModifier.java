package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ConfigurationChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.MeasurementValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.NotifyValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Timer;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.BooleanValue;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;
import de.uni_hildesheim.sse.monitoring.runtime.recording.Recorder;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.ShutdownMonitor;

import static de.uni_hildesheim.sse.monitoring.runtime.instrumentation.
    asmTree.Utils.*;

/**
 * Implements the code modifier for ASM. Stack size modifications in this class
 * are calculated based on the inserted fragments (start/end of method) and the
 * stack requirements of the fragments to be pre/appended.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CodeModifier implements ICodeModifier, Opcodes {

    /**
     * An internal array index denoting a local temporary variable for array
     * arguments.
     */
    private static final int DATA_ARRAY_VAR = 0;

    /**
     * An internal array index denoting a local temporary variable containing 
     * the maximum stack size.
     */
    private static final int DATA_MAXSTACK = 1;

    /**
     * An internal array index denoting a local temporary variable containing 
     * the maximum number of local variables.
     */
    private static final int DATA_MAXLOCALS = 2;

    /**
     * The size of an integer array for {@link #DATA_ARRAY_VAR}, 
     * {@link #DATA_MAXSTACK} and {@link #DATA_MAXLOCALS}.
     */
    private static final int DATA_SIZE = DATA_MAXLOCALS + 1;

    /**
     * Stores the descriptor for the enter/exit notification calls.
     */
    private static final String ENTER_EXIT_DESCR 
        = "(" + STRING_DESCR + STRING_DESCR + "ZZ)V";

    /**
     * Stores the descriptor for the assignAllTo notification calls.
     */
    private static final String ASSIGNALLTO_DESCR = "(" + STRING_DESCR + "Z)V";
    
    /**
     * Stores the descriptor for memory allocation/unallocation notification 
     * calls.
     */
    private static final String MEMORY_DESCR = "(" + OBJECT_DESCR + ")V";

    /**
     * Stores the descriptor for registering a thread.
     */
    private static final String REGISTER_THREAD_DESCR = "(Z)V";
    
    /**
     * Stores the descriptor for <code>java.lang.Runnable.run</code> or 
     * <code>java.lang.Thread.run</code>.
     */
    private static final String RUNNABLE_RUN_DESCR = "()V";
    
    /**
     * Stores the internal name for {@link TimerState}.
     */
    private static final String TIMER_STATE = Factory.toInternalName(
        TimerState.class.getName());

    /**
     * Stores the descriptor for {@link TimerState}.
     */
    private static final String TIMER_STATE_DESCR = getClassTypeDescriptor(
        TimerState.class.getName(), false);

    /**
     * Stores the descriptor for timer notification calls.
     */
    private static final String NOTIFY_TIMER_DESCR = "(" + STRING_DESCR 
        + TIMER_STATE_DESCR + "Z)V";

    /**
     * Stores the internal name for {@link StreamType}.
     */
    private static final String STREAM_TYPE = Factory.toInternalName(
        StreamType.class.getName());

    /**
     * Stores the descriptor for {@link StreamType}.
     */
    private static final String STREAM_TYPE_DESCR = getClassTypeDescriptor(
        StreamType.class.getName(), false);

    /**
     * Stores the descriptor for the IO notification calls.
     */
    private static final String IO_DESCR = "(" + STRING_DESCR 
        + STRING_DESCR + "I" + STREAM_TYPE_DESCR + ")I";

    /**
     * Stores the descriptor for class initializers.
     */
    private static final String INITIALIZER_DESCR = "()V";
    
    /**
     * Stores the descriptor for the configuration change call.
     */
    private static final String CONFIGURATION_CHANGE_DESCR = 
        "(" + STRING_DESCR + ")V";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTimerCall(IBehavior behavior, TimerState state,
        Timer notifyTimer, boolean before) throws InstrumenterException {
        MethodNode mNode = ((ABehavior) behavior).getNode();
        InsnList instructions = mNode.instructions;
        InsnList instr = new InsnList();

        //recorder.notifyTimer(id,state,considerThreads)
        int tmp = appendRecorderCallProlog(instr, false);
        int maxStack = getLSB(tmp);
        String id = Helper.trimId(notifyTimer.id());
        instr.add(new LdcInsnNode(id));
        if (null == state) {
            state = notifyTimer.state();
        }
        instr.add(new FieldInsnNode(GETSTATIC, TIMER_STATE, state.name(), 
            TIMER_STATE_DESCR));
        instr.add(booleanToNode(notifyTimer.considerThreads()));
        maxStack += 3;
        tmp = appendRecorderCall(instr, "notifyTimer", 
            NOTIFY_TIMER_DESCR);
        maxStack = Math.max(maxStack, getLSB(tmp));
        if (before) {
            insertAtBeginning(instr, instructions);
        } else {
            insertBeforeReturn(instr, instructions, -1, null);
        }
        mNode.maxStack = maxStack;
    }
    
    /**
     * Adds the instructions for a call of 
     * {@link RecorderFrontend#registerThisThread(boolean)}.
     * 
     * @param instr the instruction set to add the call code
     * @param register register or unregister the thread
     * @return the change to the stack size
     * 
     * @since 1.00
     */
    private int addRegisterThreadCall(InsnList instr, boolean register) {
        int tmp = appendRecorderCallProlog(instr, false);
        int maxStack = getLSB(tmp);
        instr.add(booleanToNode(register));
        maxStack = Math.max(maxStack, 3);
        tmp = appendRecorderCall(instr, "registerThisThread", 
            REGISTER_THREAD_DESCR);
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));
        return maxStack;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean notifyRegisterThread(IClass cls, IBehavior behavior,
        boolean isMain) throws InstrumenterException {
        boolean modified = false;
// TODO validate
        if (isMain) {
            if (Configuration.INSTANCE.registerThreads()) {
                MethodNode mNode = ((ABehavior) behavior).getNode();
                InsnList instructions = mNode.instructions;
                int maxStack = 0;
                InsnList instr = new InsnList();
                maxStack = Math.max(maxStack, 
                    addRegisterThreadCall(instr, true));
                insertBeforeReturn(instr, instructions, -1, null);
                mNode.maxStack = Math.max(mNode.maxStack, maxStack);
                modified = true;
            }
        } else {
            if (null == behavior) {
                AClass aClass = (AClass) cls;
                ClassNode cNode = aClass.getNode();
                final String name = "run";
                MethodNode method = new MethodNode(ACC_PUBLIC, name, 
                    RUNNABLE_RUN_DESCR, null, null);
                InsnList instructions = method.instructions;
                int maxStack = 0;
                int maxLocals = 0;
                if (Configuration.INSTANCE.registerThreads()) {
                    maxStack = Math.max(maxStack, 
                        addRegisterThreadCall(instructions, true));
                }
                IClass sClass = aClass
                    .findSuperclassWithMethodWoParameter(name);
                if (null != sClass) {
                    maxLocals++;
                    instructions.add(new VarInsnNode(ALOAD, 0));
                    // might require searching for superclass method
                    instructions.add(new MethodInsnNode(INVOKESPECIAL, 
                        cNode.superName, "run", RUNNABLE_RUN_DESCR));
                    sClass.release();
                }
                if (Configuration.INSTANCE.registerThreads()) {
                    maxStack = Math.max(maxStack, 
                        addRegisterThreadCall(instructions, false));
                }
                instructions.add(new InsnNode(RETURN));
                method.maxStack = Math.max(maxStack, 
                    addThreadEndCall(instructions));
                method.maxLocals = maxLocals; 
                cNode.methods.add(method);
                modified = true;
            } else {
                MethodNode mNode = ((ABehavior) behavior).getNode();
                InsnList instructions = mNode.instructions;
                int maxStack = 0;
                InsnList instr = new InsnList();
                if (Configuration.INSTANCE.registerThreads()) {
                    maxStack = Math.max(maxStack, 
                        addRegisterThreadCall(instr, false));
                    insertAtBeginning(instr, instructions);
                    instr.clear();
                    maxStack = Math.max(maxStack, 
                        addRegisterThreadCall(instr, false));
                }
                maxStack = Math.max(maxStack, 
                    addThreadEndCall(instructions));
                insertBeforeReturn(instr, instructions, -1, null);
                mNode.maxStack = Math.max(mNode.maxStack, maxStack);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Adds the instructions for a call of 
     * {@link RecorderFrontend#notifyThreadEnd}.
     * 
     * @param instr the instruction set to add the call code
     * @return the change to the stack size
     * 
     * @since 1.00
     */
    private int addThreadEndCall(InsnList instr) {
        int tmp = appendRecorderCallProlog(instr, false);
        int maxStack = getLSB(tmp);
        //maxStack = Math.max(maxStack, 3);
        tmp = appendRecorderCall(instr, "notifyThreadEnd", 
            "()V");
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));
        return maxStack;
    }

    
    /**
     * Adds an assignAllTo recorder call to <code>instr</code>.
     * 
     * @param instr the instruction list to add the call to
     * @param recId the recording id (may be also the variability prefix)
     * @param enter <code>true</code> if this is a method enter event, 
     *   <code>false</code> else
     * @param maxStack the current maximum number of elements on the stack
     * @return the new maximum number of elements on the stack
     * 
     * @since 1.00
     */
    private static int addAssignAllToCall(InsnList instr, String recId, 
        boolean enter, int maxStack) {
        int tmp = appendRecorderCallProlog(instr, false);
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));
        instr.add(new LdcInsnNode(recId));
        instr.add(booleanToNode(enter));
        tmp = appendRecorderCall(instr, "assignAllTo", ASSIGNALLTO_DESCR);
        return Math.max(maxStack, getLSB(tmp) + 3);
    }

    @Override
    public void valueNotification(IBehavior behavior, String recId,
        NotifyValue ann) throws InstrumenterException {
        ABehavior aBehav = (ABehavior) behavior;
        MethodNode mNode = aBehav.getNode();
        InsnList before = new InsnList();
        InsnList after = new InsnList();
        int maxLocals = mNode.maxLocals;
        int maxStack = mNode.maxStack;
        if (MeasurementValue.MEM_ALLOCATED == ann.value() 
            || MeasurementValue.MEM_UNALLOCATED == ann.value()) {
            int tmp = appendRecorderCallProlog(after, false);
            maxStack = Math.max(maxStack, Utils.getLSB(tmp));
            Expressions.Result exprResult 
                = Expressions.createExpressionCode(aBehav, 
                    ann.tagExpression(), after, before, null);
            maxStack += exprResult.getStackInc();
            maxLocals += exprResult.getLocalsInc();
            String tagType = exprResult.getTypeDescriptor();
            exprResult.release();
            exprResult = Expressions.createExpressionCode(aBehav, 
                ann.expression(), after, before, null);
            maxStack += exprResult.getStackInc();
            maxLocals += exprResult.getLocalsInc();
            String valType = exprResult.getTypeDescriptor();
            exprResult.release();
            String methodName;
            if (MeasurementValue.MEM_UNALLOCATED == ann.value()) {
                methodName = "memoryFreed";
            } else {
                methodName = "memoryAllocated";
            }
            tmp = appendRecorderCall(after, methodName, "(" 
                + tagType + valType + ")V");
            maxStack += 2; // 2 param
        } else if (MeasurementValue.ALL == ann.value()) {
            maxStack = addAssignAllToCall(before, recId, true, maxStack);
            maxStack = addAssignAllToCall(after, recId, false, maxStack);
        } else {
            if (MeasurementValue.VALUE == ann.value()) {
                int tmp = appendRecorderCallProlog(after, false);
                maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                after.add(new LdcInsnNode(recId));
                Expressions.Result exprResult 
                    = Expressions.createExpressionCode(aBehav, 
                        ann.expression(), after, before, null);
                maxStack += exprResult.getStackInc();
                maxLocals += exprResult.getLocalsInc();
                tmp = appendRecorderCall(after, "notifyValueChange", "(" 
                    + STRING_DESCR + exprResult.getTypeDescriptor() + ")V");
                maxStack += 2; // 2 param
                exprResult.release();
            } else {
                int tmp = fileValueNotification(behavior, recId, ann, before, 
                    after);
                maxStack = getLSB(tmp);
                maxLocals = getMSB(tmp);
            }
        }
        if (0 == before.size() && 0 == after.size()) {
            System.err.println("no code generation registered for " 
                + ann.value());
        } else {
            insertAtBeginning(before, mNode.instructions);
            int tmp = insertBeforeReturn(after, mNode.instructions, maxLocals, 
                mNode);
            maxStack = Math.max(maxStack, getLSB(tmp));
            mNode.maxStack = Math.max(mNode.maxStack, maxStack);
            mNode.maxLocals = maxLocals;
        }
    }
    
    /**
     * Creates the file notification code.
     * 
     * @param behavior the method to be changed
     * @param recId the recorder identification
     * @param ann the value annotation
     * @param before the instruction list to be inserted before the original 
     *   code (to be modified as a side effect)
     * @param after the instruction list to be inserted after the original code
     *   (to be modified as a side effect)
     * @return the new maxStack in LSB, the new maxLocals in MSB
     * @throws InstrumenterException in case of any modification errors
     * 
     * @since 1.00
     */
    private int fileValueNotification(IBehavior behavior, String recId, 
        NotifyValue ann, InsnList before, InsnList after) 
        throws InstrumenterException {
        ABehavior aBehav = (ABehavior) behavior;
        MethodNode mNode = aBehav.getNode();
        int maxLocals = mNode.maxLocals;
        int maxStack = mNode.maxStack;
        String rMethodName;
        StreamType sType;
        switch (ann.value()) {
        case NET_OUT:
            rMethodName = "writeIo";
            sType = StreamType.NET;
            break;
        case NET_IN:
            rMethodName = "readIo";
            sType = StreamType.NET;
            break;
        case FILE_OUT:
            rMethodName = "writeIo";
            sType = StreamType.FILE;
            break;
        case FILE_IN:
            rMethodName = "readIo";
            sType = StreamType.FILE;
            break;
        default:
            rMethodName = null;
            sType = null;
            break;
        }
        if (null != rMethodName) {
            if (ann.notifyDifference()) {
                // before
                // fstExpr (to be added to before at end of this alternative)
                // instructions
                // after
                InsnList fstExpr = new InsnList();
                Expressions.Result exprResult 
                    = Expressions.createExpressionCode(aBehav, 
                        ann.expression(), fstExpr, before, null);
                maxStack += exprResult.getStackInc();
                maxLocals += exprResult.getLocalsInc();

                int var = maxLocals++;
                fstExpr.add(new VarInsnNode(ISTORE, var));

                int tmp = appendRecorderCallProlog(before, false);
                int tmpStack = getLSB(tmp);
                after.add(new LdcInsnNode(recId));
                tmpStack++;
                String staticCls;
                if (behavior.isStatic()) {
                    staticCls = behavior.getDeclaringClassName();
                } else {
                    staticCls = null;
                }
                tmpStack += classNameToStack(after, staticCls);
                after.add(new VarInsnNode(ILOAD, var));
                tmpStack++;
                exprResult = Expressions.createExpressionCode(aBehav, 
                    ann.expression(), after, before, exprResult);
                tmpStack += exprResult.getStackInc();
                maxLocals += exprResult.getLocalsInc();
                exprResult.release();
                after.add(new InsnNode(ISUB));
                tmpStack--;
                after.add(new FieldInsnNode(GETSTATIC, STREAM_TYPE, 
                    sType.name(), STREAM_TYPE_DESCR));
                tmpStack++;
                tmp = appendRecorderCall(after, rMethodName, 
                    IO_DESCR);
                maxStack = Math.max(maxStack, tmpStack + getLSB(tmp));
                before.add(fstExpr);
            } else {
                int tmp = appendRecorderCallProlog(before, false);
                maxStack = Math.max(maxStack, getLSB(tmp));
                int tmpMaxStack = 1;
                after.add(new LdcInsnNode(recId));
                String staticClsName;
                if (behavior.isStatic()) {
                    staticClsName = Factory.toInternalName(
                        behavior.getDeclaringClassName());
                } else {
                    staticClsName = null;
                }
                tmpMaxStack += classNameToStack(after, staticClsName);
                Expressions.Result exprResult 
                    = Expressions.createExpressionCode(aBehav, 
                        ann.expression(), after, before, null);
                tmpMaxStack += exprResult.getStackInc();
                maxLocals += exprResult.getLocalsInc();
                exprResult.release();
                after.add(new FieldInsnNode(GETSTATIC, STREAM_TYPE, 
                    sType.name(), STREAM_TYPE_DESCR));
                maxStack = Math.max(maxStack, tmpMaxStack + 1);
                tmp = appendRecorderCall(after, rMethodName, IO_DESCR);
                maxStack = Math.max(maxStack, getLSB(tmp));
            } 
        } else {
            System.err.println("no code generation registered for " 
                + ann.value());
        }
        return buildInt(maxLocals, maxStack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentRandomAccessFile(IClass cls)
        throws InstrumenterException {
        AClass aClass = (AClass) cls;
        ClassNode cNode = aClass.getNode();
        cNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "recId", 
            STRING_DESCR, null, null));
        int count = aClass.getDeclaredBehaviorCount();
        for (int i = 0; i < count; i++) {
            ABehavior behav = (ABehavior) aClass.getDeclaredBehavior(i);
            String name = behav.getName();
            boolean isWriteChars = name.equals("writeChars");
            boolean isWriteBytes = name.equals("writeBytes"); 
            if ((isWriteChars || isWriteBytes) 
                && 1 == behav.getParameterCount()) {
                MethodNode mNode = behav.getNode();
                InsnList instructions = mNode.instructions;
                InsnList instr = new InsnList();
                int maxStack = mNode.maxStack;
                int maxLocals = mNode.maxLocals;
                int argSaveVariable;
                int argBase;
                if (behav.isStatic()) {
                    argBase = 0;
                } else {
                    argBase = 1;
                }

                // save first parameter (array)
                instr.add(new VarInsnNode(ALOAD, argBase + 0));
                argSaveVariable = maxLocals++;
                instr.add(new VarInsnNode(ASTORE, argSaveVariable));
                insertAtBeginning(instr, instructions);
                instr.clear();

                int tmp = appendRecorderCallProlog(instr, true);
                maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                LabelNode end = new LabelNode();
                // if recorder==null
                instr.add(new JumpInsnNode(IFNULL, end));
                
                // recorder.writeIo(recId, null, (char ? 2 : 1) * $1.length(), 
                //   FILE);"
                tmp = appendRecorderCallProlog(instr, true);
                maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                addIORecorderCallPart1(instr, cNode.name);
                instr.add(new VarInsnNode(ALOAD, argSaveVariable));
                instr.add(new MethodInsnNode(INVOKEVIRTUAL, STRING, 
                    "length", "()I"));
                int maxStackOffset;
                if (isWriteChars) {
                    instr.add(new InsnNode(ICONST_2));
                    instr.add(new InsnNode(IMUL));
                    maxStackOffset = 1;
                } else {
                    maxStackOffset = 0;
                }
                maxStack = Math.max(maxStack, addIORecorderCallPart2(
                    instr, StreamType.FILE, maxStack, false) + maxStackOffset);
                instr.add(new InsnNode(POP)); // result not needed
                instr.add(end);
                insertBeforeReturn(instr, instructions, -1, mNode);
                mNode.maxStack = maxStack;
                mNode.maxLocals = maxLocals;
            }
            behav.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentSocketOutputStream(IClass cls)
        throws InstrumenterException {
        AClass aClass = (AClass) cls;
        ClassNode cNode = aClass.getNode();
        cNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "recId", 
            STRING_DESCR, null, null));
        //cNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "inWrite3", 
        //    "Z", null, false));
        //cNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "inWrite1", 
        //    "Z", null, false));
        int count = aClass.getDeclaredBehaviorCount();
        for (int i = 0; i < count; i++) {
            ABehavior behav = (ABehavior) aClass.getDeclaredBehavior(i);
            instrumentStreamWrite(behav, cNode.name, StreamType.NET);
            behav.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentSocketInputStream(IClass cls)
        throws InstrumenterException {
        AClass aClass = (AClass) cls;
        ClassNode cNode = aClass.getNode();
        cNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "recId", 
            STRING_DESCR, null, null));
        //cNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "inRead3", 
        //    "Z", null, false));
        //cNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "inRead1", 
        //    "Z", null, false));
        int count = aClass.getDeclaredBehaviorCount();
        for (int i = 0; i < count; i++) {
            ABehavior behav = (ABehavior) aClass.getDeclaredBehavior(i);
            instrumentStreamRead(behav, cNode.name, StreamType.NET);
            behav.release();
        }
    }

    /**
     * Instruments write methods of a stream. This method should be applied
     * to stream classes only.
     * 
     * @param method the method to be considered for instrumentation
     * @param owner the internal name of the owning class
     * @param type the type of the stream
     * @throws InstrumenterException if problems occur
     * 
     * @since 1.00
     */
    private static void instrumentStreamWrite(ABehavior method, String owner,
        StreamType type) throws InstrumenterException {
        if (method.getName().equals("write")) {
            MethodNode mNode = method.getNode();
            InsnList instructions = mNode.instructions;
            int maxStack = mNode.maxStack;
            int maxLocals = mNode.maxLocals;
            //int argSaveVariable;
            //int argBase;
            //if (method.isStatic()) {
            //    argBase = 0;
            //} else {
            //    argBase = 1;
            //}
            InsnList instr = new InsnList();
            int paramCount = method.getParameterCount();
            switch (paramCount) {
            case 1:
                if (method.getParameterTypeName(0).startsWith("[")) { // unsure
                    // save first parameter (array)
                    //instr.add(new VarInsnNode(ALOAD, argBase + 0));
                    //argSaveVariable = maxLocals++;
                    //instr.add(new VarInsnNode(ASTORE, argSaveVariable));
                    //insertAtBeginning(instr, instructions);
                    //instr.clear();
                    
                    // inWrite1 = true;
                    //instr.add(new VarInsnNode(ALOAD, 0)); // this
                    //instr.add(booleanToNode(true)); // value
                    //instr.add(new FieldInsnNode(PUTFIELD, owner, 
                    //    "inWrite1", "Z"));
                    //instructions.insertBefore(instructions.getFirst(), instr);
                    maxStack = Math.max(maxStack, 2);
                    instr.clear();

                    int tmp = appendRecorderCallProlog(instr, true);
                    maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                    LabelNode end = new LabelNode();
                    // if recorder==null
                    instr.add(new JumpInsnNode(IFNULL, end));
                    // if !inWrite3
                    //instr.add(new VarInsnNode(ALOAD, 0)); // this
                    //instr.add(new FieldInsnNode(GETFIELD, owner, 
                    //    "inWrite3", "Z"));
                    //instr.add(new JumpInsnNode(IFNE, end));
                    // recorder.writeIo(recid, null, $1.length, type)
                    tmp = appendRecorderCallProlog(instr, true);
                    addIORecorderCallPart1(instr, owner);
                    instr.add(new VarInsnNode(ALOAD, 1));
                    instr.add(new InsnNode(ARRAYLENGTH));
                    maxStack = Math.max(maxStack, Utils.getLSB(tmp) 
                        + addIORecorderCallPart2(instr, type, maxStack, false));
                    instr.add(new InsnNode(POP));

                    // inWrite1 = false
                    //instr.add(new VarInsnNode(ALOAD, 0)); // this
                    //instr.add(booleanToNode(false));
                    //instr.add(new FieldInsnNode(PUTFIELD, owner, 
                    //    "inWrite1", "Z"));
                    instr.add(end);
                } else {
                    int tmp = appendRecorderCallProlog(instr, true);
                    maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                    LabelNode end = new LabelNode();
                    // if recorder==null
                    instr.add(new JumpInsnNode(IFNULL, end));
                    //instr.add(new VarInsnNode(ALOAD, 0)); // this
                    //instr.add(new FieldInsnNode(GETFIELD, owner, 
                    //    "inWrite1", "Z"));
                    // if !inWrite1
                    //instr.add(new JumpInsnNode(IFNE, end));
                    //instr.add(new VarInsnNode(ALOAD, 0)); // this
                    //instr.add(new FieldInsnNode(GETFIELD, owner, 
                    //    "inWrite3", "Z"));
                    // if !inWrite3
                    //instr.add(new JumpInsnNode(IFNE, end));
                    maxStack = Math.max(maxStack, 1);
                    
                    // recorder.writeIo(recid, null, 1, type)
                    tmp = appendRecorderCallProlog(instr, true);
                    addIORecorderCallPart1(instr, owner);
                    instr.add(new InsnNode(ICONST_1));
                    maxStack = Math.max(maxStack, Utils.getLSB(tmp) 
                        + addIORecorderCallPart2(instr, type, maxStack, false));
                    instr.add(new InsnNode(POP));
                    instr.add(end);
                }
                break;
            case 3: 
                // save third parameter (int), instance-method
                //instr.add(new VarInsnNode(ILOAD, argBase + 2));
                //argSaveVariable = maxLocals++;
                //instr.add(new VarInsnNode(ISTORE, argSaveVariable));
                //insertAtBeginning(instr, instructions);
                //instr.clear();

                // inWrite3 = true;
                //instr.add(new VarInsnNode(ALOAD, 0)); // this
                //instr.add(booleanToNode(true)); // value
                //instr.add(new FieldInsnNode(PUTFIELD, owner,"inWrite3", "Z"));
                //instructions.insertBefore(instructions.getFirst(), instr);
                //instr.clear();
                //maxStack = Math.max(maxStack, 1);

                int tmp = appendRecorderCallProlog(instr, true);
                maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                LabelNode end = new LabelNode();
                // if recorder==null
                instr.add(new JumpInsnNode(IFNULL, end));
                //instr.add(new VarInsnNode(ALOAD, 0)); // this
                //instr.add(new FieldInsnNode(GETFIELD, owner, 
                //    "inWrite1", "Z"));
                // if !inWrite3
                //instr.add(new JumpInsnNode(IFNE, end));
                
                // recorder.writeIo(recid, null, $3, type)
                tmp = appendRecorderCallProlog(instr, true);
                addIORecorderCallPart1(instr, owner);
                instr.add(new VarInsnNode(ILOAD, 3));
                maxStack = Math.max(maxStack, Utils.getLSB(tmp) 
                    + addIORecorderCallPart2(instr, type, maxStack, false));
                instr.add(new InsnNode(POP));
                
                // inWrite1 = false
                //maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                //instr.add(new VarInsnNode(ALOAD, 0)); // this
                //instr.add(booleanToNode(false)); // value
                //instr.add(new FieldInsnNode(PUTFIELD, owner, 
                //    "inWrite3", "Z"));
                instr.add(end);
                break;
            default:
                // do nothing
                break;
            }
            if (instr.size() > 0) {
                int tmp = insertBeforeReturn(instr, instructions, -1, mNode);
                maxStack = Math.max(maxStack, getLSB(tmp));
                mNode.maxStack = Math.max(mNode.maxStack, maxStack);
                mNode.maxLocals = maxLocals;
            }
        }
    }

    /**
     * Adds the first two parameters of a readIo/writeIo notification call. To
     * be called before {@link #addIORecorderCallPart2(InsnList, StreamType, 
     * int, boolean)}. The third parameter is individual and should be directly
     * inserted after this call.
     * 
     * @param instr the instruction list to be modified
     * @param owner the owner of the method being modified
     * 
     * @since 1.00
     */
    private static void addIORecorderCallPart1(InsnList instr, String owner) {
        instr.add(new VarInsnNode(ALOAD, 0)); // this
        instr.add(new FieldInsnNode(GETFIELD, owner, 
            "recId", STRING_DESCR));
        instr.add(new InsnNode(ACONST_NULL));
    }

    /**
     * Adds the fourth parameter of a readIo/writeIo notification call. This 
     * method completes the parameters added by 
     * {@link #addIORecorderCallPart1(InsnList, String)} and the manual third
     * parameter.
     * 
     * @param instr the instruction list to be modified
     * @param type the stream type
     * @param maxStack the current value of max stack
     * @param read read or write call
     * @return the maxStack in the MSB, the difference to maxLocals in MSB
     * 
     * @since 1.00
     */
    private static int addIORecorderCallPart2(InsnList instr,  
        StreamType type, int maxStack, boolean read) {
        instr.add(new FieldInsnNode(GETSTATIC, STREAM_TYPE, 
            type.name(), STREAM_TYPE_DESCR));
        maxStack = Math.max(maxStack, 4);
        String name;
        if (read) {
            name = "readIo";
        } else {
            name = "writeIo";
        }
        int tmp = appendRecorderCall(instr, name, IO_DESCR);
        return Math.max(maxStack, Utils.getLSB(tmp));
    }

    
    /**
     * Instruments read methods of a stream. This method should be applied
     * to stream classes only.
     * 
     * @param method the method to be considered for instrumentation
     * @param owner the internal name of the owning class
     * @param type the type of the stream
     * @throws InstrumenterException if problems occur
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.OR)
    private static void instrumentStreamRead(ABehavior method, String owner,
        StreamType type) throws InstrumenterException {
        if (method.getName().equals("read")) {
            int paramCount = method.getParameterCount();
            // do not instrument the other read methods as they all delegate
            // to this one
            if (3 == paramCount) {
                MethodNode mNode = method.getNode();
                int maxStack = mNode.maxStack;
                InsnList instr = new InsnList();
                int var = mNode.maxLocals++;
                int tmp = appendRecorderCallProlog(instr, true);
                maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                LabelNode end = new LabelNode();
                instr.add(new JumpInsnNode(IFNULL, end));
                instr.add(new VarInsnNode(ISTORE, var));
                appendRecorderCallProlog(instr, true);
                addIORecorderCallPart1(instr, owner);
                //instr.add(new InsnNode(NOP)); // -> insertBeforeReturn
                instr.add(new VarInsnNode(ILOAD, var));
                maxStack = Math.max(maxStack, 
                    addIORecorderCallPart2(instr, type, maxStack, true));
                instr.add(end);
                tmp = insertBeforeReturn(instr, mNode.instructions, 
                    -1, mNode);
                maxStack = Math.max(maxStack, getLSB(tmp));
                mNode.maxStack = Math.max(mNode.maxStack, maxStack);
                mNode.maxLocals += getMSB(tmp);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentEndSystem(IBehavior behavior,
        boolean printStatistics, String invoke) throws InstrumenterException {
        MethodNode mNode = ((ABehavior) behavior).getNode();
        InsnList instructions = mNode.instructions;
        int maxStack = 0;
        InsnList instr = new InsnList();
        String shutdownM = ShutdownMonitor.class.getName();

        instr.add(booleanToNode(printStatistics));
        int tmp = classLoaderToStack(instr, Factory.toInternalName(
            behavior.getDeclaringClassName()));
        instr.add(new LdcInsnNode(behavior.expandInvoke(invoke)));
        instr.add(new MethodInsnNode(INVOKESTATIC, 
            Factory.toInternalName(shutdownM), "endMonitoring", 
            "(Z" + CLASSLOADER_DESCR + STRING_DESCR + ")V"));
        maxStack = Math.max(maxStack, 2 + tmp);
        
        int pos = 0;
        int maxPos = instructions.size();
        int local = -1;
        while (pos < maxPos) {
            AbstractInsnNode instruction = instructions.get(pos);
            if (INVOKESTATIC == instruction.getOpcode()) {
                MethodInsnNode mInstruction = (MethodInsnNode) instruction;
                if (mInstruction.name.equals("exit") 
                    && mInstruction.owner.equals(SYSTEM)) {
                    if (local < 0) {
                        local = mNode.maxLocals;
                        mNode.maxLocals++;
                    }
                    InsnList tmpInsn = new InsnList();
                    tmpInsn.add(new VarInsnNode(ISTORE, local));
                    copy(instr, tmpInsn, null);
                    tmpInsn.add(new VarInsnNode(ILOAD, local));
                    instructions.insertBefore(instruction, tmpInsn);
                    // heuristics: exact would require to analyze alternatives
                    // and try-catch-blocks... if System.exit is before, than
                    // the program is terminated before the duplicated insertion
                }
            }
            pos++;
        }
        insertBeforeReturn(instr, instructions, -1, null);
        mNode.maxStack = Math.max(mNode.maxStack, maxStack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentStartSystem(IBehavior behavior, boolean shutdownHook,
        String invoke) throws InstrumenterException {
        MethodNode mNode = ((ABehavior) behavior).getNode();
        InsnList instructions = mNode.instructions;
        int maxStack = mNode.maxStack;
        InsnList instr = new InsnList();
        int tmp;

        if (shutdownHook) {
            String runtime = Factory.toInternalName(Runtime.class.getName());
            String runtimeDescr = getClassTypeDescriptor(runtime, true);
            String shutdownMonitor = Factory.toInternalName(
                ShutdownMonitor.class.getName());
            String classLoaderDescr = getClassTypeDescriptor(
                ClassLoader.class.getName(), false);
            String threadDescr = getClassTypeDescriptor(
                Thread.class.getName(), false);

            instr.add(new MethodInsnNode(INVOKESTATIC, runtime, "getRuntime", 
                "()" + runtimeDescr));

            // instantiate shutdownMonitor
            instr.add(new TypeInsnNode(NEW, shutdownMonitor));
            instr.add(new InsnNode(DUP));
            tmp = classLoaderToStack(instr, Factory.toInternalName(
                behavior.getDeclaringClassName()));
            instr.add(new LdcInsnNode(behavior.expandInvoke(invoke)));
            instr.add(booleanToNode(Configuration.INSTANCE.printStatistics()));
            instr.add(new MethodInsnNode(INVOKESPECIAL, shutdownMonitor, 
                "<init>", "(" + classLoaderDescr + STRING_DESCR + ")V"));

            instr.add(new MethodInsnNode(INVOKEVIRTUAL, runtime, 
                "addShutdownHook", "(" + threadDescr + ")V"));
            maxStack = Math.max(maxStack, 4 + tmp);
        }
        
        instr.add(new MethodInsnNode(INVOKESTATIC, 
            Factory.toInternalName(Recorder.class.getName()), 
            "initialize", "()V")); // keep this line

        tmp = appendRecorderCallProlog(instr, false);
        maxStack = Math.max(maxStack, getLSB(tmp));
        tmp = appendRecorderCall(instr, "notifyProgramStart", "()V");
        maxStack = Math.max(maxStack, getLSB(tmp));

        insertAtBeginning(instr, instructions);
        mNode.maxStack = maxStack;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentConfigurationChange(IBehavior behavior,
        ConfigurationChange annotation) throws InstrumenterException {
        ABehavior aBehav = ((ABehavior) behavior);
        MethodNode mNode = aBehav.getNode();
        InsnList instructions = mNode.instructions;
        int maxStack = mNode.maxStack;
        int maxLocals = mNode.maxLocals;
        InsnList instr = new InsnList();
        InsnList init = new InsnList();

        int tmp = appendRecorderCallProlog(instr, false);
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));

        if (annotation.idExpression().length() > 0) {
            instr.add(new LdcInsnNode(annotation.idExpression()));
            maxStack = Math.max(maxStack, 1);
        } else {
            Expressions.Result exprResult 
                = Expressions.createExpressionCode(aBehav, 
                    annotation.valueExpression(), instr, init, null);
            maxStack += exprResult.getStackInc();
            maxLocals += exprResult.getLocalsInc();
        }
        tmp = appendRecorderCall(instr, "configurationChange", 
            CONFIGURATION_CHANGE_DESCR);
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));
        insertAtBeginning(instr, instructions);
        insertAtBeginning(init, instructions);
        mNode.maxStack = Math.max(mNode.maxStack, maxStack);
        mNode.maxLocals = maxLocals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentFinalize(IBehavior method)
        throws InstrumenterException {
        if (method.isFinalize()) {
            insertSelfMemoryCallBeforeReturn(method, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentConstructor(IBehavior method)
        throws InstrumenterException {
        insertSelfMemoryCallBeforeReturn(method, true);
    }
    
    /**
     * Inserts a memory recorder notification call for the actual object
     * at the end of the specified method.
     * 
     * @param method the method to insert the memory call
     * @param allocated is it an allocation or a free call
     * @throws InstrumenterException in case of any problems
     * 
     * @since 1.00
     */
    private void insertSelfMemoryCallBeforeReturn(IBehavior method, 
        boolean allocated) throws InstrumenterException {
// TODO validate
        MethodNode mNode = ((ABehavior) method).getNode();
        InsnList instructions = mNode.instructions;
        int maxStack = 0;
        InsnList instr = new InsnList();
        int tmp = appendRecorderCallProlog(instr, false);
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));
        instr.add(new VarInsnNode(ALOAD, 0));
        String name;
        if (allocated) {
            name = "memoryAllocated";
        } else {
            name = "memoryFreed";
        }
        tmp = appendRecorderCall(instr, name, MEMORY_DESCR);
        maxStack = Math.max(maxStack, Utils.getLSB(tmp));
        insertBeforeReturn(instr, instructions, -1, null);
        mNode.maxStack = Math.max(mNode.maxStack, maxStack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentTiming(IBehavior behavior, Monitor mGroup,
        boolean exclude, boolean directId) throws InstrumenterException {
        MethodNode mNode = ((ABehavior) behavior).getNode();
        InsnList instructions = mNode.instructions;
        int maxStack = 0;
        int maxLocals = mNode.maxLocals;
        InsnList instr = new InsnList();

        // label the original method code
        LabelNode methodEndLabel = new LabelNode(new Label());
        instructions.add(methodEndLabel);
        
        int tmp = Utils.appendRecorderCallProlog(instr, false);
        maxStack = Math.max(maxStack, getLSB(tmp));
        String staticClsName = null;
        if (behavior.isStatic() || behavior.isConstructor()) {
            staticClsName = Factory.toInternalName(
                behavior.getDeclaringClassName());
        }
        maxStack += classNameToStack(instr, staticClsName);

        String recId = null;
        int varInUse = -1;
        if (null != mGroup) {
            recId = Configuration.INSTANCE.getRecId(mGroup.id());
            if (null == recId) {
                // we need class name as second param
                varInUse = maxLocals++;
                instr.add(new VarInsnNode(ASTORE, varInUse));
                // keep as first param
                instr.add(new VarInsnNode(ALOAD, varInUse));
            } // otherwise class name is first param
        } // otherwise class name is first param
        
        if (null != mGroup) {
            if (null == recId) {
                instr.add(new VarInsnNode(ALOAD, varInUse));
                maxStack++;
            } else {
                instr.add(new LdcInsnNode(recId));    
            }
        } else {
            instr.add(new LdcInsnNode(""));
        }
        instr.add(booleanToNode(exclude));
        instr.add(booleanToNode(directId));
        maxStack += 2;
        
        // copy the params for second and third call
        InsnList paramCopy = new InsnList();
        copy(instr, paramCopy, null);
        
        // add method call before original method
        LabelNode methodStartLabel = new LabelNode(new Label());
        instr.insertBefore(instr.getFirst(), methodStartLabel);
        tmp = appendRecorderCall(instr, "enter", ENTER_EXIT_DESCR);
        maxStack += getLSB(tmp);
        
        insertAtBeginning(instr, instructions);

        // add exit call
        tmp = Utils.appendRecorderCall(paramCopy, "exit", ENTER_EXIT_DESCR);
        // stack same as above
        instr.clear();
        copy(paramCopy, instr, null);
        insertBeforeReturn(paramCopy, instructions, -1, null);

        // provide handler
        LabelNode handlerLabel = new LabelNode(new Label());
        instructions.add(handlerLabel);
        int exceptionVar = maxLocals++;
        instructions.add(new VarInsnNode(ASTORE, exceptionVar));
        maxStack++;
        instructions.add(instr);
        instructions.add(new VarInsnNode(ALOAD, exceptionVar));
        instructions.add(new InsnNode(ATHROW));
        instructions.add(new InsnNode(RETURN));
        TryCatchBlockNode tcn = new TryCatchBlockNode(methodStartLabel, 
            methodEndLabel, handlerLabel, THROWABLE);
        mNode.tryCatchBlocks.add(tcn);
        
        if (!mNode.exceptions.contains(THROWABLE)) {
            mNode.exceptions.add(THROWABLE);
        }

        mNode.maxStack = Math.max(mNode.maxStack, maxStack);
        mNode.maxLocals = maxLocals;
    }
        
    /**
     * {@inheritDoc}
     */
    @Override
    public void addRegisteringInitializer(IClass cls, Monitor mGroup)
        throws InstrumenterException {
// TODO validate
        AClass aClass = (AClass) cls;
        ClassNode cNode = aClass.getNode();
        MethodNode initializer = findInitializer(cNode, true);
        if (null == initializer) {
            initializer = createInitializer(cNode, true);
        }
        InsnList instr = new InsnList();
        int[] data = new int[DATA_SIZE];
        data[DATA_MAXSTACK] = initializer.maxStack;
        data[DATA_MAXLOCALS] = initializer.maxLocals;
        
        int settingsVar = data[DATA_MAXLOCALS]++;
        String settingsClassName = MonitoringGroupSettings.class.getName();
        String settingsName = Factory.toInternalName(settingsClassName);
        String settingsDescr = getClassTypeDescriptor(settingsClassName, false);
        String groupAccounting = Factory.toInternalName(
            GroupAccountingType.class.getName());
        String groupAccountingDescr = getClassTypeDescriptor(
            groupAccounting, true);

        
        // MonitoringGroupsSettings.getFromPool()
        instr.add(new MethodInsnNode(INVOKESTATIC, settingsName, "getFromPool", 
            "()" + settingsDescr));
        instr.add(new VarInsnNode(ASTORE, settingsVar));

        // param 0: settings
        instr.add(new VarInsnNode(ALOAD, settingsVar)); 
        data[DATA_MAXSTACK]++;

        data[DATA_ARRAY_VAR] = -1;
        // param 1: prepare string array with ids, maxlocals = 4 -> 2
        String stringArrayDescr = addStringIdArray(cls, mGroup, instr, data);

        // param 2: prepare state array, maxlocals = 5 -> 3
        String debugStateArrayDescr = addDebugStateArray(cls, mGroup, instr, 
            data);
        
        // param 3: group accounting type
        instr.add(new FieldInsnNode(GETSTATIC, groupAccounting, 
            mGroup.groupAccounting().name(), groupAccountingDescr));
        data[DATA_MAXSTACK]++;
        
        // param 4: prepare resources array
        String resourceTypeArrayDescr = addResourceTypeArray(cls, 
            mGroup, instr, data);

        // call setBasics(String[] id, DebugState[] debugStates, 
        //  GroupAccountingType gType, ResourceType[] resources)
        instr.add(new MethodInsnNode(INVOKEVIRTUAL, settingsName, "setBasics", 
            "(" + stringArrayDescr + debugStateArrayDescr 
            + groupAccountingDescr + resourceTypeArrayDescr + ")V"));

        // optional: produce code for filling multi values
        if (mGroup.id().length > 1) {
            String booleanValue = Factory.toInternalName(
                BooleanValue.class.getName());
            String booleanValueDescr = getClassTypeDescriptor(
                booleanValue, true);
            // param 0: settings
            instr.add(new VarInsnNode(ALOAD, settingsVar));
            // param 1: 
            instr.add(new FieldInsnNode(GETSTATIC, booleanValue, 
                mGroup.distributeValues().name(), booleanValueDescr));
            // param 2: 
            instr.add(new FieldInsnNode(GETSTATIC, booleanValue, 
                mGroup.considerContained().name(), booleanValueDescr));
            // call setMulti(BooleanValue, BooleanValue)
            instr.add(new MethodInsnNode(INVOKEVIRTUAL, settingsName, 
                "setMulti", "(" + booleanValueDescr + booleanValueDescr 
                + ")V"));
            data[DATA_MAXSTACK] = Math.max(data[DATA_MAXSTACK], 3);
        }
        
        // produce code for registering the group
        int tmp = Utils.appendRecorderCallProlog(instr, true);
        data[DATA_MAXSTACK] = Math.max(data[DATA_MAXSTACK], getLSB(tmp));
        data[DATA_MAXLOCALS] += getMSB(tmp);
        instr.add(new LdcInsnNode(cls.getName()));
        instr.add(new VarInsnNode(ALOAD, settingsVar));
        tmp = Utils.appendRecorderCall(instr, "registerForRecording", 
            "(" + STRING_DESCR + settingsDescr + ")V");
        data[DATA_MAXSTACK] = Math.max(data[DATA_MAXSTACK], getLSB(tmp));
        data[DATA_MAXLOCALS] += getMSB(tmp);

        // param 0
        instr.add(new VarInsnNode(ALOAD, settingsVar));
        // MonitoringGroupSettings.release(_settings);
        instr.add(new MethodInsnNode(INVOKESTATIC, settingsName, "release", 
            "(" + settingsDescr + ")V"));
        data[DATA_MAXSTACK] = Math.max(data[DATA_MAXSTACK], 1);
        
        insertBeforeReturn(instr, initializer.instructions, -1, null);
        initializer.maxLocals = data[DATA_MAXLOCALS];
        initializer.maxStack = data[DATA_MAXSTACK];
    }
    
    /**
     * Adds the creation of a string array of identifiers from 
     * <code>mGroup</code> to <code>instr</code>.
     * 
     * @param cls the class to work on
     * @param mGroup the annotation to process
     * @param instr the instruction list to be modified
     * @param data intermediary data, an array of length {@link #DATA_SIZE} 
     *   storing the local variable storing temporary arrays in
     *   {@link #DATA_ARRAY_VAR}, the current maximum stack size in 
     *   {@link #DATA_MAXSTACK} and the maximum number of local variables in 
     *   {@link #DATA_MAXLOCALS}.
     * @return the descriptor of the array created
     * 
     * @since 1.00
     */
    private String addStringIdArray(IClass cls, Monitor mGroup, InsnList instr, 
        int[] data) {
        String[] rId = mGroup.id();
        if (rId.length > 0) {
            instr.add(integerToNode(rId.length));
            instr.add(new TypeInsnNode(ANEWARRAY, STRING));
            if (data[DATA_ARRAY_VAR] < 0) {
                data[DATA_ARRAY_VAR] = data[DATA_MAXLOCALS]++;
            }
            instr.add(new VarInsnNode(ASTORE, data[DATA_ARRAY_VAR]));
            for (int i = 0; i < rId.length; i++) {
                instr.add(new VarInsnNode(ALOAD, data[DATA_ARRAY_VAR]));
                instr.add(integerToNode(i));
                String id = rId[i].trim();
                if (0 == id.length()) {
                    id = cls.getName();
                }
                instr.add(new LdcInsnNode(id));
                instr.add(new InsnNode(AASTORE));
            }
            instr.add(new VarInsnNode(ALOAD, data[DATA_ARRAY_VAR]));
            data[DATA_MAXSTACK] += 3;
        } else {
            instr.add(new InsnNode(ACONST_NULL));
            data[DATA_MAXSTACK]++;            
        }
        return "[" + STRING_DESCR;
    }
    
    /**
     * Adds the creation of a {@link DebugState} array from 
     * <code>mGroup</code> to <code>instr</code>.
     * 
     * @param cls the class to work on
     * @param mGroup the annotation to process
     * @param instr the instruction list to be modified
     * @param data intermediary data, an array of length {@link #DATA_SIZE} 
     *   storing the local variable storing temporary arrays in
     *   {@link #DATA_ARRAY_VAR}, the current maximum stack size in 
     *   {@link #DATA_MAXSTACK} and the maximum number of local variables in 
     *   {@link #DATA_MAXLOCALS}.
     * @return the descriptor of the array created
     * 
     * @since 1.00
     */
    private String addDebugStateArray(IClass cls, Monitor mGroup, 
        InsnList instr, int[] data) {
        String debugState = Factory.toInternalName(
            DebugState.class.getName());
        String debugStateDescr = getClassTypeDescriptor(debugState, true);
        String debugStateArrayDescr = "[" + debugStateDescr;
        
        DebugState[] states = mGroup.debug();
        if (0 == states.length) {
            instr.add(new FieldInsnNode(GETSTATIC, debugState, "NONE", 
                debugStateArrayDescr));
            data[DATA_MAXSTACK]++;            
        } else {
            instr.add(integerToNode(states.length));
            instr.add(new TypeInsnNode(ANEWARRAY, debugStateArrayDescr));
            if (data[DATA_ARRAY_VAR] < 0) {
                data[DATA_ARRAY_VAR] = data[DATA_MAXLOCALS]++;
            }
            instr.add(new VarInsnNode(ASTORE, data[DATA_ARRAY_VAR]));
            for (int i = 0; i < states.length; i++) {
                instr.add(new VarInsnNode(ALOAD, data[DATA_ARRAY_VAR]));
                instr.add(integerToNode(i));
                instr.add(new FieldInsnNode(GETSTATIC, debugState, 
                    states[i].name(), debugStateDescr));
                instr.add(new InsnNode(AASTORE));
            }
            instr.add(new VarInsnNode(ALOAD, data[DATA_ARRAY_VAR]));
            data[DATA_MAXSTACK] += 3;
        }
        return debugStateArrayDescr;
    }

    /**
     * Adds the creation of a {@link ResourceType} array from 
     * <code>mGroup</code> to <code>instr</code>.
     * 
     * @param cls the class to work on
     * @param mGroup the annotation to process
     * @param instr the instruction list to be modified
     * @param data intermediary data, an array of length {@link #DATA_SIZE} 
     *   storing the local variable storing temporary arrays in
     *   {@link #DATA_ARRAY_VAR}, the current maximum stack size in 
     *   {@link #DATA_MAXSTACK} and the maximum number of local variables in 
     *   {@link #DATA_MAXLOCALS}.
     * @return the descriptor of the array created
     * 
     * @since 1.00
     */
    private String addResourceTypeArray(IClass cls, Monitor mGroup, 
        InsnList instr, int[] data) {
        String resourceType = Factory.toInternalName(
            ResourceType.class.getName());
        String resourceTypeDescr = getClassTypeDescriptor(
            resourceType, true);
        String resourceTypeArrayDescr = "[" + resourceTypeDescr;

        ResourceType[] resources = mGroup.resources();
        if (0 == resources.length) {
            instr.add(new FieldInsnNode(GETSTATIC, resourceType, "SET_DEFAULT", 
                resourceTypeArrayDescr));
            data[DATA_MAXSTACK]++;
        } else {
            instr.add(integerToNode(resources.length));
            instr.add(new TypeInsnNode(ANEWARRAY, resourceTypeArrayDescr));
            if (data[DATA_ARRAY_VAR] < 0) {
                data[DATA_ARRAY_VAR] = data[DATA_MAXLOCALS]++;
            }
            for (int i = 0; i < resources.length; i++) {
                instr.add(new VarInsnNode(ALOAD, data[DATA_ARRAY_VAR]));
                instr.add(integerToNode(i));
                instr.add(new FieldInsnNode(GETSTATIC, resourceType, 
                    resources[i].name(), resourceTypeDescr));
                instr.add(new InsnNode(AASTORE));
            }
            instr.add(new VarInsnNode(ALOAD, data[DATA_ARRAY_VAR]));
            data[DATA_MAXSTACK] += 3;
        }
        return resourceTypeArrayDescr;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void instrumentVariabilityHandler(IBehavior behavior)
        throws InstrumenterException {
        
        MethodNode mNode = ((ABehavior) behavior).getNode();
        InsnList instructions = mNode.instructions;
        int maxStack = 0;
        InsnList insn = new InsnList();
        
        maxStack = Math.max(maxStack, 
            buildEnableVariabilityDetectionCall(insn, false));
        insertAtBeginning(insn, instructions);
        insn.clear();
        
        maxStack = Math.max(maxStack, 
            buildEnableVariabilityDetectionCall(insn, true));
        Utils.insertBeforeReturn(insn, instructions, -1, null);
        
        mNode.maxStack = Math.max(mNode.maxStack, maxStack);
    }
    
    /**
     * Builds a call to the Recorder method 
     * <code>enableVariabilityDetection</code> with the given 
     * <code>enable</code> parameter.
     * 
     * @param insn the instruction set to which to append the new instructions
     * @param enable the parameter to be passed to the call to be inserted
     * @return the worst case max stack size
     * 
     * @since 1.00
     */
    private int buildEnableVariabilityDetectionCall(InsnList insn, 
        boolean enable) {
        int maxStackDelta = 0;
        // push static class id onto stack (+1)
        int tmp = appendRecorderCallProlog(insn, false);
        maxStackDelta = Math.max(maxStackDelta, Utils.getLSB(tmp));
        // push false onto stack (+1)
        insn.add(booleanToNode(enable));
        maxStackDelta++;
        
        // push method call onto stack, no return value
        tmp = appendRecorderCall(insn, 
            "enableVariabilityDetection", "(Z)V");
        maxStackDelta = Math.max(maxStackDelta, Utils.getLSB(tmp));

        return maxStackDelta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFinalizer(IClass cls, boolean overhead)
        throws InstrumenterException {
        if (!overhead) {
            AClass aClass = (AClass) cls;
            if (!cls.hasSuperClassFinalFinalizer()) {
                final String descriptor = "()V";
                final String name = "finalize";
                ClassNode cNode = aClass.getNode();
                final String[] exceptions = new String[] {STRING};
                MethodNode mNode = new MethodNode(ACC_PROTECTED, 
                    name, descriptor, null, exceptions);
                IClass sClass = 
                    aClass.findSuperclassWithMethodWoParameter(name);
                if (null != sClass) {
                    mNode.instructions.add(new VarInsnNode(ALOAD, 0));
                    mNode.instructions.add(new MethodInsnNode(INVOKESPECIAL, 
                        Factory.toInternalName(sClass.getName()), name, 
                        descriptor));
                    int maxStack = 1;
                    int maxLocals = 1; // param 0

                    int tmp = appendRecorderCallProlog(mNode.instructions, 
                        false);
                    maxStack = Math.max(maxStack, getLSB(tmp));
                    maxLocals += getMSB(tmp);
                    mNode.instructions.add(new VarInsnNode(ALOAD, 0));
                    maxStack = Math.max(maxStack, Utils.getLSB(tmp) + 2);
                    tmp = appendRecorderCall(mNode.instructions, "memoryFreed", 
                        MEMORY_DESCR);
                    maxStack = Math.max(maxStack, Utils.getLSB(tmp));
                    maxLocals += getMSB(tmp);
                    
                    mNode.instructions.add(new InsnNode(RETURN));
                    mNode.maxStack = maxStack;
                    mNode.maxLocals = maxLocals;
                    cNode.methods.add(mNode);
                    sClass.release();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAnnotation(IClass cls, Class<? extends Annotation> ann, 
        HashMap<String, Object> values)
        throws InstrumenterException {
        AClass aClass = (AClass) cls;
        ClassNode cNode = aClass.getNode();
        AnnotationNode aNode = new AnnotationNode(
            getClassTypeDescriptor(ann.getName(), false));
        setAnnotationValues(aNode, values);
        if (null == cNode.visibleAnnotations) {
            cNode.visibleAnnotations = new ArrayList<AnnotationNode>(1);
        }
        cNode.visibleAnnotations.add(aNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAnnotation(IBehavior behavior, 
        Class<? extends Annotation> ann, HashMap<String, Object> values)
        throws InstrumenterException {
        MethodNode cMethod = ((ABehavior) behavior).getNode();
        AnnotationNode aNode = new AnnotationNode(
            getClassTypeDescriptor(ann.getName(), false));
        setAnnotationValues(aNode, values);
        if (null == cMethod.visibleAnnotations) {
            cMethod.visibleAnnotations = new ArrayList<AnnotationNode>(1);
        }
        cMethod.visibleAnnotations.add(aNode);
    }

    /**
     * Sets the annotation values.
     * 
     * @param aNode the annotation node
     * @param values the name-value mappings
     * 
     * @since 1.00
     */
    private void setAnnotationValues(AnnotationNode aNode, 
        HashMap<String, Object> values) {
        if (null != values) {
            if (null == aNode.values) {
                aNode.values = new ArrayList<Object>();
            }
            Iterator<HashMap.Entry<String, Object>> iter 
                = values.entries().iterator();
            while (iter.hasNext()) {
                HashMap.Entry<String, Object> ent = iter.next();
                aNode.values.add(ent.getKey());
                Object val = ent.getValue();
                if (val.getClass().isArray()) {
                    val = Arrays.asList((Object[]) val);
                }
                aNode.values.add(val);
            }
        }
    }
    
    /**
     * Returns the internal name of a descriptor.
     * 
     * @param isStatic whether it should be a static or an instance initializer
     * @return the internal name
     * 
     * @since 1.00
     */
    private static String getDescriptorName(boolean isStatic) {
        String searchName;
        if (isStatic) {
            searchName = "<clinit>";
        } else {
            searchName = "<init>";
        }
        return searchName;
    }
    
    /**
     * Searches for an initializer.
     * 
     * @param cNode the node to create the initializer in
     * @param isStatic whether it should be a static or an instance initializer
     * @return the initializer or <b>null</b> if none was found
     * 
     * @since 1.00
     */
    private static MethodNode findInitializer(
        ClassNode cNode, boolean isStatic) {
        String searchName = getDescriptorName(isStatic);
        MethodNode foundMethod = null;
        int mCount = cNode.methods.size();
        for (int m = 0; null == foundMethod && m < mCount; m++) {
            MethodNode mNode = (MethodNode) cNode.methods.get(m);
            // descriptor unclear
            if (mNode.name.equals(searchName) 
                && mNode.desc.equals(INITIALIZER_DESCR)) {
                foundMethod = mNode;
            }
        }
        return foundMethod;
    }
    
    /**
     * Creates a class initializer. This method creates and adds an initializer
     * regardless whether there is an existing initializer.
     * 
     * @param cNode the node to create the initializer in
     * @param isStatic whether it should be a static or an instance initializer
     * @return the created initializer
     * 
     * @since 1.00
     */
    private static MethodNode createInitializer(ClassNode cNode, 
        boolean isStatic) {
        int access;
        if (isStatic) {
            access = ACC_STATIC | ACC_PUBLIC;
        } else {
            access = ACC_PUBLIC;
        }
        MethodNode result  = new MethodNode(access, 
            getDescriptorName(isStatic), INITIALIZER_DESCR, null, null);
        result.instructions.add(new InsnNode(RETURN));
        cNode.methods.add(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeConfiguration(IClass cls, String args)
        throws InstrumenterException {
        AClass aClass = (AClass) cls;
        ClassNode cNode = aClass.getNode();
        int count = cNode.fields.size();
        FieldNode foundField = null;
        for (int i = 0; null == foundField && i < count; i++) {
            FieldNode field = (FieldNode) cNode.fields.get(i);
            if (field.name.equals(CONFIGURATION_FIELD_NAME)) {
                foundField = field;
            }
        }
        if (null != foundField) {
            boolean isStatic = Flags.isSet(foundField.access, 
                Opcodes.ACC_STATIC);
            MethodNode foundMethod = findInitializer(cNode, isStatic);
            if (null == foundMethod) {
                foundMethod = createInitializer(cNode, isStatic);
            }
            InsnList instr = new InsnList();
            instr.add(new LdcInsnNode(args));
            int opcode;
            if (isStatic) {
                opcode = PUTSTATIC;
            } else {
                opcode = PUTFIELD;
            }
            instr.add(new FieldInsnNode(opcode, cNode.name, 
                CONFIGURATION_FIELD_NAME, STRING_DESCR));
            insertBeforeReturn(foundMethod.instructions, instr, -1, null);
        }
    }

}
