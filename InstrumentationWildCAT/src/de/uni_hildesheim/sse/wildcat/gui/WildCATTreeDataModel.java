package de.uni_hildesheim.sse.wildcat.gui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * Model for the WildCAT JTree.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATTreeDataModel implements TreeModel {
    
    /**
     * Stores the {@link TreeModelListener}s.
     * 
     * @since 1.00
     */
    private List<TreeModelListener> listeners;
    
    /**
     * The root node.
     * 
     * @since 1.00
     */
    private WildCATTreeNode root;

    /**
     * Constructor.
     * 
     * @param root The root node.
     * 
     * @since 1.00
     */
    public WildCATTreeDataModel(WildCATTreeNode root) {
        this.root = root;
        listeners = new LinkedList<TreeModelListener>();
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((WildCATTreeNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof WildCATTreeNode) {
            return ((WildCATTreeNode) parent).getChildCount();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((WildCATTreeNode) node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((WildCATTreeNode) parent).getIndex((WildCATTreeNode) child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove(listener);
    }
    
}
