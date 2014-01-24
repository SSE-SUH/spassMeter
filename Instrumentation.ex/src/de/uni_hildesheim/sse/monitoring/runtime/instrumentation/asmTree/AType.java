package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import org.objectweb.asm.tree.ClassNode;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IClass;

/**
 * Defines an abstract base class for primitives and classes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AType extends IClass {

    /**
     * Stores the JVM internal name of this type.
     */
    private String internalName;
    
    /**
     * Stores the fqn (code) name of this type. Lazy initialization on demand.
     */
    private String name;
    
    /**
     * Defines the internal name and clears {@link #name} as a side effect.
     * 
     * @param internalName the JVM internal name
     * 
     * @since 1.00
     */
    protected void setInternalName(String internalName) {
        this.internalName = internalName;
        this.name = null;
    }

    /**
     * Returns the internal JVM name of this type.
     * 
     * @return the internal JVM name
     * 
     * @since 1.00
     */
    String getInternalName() {
        return internalName;
    }
    
    /**
     * Returns the name of the class.
     * 
     * @return the name of the class
     * 
     * @since 1.00
     */
    @Override
    public String getName() {
        if (null == name) {
            name = Factory.toCodeName(internalName, true);
        }
        return name;
    }

    /**
     * Returns the class node attached to this type.
     * 
     * @return the class node (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public abstract ClassNode getNode();
    
    /**
     * Returns the component type in case of arrays.
     * 
     * @return the component type (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public abstract AType getComponentType();

    /**
     * Returns whether this type is an instance of <code>type</code>.
     * 
     * @param type the type to check for
     * @return <code>true</code> if this type is an instance of 
     *   <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    abstract boolean isInstanceOf(AType type);
    
    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        internalName = null;
        name = null;
    }
}
