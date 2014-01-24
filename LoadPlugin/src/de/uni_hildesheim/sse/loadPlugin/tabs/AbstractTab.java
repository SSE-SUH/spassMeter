package de.uni_hildesheim.sse.loadPlugin.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.date.SerialDate;

import com.sun.tools.jconsole.JConsoleContext;

import de.uni_hildesheim.sse.loadPlugin.data.TabData;
import de.uni_hildesheim.sse.loadPlugin.tableModel.DataTableModel;

/**
 * Defines an abstract tab providing an additional specialized interface.
 * 
 * @author Holger eichelberger
 * @since 1.00
 * @version 1.00
 */
@SuppressWarnings("serial")
public abstract class AbstractTab extends JPanel {

    /**
     * Stores the chart.
     * 
     * @since 1.00
     */
    private JFreeChart chart;
    
    /**
     * Stores the dataset for the chart.
     * 
     * @since 1.00
     */
    private TimeSeriesCollection dataset;
    
    /**
     * Stores the model for the details table.
     * 
     * @since 1.00
     */
    private DataTableModel model;
    
    /**
     * Stores the data for this tab.
     * 
     * @since 1.00
     */
    private TabData data;
    
    /**
     * Stores all {@link TimeSeries} which will be displayed in the chart.
     * 
     * @since 1.00
     */
    private Map<String, TimeSeries> timeSeries;

    /**
     * Stores the names of the displayed data and their intern representation.
     * 
     * @since 1.00
     */
    private Map<String, String> dataNames;
    
    /**
     * Stores the units to display in the table.
     * 
     * @since 1.00
     */
    private List<String> dataDisplayUnits;
    
    /**
     * Stores the Label for the x-axis.
     * 
     * @since 1.00
     */
    private String xAxisLabel = "";
    
    /**
     * Stores the Label for the y-axis.
     * 
     * @since 1.00
     */
    private String yAxisLabel = "";
    
    /**
     * The position of the time period data of the {@link TabData} which should 
     * be displayed in the chart. Initial value is 0. 
     * 
     * @since 1.00
     */
    private int dataPosition = 0;
    
    /**
     * Stores the {@link MBeanServerConnection}.
     * 
     * @since 1.00
     */
    private MBeanServerConnection mbsc;
    
    /**
     * Creates a tab with a specified layout.
     * 
     * Don't forget to set the dataposition -> setDataPosition()
     * (The position of the time period data of the {@link TabData} which 
     * should be displayed in the chart. Initial value is 0.If 0 is ok you 
     * don't have to set this value)
     * 
     * @param layout the layout.
     * @param context The jconsole context.
     * 
     * @since 1.00
     */
    public AbstractTab(LayoutManager layout, JConsoleContext context) {
        super(layout);
        // getting the MBeanServerConnection from the context
        this.mbsc = context.getMBeanServerConnection();
        initializeTab();
    }
    
    /**
     * Initializes all tab relevant data and the gui.
     * 
     * @since 1.00
     */
    public void initializeTab() {
        // creating new TimerSeriesCollection
        dataset = new TimeSeriesCollection();
        // creating new tab data for data holding
        data = new TabData();
        // creating data names map 
        dataNames = new LinkedHashMap<String, String>();
        // creating TimeSeries map 
        timeSeries = new LinkedHashMap<String, TimeSeries>();
        // creating data display units list (for the table)
        dataDisplayUnits = new LinkedList<String>();
        // Setting default labels for chart
        setXAxisLabel("X-Axis");
        setYAxisLabel("Y-Axis");
        
        // Initialized all tab specific data.
        initializeTabSpecific();
        
        setupTab();
    }

    /**
     * Init method for the jconsole tab.
     * 
     * @since 1.00
     */
    private void setupTab() {
        // Borderlayout for the tab
        setLayout(new BorderLayout());

        // north
        add(new JLabel(" "), BorderLayout.NORTH);

        // south
        add(createDetailTable(), BorderLayout.SOUTH);
        
        // center
        chart = createChart();
        updateChart();
        ChartPanel pChart = new ChartPanel(chart);
        TitledBorder border = BorderFactory
                .createTitledBorder("Memory Load Information");
        pChart.setBorder(border);
        add(pChart, BorderLayout.CENTER);

        // west
        add(new JLabel("   "), BorderLayout.WEST);

        // east
        add(new JLabel("   "), BorderLayout.EAST);
    }
    
    /**
     * Creates the chart.
     * 
     * @return The created chart.
     * 
     * @since 1.00
     */
    private JFreeChart createChart() {
        JFreeChart tChart = ChartFactory.createTimeSeriesChart(
                null, // chart title
                getXAxisLabel(), // domain axis label
                getYAxisLabel(), // range axis label
                dataset, // data
                true, // include legend
                false, // tooltips
                false); // urls 
        tChart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) tChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);

        LogarithmicAxis rangeAxis = new LogarithmicAxis(getYAxisLabel());
        rangeAxis.setStrictValuesFlag(false);
        plot.setRangeAxis(rangeAxis);
        
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setPositiveArrowVisible(true);
        
        ValueAxis valueAxis = plot.getDomainAxis();
        valueAxis.setPositiveArrowVisible(true);
        
        // add all timeseries to the dataset
        for (String key : timeSeries.keySet()) {
            dataset.addSeries(timeSeries.get(key));
        }
        return tChart;
    }
    
    /**
     * Returns the x-axis label.
     * 
     * @return The x-axis label
     * 
     * @since 1.00
     */
    public String getXAxisLabel() {
        return xAxisLabel;
    }
    
    /**
     * Sets the value of x-axis label.
     * 
     * @param label The new label
     * 
     * @since 1.00
     */
    public void setXAxisLabel(String label) {
        xAxisLabel = label;
    }
        
    /**
     * Returns the x-axis label.
     * 
     * @return The x-axis label
     * 
     * @since 1.00
     */
    public String getYAxisLabel() {
        return yAxisLabel;
    }
    
    /**
     * Sets the value of y-axis label.
     * 
     * @param label The new label
     * 
     * @since 1.00
     */
    public void setYAxisLabel(String label) {
        yAxisLabel = label;
    }
    
    /**
     * Creates the details panel.
     * 
     * @return The details panel.
     * 
     * @since 1.00
     */
    private JPanel createDetailTable() {
        JPanel south = new JPanel();
        south.setPreferredSize(new Dimension(100, 146));
        south.setLayout(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder("Details");
        south.setBorder(border);
        
        // north
        south.add(new JLabel("   "), BorderLayout.NORTH);

        // south
        south.add(new JLabel("   "), BorderLayout.SOUTH);
        
        // west
        south.add(new JLabel("   "), BorderLayout.WEST);

        // east
        south.add(new JLabel("   "), BorderLayout.EAST);
        
        // Center
        // new data table model
        model = new DataTableModel(data, dataDisplayUnits, 
                new LinkedList<String>(dataNames.keySet()));
        JTable table = new JTable(model) {
            
            /**
             * Stores a default serial version UID.
             * 
             * @since 1.00
             */
            private static final long serialVersionUID = 1L;
            
            @Override
            public Component prepareRenderer(final TableCellRenderer renderer,
                    final int row, final int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JLabel) {
                    JLabel l = (JLabel) c;
                    if (column != 0) {
                        // Align right for all columns except the first
                        l.setHorizontalAlignment(JLabel.RIGHT);
                    } else {
                        l.setHorizontalAlignment(JLabel.LEFT);
                    }
                }
                return c;
            }
        };
        JScrollPane scroll = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        south.add(scroll, BorderLayout.CENTER);
        return south;
    }
    
    /**
     * Returns the {@link MBeanServerConnection}.
     * 
     * @return The {@link MBeanServerConnection}.
     * 
     * @since 1.00
     */
    public MBeanServerConnection getMBeanServerConnection() {
        return mbsc;
    }
    
    /**
     * Adds the given data to the tab data for displaying in the tab.
     * 
     * @param key The key for the data.
     * @param values the data.
     * 
     * @since 1.00
     */
    public void addNewData(String key, LinkedList<Object> values) {
        data.addTabData(key, values);
    }
    
    /**
     * Retruns the last added data with the given key.
     * 
     * @param key The key for the data to get.
     * 
     * @return The data with the key or null if no value with this key could 
    be 
     *            found.
     *            
     * @since 1.00
     */
    public List<Object> getLastData(String key) {
        return data.getLastTabData(key);
    }

    /**
     * Adds a data name to the tab.
     * 
     * @param key The key for this data name (will be displayed!).
     * @param value The attribute name for querying the MBean server.
     * 
     * @since 1.00
     */
    public void addDataName(String key, String value) {
        dataNames.put(key, value);
    }

    /**
     * Returns all data names.
     * 
     * @return A {@link Set} with all data names.
     * 
     * @since 1.00
     */
    public Set<String> getDataNames() {
        return dataNames.keySet();
    }
    
    /**
     * Returns the attribute name to a given key for querying the MBean server.
     * 
     * @param key The key for the attribute name.
     * 
     * @return The attribute name or null if no value with this key could be 
     *            found.
     * 
     * @since 1.00
     */
    public String getDataNameValue(String key) {
        return dataNames.get(key);
    }

    /**
     * Adds a {@link TimeSeries} to display in the chart.
     * 
     * @param key The key to access the {@link TimeSeries}.
     * @param value The {@link TimeSeries}.
     * 
     * @since 1.00
     */
    public void addTimeSeries(String key, TimeSeries value) {
        timeSeries.put(key, value);
    }

    /**
     * Adds a data display unit to display in the chart.
     * 
     * @param value The unit to display.
     * 
     * @since 1.00
     */
    public void addDataDisplayUnit(String value) {
        dataDisplayUnits.add(value);
    }

    /**
     * Updates all chart data.
     * 
     * @since 1.00
     */
    public void updateChart() {
        // Gathering all required load informations
        gatherData();
        
        // Creating a period for the time series
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String[] time = sdf.format(new Date()).split(":");
        RegularTimePeriod period = new Second(Integer.parseInt(time[2]),
                Integer.parseInt(time[1]), Integer.parseInt(time[0]), 1,
                SerialDate.JANUARY, 2011);
    
        // adding the new values to the time series
        Iterator<String> iter = dataNames.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            TimeSeries series = timeSeries.get(key);
            List<Object> lastTabData = data.getLastTabData(key);
            if (null != lastTabData) {
                Object data = lastTabData.get(dataPosition);
                if (null != data) {
                    series.addOrUpdate(period, 
                        Double.parseDouble(data.toString()));
                }
            }
        }
        // notify update chart and table
        dataset.seriesChanged(new SeriesChangeEvent(dataset));
        chart.fireChartChanged();        
        model.fireTableDataChanged();
    }

    /**
     * initializes all tab specific things.
     * 
     * @since 1.00
     */
    public abstract void initializeTabSpecific();
    
    /**
     * Sets the data position.
     * 
     * @param dataPosition The new data position.
     * 
     * @since 1.00
     */
    public void setDataPosition(int dataPosition) {
        this.dataPosition = dataPosition;
    }

    /**
     * Gathers the relevant data from the MBean server and adds this data to 
     * the {@link TabData}.
     * 
     * @since 1.00
     */
    public abstract void gatherData();
    
}
