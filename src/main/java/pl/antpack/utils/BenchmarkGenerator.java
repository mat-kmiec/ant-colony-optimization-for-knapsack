package pl.antpack.utils;

import pl.antpack.model.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BenchmarkGenerator {

    /**
     * Generates a "hard knapsack" problem instance based on the specified parameters.
     * The method creates a list of items with randomized weights and values,
     * calculates the total weight of the items, and derives the knapsack capacity
     * using the specified capacity factor.
     *
     * @param itemCount the number of items to include in the problem instance
     * @param capacityFactor the factor (as a percentage) used to determine the knapsack capacity
     *                       relative to the total weight of all items
     * @return a {@code ProblemInstance} containing the generated list of items and
     *         the calculated knapsack capacity
     */
    public static ProblemInstance generateHardProblem(int itemCount, int capacityFactor) {
        List<Item> items = new ArrayList<>();
        Random random = new Random(12345);

        int totalWeight = 0;

        for (int i = 0; i < itemCount; i++) {
            int weight = 10 + random.nextInt(90);
            int value = weight + 10;

            items.add(new Item(i, weight, value));
            totalWeight += weight;
        }
        int capacity = (int) (totalWeight * (capacityFactor / 100.0));

        return new ProblemInstance(items, capacity);
    }

    public record ProblemInstance(List<Item> items, int capacity) {}
}