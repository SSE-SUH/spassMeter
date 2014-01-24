package de.uni_hildesheim.sse.loadPlugin.tableModel;

import java.text.NumberFormat;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.uni_hildesheim.sse.loadPlugin.data.TabData;


/**
 * Table model for the table in the detail panel of load tabs.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class DataTableModel extends AbstractTableModel {

    /**
     * Stores a default serial version UID.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Stores the {@link TabData} to display in the table.
     * 
     * @since 1.00
     */
    private TabData data;
    
    /**
     * Stores the column names.
     * 
     * @since 1.00
     */
    private List<String> columnNames;
    
    /**
     * Stores the row names.
     * 
     * @since 1.00
     */
    private List<String> rowNames;
    
    /**
     * Creates a table model for the table in the detail panel of load tabs.
     * 
     * @param data The {@link TabData} to display.
     * @param columnNames The column names.
     * @param rowNames The row names.
     * 
     * @since 1.00
     */
    public DataTableModel(TabData data, List<String> columnNames, 
            List<String> rowNames) {
        this.data = data;
        this.columnNames = columnNames;
        this.rowNames = rowNames;        
    }

    @Override
    public int getRowCount() {
        return rowNames.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = "";
        
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);
        
        if (columnIndex == 0) {
            result = rowNames.get(rowIndex);
        } else {
            String key = rowNames.get(rowIndex);
            if (null != key) {
                List<Object> lastData = data.getLastTabData(key);
                if (null != lastData) {
                    result = nf.format(lastData.get(columnIndex - 1));   
                }
            }
            if (null == result) {
                result = " not available";
            }
        }
        
        return result;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
