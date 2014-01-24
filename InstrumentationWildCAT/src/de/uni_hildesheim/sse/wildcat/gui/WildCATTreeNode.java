package de.uni_hildesheim.sse.wildcat.gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.ow2.wildcat.Context;
import org.ow2.wildcat.ContextException;

/**
 * TreeNode for a WildCAt {@link Context} tree.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATTreeNode extends DefaultMutableTreeNode implements 
    Comparable<WildCATTreeNode> {

    /**
     * Stores the default serial version uid.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The {@link Context} for the tree.
     * 
     * @since 1.00
     */
    private Context ctx;
    
    /**
     * The root in the tree.
     * 
     * @since 1.00
     */
    private String path;
    
    /**
     * The name of the node.
     * 
     * @since 1.00
     */
    private String name;

    /**
     * Stores all children.
     * 
     * @since 1.00
     */
    private List<WildCATTreeNode> children;
    
    /**
     * Creates a {@link WildCATTreeNode.
     * 
     * @param ctx The {@link Context}.
     * @param path The path in the {@link Context}.
     * @param name The name of the node.
     * 
     * @since 1.00
     */
    public WildCATTreeNode(Context ctx, String path, String name) {
        super(name);
        this.ctx = ctx;
        this.path = path;
        this.name = name;
        children = new LinkedList<WildCATTreeNode>();
    }
    
    @Override
    public boolean isLeaf() {
        boolean result = true; 
        if (name.contains("#")) {
            result = true;
        } else {
            if (this.listChilds(path).size() <= 0) {
                result = true;
            } else {
                result = false;
            }
        }
        return result;
    }
    
    /**
     * List all child's to the given path.
     * 
     * @param path The path to get child's.
     * 
     * @return A {@link Set} with all child's.
     * 
     * @since 1.00
     */
    private Set<String> listChilds(String path) {
        Set<String> result = null;
        try {
            result = ctx.list(path);
        } catch (ContextException e) {
            Calendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("GMT+1:00"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            System.out.println("Error\t" + sdf.format(cal.getTime())
                    + " - Error while creating WildCAT context tree.");
            System.out.println("Info\t" + sdf.format(cal.getTime())
                    + " - System will exit.");
            System.exit(0);
        }
        return result;
    }
    
    @Override
    public int getChildCount() {
        // listing all elements in this path
        Set<String> tmp = listChilds(path);
        WildCATTreeNode child;
        // for all elements in this path
        for (String s : tmp) {
            // create new path for new tree node
            String newPath = path + s;
            if (!s.contains("#")) {
                newPath += "/";
            }
            // create new child
            child = new WildCATTreeNode(ctx, newPath, s);
            // add new child to children
            children.add(child);
        }
        Collections.sort(children);
        return tmp.size();
    }
    
    @Override
    public TreeNode getChildAt(int index) {
        return children.get(index);
    }
    
    /**
     * Adds a child to the node.
     * 
     * @param newChild The new Child to add.
     * 
     * @since 1.00
     */
    public void addChild(WildCATTreeNode newChild) {
        children.add(newChild);
        Collections.sort(children);
    }
    
    @Override
    public int getIndex(TreeNode aChild) {
        return children.indexOf(aChild);
    }

    /**
     * Returns the path in the {@link Context}.
     * 
     * @return The path in the {@link Context}.
     * 
     * @since 1.00
     */
    public String getWildCATContextPath() {
        return path;
    }
    
    /**
     * Returns the name of the tree node.
     * 
     * @return The name of the tree node.
     * 
     * @since 1.00
     */
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(WildCATTreeNode node) {
        return name.compareTo(node.getName());
    }
    
}
