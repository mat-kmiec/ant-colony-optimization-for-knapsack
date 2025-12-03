package pl.antpack.core;

import pl.antpack.model.Item;
import pl.antpack.model.Knapsack;

import java.util.*;

public class Ant {

    private final List<Item> availableItems;
    private final double[] pheromones;
    private final double alpha;
    private final double beta;

    private final List<Item> selectedItems = new ArrayList<>();

    public Ant(List<Item> availableItems, double[] pheromones, double alpha, double beta) {
        this.availableItems = availableItems;
        this.pheromones = pheromones;
        this.alpha = alpha;
        this.beta = beta;
    }

    public Solution buildSolution(int capacity) {

        Knapsack knapsack = new Knapsack(capacity);
        selectedItems.clear();

        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < availableItems.size(); i++) {
            remaining.add(i);
        }

        Random r = new Random();

        while (!remaining.isEmpty()) {

            double sum = 0;
            double[] probability = new double[remaining.size()];

            for (int i = 0; i < remaining.size(); i++) {
                int idx = remaining.get(i);
                Item it = availableItems.get(idx);

                double pher = Math.pow(pheromones[idx], alpha);
                double heur = Math.pow((double) it.getValue() / it.getWeight(), beta);

                probability[i] = pher * heur;
                sum += probability[i];
            }

            if (sum == 0)
                break;

            double pick = r.nextDouble() * sum;
            double acc = 0;
            int chosen = -1;

            for (int i = 0; i < probability.length; i++) {
                acc += probability[i];
                if (acc >= pick) {
                    chosen = remaining.get(i);
                    break;
                }
            }

            if (chosen == -1)
                chosen = remaining.get(remaining.size() - 1);

            Item item = availableItems.get(chosen);

            if (knapsack.canAdd(item)) {
                selectedItems.add(item);
                knapsack.add(item);
            }

            remaining.remove((Integer) chosen);
        }

        int value = selectedItems.stream().mapToInt(Item::getValue).sum();
        return new Solution(selectedItems, value);
    }
}
