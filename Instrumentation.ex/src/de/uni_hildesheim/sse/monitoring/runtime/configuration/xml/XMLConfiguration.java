package de.uni_hildesheim.sse.monitoring.runtime.configuration.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.regex.PatternSyntaxException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.*;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    IAnnotationBuilder;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.xml.QdParser;
import de.uni_hildesheim.sse.monitoring.runtime.utils.xml.QdParserException;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.
    InstrumentedFileInputStream;

/**
 * Represents an instrumenter configuration read from XML.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.CONFIG_XML)
public class XMLConfiguration {

    /**
     * Stores the configuration, i.e. a mapping from signatures to annotation
     * types to annotation builders.
     */
    private HashMap<String, AnnotationBuilderMap> configuration;
    
    /**
     * Stores the patterns.
     */
    private ArrayList<Pattern> patterns;
    
    /**
     * Stores the group configurations.
     */
    private HashMap<String, MonitoringGroupConfiguration> groupConfigurations;
    
    /**
     * Stores if this configuration is exclusive, i.e. authoritive or if it may
     * overlap with source code annotations. Default is <code>true</code>.
     */
    private boolean exclusive = true;
    
    
    /**
     * Reads a configuration from a given file.
     * 
     * @param file the file to read
     * @return the read configuration
     * @throws IOException in case of any reading error, including erroneous
     *     structures and unknown information
     * 
     * @since 1.00
     */
    public static XMLConfiguration read(String file) throws IOException {
        XMLConfiguration result = new XMLConfiguration();
        try {
            QdParser parser = new QdParser();
            XMLHandler handler = new XMLHandler();
            FileInputStream fis;
            if (Configuration.INSTANCE.recordOverhead()) {
                fis = new InstrumentedFileInputStream(file, Helper.RECORDER_ID);
            } else {
                fis = new FileInputStream(file);
            }
            parser.parse(handler, fis, false);
            result.configuration = handler.getConfiguration();
            result.exclusive = handler.isExclusive();
            result.patterns = handler.getPatterns();
            result.groupConfigurations = handler.getGroupConfigurations();
        } catch (QdParserException e) {
            throw new IOException(e);
        }
/*        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XMLHandler handler = new XMLHandler();
            FileInputStream fis;
            if (Configuration.INSTANCE.recordOverhead()) {
                fis = new InstrumentedFileInputStream(file, Helper.RECORDER_ID);
            } else {
                fis = new FileInputStream(file);
            }
            saxParser.parse(fis, handler);
            result.configuration = handler.getConfiguration();
            result.exclusive = handler.isExclusive();
            result.patterns = handler.getPatterns();
            result.groupConfigurations = handler.getGroupConfigurations();
        } catch (SAXException e) {
            throw new IOException(e);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        }
        */
        return result;
    }

    /**
     * Returns if this configuration states that the given signature has an
     * "annotation" of the given annotation class.
     * 
     * @param <T> the annotation type (inferred)
     * @param signature the signature to search for (no return type, no spaces,
     *    no parameter names, no exceptions, fully qualified names only, 
     *    prefixed fully qualified name for methods)
     * @param classOfSignature an object representing the class of the 
     *    signature. This instance may be passed to <code>resolver</code> and
     *    is therefore dependent on the implementation of the instrumenter
     * @param cls the annotation class to match
     * @param resolver an optional resolver instance to create annotations 
     *   based on parameterized data on call (may be <b>null</b>)
     * @return <code>true</code> if a mapping exist, i.e. an annotation is 
     *     defined for the signature, <code>false</code> else
     * 
     * @since 1.00
     */
    public <T extends Annotation> boolean hasAnnotation(String signature, 
        Object classOfSignature, Class<T> cls, IResolver resolver) {
        IAnnotationBuilder<?> builder = getBuilderForPattern(signature, 
            classOfSignature, cls, resolver);
        if (null == builder && null != configuration) {
            HashMap<Class<? extends Annotation>, IAnnotationBuilder<?>> map 
                = configuration.get(signature);
            if (null != map) {
                builder = map.get(cls);
            }
        }
        return (null != builder && builder.getInstanceClass() == cls);
    }

    /**
     * Returns the annotation attached to the given signature.
     * 
     * @param <T> the annotation type (inferred)
     * @param signature the signature to search for (no return type, no spaces,
     *    no parameter names, no exceptions, fully qualified names only, 
     *    prefixed fully qualified name for methods)
     * @param classOfSignature an object representing the class of the 
     *    signature. This instance may be passed to <code>resolver</code> and
     *    is therefore dependent on the implementation of the instrumenter
     * @param cls the annotation class to match
     * @param resolver an optional resolver instance to create annotations 
     *   based on parameterized data on call (may be <b>null</b>)
     * @return the annotation or <b>null</b> if not found
     * 
     * @since 1.00
     */
    public <T extends Annotation> T getAnnotation(String signature, 
        Object classOfSignature, Class<T> cls, IResolver resolver) {
        IAnnotationBuilder<?> builder = getBuilderForPattern(signature, 
            classOfSignature, cls, resolver);
        if (null == builder && null != configuration) {
            HashMap<Class<? extends Annotation>, IAnnotationBuilder<?>> map 
                = configuration.get(signature);
            if (null != map) {
                builder = map.get(cls);
            }
        }
        T result = null;
        if (null != builder && builder.getInstanceClass() == cls) {
            result = cls.cast(builder.create());
        }
        return result;
    }

    /**
     * Returns the annotation builder derived from a pattern or a type 
     * constrained from the given signature.
     * 
     * @param <T> the annotation type (inferred)
     * @param signature the signature to search for (no return type, no spaces,
     *    no parameter names, no exceptions, fully qualified names only, 
     *    prefixed fully qualified name for methods)
     * @param classOfSignature an object representing the class of the 
     *    signature. This instance may be passed to <code>resolver</code> and
     *    is therefore dependent on the implementation of the instrumenter
     * @param cls the annotation class to match
     * @param resolver an optional resolver instance to create annotations 
     *   based on parameterized data on call (may be <b>null</b>)
     * @return the annotation builder or <b>null</b> if not found
     * 
     * @since 1.00
     */
    private <T extends Annotation> IAnnotationBuilder<?> getBuilderForPattern(
        String signature, Object classOfSignature, Class<T> cls, 
        IResolver resolver) {
        IAnnotationBuilder<?> result = null;
        if (null != patterns) {
            for (int p = 0; null == result && p < patterns.size(); p++) {
                Pattern pattern = patterns.get(p);
                boolean matchesPattern = true;
                if (null != pattern.getPattern()) {
                    if (!pattern.getPattern().equals(".*")) {
                        try {
                            matchesPattern 
                                = signature.matches(pattern.getPattern());
                        } catch (PatternSyntaxException e) {
                            // should be handled in pattern before!
                        }
                    } // otherways initialized to "true"
                }
                if (matchesPattern) {
                    if (null != pattern.getTypeOf()) {
                        matchesPattern = resolver.isInstanceOf(
                            classOfSignature, pattern.getTypeOf());
                    }
                }
                if (matchesPattern) {
                    result = pattern.get(cls);
                }
            }
        }
        return result;
    }
    
    /**
     * Returns if this configuration is exclusive, i.e. authoritive.
     * 
     * @return <code>true</code> if exclusive, <code>false</code> if it should
     *   be combined with source code annotations
     * 
     * @since 1.00
     */
    public boolean isExclusive() {
        return exclusive;
    }
    
    /**
     * Returns the monitoring group configuration for the specified recorder
     * identification.
     * 
     * @param recId the recorder identification to return the configuration for
     * @return the group configuration (may be 
     *     {@link MonitoringGroupConfiguration#DEFAULT} but not <b>null</b>)
     * 
     * @since 1.00
     */
    public MonitoringGroupConfiguration getMonitoringGroupConfiguration(
        String recId) {
        MonitoringGroupConfiguration result = groupConfigurations.get(recId);
        if (null == result) {
            result = MonitoringGroupConfiguration.DEFAULT;
        }
        return result;
    }

}
