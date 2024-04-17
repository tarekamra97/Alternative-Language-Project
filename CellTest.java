import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CellTest {
    private Map<String, String> baseRow;

    @Before
    public void setUp() {
        Cell.registry.clear(); // Clear the registry before each test
        baseRow = new HashMap<>();
        baseRow.put("oem", "TestOEM");
        baseRow.put("model", "TestModel");
        baseRow.put("launch_announced", "2020, Announced, 2020 Released, 2021");
        baseRow.put("body_weight", "150 g");
        baseRow.put("features_sensors", "GPS");
        // Add other necessary attributes
    }

    @Test
    public void testCleanWeight_ValidWeight() {
        Integer expectedWeight = 150;
        Integer actualWeight = Cell.cleanWeight("150 g");
        assertEquals(expectedWeight, actualWeight);
    }

    @Test
    public void testCleanWeight_InvalidWeight() {
        assertNull(Cell.cleanWeight("-"));
    }

    @Test
    public void testPhonesAnnouncedReleasedDifferentYears() {
        new Cell(1, baseRow); // Create a cell with differing announced and released years
        List<String> results = Cell.phonesAnnouncedReleasedDifferentYears();
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).contains("2020") && results.get(0).contains("2021"));
    }

    @Test
    public void testHighestAvgWeight_MultipleEntries() {
        baseRow.put("body_weight", "200 g");
        new Cell(1, baseRow);
        baseRow.put("oem", "TestOEM2");
        baseRow.put("body_weight", "100 g");
        new Cell(2, baseRow);

        Optional<Map.Entry<String, Double>> result = Cell.highestAvgWeight();
        assertTrue(result.isPresent());
        assertEquals("TestOEM", result.get().getKey());
        assertEquals(200.0, result.get().getValue(), 0.0);
    }

    @Test
    public void testCalculateWeightStats_SingleEntry() {
        new Cell(1, baseRow);
        Map<String, Double> stats = Cell.calculateWeightStats();
        assertEquals(150.0, stats.get("mean"), 0.0);
        assertEquals(150.0, stats.get("median"), 0.0);
        assertEquals(0.0, stats.get("standard_deviation"), 0.0);
    }
}
