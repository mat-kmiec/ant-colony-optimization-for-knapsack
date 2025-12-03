package pl.antpack.model;

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
