package de.uni_hildesheim.sse.wildcat.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for the properties table.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATTableModel extends AbstractTableModel {

    /**
     * Stores a default serial version UID.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Stores the column names.
     * 
     * @since 1.00
     */
    private String[] columnNames = {"Property", "Value"};
    
    /**
     * The data for the table.
     * 
     * @since 1.00
     */
    private Map<String, Object> data;
    
    /**
     * Contains all data keys.
     * 
     * @since 1.00
     */
    private List<String> dataKeys;
    
    /**
     * Constructor.
     * 
     * @since 1.00
     */
    public WildCATTableModel() {
        data = new HashMap<String, Object>();
        dataKeys = new LinkedList<String>();
    }
    
    @Override
    public int getRowCount() {
        if (data.size() == 0) {
            return 1;
        } else {
            return data.size();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = "";
        if (data.size() == 0) {
            // if there are no elements to display
            return " -";
        }
        switch (columnIndex) {
        case 0:
            result = dataKeys.get(rowIndex);
            break;
        case 1:
            result = data.get(dataKeys.get(rowIndex));
            break;
        default:
            result = " -";
            break;
        }
        return result;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    /**
     * Changes the table data.
     * 
     * @param data The new data for the table.
     * 
     * @since 1.00
     */
    public void dataChanged(Map<String, Object> data) {
        // setting new data
        this.data = data;
        // setting new data keys
        dataKeys = new LinkedList<String>(data.keySet());
        // sorting new keys
        Collections.sort(dataKeys);
        // update table values
        fireTableDataChanged();
    }

}
