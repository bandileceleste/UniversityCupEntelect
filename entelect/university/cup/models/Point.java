package entelect.university.cup.models;

import java.util.List;
import java.util.Objects;

/**
 * Represents a 3D point/coordinate in the zoo.
 */
public class Point {
    private final int x;
    private final int y;
    private final int z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /**
     * Calculates the Euclidean distance in the XY plane.
     *
     * @param other The other point.
     * @return The 2D Euclidean distance.
     */
    public double distance2D(Point other) {
        if (other == null) {
            return Double.POSITIVE_INFINITY;
        }
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    // For output formatting (x,y)
    public String to2DString() {
        return "(" + x + "," + y + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y && z == point.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}