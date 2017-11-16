package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import java.lang.annotation.Annotation;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;
import de.uni_hildesheim.sse.codeEraser.util.Annotations;
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

/**
 * Implements a delegating editor. This class replicates some
 * code from the <code>ExprEditor</code> defined in Javassist in order
 * to increase performance. Thus, the official interface of 
 * <code>ExprEditor</code> is not supported afterwards and changes in
 * new versions of Javassist need to be replicated here.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class AllDelegatingEditor extends ExprEditor 
    implements IStatementModifier {

    /**
     * Stores the instance pool.
     */
    private static final ArrayList<AllDelegatingEditor> POOL 
        = new ArrayList<AllDelegatingEditor>(1);
    
    /**
     * Stores the behavior editor.
     */
    private BehaviorEditor editor;

    /**
     * Stores the expression to be modified.
     */
    private transient Expr expression;

    /**
     * Stores the current method call to be modified. If this attribute is not
     * <b>null</b> it is the same as {@link #expression}.
     */
    private transient MethodCall methodCall;

    /**
     * Stores the declaring class of the current method call to be modified. 
     * Lazy initialization.
     */
    private transient CtClass methodCallDeclaringClass;
    
    /**
     * Stores the parameter types of the current method call to be modified. 
     * Lazy initialization.
     */
    private transient CtClass[] methodCallParameter;
    
    /**
     * Stores the current field access to be modified. If this attribute is not
     * <b>null</b> it is the same as {@link #expression}.
     */
    private transient CtField fieldAccessField;
    
    /**
     * Creates the delegating editor.
     * 
     * @param editor the editor to delegate to
     * 
     * @since 1.00
     */
    private AllDelegatingEditor(BehaviorEditor editor) {
        // available after patching javassist
//        disableCast = true;
//        disableHandler = true;
//        disableInstanceof = true;
        // available after patching javassist
        setDelegate(editor);
    }
    
    /**
     * Returns an instance from the pool.
     * 
     * @param editor the editor to set as delegate
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized AllDelegatingEditor getFromPool(
        BehaviorEditor editor) {
        AllDelegatingEditor result;
        int size = POOL.size();
        if (size > 0) {
            result = POOL.remove(size - 1);
            result.setDelegate(editor);
        } else {
            result = new AllDelegatingEditor(editor);
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
    static final synchronized void releaseClass(AllDelegatingEditor editor) {
        POOL.add(editor);
    }
        
    /**
     * Sets the delegate (for reuse).
     * 
     * @param editor the editor
     * 
     * @since 1.00
     */
    public void setDelegate(BehaviorEditor editor) {
        this.editor = editor;
        editor.setCodeModifier(this);
    }
    
    /**
     * Instruments a field access for notifying the recorder about value 
     * changes.
     * 
     * @param fa the field access
     * @throws CannotCompileException in case that the new code does 
     *   not compile
     */
    public void edit(FieldAccess fa) throws CannotCompileException {
        try {
            expression = fa;
            fieldAccessField = fa.getField();
            JAField field = JAClass.getFieldFromPool(fa.getField());
            editor.editFieldAccess(field.getName(), field.getTypeName(), 
                fa.isWriter());
            field.release();
            fieldAccessField = null;
        } catch (NotFoundException e) {
            Utils.warn(e);
        } catch (InstrumenterException e) {
            throw new CannotCompileException(e);
        }
    }
    
    /**
     * Instruments a method call, here starting additional threads.
     * 
     * @param mc the method call
     * @throws CannotCompileException in case that the new code does 
     *   not compile
     */
    @Override
    public void edit(MethodCall mc) throws CannotCompileException {
        try {
            expression = mc;
            methodCall = mc;
            editor.editMethodCall(mc.getMethodName(), mc.getSignature(), 
                mc.getClassName());
            methodCall = null;
            methodCallDeclaringClass = null;
            methodCallParameter = null;
        } catch (InstrumenterException e) {
            throw new CannotCompileException(e);
        }
    }

    /**
     * Instruments the creation of an array.
     * 
     * @param na the array creation
     * 
     * @throws CannotCompileException in case that the new code does 
     *   not compile
     */
    @Override
    public void edit(NewArray na) throws CannotCompileException {
        try {
            expression = na;
            editor.editNewArray();
        } catch (InstrumenterException e) {
            throw new CannotCompileException(e);
        }
    }

    /**
     * Instruments an object creation.
     * 
     * @param ne the expression to be annotated
     * @throws CannotCompileException in case that the new code does not 
     *   compile
     */
    public void edit(NewExpr ne) throws CannotCompileException {
        try {
            expression = ne;
            editor.editNewExpression(ne.getClassName());
        } catch (InstrumenterException e) {
            throw new CannotCompileException(e);
        }
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
        StringBuilder tmp = new StringBuilder();
        tmp.append("$_ = new ");
        tmp.append(newType);
        tmp.append("($$);");
        String call = CodeModifier.MEM_ALLOC_CALL;
        if (accountMemory && null != call && call.length() > 0) {
            tmp.append(CodeModifier.MEM_ALLOC_CALL);
        }
        try {
            expression.replace(tmp.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
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
        String call = CodeModifier.MEM_ALLOC_CALL;
        if (null != call && call.length() > 0) {
            StringBuilder tmp = new StringBuilder();
            insertContextNotificationCode(tmp, contextId, true);
            tmp.append("$_ = $proceed($$); ");
            tmp.append(CodeModifier.MEM_ALLOC_CALL);
            insertContextNotificationCode(tmp, contextId, false);
            try {
                expression.replace(tmp.toString());
            } catch (CannotCompileException e) {
                if (!(e.getCause() instanceof ClassCircularityError)) { // typically in JDK...
                    throw Utils.warnOrConvert(e);
                }
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
    public void notifyValueChanged(String recId) throws InstrumenterException {
        StringBuilder buf = new StringBuilder(CodeModifier.RECORDER);
        buf.append(".notifyValueChange(");
        if (null == recId) {
            buf.append("(String)null");
        } else {
            buf.append("\"");
            buf.append(recId);
            buf.append("\"");
        }
        buf.append(",$1); $proceed($$);");
        try {
            expression.replace(buf.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
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
        StringBuilder call = new StringBuilder();
        if (null != typeExpression) {
            call.append(StreamType.class.getName());
            call.append(" _instr_te = ");
            switch (typeExpression) {
            case URL_TYPE:
                call.append(StreamType.class.getName());
                call.append(".getForURL($0.getProtocol())");
                break;
            default:
                throw new InstrumenterException(
                    "unknown type expression constant");
            }
            call.append(";");
            call.append("if (null != _instr_te) {");
        }
        call.append("$_ = new ");
        if (input) {
            call.append(DelegatingInputStream.class.getName());
        } else {
            call.append(DelegatingOutputStream.class.getName());
        }
        call.append("($proceed($$), ");
        if (null != typeExpression) {
            call.append("_instr_te");
        } else {
            call.append(type.getClass().getName());
            call.append(".");
            call.append(type.name());
        }
        /*if (overhead) {
            call.append(",\"");
            call.append(Helper.RECORDER_ID);
            call.append("\"");
        }*/
        call.append(");");
        if (null != typeExpression) {
            call.append("}");
        }

        try {
            expression.replace(call.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
    }
    
    /**
     * Inserts additional notification code needed for context changes.
     * 
     * @param buf the buffer to be modified as a side effect by appending code
     * @param contextId the context id (may be <b>null</b>)
     * @param before code for inserting before the actual notification
     * @return <code>true</code> if code was added, <code>false</code> else
     * 
     * @since 1.00
     */
    private boolean insertContextNotificationCode(StringBuilder buf, 
        String contextId, boolean before) {
        boolean result = false;
        if (null != contextId) {
            buf.append(CodeModifier.RECORDER);
            buf.append(".changeValueContext(\"");
            buf.append(contextId);
            buf.append("\",");
            buf.append(before);
            buf.append(");");
            result = true;
        }
        return result;
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
        StringBuilder buf = new StringBuilder();
        insertContextNotificationCode(buf, contextId, true);
        buf.append(CodeModifier.RECORDER);
        buf.append(".notifyThreadStart($0);$_ = $proceed($$);");
        insertContextNotificationCode(buf, contextId, false);
        try {
            expression.replace(buf.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
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
        StringBuilder call = new StringBuilder();
        insertContextNotificationCode(call, contextId, true);
        call.append("$_ = $proceed($$);");
        call.append(CodeModifier.RECORDER);
        call.append(".");
        if (write) {
            call.append("write");
        } else {
            call.append("read");
        }
        call.append("Io(null,null,$1.getLength(),");
        call.append(StreamType.class.getName());
        call.append(".");
        call.append(StreamType.NET.name());
        call.append(");");
        insertContextNotificationCode(call, contextId, false);
        try {
            expression.replace(call.toString());
        } catch (CannotCompileException e) {
            throw new InstrumenterException(e);
        }
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
        if (null != methodCall) {
            try {
                StringBuilder call = new StringBuilder();
                boolean modify = insertContextNotificationCode(
                    call, contextId, true);
                if (!methodCall.getSignature().endsWith(")V")) { 
                   // avoid resolve the call at latest time -> Class Circularity
                    call.append("$_ = ");
                }
                call.append("$proceed($$);");
                modify |= insertContextNotificationCode(call, contextId, false);
                if (modify) {
                    methodCall.replace(call.toString());
                }
            } catch (CannotCompileException e) {
                throw new InstrumenterException(e);
            } catch (ClassCircularityError e) {
                // bad but ignore
            }
        }
    }
    
    /**
     * Cleans this instance for reuse.
     * 
     * @since 1.00
     */
    public void clean() {
        editor = null;
        expression = null;
        methodCall = null;
        methodCallDeclaringClass = null;
        methodCallParameter = null;
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
        boolean result;
        if (null != methodCall) {
            if (null == methodCallDeclaringClass) {
                try {
                    methodCallDeclaringClass 
                        = methodCall.getMethod().getDeclaringClass();
                } catch (NotFoundException e) {
                    throw new InstrumenterException(e);
                }
            }
            result = JAClass.isInstanceOf(methodCallDeclaringClass, type);
        } else {
            result = false;
        }
        return result;
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
    public String mcDeclaringClassName() throws InstrumenterException {
        String result;
        if (null != methodCall) {
            if (null == methodCallDeclaringClass) {
                try {
                    methodCallDeclaringClass 
                        = methodCall.getMethod().getDeclaringClass();
                } catch (NotFoundException e) {
                    throw new InstrumenterException(e);
                }
            }
            result = methodCallDeclaringClass.getName();
        } else {
            result = "";
        }
        return result;
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
    public int mcParameterCount() throws InstrumenterException {
        int result;
        if (null != methodCall) {
            if (null == methodCallParameter) {
                try {
                    methodCallParameter 
                        = methodCall.getMethod().getParameterTypes();
                } catch (NotFoundException e) {
                    throw new InstrumenterException(e);
                }
            }
            result = methodCallParameter.length;
        } else {
            result = 0;
        }
        return result;
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
    public String mcParameterTypeName(int index) throws InstrumenterException {
        String result;
        if (null != methodCall) {
            if (null == methodCallParameter) {
                try {
                    methodCallParameter 
                        = methodCall.getMethod().getParameterTypes();
                } catch (NotFoundException e) {
                    throw new InstrumenterException(e);
                }
            }
            result = methodCallParameter[index].getName();
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Returns whether the current method call goes to a static 
     * method. This method works properly only if the statement 
     * being processed is a method call. [performance]
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
        boolean result;
        if (null != methodCall) {
            try {
                result = Modifier.isStatic(
                    methodCall.getMethod().getModifiers());
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        } else {
            result = false;
        }
        return result;
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
    public <T extends Annotation> T fAnnotation(Class<T> annotation, 
        AnnotationSearchType search) {
        // check AbstractClassTransformer
        T result =  null;
        if (null != fieldAccessField) {
            boolean done = false;
            XMLConfiguration config = Configuration.INSTANCE.getXMLConfig();
            if (null != config) {
                result = config.getAnnotation(
                    JAField.getSignature(fieldAccessField), 
                    fieldAccessField.getDeclaringClass(), annotation, 
                    IFactory.getInstance());
                done = config.isExclusive();
            } 
            if (!done && null == result) {
                result = Annotations.getAnnotation(fieldAccessField, 
                    annotation, false);
            }
        }
        return result;
    }
 
    /**
     * Returns the name of the declaring class of the current field.
     * This method works properly only if the statement being processed is a 
     * field access. [performance, uncached]
     * 
     * @return the name of the declaring class.
     * @throws InstrumenterException in case that the declaring class is 
     *   not found
     * 
     * @since 1.00
     */
    public String fDeclaringClassName() throws InstrumenterException {
        String result;
        if (null != fieldAccessField) {
            result = fieldAccessField.getDeclaringClass().getName();
        } else {
            result = "";
        }
        return result;
    }
    
}