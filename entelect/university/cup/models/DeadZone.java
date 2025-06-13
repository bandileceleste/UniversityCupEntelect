package entelect.university.cup.models;

/**
 * Represents a Dead Zone (circular area to avoid in XY plane).
 */
public class DeadZone {
    private final Point center; // Z coordinate is ignored for checks
    private final int radius;

    public DeadZone(Point center, int radius) {
        // Ensure center only uses X and Y for calculations if needed elsewhere,
        // but store the full point for consistency.
        this.center = new Point(center.getX(), center.getY(), 0); // Standardize Z for center checks
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    /**
     * Checks if a 2D line segment intersects this dead zone.
     * Uses standard line-circle intersection geometry.
     *
     * @param p1 Start point of the drone's horizontal flight segment.
     * @param p2 End point of the drone's horizontal flight segment.
     * @return true if the segment intersects the dead zone circle, false otherwise.
     */
    public boolean intersects(Point p1, Point p2) {
        // Simplified check: distance from center to line segment
        double p1x = p1.getX();
        double p1y = p1.getY();
        double p2x = p2.getX();
        double p2y = p2.getY();
        double dzx = this.center.getX();
        double dzy = this.center.getY();
        double r = this.radius;

        double dx = p2x - p1x;
        double dy = p2y - p1y;
        double lenSq = dx * dx + dy * dy;

        // Handle case where p1 and p2 are the same point
        if (lenSq == 0.0) {
            return Math.sqrt(Math.pow(dzx - p1x, 2) + Math.pow(dzy - p1y, 2)) < r;
        }

        // Parameter t representing the projection of the center onto the line
        double t = ((dzx - p1x) * dx + (dzy - p1y) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t)); // Clamp t to the segment [0, 1]

        // Coordinates of the closest point on the line segment to the deadzone center
        double closestX = p1x + t * dx;
        double closestY = p1y + t * dy;

        // Distance from the deadzone center to this closest point
        double distSq = Math.pow(dzx - closestX, 2) + Math.pow(dzy - closestY, 2);

        // Intersects if the squared distance is less than the squared radius
        return distSq < (r * r);
    }


    @Override
    public String toString() {
        return "DeadZone{" +
               "center=(" + center.getX() + "," + center.getY() + ")" +
               ", radius=" + radius +
               '}';
    }
}