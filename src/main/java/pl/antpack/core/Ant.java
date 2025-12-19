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
            List<Integer> candidates = new ArrayList<>();
            for (Integer idx : remaining) {
                if (knapsack.canAdd(availableItems.get(idx))) {
                    candidates.add(idx);
                }
            }

            if (candidates.isEmpty()) {
                break;
            }

            double sum = 0;
            double[] probabilities = new double[candidates.size()];

            for (int i = 0; i < candidates.size(); i++) {
                int idx = candidates.get(i);
                Item item = availableItems.get(idx);

                double tau = pheromones[idx];
                double eta = (double) item.getValue() / item.getWeight();

                if (tau <= 0) tau = 0.0001;

                double p = Math.pow(tau, alpha) * Math.pow(eta, beta);
                probabilities[i] = p;
                sum += p;
            }

            int selectedIdxInCandidates = -1;

            if (sum == 0) {
                selectedIdxInCandidates = r.nextInt(candidates.size());
            } else {
                double pick = r.nextDouble() * sum;
                double current = 0;
                for (int i = 0; i < probabilities.length; i++) {
                    current += probabilities[i];
                    if (current >= pick) {
                        selectedIdxInCandidates = i;
                        break;
                    }
                }
            }

            if (selectedIdxInCandidates == -1) {
                selectedIdxInCandidates = candidates.size() - 1;
            }

            Integer actualItemIndex = candidates.get(selectedIdxInCandidates);
            Item itemToAdd = availableItems.get(actualItemIndex);

            knapsack.add(itemToAdd);
            selectedItems.add(itemToAdd);

            remaining.remove(actualItemIndex);
        }

        int value = selectedItems.stream().mapToInt(Item::getValue).sum();
        return new Solution(selectedItems, value);
    }
}
