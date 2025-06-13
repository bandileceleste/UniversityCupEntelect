import entelect.university.cup.file.ZooFileHandler;
import entelect.university.cup.models.Point;
import entelect.university.cup.models.Zoo;
import entelect.university.cup.solver.PathFinder;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar DroneKeeper.jar <input_zoo_file> <output_path_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        System.out.println("Reading zoo data from: " + inputFile);

        try {
            // 1. Read Zoo Data
            Zoo zoo = ZooFileHandler.readZooFromFile(inputFile);
            System.out.println("Zoo data loaded successfully:");
            System.out.println(zoo);
            System.out.println("  Drone Depot: " + zoo.getDroneDepot());
            System.out.println("  Battery Capacity: " + zoo.getBatteryCapacity());
             System.out.println("  Allowed Runs: " + (zoo.getBatterySwaps() + 1));


            // 2. Find Paths
            System.out.println("\nCalculating drone paths...");
            PathFinder pathFinder = new PathFinder(zoo);
            List<List<Point>> dronePaths = pathFinder.generatePaths(); // This will execute the algorithm

            // 3. Write Paths to File
            System.out.println("\nWriting calculated paths to: " + outputFile);
            ZooFileHandler.writePathsToFile(dronePaths, outputFile);

            System.out.println("\nProcessing complete.");

        } catch (IOException e) {
            System.err.println("Error during file operation: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing input file or invalid data: " + e.getMessage());
             if (e.getCause() != null) {
                 e.getCause().printStackTrace();
             } else {
                 e.printStackTrace();
             }
            System.exit(3);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        }
    }
}