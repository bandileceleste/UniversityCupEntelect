package entelect.university.cup.solver;

import entelect.university.cup.models.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Placeholder class for the pathfinding algorithm.
 * This needs to be implemented with a specific strategy (e.g., greedy, TSP variant).
 */
public class PathFinder {

    private final Zoo zoo;
    private List<List<Point>> allRuns;
    private Set<Enclosure> fedEnclosuresGlobal; // Track across all runs

    public PathFinder(Zoo zoo) {
        this.zoo = zoo;
        this.allRuns = new ArrayList<>();
        this.fedEnclosuresGlobal = new HashSet<>();
    }

    /**
     * The main method to generate all drone runs.
     *
     * @return A list of paths (each path is a list of Points).
     */
    public List<List<Point>> generatePaths() {
        // Reset state if called multiple times
        allRuns.clear();
        fedEnclosuresGlobal.clear();
        // Reset fed status for all enclosures in the zoo model
        for (Enclosure enc : zoo.getEnclosures()) {
            enc.setFed(false);
        }

        int runsRemaining = zoo.getBatterySwaps() + 1; // Initial run + swaps

        // --- !!! THIS IS WHERE THE CORE ALGORITHM LOGIC GOES !!! ---
        // Example: Very Basic Greedy Strategy (will likely be suboptimal)
        // Group enclosures by diet
        Map<Character, List<Enclosure>> enclosuresByDiet = zoo.getEnclosures().stream()
                .collect(Collectors.groupingBy(Enclosure::getDietType));

        // Sort food storages by diet for easy lookup
        Map<Character, List<FoodStorage>> storagesByDiet = zoo.getFoodStorages().stream()
                .collect(Collectors.groupingBy(FoodStorage::getDietType));

        // Priority queue for enclosures (highest importance first)
        PriorityQueue<Enclosure> enclosureQueue = new PriorityQueue<>(
                Comparator.<Enclosure, Double>comparing(Enclosure::getImportance).reversed()
        );
        enclosureQueue.addAll(zoo.getEnclosures());


        while (runsRemaining > 0 && !enclosureQueue.isEmpty()) {
             // Try to build a run
             List<Point> currentRunPath = buildSingleGreedyRun(enclosureQueue, storagesByDiet);

             if (currentRunPath != null && currentRunPath.size() > 2) { // A valid run must visit at least one location
                 allRuns.add(currentRunPath);
                 runsRemaining--;
             } else {
                 // Cannot build any more valid runs with remaining enclosures
                 break;
             }
             // Remove fed enclosures from the queue for the next iteration
             enclosureQueue.removeIf(Enclosure::isFed);
        }
        // --- !!! END OF ALGORITHM PLACEHOLDER !!! ---


        System.out.println("Generated " + allRuns.size() + " runs.");
        return allRuns;
    }

    /**
     * Placeholder for building a single drone run using a greedy approach.
     * Tries to feed the highest importance, unfed animal it can reach.
     * THIS IS A VERY BASIC EXAMPLE AND LIKELY INEFFICIENT/SUBOPTIMAL.
     */
    private List<Point> buildSingleGreedyRun(PriorityQueue<Enclosure> availableEnclosures,
                                             Map<Character, List<FoodStorage>> storagesByDiet) {

        List<Point> path = new ArrayList<>();
        path.add(zoo.getDroneDepot()); // Start at depot

        double currentBatteryUsed = 0.0;
        Point currentLocation = zoo.getDroneDepot();
        char currentFood = ' '; // No food initially
        FoodStorage lastFoodStorageVisited = null;
        List<Enclosure> fedInThisRun = new ArrayList<>();

        boolean canAddMore = true;
        while (canAddMore) {
            Enclosure bestTargetEnclosure = null;
            FoodStorage requiredFoodStorage = null;
            double costToFeedBestTarget = Double.POSITIVE_INFINITY;

            // Find the best *possible* next enclosure to feed (highest importance, unfed)
            List<Enclosure> candidates = new ArrayList<>(availableEnclosures);
            candidates.sort(Comparator.<Enclosure, Double>comparing(Enclosure::getImportance).reversed());

            for (Enclosure targetEnc : candidates) {
                 if (targetEnc.isFed()) continue; // Already fed globally

                 char neededDiet = targetEnc.getDietType();
                 FoodStorage storageToUse = null;
                 List<Point> potentialPathSegment = new ArrayList<>();
                 double segmentCost = 0;

                 // Do we need to get food?
                 if (currentFood != neededDiet) {
                      // Find the *closest* food storage for the needed diet
                      storageToUse = findClosestStorage(currentLocation, storagesByDiet.get(neededDiet));
                      if (storageToUse == null) continue; // No storage for this diet

                      potentialPathSegment.add(storageToUse.getLocation()); // Fly to storage
                 }
                 potentialPathSegment.add(targetEnc.getFeedingPoint()); // Fly to enclosure

                 // Calculate cost of this segment (current -> [storage?] -> enclosure -> depot)
                 List<Point> tempFullPath = new ArrayList<>(path);
                 tempFullPath.addAll(potentialPathSegment);
                 tempFullPath.add(zoo.getDroneDepot()); // Add final return leg for cost check

                 // Check if *this segment* plus return is valid (approximate check)
                 List<Point> segmentCheckPath = new ArrayList<>();
                 segmentCheckPath.add(currentLocation);
                 segmentCheckPath.addAll(potentialPathSegment);
                 segmentCheckPath.add(zoo.getDroneDepot());

                 if (DroneMathUtils.isPathValid(tempFullPath, zoo)) { // Check full path validity
                     double potentialTotalDistance = DroneMathUtils.calculateTotalRunDistance(tempFullPath, zoo);
                     if (potentialTotalDistance <= zoo.getBatteryCapacity()) {
                         // This enclosure is reachable within constraints *from current state*
                         // In a purely greedy approach, we might just take the highest importance one.
                         // Let's calculate the cost *added* by visiting this enclosure
                         double addedDistance = calculateAddedDistance(path, potentialPathSegment, zoo);

                         // Simple greedy: choose highest importance reachable enclosure
                         bestTargetEnclosure = targetEnc;
                         requiredFoodStorage = storageToUse; // Might be null if food already held
                         break; // Found the best one according to priority queue order
                     }
                 }
            } // End finding best target

            // If we found a target we can feed
            if (bestTargetEnclosure != null) {
                // Add storage to path if needed
                if (requiredFoodStorage != null) {
                    path.add(requiredFoodStorage.getLocation());
                    currentLocation = requiredFoodStorage.getLocation();
                    currentFood = requiredFoodStorage.getDietType(); // Picked up food
                    lastFoodStorageVisited = requiredFoodStorage;
                }
                // Add enclosure to path
                path.add(bestTargetEnclosure.getFeedingPoint());
                currentLocation = bestTargetEnclosure.getFeedingPoint();
                fedInThisRun.add(bestTargetEnclosure);
                bestTargetEnclosure.setFed(true); // Mark as fed globally
                availableEnclosures.remove(bestTargetEnclosure); // Remove from candidates for this run

                // Check if we can still return to depot after adding this
                 List<Point> finalPathCheck = new ArrayList<>(path);
                 finalPathCheck.add(zoo.getDroneDepot());
                 if (!DroneMathUtils.isPathValid(finalPathCheck, zoo) || DroneMathUtils.calculateTotalRunDistance(finalPathCheck, zoo) > zoo.getBatteryCapacity()) {
                     // Cannot complete the run, revert last step
                     path.remove(path.size()-1); // Remove enclosure
                     if (requiredFoodStorage != null) {
                         path.remove(path.size()-1); // Remove storage
                         // Revert food status? More complex state needed...
                     }
                     bestTargetEnclosure.setFed(false); // Unmark
                     availableEnclosures.add(bestTargetEnclosure); // Add back
                     fedInThisRun.remove(bestTargetEnclosure);
                     canAddMore = false; // Stop adding to this run
                 }
                 // Else: Successfully added, continue trying to add more

            } else {
                canAddMore = false; // No suitable enclosure found
            }
        } // End while canAddMore

        // Finalize run path by adding return to depot
        if (path.size() > 1) { // If we visited at least one place
             path.add(zoo.getDroneDepot());

             // Final validation check (redundant if logic above is correct, but safe)
             if (!DroneMathUtils.isPathValid(path, zoo) || DroneMathUtils.calculateTotalRunDistance(path, zoo) > zoo.getBatteryCapacity()) {
                  // This run configuration became invalid, potentially need backtracking
                  System.err.println("Warning: Greedy run became invalid at the end.");
                  // For this simple greedy, we might return null or an empty path
                   // Revert fed status for this run
                  for (Enclosure reverted : fedInThisRun) {
                      reverted.setFed(false);
                      availableEnclosures.add(reverted); // Add back to main queue
                  }
                  return null;
             }
        } else {
            return null; // No valid stops could be made
        }


        return path;
    }

    // Helper to find the closest food storage of a specific type
    private FoodStorage findClosestStorage(Point currentLocation, List<FoodStorage> storages) {
        if (storages == null || storages.isEmpty()) {
            return null;
        }
        FoodStorage closest = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (FoodStorage storage : storages) {
            double dist = currentLocation.distance2D(storage.getLocation());
            if (dist < minDistance) {
                minDistance = dist;
                closest = storage;
            }
        }
        return closest;
    }

     // Helper to estimate added distance (simplistic)
     private double calculateAddedDistance(List<Point> basePath, List<Point> segmentToAdd, Zoo zoo) {
         List<Point> pathWithSegment = new ArrayList<>(basePath);
         pathWithSegment.addAll(segmentToAdd);
         pathWithSegment.add(zoo.getDroneDepot());

         List<Point> pathWithoutSegment = new ArrayList<>(basePath);
         pathWithoutSegment.add(zoo.getDroneDepot());

         double distWith = DroneMathUtils.calculateTotalRunDistance(pathWithSegment, zoo);
         double distWithout = DroneMathUtils.calculateTotalRunDistance(pathWithoutSegment, zoo);

         return distWith - distWithout;
     }
}