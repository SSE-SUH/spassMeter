package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Defines the test window for Swing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class TestSwing {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private TestSwing() {
    }
    
    /**
     * The main method. Opens a Swing window to display gathered information.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (UnsupportedLookAndFeelException e) {
        }
        
        final JEditorPane text = new JEditorPane();
        text.setEditable(false);
        text.setContentType("text/html");
        JButton doIt = new JButton("gather");
        
        JFrame frame = new JFrame("GearsBridgeJ Swing Test");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(text, BorderLayout.CENTER);
        text.setPreferredSize(new Dimension(400, 300));
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        buttons.add(doIt);
        frame.add(buttons, BorderLayout.SOUTH);
        
        doIt.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent event) {
                StringBuilder buf = new StringBuilder();

                IThisProcessDataGatherer pdg = 
                    GathererFactory.getThisProcessDataGatherer();
                IoStatistics io = pdg.getCurrentProcessIo();                
                buf.append("<html>");
                buf.append("<center><table border=\"1\">");
                addRow(buf, "jvm read", 
                    io.read);
                addRow(buf, "jvm write", 
                    io.write);
                addRow(buf, "jvm user", 
                    pdg.getCurrentProcessUserTimeTicks());
                addRow(buf, "jvm kernel", 
                    pdg.getCurrentProcessKernelTimeTicks());
                addRow(buf, "jvm mem", 
                    pdg.getCurrentProcessMemoryUse());
                addRow(buf, "jvm system", 
                    pdg.getCurrentProcessSystemTimeTicks());

                if (GathererFactory.getDataGatherer().supportsJVMTI()) {
                    addRow(buf, "data size", GathererFactory
                        .getMemoryDataGatherer().getObjectSize(this));
                } else {
                    addRow(buf, "data size", "N/A");
                }
                buf.append("</table></center>");
                buf.append("</html>");
                text.setText(buf.toString());
            }
        });
        
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Adds a table row with arbitrary values.
     * 
     * @param buf the target string builder to be modified as a side effect
     * @param vals the values to be used as cell values
     * @return <code>buf</code>
     * 
     * @since 1.00
     */
    private static StringBuilder addRow(StringBuilder buf, Object... vals) {
        buf.append("<tr>");
        for (Object val : vals) {
            buf.append("<td>");
            buf.append(val);
            buf.append("</td>");
        }
        buf.append("</tr>");
        return buf;
    }

}
