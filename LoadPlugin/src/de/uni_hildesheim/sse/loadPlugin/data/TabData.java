package de.uni_hildesheim.sse.loadPlugin.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Stores the data for a tab.
 * <br>
 * The data will be stores in a map. The key for the data is a string. The 
 * data itself will be saved in form of a {@link List} which again contain a 
 * {@link List}.
 * <br>
 * the first list contains all data at one time which should be displayed. 
 * <br>
 * The second list (list in the list) contain the different values to be 
 * displayed. (e.g. memory use in byte, kilobyte and megabyte).  
 * <br><br>
 * Example: Data which should be shown in the table:
 * <br>
 * <table border="1" width="200">
 * <tr align="center"><td><b>A</b></td><td><b>B</b></td><td><b>C</b></td></tr>
 * <tr align="center"><td>1</td><td>10</td><td>100</td></tr>
 * </table><br>
 * In this case the lists are organized as follows:<br>
 * {{1, 10, 100}}
 * <br><br>
 * If the data changes over time and new data will be added the lists will be 
 * organized as follows:<br>
 * {{1, 10, 100}, {2, 20, 200}, {3, 30, 300}}
 * <br><br>
 * The map looks like this:<br>
 * {key1={{'values'}, ...}, key2={{'values'}, ...}, ..., keyn={{'values'}, ...}}
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class TabData {

    /**
     * Stores the data.
     * 
     * @since 1.00
     */
    private Map<String, LinkedList<LinkedList<Object>>> data;
    
    /**
     * Creates an instance of {@link TabData}.
     * 
     * @since 1.00
     */
    public TabData() {
        data = new HashMap<String, LinkedList<LinkedList<Object>>>();
    }
    
    /**
     * Adds data to the {@link TabData}.
     * 
     * @param key The key with which the specified value is to be associated.
     * @param value The values to be associated with the specified key in form 
     *            of a list.
     * 
     * @return true if added.
     * 
     * @since 1.00
     */
    public boolean addTabData(String key, LinkedList<Object> value) {
        if (null == data.get(key)) {
            data.put(key, new LinkedList<LinkedList<Object>>());
        }
        return data.get(key).add(value);
    }
    
    /**
     * Returns all stored data with the given key.
     * 
     * @param key The key for the data.
     * 
     * @return A {@link List} with all data to the given key.
     * 
     * @since 1.00
     */
    public List<LinkedList<Object>> getAllTabData(String key) {
        return data.get(key);
    }
    
    /**
     * Returns the last stored data with the given key.
     * 
     * @param key The key for the data.
     * 
     * @return The data with the given key or <code>null</code> if empty.
     * 
     * @since 1.00
     */
    public List<Object> getLastTabData(String key) {
        List<Object> result = null;
        LinkedList<LinkedList<Object>> values = data.get(key);
        if (null != values && values.size() > 0) {
            result = values.get(values.size() - 1);
        }
        return result;
    }
    
    @Override
    public String toString() {
        System.out.println(data.keySet() + " *** " + data.values());
        StringBuilder sb = new StringBuilder();
        sb.append("TabData : " + data.size() + "\n");
        for (String s : data.keySet()) {
            sb.append("keys: " + s + " - values: " + data.get(s));
        }
        return "TabData : " + data.size();
    }
    
}
