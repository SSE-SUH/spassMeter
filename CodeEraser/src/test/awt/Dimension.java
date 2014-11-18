package test.awt;

/**
 * A graphical dimension.
 * 
 * @author Holger Eichelberger
 */
public class Dimension {

    /**
     * The width dimension; negative values can be used.
     */
    private int width;

    /**
     * The height dimension; negative values can be used.
     */
    private int height;

    /**
     * Creates an instance of <code>Dimension</code> with a width
     * of zero and a height of zero.
     */
    public Dimension() {
        this(0, 0);
    }

    /**
     * Constructs a <code>Dimension</code> and initializes
     * it to the specified width and specified height.
     *
     * @param width the specified width
     * @param height the specified height
     */
    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width.
     * 
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height.
     * 
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns a string representation of the values of this
     * <code>Dimension</code>.
     *
     * @return  a string representation of this <code>Dimension</code>
     *          object
     */
    public String toString() {
        return "[width=" + width + ",height=" + height + "]";
    }

}
