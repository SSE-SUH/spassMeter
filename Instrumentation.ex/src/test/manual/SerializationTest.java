package test.manual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Implements a simple serialization/deserialization test.
 * 
 * @author Holger Eichelberger
 */
public class SerializationTest {

    /**
     * The expected test data.
     */
    private static final String TEXT = "text";
    
    /**
     * The expected numerical data.
     */
    private static final int NUMBER = -1;

    /**
     * Prevents external creation.
     */
    private SerializationTest() {
    }

    /**
     * A data class to be serialized.
     * 
     * @author Holger Eichelberger
     */
    @SuppressWarnings("serial")
    private static class Data implements Serializable {
        
        /**
         * Stores the text data.
         */
        private String text;

        /**
         * Stores numerical data.
         */
        private int number;
        
        /**
         * Creates a data object.
         * 
         * @param text the text data
         * @param number the numerical data
         */
        public Data(String text, int number) {
            this.text = text;
            this.number = number;
        }

        /**
         * Returns the text data.
         * 
         * @return the text data
         */
        public String getText() {
            return text;
        }

        /**
         * Returns the numerical data.
         * 
         * @return the numerical data
         * 
         * @since 1.00
         */
        public int getNumber() {
            return number;
        }
        
    }
    
    /**
     * Executes the test.
     * 
     * @param args either write or read
     * @throws IOException in case of I/O problems
     * @throws ClassNotFoundException in case of classes not found during 
     *     deserialization
     * 
     * @since 1.00
     */
    public static void main(String[] args) throws IOException, 
        ClassNotFoundException {
        if (1 == args.length) {
            File f = new File("generated/data.ser");
            if ("write".equals(args[0])) {
                Data d = new Data(TEXT, NUMBER);
                ObjectOutputStream o = new ObjectOutputStream(
                    new FileOutputStream(f));
                o.writeObject(d);
                o.close();
            } else if ("read".equals(args[0])) {
                ObjectInputStream i = new ObjectInputStream(
                    new FileInputStream(f));
                Data d = (Data) i.readObject();
                i.close();
                if (!TEXT.equals(d.getText()) || NUMBER != d.getNumber()) {
                    System.err.println("Expected value not equals: " 
                        + d.getText() + " " + d.getNumber());
                }
            }
        }
    }
    
}
