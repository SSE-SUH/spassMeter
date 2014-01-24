package de.uni_hildesheim.sse.codeEraser.tool;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The code eraser main class. Performs usual command line parsing and removes
 * disabled code.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CodeEraser {
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private CodeEraser() {
    }
    
    /**
     * The main method executing the code eraser.
     * 
     * @param args the command line arguments (see -help for all options)
     * 
     * @since 1.00
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        Options options = new Options();
        Option helpOption = new Option("help", false, 
            "prints this help summary"); 
        options.addOption(helpOption);
        Option listOption = new Option("list", false, "lists all annotations " 
            + "in the source code considered by CodeEraser");
        options.addOption(listOption);
        Option jarOption = new Option("in", true, 
            "specifies the jar file to read");
        options.addOption(jarOption);
        Option outOption = new Option("out", true, 
            "specifies the output jar to write");
        options.addOption(outOption);
        Option checkFlat = new Option("flat", false, 
            "do not search recursively for annotations");
        options.addOption(checkFlat);
        Option bindingsOptions = new Option("bindings", true, 
            "specifies the file containing the bindings, i.e. the list of ids " 
            + "to be removed");
        options.addOption(bindingsOptions);
        Option idOption = OptionBuilder.withArgName("id=value").hasArgs(2)
            .withValueSeparator().withDescription("specify individual bindings")
            .create("B");
        options.addOption(idOption);
        
        CommandLineParser parser = new GnuParser();
        Configuration conf = new Configuration();

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(listOption.getOpt())) {
                conf.setCommand(Configuration.Command.LIST_ANNOTATIONS);
            }
            if (line.hasOption(helpOption.getOpt())) {
                conf.setCommand(Configuration.Command.HELP);
            }
            if (line.hasOption(checkFlat.getOpt())) {
                conf.setCheckRecursively(false);
            }

            if (line.hasOption(jarOption.getOpt())) {
                conf.setJar(line.getOptionValue(jarOption.getOpt()));
            }
            if (line.hasOption(outOption.getOpt())) {
                conf.setOut(line.getOptionValue(outOption.getOpt()));
            }
            if (line.hasOption(bindingsOptions.getOpt())) {
                conf.readBindingsFromProperties(
                    line.getOptionValue(bindingsOptions.getOpt()));
            }
            if (line.hasOption(idOption.getOpt())) {
                Properties props = line.getOptionProperties(idOption.getOpt());
                for (Map.Entry<Object, Object> entry : props.entrySet())  {
                    conf.putBinding(entry.getKey(), entry.getValue());
                }
            }
        } catch (ParseException e) {
            conf.addErrorMsg("Parsing failed " + e.getMessage());
            conf.setCommand(Configuration.Command.HELP);
        }
        CodeProcessor processor = new CodeProcessor(conf);
        processor.process();
        
        if (Configuration.Command.HELP == conf.getCommand()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CodeEraser", options);
        } 
        if (null != conf.getErrorMsg()) {
            System.out.println(conf.getErrorMsg());
        }
    }

}
