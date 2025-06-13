package entelect.university.cup.file;

import entelect.university.cup.models.*; // Import all model classes

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles reading Zoo specifications from text files and writing drone paths to text files.
 */
public class ZooFileHandler {

    // Patterns for parsing coordinates and lists
    private static final Pattern POINT_PATTERN = Pattern.compile("\\((\\d+),(\\d+),(\\d+)\\)");
    private static final Pattern FOOD_STORAGE_ITEM_PATTERN = Pattern.compile("\\((\\d+),(\\d+),(\\d+),([cho])\\)");
    private static final Pattern ENCLOSURE_ITEM_PATTERN = Pattern.compile("\\((\\d+),(\\d+),(\\d+),(\\d+\\.?\\d*),([cho])\\)");
    private static final Pattern DEADZONE_ITEM_PATTERN = Pattern.compile("\\((\\d+),(\\d+),(\\d+)\\)"); // Simplified, radius is separate in text? No, PDF example has (x,y,r)
    private static final Pattern DEADZONE_ITEM_PATTERN_PDF = Pattern.compile("\\((\\d+),(\\d+),(\\d+)\\)"); // Matches PDF example (5,20,1)

    /**
     * Reads the zoo specification from a given file path.
     *
     * @param filePath The path to the zoo specification file.
     * @return A Zoo object representing the parsed specification.
     * @throws IOException If an error occurs during file reading.
     * @throws IllegalArgumentException If the file format is invalid.
     */
    public static Zoo readZooFromFile(String filePath) throws IOException, IllegalArgumentException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Point dimensions = parsePoint(reader.readLine());
            Point droneDepot = parsePoint(reader.readLine());
            int batteryCapacity = Integer.parseInt(reader.readLine().trim());
            List<FoodStorage> foodStorages = parseFoodStorageList(reader.readLine());
            List<Enclosure> enclosures = parseEnclosureList(reader.readLine());
            List<DeadZone> deadzones = parseDeadZoneList(reader.readLine());

            // Determine batterySwaps based on level conventions (approximate)
            // A more robust way might be needed if file structure changes
            int batterySwaps = 0; // Default for Level 1 adjusted later
            if (batteryCapacity == 1125) batterySwaps = 10;
            else if (batteryCapacity == 2750) batterySwaps = 50;
            else if (batteryCapacity == 9250) batterySwaps = 250;
            else if (batteryCapacity == 999999) batterySwaps = 0; // Level 1 case


            return new Zoo(dimensions, droneDepot, batteryCapacity, batterySwaps, foodStorages, enclosures, deadzones);

        } catch (IOException e) {
            System.err.println("Error reading zoo file: " + filePath);
            throw e;
        } catch (NullPointerException | IllegalArgumentException e) {
            System.err.println("Error parsing zoo file content: " + filePath);
            throw new IllegalArgumentException("Invalid file format in " + filePath, e);
        }
    }

    private static Point parsePoint(String line) {
        if (line == null) throw new IllegalArgumentException("Missing line for Point.");
        Matcher matcher = POINT_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            return new Point(x, y, z);
        }
        throw new IllegalArgumentException("Invalid Point format: " + line);
    }

    private static List<FoodStorage> parseFoodStorageList(String line) {
        List<FoodStorage> list = new ArrayList<>();
        if (line == null || line.trim().equals("[]")) return list;

        Matcher matcher = FOOD_STORAGE_ITEM_PATTERN.matcher(line);
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            char diet = matcher.group(4).charAt(0);
            list.add(new FoodStorage(new Point(x, y, z), diet));
        }
        // Basic check if any items were found if the line wasn't empty
        if (!line.trim().matches("\\[\\s*\\]") && list.isEmpty() && line.trim().length() > 2) {
             throw new IllegalArgumentException("Invalid FoodStorage list format or content: " + line);
        }
        return list;
    }

     private static List<Enclosure> parseEnclosureList(String line) {
        List<Enclosure> list = new ArrayList<>();
         if (line == null || line.trim().equals("[]")) return list;

        Matcher matcher = ENCLOSURE_ITEM_PATTERN.matcher(line);
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            double importance = Double.parseDouble(matcher.group(4));
            char diet = matcher.group(5).charAt(0);
            list.add(new Enclosure(new Point(x, y, z), importance, diet));
        }
         if (!line.trim().matches("\\[\\s*\\]") && list.isEmpty() && line.trim().length() > 2) {
             throw new IllegalArgumentException("Invalid Enclosure list format or content: " + line);
         }
        return list;
    }

    // Adjusted DeadZone parsing based on PDF example (x,y,r) where r is the 3rd int
     private static List<DeadZone> parseDeadZoneList(String line) {
        List<DeadZone> list = new ArrayList<>();
         if (line == null || line.trim().equals("[]")) return list;

        // Use the PDF example pattern matcher
        Matcher matcher = DEADZONE_ITEM_PATTERN_PDF.matcher(line);
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int r = Integer.parseInt(matcher.group(3)); // Third element is radius 'r'
            // Create a dummy Point for the center, Z is irrelevant
            list.add(new DeadZone(new Point(x, y, 0), r));
        }
         if (!line.trim().matches("\\[\\s*\\]") && list.isEmpty() && line.trim().length() > 2) {
             // Allow empty list if line genuinely represents empty []
             if (!line.trim().equals("[]")) {
                 throw new IllegalArgumentException("Invalid DeadZone list format or content: " + line);
             }
         }
        return list;
    }


    /**
     * Writes the calculated drone paths to a file in the specified format.
     *
     * @param droneRuns The list of drone runs (each run is a list of Points).
     * @param filePath  The path to the output file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void writePathsToFile(List<List<Point>> droneRuns, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("["); // Start of the main list

        boolean firstRun = true;
        for (List<Point> runPath : droneRuns) {
            if (!firstRun) {
                sb.append(","); // Separator between runs
            }
            sb.append("["); // Start of a single run list

            boolean firstPoint = true;
            for (Point p : runPath) {
                if (!firstPoint) {
                    sb.append(","); // Separator between points
                }
                // Format as (x,y) - using the helper method from Point class
                sb.append("(").append(p.getX()).append(",").append(p.getY()).append(")");
                firstPoint = false;
            }
            sb.append("]"); // End of a single run list
            firstRun = false;
        }

        sb.append("]"); // End of the main list

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("Error writing paths file: " + filePath);
            throw e;
        }
    }
}