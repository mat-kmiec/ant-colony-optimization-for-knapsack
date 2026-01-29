package pl.antpack.model;

/**
 * Represents an item with a unique identifier, weight, and value.
 * Items can be used in scenarios such as optimization problems, including
 * the knapsack problem, where selecting items with consideration of their
 * weight and value is required.
 */
public class Item {
    private final int id;
    private final int weight;
    private final int value;

    public Item(int id, int weight, int value) {
        this.id = id;
        this.weight = weight;
        this.value = value;
    }

    public int getId() { return id; }
    public int getWeight() { return weight; }
    public int getValue() { return value; }
}
