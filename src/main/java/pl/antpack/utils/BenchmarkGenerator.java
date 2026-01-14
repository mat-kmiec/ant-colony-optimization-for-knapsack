package pl.antpack.utils;

import pl.antpack.model.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BenchmarkGenerator {

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