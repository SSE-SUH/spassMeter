package test;

import java.awt.Point;

/**
 * Uses a point (for binary replacement).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ReplaceTest {

    /**
     * Stores the point.
     */
    private Point point;
    
    /**
     * Creates a point.
     * 
     * @return the created point.
     * 
     * @since 1.00
     */
    public Point createPoint() {
        if (null == point) {
            point = new Point(10, 20);
        }
        return point;
    }
    
    /**
     * Executes the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        ReplaceTest test = new ReplaceTest();
        Point point = test.createPoint();
        System.out.println(point);
    }
}
