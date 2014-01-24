package test.awt;

/**
 * A graphical point.
 * 
 * @author Jurij Töpfer
 */
public class Point {

    /**
     * Stores the horizontal position.
     */
    private int x;

    /**
     * Stores the vertical position.
     */
    private int y;

    /**
     * Creates a new point.
     * 
     * @param xPos the horizontal position
     * @param yPos the vertical position
     * 
     * @since 1.00
     */
    public Point(int xPos, int yPos) {
        this.x = xPos;
        this.y = yPos;
    }

    /**
     * Returns the horizontal position.
     * 
     * @return the horizontal position
     * 
     * @since 1.00
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the horizontal position.
     * 
     * @return the horizontal position
     * 
     * @since 1.00
     */
    public int getY() {
        return y;
    }

    /**
     * Returns a textual description of this object.
     * 
     * @return a textual description
     */
    public String toString() {
        return "test.awt.Point(" + x + "," + y + ")";
    }
    
}
