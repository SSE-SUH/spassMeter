package de.uni_hildesheim.sse.monitoring.runtime.configuration.xml;

import java.lang.annotation.Annotation;
import java.util.regex.PatternSyntaxException;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    IAnnotationBuilder;
import de.uni_hildesheim.sse.monitoring.runtime.utils.xml.QdParserException;

/**
 * Defines a pattern, i.e. a qualified path to a programming language element
 * as a regular expression or a type restriction.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class Pattern {
    
    /**
     * Stores the name pattern.
     */
    private String pattern;
    
    /**
     * Stores the type pattern, i.e. the matching type must be equal or a 
     * subtype of <code>typeOf</code>.
     */
    private String typeOf;
    
    /**
     * Stores the associated builders.
     */
    private AnnotationBuilderMap builders;
    
    /**
     * Creates a new pattern.
     * 
     * @param pattern the pattern (may be <b>null</b>)
     * @param typeOf the supertype type restriction (may be <b>null</b>)
     * @throws QdParserException in case that pattern is not valid
     * 
     * @since 1.00
     */
    public Pattern(String pattern, String typeOf) throws QdParserException {
        try {
            if (null != pattern) {
                // replace all non-escaped "." by "\."
//                pattern = pattern.replaceAll("([^\\\\])\\.", "$1\\\\.");
                // replace all non-escaped "$" by "\$"
 //               pattern = pattern.replaceAll("([^\\\\])\\$", "$1\\\\\\$");
                // test pattern
                "".matches(pattern);
            }
        } catch (PatternSyntaxException e) {
            throw new QdParserException(e);
        }
        this.pattern = pattern;
        this.typeOf = typeOf;
        builders = new AnnotationBuilderMap();
    }
    
    /**
     * Returns a builder for a given annotation.
     * 
     * @param cls the annotation class
     * @return the builder or <b>null</b> if none is registered
     * 
     * @since 1.00
     */
    public IAnnotationBuilder<?> get(Class<? extends Annotation> cls) {
        return builders.get(cls);
    }
    
    /**
     * Registers a builder.
     * 
     * @param cls the annotation class to register the builder for
     * @param builder the builder
     * 
     * @since 1.00
     */
    void register(Class<? extends Annotation> cls, 
        IAnnotationBuilder<?> builder) {
        builders.put(cls, builder);
    }
    
    /**
     * Returns the pattern.
     * 
     * @return the pattern
     * 
     * @since 1.00
     */
    public String getPattern() {
        return pattern;
    }
    
    /**
     * Returns the super type as restriction.
     * 
     * @return the super type as restriction (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public String getTypeOf() {
        return typeOf;
    }

    /**
     * Returns a textual representation of this object.
     * 
     * @return a textual representation
     */
    public String toString() {
        return "pattern " + pattern + " " + typeOf + " " + builders.size();
    }

}
