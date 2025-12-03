package pl.antpack.model;

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
