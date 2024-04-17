import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Cell {
    private static final Map<Integer, Cell> registry = new HashMap<>();
    private int index;
    private String oem;
    private String model;
    private String launchAnnounced;
    private String launchStatus;
    private String bodyDimensions;
    private Integer bodyWeight;
    private String bodySim;
    private String displayType;
    private String displaySize;
    private String displayResolution;
    private String featuresSensors;
    private String platformOs;

    public Cell(int index, Map<String, String> row) {
        this.index = index;
        this.oem = row.get("oem");
        this.model = row.get("model");
        this.launchAnnounced = row.get("launch_announced");
        this.launchStatus = row.get("launch_status");
        this.bodyDimensions = row.get("body_dimensions");
        this.bodyWeight = cleanWeight(row.get("body_weight"));
        this.bodySim = row.get("body_sim");
        this.displayType = row.get("display_type");
        this.displaySize = row.get("display_size");
        this.displayResolution = row.get("display_resolution");
        this.featuresSensors = row.get("features_sensors");
        this.platformOs = row.get("platform_os");

        registry.put(this.index, this);
    }

    private static Integer cleanWeight(String weight) {
        if (weight == null || weight.equals("-")) {
            return null;
        }
        Matcher m = Pattern.compile("(\\d+)").matcher(weight);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    public static List<String> phonesAnnouncedReleasedDifferentYears() {
        return registry.values().stream()
            .filter(c -> c.launchAnnounced != null && c.launchAnnounced.contains("Released"))
            .map(c -> {
                String[] parts = c.launchAnnounced.split("Released");
                String announcedYear = parts[0].trim().split(", ")[1].trim();
                String releasedYear = parts[1].trim().split(", ")[1].trim();
                if (!announcedYear.equals(releasedYear)) {
                    return c.oem + " " + c.model + ", Announced: " + announcedYear + ", Released: " + releasedYear;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static int countSingleFeatureSensors() {
        return (int) registry.values().stream()
            .filter(c -> c.featuresSensors != null && c.featuresSensors.split(",").length == 1)
            .count();
    }

    public static Optional<Map.Entry<String, Double>> highestAvgWeight() {
        return registry.values().stream()
            .filter(c -> c.bodyWeight != null)
            .collect(Collectors.groupingBy(c -> c.oem, Collectors.averagingInt(c -> c.bodyWeight)))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue());
    }

    public static Map<String, Double> calculateWeightStats() {
        List<Integer> weights = registry.values().stream()
            .map(c -> c.bodyWeight)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        double average = weights.stream().mapToDouble(a -> a).average().orElse(0.0);
        double median = weights.stream().sorted().skip(weights.size() / 2).findFirst().orElse(0);
        double stddev = Math.sqrt(weights.stream().mapToDouble(x -> Math.pow(x - average, 2)).sum() / weights.size());
        Map<String, Double> stats = new HashMap<>();
        stats.put("mean", average);
        stats.put("median", median);
        stats.put("standard_deviation", stddev);
        return stats;
    }

    public static void main(String[] args) {
        try {
            CSVReader reader = new CSVReader(new FileReader("cells.csv"));
            String[] nextLine;
            int index = 0;
            while ((nextLine = reader.readNext()) != null) {
                Map<String, String> row = new HashMap<>();
                row.put("oem", nextLine[0]);
                row.put("model", nextLine[1]);
                // Add other fields here as needed
                new Cell(index++, row);
            }

            System.out.println("Total cells loaded: " + registry.size());
            System.out.println("Phones with differing announced and released years: " + phonesAnnouncedReleasedDifferentYears());
            System.out.println("Number of phones with only one feature sensor: " + countSingleFeatureSensors());
            highestAvgWeight().ifPresent(max -> System.out.println("OEM with highest average body weight: " + max.getKey() + ", Weight: " + max.getValue()));
            Map<String, Double> weightStats = calculateWeightStats();
            System.out.println("Weight stats: " + weightStats);
        } catch (Exception e) {
            System.err.println("Error reading from CSV: " + e.getMessage());
        }
    }
}
