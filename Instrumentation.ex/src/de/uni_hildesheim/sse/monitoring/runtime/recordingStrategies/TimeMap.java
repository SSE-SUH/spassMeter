package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.utils.AbstractLongHashMap;

/**
 * Implements an internal map storing the thread-related tomes for the
 * {@link DefaultRecorderElement}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class TimeMap extends AbstractLongHashMap {

    /**
     * Defines the map element which specifies the instaces to be stored in this
     * class.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class MapElement extends AbstractLongHashMap.MapElement {
        
        /**
         * Stores the start CPU time.
         */
        private long startCpuTime;
        
        /**
         * Stores the start system time.
         */
        private long startSystemTime;
        
        /**
         * Constructor of a map element.
         * 
         * @param key the hash key of the element
         */
        protected MapElement(long key) {
            super(key);
        }
    
        /**
         * Returns the start CPU time.
         * 
         * @return the start CPU time
         * 
         * @since 1.00
         */
        public long getStartCpuTime() {
            return startCpuTime;
        }

        /**
         * Returns the start system time.
         * 
         * @return the start system time
         * 
         * @since 1.00
         */
        public long getStartSystemTime() {
            return startSystemTime;
        }

        /**
         * Changes the start CPU time.
         * 
         * @param startCpuTime the new start CPU time
         * 
         * @since 1.00
         */
        public void setStartCpuTime(long startCpuTime) {
            this.startCpuTime = startCpuTime;
        }

        /**
         * Changes the start system time.
         * 
         * @param startSystemTime the new start system time
         * 
         * @since 1.00
         */
        public void setStartSystemTime(long startSystemTime) {
            this.startSystemTime = startSystemTime;
        }

        /**
         * Changes start CPU and system time.
         * 
         * @param startCpuTime the new start CPU time
         * @param startSystemTime the new start system time
         * 
         * @since 1.00
         */
        public void setTimes(long startCpuTime, long startSystemTime) {
            this.startCpuTime = startCpuTime;
            this.startSystemTime = startSystemTime;
        }

        /**
         * Takes over the information stored in <code>elt</code>.
         * 
         * @param elt the map element storing the information
         * 
         * @since 1.00
         */
        public void takeOver(MapElement elt) {
            this.startCpuTime = elt.startCpuTime;
            this.startSystemTime = elt.startSystemTime;
        }
    }
    
    /**
     * Puts all the keys and values in the specified hash
     * map into this hash map.
     *
     * @param map the source map (a {@link TimeMap})
     */  
    public void putAll(AbstractLongHashMap map) {
        TimeMap lMap = (TimeMap) map;
        long[] keys = map.keySet();
        for (int i = 0; i < map.size(); i++) {
            MapElement elt = (MapElement) putElement(keys[i], null);
            elt.takeOver(lMap.get(keys[i]));
        }
    }
     
    /**
     * Associates initial timing information <code>key</code> (thread 
     * identifier). 
     * 
     * @param key the key representing an object
     * @param startCpuTime the start CPU time
     * @param startSystemTime the start system time
     * @return the new map element inserted into this map
     * 
     * @since 1.00
     */
    public MapElement put(long key, long startCpuTime, long startSystemTime) {
        // pass in thread data to differentiate between indirect and direct 
        putElement(key, null); 
        MapElement elt = (MapElement) getElement(key);
        if (null != elt) {
            elt.setTimes(startCpuTime, startSystemTime);
        }
        return elt;
    }

    /**
     * Creates an element.
     * 
     * @param key the hash key of the element
     * @param value the generic value to be attached
     * 
     * @return the new element
     * 
     * @since 1.00
     */
    protected MapElement create(long key, Object value) {
        return new MapElement(key);
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key The key whose mapping is to be removed from the map.
     */
    public void remove(long key) {
        removeElement(key);
    }
     
    /**
     * Returns the map element for the given key.
     * 
     * @param key the key to return the map element for
     * @return the associated map element or <b>null</b> if not found
     * 
     * @since 1.00
     */
    public MapElement get(long key) {
        return (MapElement) getElement(key);
    }
    
}
