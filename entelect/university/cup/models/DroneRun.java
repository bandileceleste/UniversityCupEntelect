package entelect.university.cup.models;

import java.util.Collections;
import java.util.List;

/**
 * Represents a single drone run/path.
 * Could be enhanced to store calculated distance, score, etc.
 */
public class DroneRun {
    private final List<Point> path; // Includes start/end depot
    private double distance;
    private double score;
    // Add other relevant metrics if needed by the algorithm

    public DroneRun(List<Point> path) {
        this.path = Collections.unmodifiableList(path);
        // Distance and score calculation would happen elsewhere (e.g., in the solver)
        this.distance = -1; // Indicate not calculated yet
        this.score = -1;    // Indicate not calculated yet
    }

    public List<Point> getPath() {
        return path;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}