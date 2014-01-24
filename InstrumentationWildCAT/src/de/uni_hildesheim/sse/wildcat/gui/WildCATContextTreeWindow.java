package de.uni_hildesheim.sse.wildcat.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.tree.TreeSelectionModel;

import org.ow2.wildcat.Context;
import org.ow2.wildcat.ContextException;

import de.uni_hildesheim.sse.wildcat.launcher.GearsBridgeContextConstants;

/**
 * GUI for WildCAT hierarchy representation.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATContextTreeWindow {
    
    /**
     * Stores the an instance of {@link WildCATContextTreeWindow}.
     * 
     * @since 1.00
     */
    private static WildCATContextTreeWindow instance = null;  
    
    /**
     * Stores the {@link Context}.
     * 
     * @since 1.00
     */
    private Context ctx;

    /**
     * Stores the tree.
     * 
     * @since 1.00
     */
    private JTree tree;
    
    /**
     * Stores the tree model.
     * 
     * @since 1.00
     */
    private WildCATTreeDataModel treeModel;

    /**
     * Stores the table model.
     * 
     * @since 1.00
     */
    private WildCATTableModel tableModel;
    
    /**
     * Indicates if the gui is open.
     * 
     * @since 1.00
     */
    private boolean isOpen = false;
    
    /**
     * Constructor.
     * 
     * @param ctx The {@link Context}.
     * 
     * @since 1.00
     */
    public WildCATContextTreeWindow(Context ctx) {
        instance = this;
        this.ctx = ctx;
        
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the GUI.
     * 
     * @since 1.00
     */
    public void open() {
        JFrame frame = new JFrame("WildCAT Context Tree");
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // adding tree to pane
        split.add(createTree());
        // adding the properties table
        split.add(createTable());
        // adding split pane to pane
        pane.add(split, BorderLayout.CENTER);
        
        // gaps left, right, top and bottom
        pane.add(new JLabel("     "), BorderLayout.NORTH);
        pane.add(new JLabel("     "), BorderLayout.SOUTH);
        pane.add(new JLabel("     "), BorderLayout.WEST);
        pane.add(new JLabel("     "), BorderLayout.EAST);
        
        // adding a window listener for closing event 
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                isOpen = false;
            }
        });

        frame.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(d.width / 2 - 400, d.height / 2 - 450, 800, 700);
        frame.setVisible(true);

        isOpen = true;
    }

    /**
     * Creates the Tree.
     * 
     * @return The tree component.
     * 
     * @since 1.00
     */
    private Component createTree() {
        // root node
        WildCATTreeNode root = new WildCATTreeNode(ctx,
                GearsBridgeContextConstants.CONTEXT_ROOT, 
                GearsBridgeContextConstants.CONTEXT_ROOT);
        // tree model
        treeModel = new WildCATTreeDataModel(root);
        
        // tree
        tree = new JTree(treeModel);
        // setting tree properties
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.
                SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setEnabled(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.
                SINGLE_TREE_SELECTION);
        // tree selection listener for updating table values
        tree.addTreeSelectionListener(new TreeSelectionListener() { 
            @SuppressWarnings("unchecked")
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                WildCATTreeNode node = (WildCATTreeNode)
                        tree.getLastSelectedPathComponent();
                if (node.isLeaf()) {
                    String path = node.getWildCATContextPath();
                    try {
                        tableModel.dataChanged((Map<String, Object>) 
                            ctx.getValue(path));   
                    } catch (ContextException e) {
                        Calendar cal = new GregorianCalendar(TimeZone
                            .getTimeZone("GMT+1:00"));
                        SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd.MM.yyyy HH:mm:ss");
                        System.out.println("Error\t"
                            + sdf.format(cal.getTime())
                            + " - Error while creating WildCAT context tree.");
                        System.out.println("Info\t" + sdf.format(cal.getTime())
                            + " - System will exit.");
                        System.exit(0);
                    }  
                } else {
                    tableModel.dataChanged(new HashMap<String, Object>());
                }
            }
        });
        // scroll pane for the tree
        JScrollPane treeScroll = new JScrollPane(tree);
        // setting min size of scroll pane
        treeScroll.setMinimumSize(new Dimension(250, 100));
        
        return treeScroll;
    }
    
    /**
     * Creates the properties table.
     * 
     * @return The properties table.
     * 
     * @since 1.00
     */
    private Component createTable() {
        // creating table model
        tableModel = new WildCATTableModel();
        // creating table
        JTable table = new JTable(tableModel);
        // setting table properties
        table.setDragEnabled(false);
        table.setToolTipText("Shows the properties of the selected element in "
            + "the tree.");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // setting table header properties
        JTableHeader header = table.getTableHeader();
        header.setResizingAllowed(true);
        header.setReorderingAllowed(false);
        header.setUpdateTableInRealTime(true);
        
        
        JScrollPane tableScroll = new JScrollPane(table);
        
        return tableScroll;
    }
    
    /**
     * Updates the properties table values.
     * 
     * @since 1.00
     */
    private void updateTableValues() {
//        try {
//            Set<String> buildInSensors = ctx.list(
//                GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN);
//            for (String s : buildInSensors) {
//                ctx.getValue(GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN 
//                    + s);
//            }
//        } catch (ContextException e) {
//            e.printStackTrace();
//        }
        tableModel.fireTableDataChanged();
    }
    
    /**
     * Updates the GUI if displayed.
     * 
     * @since 1.00
     */
    public static void updateGUI() {
        if (null != instance) {
            if (instance.isOpen) {
                instance.updateTableValues();
            }    
        }
    }

}
