package pl.antpack.model;

/**
 * The Knapsack class represents a container with a fixed capacity that can hold items.
 * It provides functionality to determine if an item can be added without exceeding
 * the capacity and to keep track of the used weight of the knapsack.
 */
public class Knapsack {
    private final int capacity;
    private int usedWeight = 0;

    public Knapsack(int capacity) {
        this.capacity = capacity;
    }

    public boolean canAdd(Item item) {
        return usedWeight + item.getWeight() <= capacity;
    }

    public void add(Item item) {
        usedWeight += item.getWeight();
    }

    public int getUsedWeight() {
        return usedWeight;
    }

    public int getCapacity() {
        return capacity;
    }
}
