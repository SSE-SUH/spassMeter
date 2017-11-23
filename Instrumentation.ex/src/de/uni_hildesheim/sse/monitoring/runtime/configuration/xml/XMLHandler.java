package de.uni_hildesheim.sse.monitoring.runtime.configuration.xml;

import java.util.StringTokenizer;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.InstanceIdentifierKind;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.AnnotationBuilder;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Annotations;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    ConfigurationEntry;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.xml.DocHandler;
import de.uni_hildesheim.sse.monitoring.runtime.utils.xml.QdParserException;

/**
 * Implements the XML handler.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.23
 */
class XMLHandler implements DocHandler {

    /**
     * Defines the name of the "monitor" tag/annotation.
     */
    private static final String TAG_MONITOR = "monitor";
    
    /**
     * Stores the configuration, i.e. a mapping from signatures to annotation
     * types to annotation builders.
     */
    private HashMap<String, AnnotationBuilderMap> configuration 
        = new HashMap<String, AnnotationBuilderMap>();
    
    /**
     * Just stores whether the most detailed type of annotation attachment per 
     * class is a member (true) or not (false). May be <b>null</b> if 
     * plain time recording is not configured as default monitoring, i.e., 
     * other resources are considered by default.
     * 
     * @since 1.13
     */
    private HashMap<String, Boolean> analyzeMembers;
    
    /**
     * Stores if this configuration is exclusive, i.e. authoritative or if it may
     * overlap with source code annotations. Default is <code>true</code>.
     */
    private boolean exclusive = true;
    
    /**
     * Stores the actual stack of annotation builders as read from the XML file.
     */
    private ArrayList<AnnotationBuilder<?>> templateStack 
        = new ArrayList<AnnotationBuilder<?>>();

    /**
     * Stores the actual stack of patterns as read from the XML file.
     */
    private ArrayList<Pattern> patternStack = new ArrayList<Pattern>();
    
    /**
     * Stores the current path of modules and namespaces.
     */
    private ArrayList<PathElement> currentPath = new ArrayList<PathElement>();

    /**
     * Stores the patterns collected during XML file reading.
     */
    private ArrayList<Pattern> patterns = new ArrayList<Pattern>();

    /**
     * Stores the monitoring group configurations collected during XML file 
     * reading.
     */
    private HashMap<String, MonitoringGroupConfiguration> groupConfigurations 
        = new HashMap<String, MonitoringGroupConfiguration>();
    
    /**
     * Stores referenced but unresolved configurations (temporary attribute,
     * to be resolved at the end of processing.
     */
    private HashMap<String, String> unresolvedConfigurations 
        = new HashMap<String, String>();
    
    /**
     * Stores whether this configuration contains patterns.
     * 
     * @since 1.13
     */
    private boolean hasPatterns = false;
    
    /**
     * Returns whether this configuration is exclusive, i.e. source code 
     * annotations should not be considered.
     * 
     * @return <code>true</code> if this configuration is exclusive, 
     *    <code>false</code> else 
     * 
     * @since 1.00
     */
    boolean isExclusive() {
        return exclusive;
    }
    
    /**
     * Returns the mapping between class names and whether code analysis of
     * members is required according to the configuration.
     * 
     * @return the mapping of (Java) class names and whether analysis is 
     *   required, <b>null</b> if all classes must be considered
     *   
     * @since 1.13
     */
    HashMap<String, Boolean> getAnalyzeMembers() {
        return hasPatterns ? null : analyzeMembers;
    }
    
    /**
     * Returns the configuration constructed while reading an XML file.
     * 
     * @return the configuration
     * 
     * @since 1.00
     */
    HashMap<String, AnnotationBuilderMap> getConfiguration() {
        return configuration;
    }

    /**
     * Returns the patterns identified while reading an XML file.
     * 
     * @return the patterns
     * 
     * @since 1.00
     */
    ArrayList<Pattern> getPatterns() {
        return patterns;
    }
    
    /**
     * Returns the monitoring group configurations identified while reading an
     * XML file.
     * 
     * @return the group configurations
     * 
     * @since 1.00
     */
    HashMap<String, MonitoringGroupConfiguration> getGroupConfigurations() {
        return groupConfigurations;
    }
    
    /**
     * Appends a name to the current path.
     * 
     * @param type the type of the path element
     * @param name the name to be appended
     * @param pattern the pattern or <b>null</b>
     * 
     * @since 1.00
     */
    private void appendToCurrentPath(PathElement.Type type, 
        String name, String pattern) {
        currentPath.add(new PathElement(type, name, pattern));
    }
    
    /**
     * Removes the last name from the current path.
     * 
     * @since 1.00
     */
    private void removeLastFromCurrentPath() {
        if (!currentPath.isEmpty()) {
            currentPath.remove(currentPath.size() - 1);
        }
    }
    
    /**
     * Sets {@link #analyzeMembers} based on already read configuration settings, in particular whether we are 
     * instrumenting for plain time resources only.
     * 
     * @since 1.13
     */
    private void setPlainTime() {
        if (exclusive) {
            Configuration cfg = Configuration.INSTANCE;
            // time is the only accountable resource -> it is clear
            boolean isPlainTime = ResourceType.isType(
                cfg.getAccountableResources(), ResourceType.CPU_TIME);
            // or time is accountable && it is the only default resource
            isPlainTime |=
                ResourceType.isType(cfg.getDefaultGroupResources(), 
                    ResourceType.CPU_TIME) 
                && ResourceType.isType(cfg.getSumResources(), 
                    ResourceType.CPU_TIME);
            if (isPlainTime) {
                analyzeMembers = new HashMap<String, Boolean>();
            }
        }
    }
    
    /**
     * Returns whether the current path contains a pattern.
     * 
     * @return <code>true</code> for pattern, <code>false</code> else
     * 
     * @since 1.23
     */
    private boolean currentPathContainsPattern() {
        boolean contains = false;
        int size = currentPath.size();
        for (int e = 0; !contains && e < size; e++) {
            contains = null != currentPath.get(e).getPattern();
        }
        return contains;
    }
    
    /**
     * Returns the current path by calculating it from the path elements.
     * 
     * @param asRegEx should the result be given as a matching regular 
     *     expression
     * @return the current path
     * 
     * @since 1.00
     */
    private String getCurrentPath(boolean asRegEx) {
        StringBuilder builder = new StringBuilder();
        PathElement last = null;
        int size = currentPath.size();
        int size1 = size - 1;
        for (int e = 0; e < size; e++) {
            PathElement element = currentPath.get(e);
            boolean isLast = (e == size1);
            if (null != last) {
                // determine the correct separator char, consider
                // escaping meta characters inplace
                if (PathElement.Type.NAMESPACE == last.getType() 
                    || PathElement.Type.DATA == element.getType() 
                    || PathElement.Type.BEHVIOUR == element.getType()) {
                    if (asRegEx) {
                        builder.append("\\");
                    }
                    builder.append(".");
                } else {
                    if (asRegEx) {
                        builder.append("\\");
                    }
                    builder.append("$");
                }
            }
            if (asRegEx) {
                // if a regEx should be returned, append the pattern if 
                // available. Handle end of path separately in order to include
                // the element name if possible (simplifies XML)
                if (!isLast && null != element.getPattern()) {
                    builder.append(element.getPattern());
                } else {
                    builder.append(element.getName());
                }
                if (isLast && null != element.getPattern()) {
                    builder.append(element.getPattern());
                }
            } else {
                // no regEx, just add name
                builder.append(element.getName());
            }
            last = element;
        }
        return builder.toString();
    }

    /**
     * Modifies the path according to the name of the element and its
     * attributes.
     * @param tag the name of the element
     * @param attributes the attributes of <code>tag</code>, may be 
     *     <b>null</b> in order to trigger removal of the last path
     *     element if the current XML element allows this
     * @return the type of the element
     * 
     * @since 1.00
     */
    private PathElement.Type modifyPath(String tag, 
        HashMap<String, String> attributes) {
        PathElement.Type type = PathElement.Type.xmlValueOf(tag);

        if (null != type) {
            String pathElementName = null;
            if (null == pathElementName) {
                if (null != attributes) {
                    if (PathElement.Type.BEHVIOUR == type) {
                        pathElementName = attributes.get("signature");
                    } else {
                        pathElementName = attributes.get("name");
                        if (null == pathElementName) { // namespace/module without name (but pattern?)
                            pathElementName = "";
                        }
                    }
                }
            }
            // process the data
            if (null != type) {
                if (null != attributes) {
                    if (null != pathElementName) {
                        appendToCurrentPath(type, 
                            pathElementName, attributes.get("pattern"));
                    }
                } else {
                    // attributes == null is flag for removing the last
                    removeLastFromCurrentPath();
                }
            }
        }
        return type;
    }

    /**
     * Records the (deepest) annotation level for the current path.
     * 
     * @since 1.13
     */
    private void recordAnnotationLevel() {
        if (null != analyzeMembers) {
            StringBuilder builder = new StringBuilder();
            int size = currentPath.size();
            if (size > 0) {
                PathElement.Type type = null;
                for (int e = 0; e < size; e++) {
                    PathElement element = currentPath.get(e);
                    type = element.getType();
                    if (type.isMember()) {
                        break;
                    }
                    if (e > 0) {
                        builder.append(".");
                    }
                    builder.append(element.getName());
                }
                if (PathElement.Type.NAMESPACE != type) {
                    // analyze configured classes only!
                    analyzeMembers.put(builder.toString(), Boolean.TRUE);
                }
            }
        }
    }
    
    /**
      * Start of an XML Element.
      *
      * @param tag element name
      * @param attributes element attributes
      * @throws QdParserException parse error
      */
    @Override
    public void startElement(String tag, HashMap<String, String> attributes) 
        throws QdParserException {        
        Pattern pat = null;
        if ("groupConfiguration".equals(tag)) {
            String id = attributes.get("id");
            if (null == id) {
                Configuration.LOG.config("group configuration without id");
            } else {
                String refId = attributes.get("refId");
                if (null != refId) {
                    unresolvedConfigurations.put(id, refId);
                } else {
                    MonitoringGroupConfiguration conf = 
                        MonitoringGroupConfiguration.create(
                            attributes.get("debug"),
                            attributes.get("groupAccounting"), 
                            attributes.get("resources"), 
                            attributes.get("instanceIdentification"));
                    groupConfigurations.put(id, conf); 
                }
            }
        } else if ("configuration".equals(tag)) {
            String tmp = attributes.get("exclusive");
            if (null != tmp) {
                exclusive = Boolean.valueOf(tmp);
            }
            for (HashMap.Entry<String, String> ent : attributes.entries()) {
                String aName = ent.getKey();
                if (!"exclusive".equals(aName) && !aName.startsWith("xmlns")) {
                    ConfigurationEntry entry 
                        = ConfigurationEntry.getEntry(aName);
                    if (null != entry) {
                        try {
                            entry.setValue(ent.getValue());
                        } catch (IllegalArgumentException e) {
                            Configuration.LOG.severe(e.getMessage());
                        }
                    } else {
                        Configuration.LOG.config("attribute '" + aName 
                            + "' is not known");
                    }
                }
            }
            setPlainTime();
        } else {
            PathElement.Type type = modifyPath(tag, attributes);
            if (null != type && type.isMember()) {
                pat = getClosestPattern();
                String name;
                if (PathElement.Type.BEHVIOUR == type) {
                    name = attributes.get("signature");
                    name = name.replace("[", "\\[").replace("(", "\\(").replace(")", "\\)").replace("]", "\\]");
                } else {
                    name = attributes.get("name");
                }
                if (null != pat && null != pat.getPattern() && null != name) {
                    pat = new Pattern(pat.getPattern() + "." + name, null);
                    patterns.add(0, pat); // more specific ones first
                }
            }
            AnnotationBuilder<?> template = Annotations.getTemplate(tag);
            boolean nested = false;
            if (null != template) {
                template = template.prepareForUse();
                String cPath = getCurrentPath(false);
                AnnotationBuilderMap builders = configuration.get(cPath);
                if (null == builders) {
                    builders = new AnnotationBuilderMap();
                    configuration.put(cPath, builders);
                    recordAnnotationLevel();
                }
                builders.put(template.getInstanceClass(), template);
                Configuration.LOG.config("registered: signature " + cPath 
                    + " annotation: " + template.getInstanceClass().getName());
                templateStack.add(template);
            } else {
                if (type == PathElement.Type.NAMESPACE 
                    || type == PathElement.Type.MODULE) {
                    String pattern = attributes.get("pattern");
                    String typeOf = attributes.get("typeOf");
                    boolean definesPattern = null != pattern || null != typeOf;
                    if (definesPattern || currentPathContainsPattern()) {
                        AnnotationBuilder<?> templ 
                            = Annotations.getTemplate(TAG_MONITOR);
                        if (null != templ) {
                            templ = templ.prepareForUse();
                            setMonitorAnnotationConfigurationDefaults(templ, attributes);
                            String cPath = getCurrentPath(true);
                            // in default cases match everything. Else, we are matching against a signature; add .*
                            if (null == pattern || 0 == pattern.length()) {
                                cPath += ".*";
                            }
                            pat = parsePatterns(type, cPath, typeOf, templ);
                            hasPatterns = true;
                            // no defaultsOnly as implicit
                        }
                    } else { 
                        // record also those that are only mentioned - start/end
                        recordAnnotationLevel();
                    }
                } else if (!templateStack.isEmpty()) {
                    template = templateStack.get(templateStack.size() - 1);
                    nested = true;
                }
            }
            if (null != template) {
                template.startElement(tag, nested, attributes);
                Pattern p = getClosestPattern();
                if (null != p) {
                    p.register(template.getInstanceClass(), template);
                }
            }
        }
        patternStack.add(pat);
    }

    /**
     * Returns the closest pattern.
     * 
     * @return the closest pattern (may be <b>null</b> for none)
     * 
     * @since 1.21
     */
    private Pattern getClosestPattern() {
        Pattern result = null;
        for (int i = patternStack.size() - 1; null == result && i > 0; i--) {
            Pattern tmp = patternStack.get(i);
            if (null != tmp) {
                result = tmp;
            }
        }
        return result;
    }
    
    /**
     * Sets configuration defaults for the monitor annotation.
     * 
     * @param builder the annotation builder
     * @param attributes the actual attributes
     * 
     * @since 1.20
     */
    private void setMonitorAnnotationConfigurationDefaults(AnnotationBuilder<?> builder, 
        HashMap<String, String> attributes) {
        InstanceIdentifierKind kind = Configuration.INSTANCE.getInstanceIdentifierKind();
        String iik = attributes.get("instanceIdentifierKind");
        if (null != iik) {
            try {
                kind = InstanceIdentifierKind.valueOf(iik.toUpperCase());
            } catch (NullPointerException e) {
                // if value does not correspond to enum constant, ignore, use global 
            }
        }
        if (kind != InstanceIdentifierKind.DEFAULT) {
            builder.setData("instanceIdentifierKind", kind);    
        }
    }

    /**
     * Registers the given pattern for the current path and parses, if provided,
     * the type restrictions in <code>typeOf</code>.
     * 
     * @param type the type of the path element
     * @param currentPath the current path (to the specified elements)
     * @param typeOf the type restriction (may be multiple separated by commas, 
     *     may be one, may be empty, may be <b>null</b>)
     * @param template the annotation template
     * @return the created pattern (may be <b>null</b> for none)
     * @throws QdParserException in case of any parsing/interpretation error
     * 
     * @since 1.00
     */
    private Pattern parsePatterns(PathElement.Type type, String currentPath, String typeOf, 
        AnnotationBuilder<?> template) throws QdParserException {
        Pattern result = null;
        if (null == typeOf) {
            if (PathElement.Type.NAMESPACE != type) { // will be composed to qualified path pattern
                result = registerPattern(currentPath, null, template);
            }
        } else {
            StringTokenizer typeOfTokens = new StringTokenizer(typeOf, ",");
            while (typeOfTokens.hasMoreTokens()) {
                String tOf = typeOfTokens.nextToken();
                result = registerPattern(currentPath, tOf, template);
            }
        }
        return result;
    }
    
    /**
     * Registers the given pattern (for the current path and if provided the 
     * type restriction).
     * 
     * @param currentPath the current path (to the specified elements)
     * @param typeOf a type restriction, i.e. the name of an interface or a 
     *     class or <b>null</b>
     * @param template the annotation template
     * @return the created pattern object
     * @throws QdParserException in case of any parsing/interpretation error
     * 
     * @since 1.00
     */
    private Pattern registerPattern(String currentPath, String typeOf, 
        AnnotationBuilder<?> template) throws QdParserException {
        Pattern pat = new Pattern(currentPath, typeOf);
        patterns.add(pat);
        pat.register(template.getInstanceClass(), template);
        Configuration.LOG.config("registered: pattern " 
            + typeOf + " annotation: " 
            + template.getInstanceClass().getName());
        return pat;
    }

    /**
     * Ends reading an element. Cleans up the stack and delegates to the 
     * annotation builder instance.
     * 
     * @param tag the element name
     * 
     * @throws QdParserException in case of reading errors
     */
    @Override
    public void endElement(String tag) throws QdParserException {        
        AnnotationBuilder<?> template = Annotations.getTemplate(tag);
        boolean nested = null == template && templateStack.size() > 0;
        if (null != template) {
            templateStack.remove(templateStack.size() - 1);
        } else {
            if (!templateStack.isEmpty()) {
                template = templateStack.get(templateStack.size() - 1);
            }
        }
        if (null != template) {
            template.endElement(tag, nested);
        }
        modifyPath(tag, null);
        patternStack.remove(patternStack.size() - 1);
    }
    
    /**
     * Receive notification of the end of the document. Processes 
     * {@link #unresolvedConfigurations}.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end
     * of a document (such as finalising a tree or closing an output
     * file).</p>
     *
     * @throws QdParserException in case of reading errors
     */
    @Override
    //public void endDocument() throws SAXException
    public void endDocument() throws QdParserException {
        for (HashMap.Entry<String, String> entry 
            : unresolvedConfigurations.entries()) {
            String referring = entry.getKey();
            String referenced = entry.getValue();
            MonitoringGroupConfiguration configuration 
                = groupConfigurations.get(referenced);
            if (null == configuration) {
                Configuration.LOG.config("group configuration '" + referenced 
                    + "' referenced from '" + referring + "' does not exist. " 
                    + "Ignoring.");               
            } else {
                groupConfigurations.put(referring, configuration);
            }
        }
        unresolvedConfigurations.clear();
    }
    
    /**
     * Text node content.
     *
     * @param str node content
     * @throws QdParserException parse error
     */
    @Override
    public void text(String str) throws QdParserException {
    }
 
    /**
     * Start of an XML Document.
     *
     * @throws QdParserException parse error
     */
    public void startDocument() throws QdParserException {
    }
    
}