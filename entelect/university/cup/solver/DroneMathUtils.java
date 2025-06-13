package entelect.university.cup.solver;

import entelect.university.cup.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // Added for sorting
import java.util.stream.Collectors; // Added for filtering

/**
 * Contains utility methods for drone calculations (distance, path validation).
 */
public class DroneMathUtils {

    public static final int FLIGHT_ALTITUDE = 50; // meters

    /**
     * Calculates the total distance traveled for a single drone leg (point A to point B).
     * Accounts for vertical takeoff/landing and horizontal flight at altitude.
     *
     * @param p1         Starting point (can be Depot, FoodStorage, or Enclosure).
     * @param p2         Ending point (can be Depot, FoodStorage, or Enclosure).
     * @param isStartLeg True if p1 is the Drone Depot (initial takeoff).
     * @param isEndLeg   True if p2 is the Drone Depot (final landing).
     * @return The distance in meters for this leg.
     */
    public static double calculateLegDistance(Point p1, Point p2, boolean isStartLeg, boolean isEndLeg) {
        double verticalDistance = 0;
        double horizontalDistance = 0;

        // Vertical distance for takeoff from p1
        if (isStartLeg) { // Taking off from Depot
             verticalDistance += (FLIGHT_ALTITUDE - p1.getZ());
        } else { // Taking off from FoodStorage or Enclosure
             verticalDistance += (FLIGHT_ALTITUDE - p1.getZ()); // Up
        }

        // Horizontal distance at flight altitude
        horizontalDistance = p1.distance2D(p2); // Assumes diagonal movement is allowed at altitude

        // Vertical distance for landing at p2
        if (isEndLeg) { // Landing at Depot
            verticalDistance += (FLIGHT_ALTITUDE - p2.getZ());
        } else { // Landing at FoodStorage or Enclosure
             verticalDistance += (FLIGHT_ALTITUDE - p2.getZ()); // Down
        }

        // Special case: If landing/taking off at the *same* Food Storage without changing food type,
        // the drone doesn't land. However, the problem statement implies the drone *path* includes
        // the coordinate. For simplicity in path calculation, we assume it flies *to* the coordinate,
        // but the landing/takeoff vertical distance might be skipped in a more refined calculation
        // if the drone doesn't actually land.
        // For now, this calculation assumes landing/takeoff at each specified point other than potentially
        // duplicate food storage visits (which should be avoided by the path planning).

        // If moving between two points *not* involving the depot, need ascent *and* descent
        if (!isStartLeg && !isEndLeg) {
             // Ascent from p1 + Descent to p2 is already covered by the logic above?
             // Let's rethink:
             // Leg from A to B:
             // 1. Vertical from A's Z to FLIGHT_ALTITUDE
             // 2. Horizontal distance2D(A, B)
             // 3. Vertical from FLIGHT_ALTITUDE to B's Z
             verticalDistance = (FLIGHT_ALTITUDE - p1.getZ()) + (FLIGHT_ALTITUDE - p2.getZ());
        } else if (isStartLeg && !isEndLeg) { // Depot to First Stop
             verticalDistance = (FLIGHT_ALTITUDE - p1.getZ()) + (FLIGHT_ALTITUDE - p2.getZ());
        } else if (!isStartLeg && isEndLeg) { // Last Stop to Depot
             verticalDistance = (FLIGHT_ALTITUDE - p1.getZ()) + (FLIGHT_ALTITUDE - p2.getZ());
        } else { // Depot to Depot (empty run - should not happen ideally)
             verticalDistance = (FLIGHT_ALTITUDE - p1.getZ()) + (FLIGHT_ALTITUDE - p2.getZ());
        }


        return horizontalDistance + verticalDistance;
    }


     /**
     * Calculates the total distance for a complete drone run path.
     *
     * @param path The list of points visited in the run (must start and end at Depot).
     * @param zoo The Zoo object for context (Depot location).
     * @return The total distance in meters for the run.
     */
    public static double calculateTotalRunDistance(List<Point> path, Zoo zoo) {
        if (path == null || path.size() < 2) {
            return 0.0; // Or throw exception, an empty/single point path is invalid
        }
        if (!path.get(0).equals(zoo.getDroneDepot()) || !path.get(path.size() - 1).equals(zoo.getDroneDepot())) {
             System.err.println("Warning: Path does not start and end at the depot: " + path);
             // Or throw IllegalArgumentException
            return Double.POSITIVE_INFINITY; // Indicate invalid path
        }

        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);

            // Simplified leg calculation:
            // Vertical travel happens at start/end points
            double verticalDist = (FLIGHT_ALTITUDE - p1.getZ()) + (FLIGHT_ALTITUDE - p2.getZ());
            double horizontalDist = p1.distance2D(p2);
            totalDistance += verticalDist + horizontalDist;


           // More accurate logic needed here based on PDF example
           // PDF Example calculation: Rhino (5.50 importance)
           // Path: Depot(8,10,10) -> H_Storage(6,9,2) -> Rhino(7,24,5) -> Depot(8,10,10)
           // 1. Depot takeoff: 50 - 10 = 40
           // 2. Fly Depot to H_Storage: sqrt((6-8)^2 + (9-10)^2) = sqrt(4+1) = sqrt(5) = 2.236
           // 3. Land H_Storage + Takeoff H_Storage: (50-2) + (50-2) = 48 + 48 = 96
           // 4. Fly H_Storage to Rhino: sqrt((7-6)^2 + (24-9)^2) = sqrt(1 + 15^2) = sqrt(1+225) = sqrt(226) = 15.033
           // 5. Land Rhino + Takeoff Rhino: (50-5) + (50-5) = 45 + 45 = 90
           // 6. Fly Rhino to Depot: sqrt((8-7)^2 + (10-24)^2) = sqrt(1 + (-14)^2) = sqrt(1+196) = sqrt(197) = 14.036
           // 7. Land Depot: 50 - 10 = 40
           // Total: 40 + 2.236 + 96 + 15.033 + 90 + 14.036 + 40 = 297.305 (PDF example got ~161m - calculation method differs significantly)

           // LET'S USE THE PDF EXAMPLE'S LOGIC:
           // Total distance = d + (50 - z1) + (50 - z2)
           // d = horizontal distance
           // (50-z1) + (50-z2) = vertical ascent/descent total for the two points *IF LANDING OCCURS*

           // Re-evaluating based on PDF example: Seems like vertical is only counted for the specific points visited *where landing occurs*.
           // Let's try that approach.
        }

        // Correct calculation based on PDF Example:
        double pdfTotalDistance = 0;
        Point depot = zoo.getDroneDepot();

        // 1. Takeoff from Depot
        pdfTotalDistance += (FLIGHT_ALTITUDE - depot.getZ());

        // 2. Travel legs
        for (int i = 0; i < path.size() - 1; i++) {
             Point current = path.get(i);
             Point next = path.get(i + 1);

             // Horizontal flight distance
             pdfTotalDistance += current.distance2D(next);

             // Vertical distance for landing + takeoff *at the next point* IF it's not the depot
             if (!next.equals(depot)) {
                  // Check if drone actually lands (e.g., required food type, not already fed)
                  // For simplicity in this structure, we assume it lands at every non-depot point in the path.
                  // A real solver would check this.
                 pdfTotalDistance += (FLIGHT_ALTITUDE - next.getZ()) * 2.0;
             }
        }

        // 3. Final landing at Depot
        pdfTotalDistance += (FLIGHT_ALTITUDE - depot.getZ());


        // Let's re-calculate the example: D(8,10,10) -> H(6,9,2) -> R(7,24,5) -> D(8,10,10)
        // 1. Takeoff Depot: 50-10 = 40
        // 2. Fly D->H: sqrt(5) = 2.236
        // 3. Land/Takeoff H: (50-2)*2 = 96
        // 4. Fly H->R: sqrt(226) = 15.033
        // 5. Land/Takeoff R: (50-5)*2 = 90
        // 6. Fly R->D: sqrt(197) = 14.036
        // 7. Land Depot: 50-10 = 40
        // Total: 40 + 2.236 + 96 + 15.033 + 90 + 14.036 + 40 = 297.305 --- Still doesn't match PDF's 161m.

        // *** THERE IS A SIGNIFICANT DISCREPANCY BETWEEN MY INTERPRETATION/CALCULATION
        // *** AND THE PDF EXAMPLE'S CALCULATION (page 10).
        // *** The PDF example calculation adds up to 161.3m approx.
        // *** Let's strictly follow the PDF's calculation breakdown:
        // 50 – 10 = 40 (taking off from the Depot)
        // √(6 – 8)² + (9 – 10)² = 2.26 (flight to herbivore food storage)
        // (50 – 2) * 2 = 96 (landing and taking off) --- AT H STORAGE
        // √(7 – 6)² + (24 – 9)² = 15.03 (flight to Rhino)
        // (50 – 5) * 2 = 90 (landing and taking off) --- AT RHINO
        // √(8 – 7)² + (10 – 24)² = 14.04 (flight back to the Depot)
        // 50 – 10 = 40 (landing at the Depot)
        // Total: 40 + 2.26 + 96 + 15.03 + 90 + 14.04 + 40 = 297.33

        // *** CONCLUSION: The PDF's own sum (161m) does NOT match its calculation steps (297m).
        // *** Will proceed with the logic derived from the PDF's *steps*, not its final sum.

        return pdfTotalDistance; // Use the 297m logic based on PDF steps
    }

    /**
     * Checks if a planned drone path segment intersects any dead zones.
     *
     * @param p1        Start point of the horizontal flight segment.
     * @param p2        End point of the horizontal flight segment.
     * @param deadzones List of dead zones in the zoo.
     * @return true if the segment intersects any dead zone, false otherwise.
     */
    public static boolean intersectsAnyDeadZone(Point p1, Point p2, List<DeadZone> deadzones) {
        for (DeadZone dz : deadzones) {
            if (dz.intersects(p1, p2)) {
                return true;
            }
        }
        return false;
    }

     /**
      * Checks if the entire path is valid (within battery and avoids dead zones).
      * NOTE: This is a simplified check. A real implementation needs to check
      * dead zones for *each* horizontal flight leg.
      *
      * @param path            The list of points in the drone run.
      * @param zoo             The Zoo object containing constraints.
      * @return true if the path is valid, false otherwise.
      */
     public static boolean isPathValid(List<Point> path, Zoo zoo) {
         if (path == null || path.size() < 2) return false;

         double totalDistance = calculateTotalRunDistance(path, zoo);

         // Check battery
         if (totalDistance > zoo.getBatteryCapacity()) {
             // System.out.println("Path invalid: Exceeds battery capacity (" + totalDistance + " > " + zoo.getBatteryCapacity() + ")");
             return false;
         }

         // Check dead zones for each horizontal segment
         for (int i = 0; i < path.size() - 1; i++) {
             Point p1 = path.get(i);
             Point p2 = path.get(i + 1);
             // Only check horizontal flight paths (not takeoff/landing directly into/out of depot)
             if (!p1.equals(zoo.getDroneDepot()) || !p2.equals(zoo.getDroneDepot())) {
                 if (intersectsAnyDeadZone(p1, p2, zoo.getDeadzones())) {
                    // System.out.println("Path invalid: Intersects dead zone between " + p1.to2DString() + " and " + p2.to2DString());
                    return false;
                 }
             } else if (path.size() > 2) { // Special case: Depot -> P -> Depot, check the direct path P -> P
                 // If only Depot -> P -> Depot, check the horizontal path between P and itself (not needed)
                 // We only need to check paths *between* stops
                 if (!p1.equals(zoo.getDroneDepot()) && !p2.equals(zoo.getDroneDepot())) {
                     if (intersectsAnyDeadZone(p1, p2, zoo.getDeadzones())) {
                       // System.out.println("Path invalid: Intersects dead zone between " + p1.to2DString() + " and " + p2.to2DString());
                       return false;
                     }
                 }
             }
              // Check the leg from Depot to first stop and last stop to Depot
             if (i == 0 || i == path.size() - 2) {
                 if (intersectsAnyDeadZone(p1, p2, zoo.getDeadzones())) {
                    // System.out.println("Path invalid: Intersects dead zone between " + p1.to2DString() + " and " + p2.to2DString());
                    return false;
                 }
             }
         }

         return true;
     }
}