package de.uni_hildesheim.sse.codeEraser.copy;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Implements the class replacer.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ClassReplicator {

    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private ClassReplicator() {
    }

    /**
     * Executes the class replacer. 
     * 
     * @param args the command line arguments
     * 
     * @since 1.00
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        boolean printHelp = false;
        Options options = new Options();
        Option helpOption = new Option("help", false, 
            "prints this help summary"); 
        options.addOption(helpOption);
        Option outOption = new Option("out", true, 
            "specifies the output jar to write");
        options.addOption(outOption);
        Option mappingOptions = new Option("mappings", true, 
            "specifies the file containing the mappings, i.e. the list of old " 
            + "and new class names");
        options.addOption(mappingOptions);
        Option idOption = OptionBuilder.withArgName("id=value").hasArgs(2)
            .withValueSeparator().withDescription("specify individual bindings")
            .create("M");
        options.addOption(idOption);
        
        CommandLineParser parser = new GnuParser();
        Configuration conf = new Configuration();

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(helpOption.getOpt())) {
                printHelp = true;
            }
            if (line.hasOption(outOption.getOpt())) {
                conf.setOut(line.getOptionValue(outOption.getOpt()));
            }
            if (line.hasOption(mappingOptions.getOpt())) {
                conf.readFromProperties(
                    line.getOptionValue(mappingOptions.getOpt()));
            }
            if (line.hasOption(idOption.getOpt())) {
                Properties props = line.getOptionProperties(idOption.getOpt());
                for (Map.Entry<Object, Object> entry : props.entrySet())  {
                    conf.addMapping(entry.getKey().toString(), 
                        entry.getValue().toString());
                }
            }
        } catch (ParseException e) {
            conf.addErrorMsg("Parsing failed " + e.getMessage());
            printHelp = true;
        }
        CodeProcessor processor = new CodeProcessor(conf);
        processor.process();
        
        if (printHelp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ClassReplacer", options);
        } 
        if (null != conf.getErrorMsg()) {
            System.out.println(conf.getErrorMsg());
        }        
    }
     
}
