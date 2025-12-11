package pl.antpack.model;

import java.util.List;

public record KnapsackProblem(int capacity, List<Item> items) {

    public KnapsackProblem {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }
    }
}
