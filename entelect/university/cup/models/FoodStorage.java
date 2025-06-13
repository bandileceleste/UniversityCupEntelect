package entelect.university.cup.models;

/**
 * Represents a Food Storage location in the zoo.
 */
public class FoodStorage {
    private final Point location;
    private final char dietType; // 'c', 'h', 'o'

    public FoodStorage(Point location, char dietType) {
        this.location = location;
        this.dietType = dietType;
    }

    public Point getLocation() {
        return location;
    }

    public char getDietType() {
        return dietType;
    }

    @Override
    public String toString() {
        return "FoodStorage{" +
               "location=" + location +
               ", dietType=" + dietType +
               '}';
    }
}