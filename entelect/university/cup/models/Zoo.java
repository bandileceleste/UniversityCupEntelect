package entelect.university.cup.models;

import java.util.Collections;
import java.util.List;

/**
 * Represents the entire Zoo environment for a specific level.
 */
public class Zoo {
    private final Point dimensions; // Max x, y, z
    private final Point droneDepot;
    private final int batteryCapacity; // in meters
    private final int batterySwaps; // Number of runs allowed
    private final List<FoodStorage> foodStorages;
    private final List<Enclosure> enclosures;
    private final List<DeadZone> deadzones;

    public Zoo(Point dimensions, Point droneDepot, int batteryCapacity, int batterySwaps,
               List<FoodStorage> foodStorages, List<Enclosure> enclosures, List<DeadZone> deadzones) {
        this.dimensions = dimensions;
        this.droneDepot = droneDepot;
        this.batteryCapacity = batteryCapacity;
        this.batterySwaps = batterySwaps;
        // Use unmodifiable lists to prevent accidental modification after creation
        this.foodStorages = Collections.unmodifiableList(foodStorages);
        this.enclosures = enclosures; // Keep mutable for setting 'fed' status
        this.deadzones = Collections.unmodifiableList(deadzones);
    }

    public Point getDimensions() {
        return dimensions;
    }

    public Point getDroneDepot() {
        return droneDepot;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public int getBatterySwaps() {
        return batterySwaps;
    }

    public List<FoodStorage> getFoodStorages() {
        return foodStorages;
    }

    public List<Enclosure> getEnclosures() {
        return enclosures;
    }

    public List<DeadZone> getDeadzones() {
        return deadzones;
    }

    @Override
    public String toString() {
        return "Zoo{" +
               "dimensions=" + dimensions +
               ", droneDepot=" + droneDepot +
               ", batteryCapacity=" + batteryCapacity +
               ", batterySwaps=" + batterySwaps +
               ", #foodStorages=" + foodStorages.size() +
               ", #enclosures=" + enclosures.size() +
               ", #deadzones=" + deadzones.size() +
               '}';
    }
}