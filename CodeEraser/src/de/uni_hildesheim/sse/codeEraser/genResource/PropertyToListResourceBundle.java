package de.uni_hildesheim.sse.codeEraser.genResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * Generates bundle implementations from bundle files.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class PropertyToListResourceBundle {

    /**
     * Stores the resource file name suffix.
     */
    public static final String RESOURCE_FILE_SUFFIX = ".properties";
    
    /**
     * Stores the file suffix for Java sources.
     */
    public static final String SOURCE_FILE_SUFFIX = ".java";

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private PropertyToListResourceBundle() {
    }
    
    /**
     * Generates resource classes from bundle files.
     * 
     * @param args the bundle names or bundle file names
     * 
     * @since 1.00
     */
    public static void main(String [] args) {
        if (args.length < 1) {
            System.out.println("no resources specified");
        } else {
            for (int i = 1; i < args.length; i++) {
                try {
                    String className = generateClass(null, args[i]);
                    if (null != className) {
                        System.out.println("generated " + className);
                    } else {
                        System.out.println("cannot generate for " + args[i]);
                    }
                } catch (MissingResourceException mre) {
                    System.out.println("Resource not found: " 
                        + mre.getMessage());
                } catch (IOException ioe) {
                    System.out.println("I/O error: " + ioe.getMessage());
                }
            }
        }
    }
    
    /**
     * Generates the class for the specified resource (either as class name
     * or as resource file name).
     * 
     * @param baseDir an optional base dir where resource is located in
     * @param resource the resource to generate the class for
     * @return the name of the generated class
     * @throws IOException in case of I/O related errors
     * @throws MissingResourceException in case that the specified resource 
     *     cannot be found
     * 
     * @since 1.00
     */
    public static String generateClass(File baseDir, String resource) 
        throws IOException, MissingResourceException {

        String resourceFileName;
        String className;
        if (!resource.endsWith(RESOURCE_FILE_SUFFIX)) {
            className = resource;
            resourceFileName = resource.replace('.', File.separatorChar) 
                + RESOURCE_FILE_SUFFIX;
        } else {
            resourceFileName = resource;
            className = resource.substring(0, 
                resource.length() - RESOURCE_FILE_SUFFIX.length());
            className = className.replace(File.separatorChar, '.');
        }

        boolean foundIllegalChar = false;
        for (int i = 0; !foundIllegalChar && i < className.length(); i++) {
            char c = className.charAt(i);
            foundIllegalChar = !(Character.isJavaIdentifierStart(c) 
                || Character.isJavaIdentifierPart(c) || '.' == c); 
        }

        String fqn;
        if (!foundIllegalChar) {
            String classFileName = className.replace('.', File.separatorChar) 
                + SOURCE_FILE_SUFFIX;
            Properties propertiesFile = new Properties();
            File in;
            File out;
            if (null != baseDir) {
                in = new File(baseDir, resourceFileName);
                out = new File(baseDir, classFileName);
            } else {
                in = new File(resourceFileName);
                out = new File(classFileName);
            }
            fqn = className;
            String packageName;
            int pos = className.lastIndexOf('.');
            if (pos > 0) {
                packageName = className.substring(0, pos);
                className = className.substring(pos + 1);
            } else {
                packageName = "";
            }
            
            propertiesFile.load(new FileInputStream(in));
            FileWriter fw = new FileWriter(out);
            String key;

            if (packageName.length() > 0) {
                fw.write("package " + packageName + ";\n\n");
            }
            fw.write("import java.util.ListResourceBundle;\n\n");

            fw.write("/**\n * A class representing a concrete " 
                + "resource bundle.\n */\n");
            fw.write("public class " + className 
                + " extends ListResourceBundle {\n\n");

            fw.write("    /**\n" 
                + "     * Stores the contents of the resource bundle.\n" 
                + "     */\n");
            fw.write("    private static final Object [][] CONTENTS = {\n");

            Enumeration<?> e = propertiesFile.propertyNames();
            while (e.hasMoreElements()) {
                key = (String) e.nextElement();
                fw.write("        {\"" + key + "\", "
                    + toCodeString(propertiesFile.getProperty(key)) + "},\n");
            }
            fw.write("    };\n\n");
            
            fw.write("    /**\n" 
                + "     * Returns the contents of the resource bundle.\n" 
                + "     *\n"
                + "     * @return the key-value pairs\n"
                + "     */\n");
            fw.write("    public Object [][] getContents() {\n");
            fw.write("        return CONTENTS;\n");
            fw.write("    }\n\n");

            fw.write("}\n");

            fw.close();
        } else {
            fqn = null;
        }
        
        return fqn;
    }
    
    /**
     * Turns the given object into a string and performs quotations
     * in order to obtain an equal string in source code representation.
     * 
     * @param object the object to be turned into a string
     * @return the equivalent source code string
     * 
     * @since 1.00
     */
    private static final String toCodeString(Object object) {
        String text = object.toString();
        text = text.replace("\\", "\\\\"); // quote quoted parts
        text = text.replace("\"", "\\\""); // quote quotes
        while (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        text = text.replace("\n", "\"\n            + \""); // consider quotes
        return "\"" + text + "\"";
    }

}
