package pl.antpack.core;

import pl.antpack.model.Item;
import java.util.List;

/**
 *
 */
public class Solution {
    private final List<Item> items;
    private final int value;

    public Solution(List<Item> items, int value) {
        this.items = items;
        this.value = value;
    }

    public List<Item> getItems() {
        return items;
    }

    public int getValue() {
        return value;
    }
}
