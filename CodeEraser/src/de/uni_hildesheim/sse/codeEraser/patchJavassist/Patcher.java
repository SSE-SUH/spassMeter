package de.uni_hildesheim.sse.codeEraser.patchJavassist;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.uni_hildesheim.sse.codeEraser.Configuration;

/**
 * Implements the class replacer.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Patcher {

    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private Patcher() {
    }

    /**
     * Executes the class replacer. 
     * 
     * @param args the command line arguments
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        boolean printHelp = false;
        Options options = new Options();
        Option helpOption = new Option("help", false, 
            "prints this help summary"); 
        options.addOption(helpOption);
        Option jarOption = new Option("in", true, 
            "specifies the jar file to read");
        options.addOption(jarOption);
        Option outOption = new Option("out", true, 
            "specifies the output jar to write");
        options.addOption(outOption);
        
        CommandLineParser parser = new GnuParser();
        Configuration conf = new Configuration();

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(helpOption.getOpt())) {
                printHelp = true;
            }

            if (line.hasOption(jarOption.getOpt())) {
                conf.setJar(line.getOptionValue(jarOption.getOpt()));
            }
            if (line.hasOption(outOption.getOpt())) {
                conf.setOut(line.getOptionValue(outOption.getOpt()));
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
