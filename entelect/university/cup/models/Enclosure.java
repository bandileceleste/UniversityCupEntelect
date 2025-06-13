package entelect.university.cup.models;

/**
 * Represents an Animal Enclosure in the zoo.
 */
public class Enclosure {
    private final Point feedingPoint;
    private final double importance;
    private final char dietType; // 'c', 'h', 'o'
    private boolean fed; // To track if it has been visited in any run

    public Enclosure(Point feedingPoint, double importance, char dietType) {
        this.feedingPoint = feedingPoint;
        this.importance = importance;
        this.dietType = dietType;
        this.fed = false; // Initially not fed
    }

    public Point getFeedingPoint() {
        return feedingPoint;
    }

    public double getImportance() {
        return importance;
    }

    public char getDietType() {
        return dietType;
    }

    public boolean isFed() {
        return fed;
    }

    public void setFed(boolean fed) {
        this.fed = fed;
    }

    @Override
    public String toString() {
        return "Enclosure{" +
               "feedingPoint=" + feedingPoint +
               ", importance=" + importance +
               ", dietType=" + dietType +
               ", fed=" + fed +
               '}';
    }
}