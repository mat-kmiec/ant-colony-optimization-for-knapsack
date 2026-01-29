package pl.antpack.core;

import pl.antpack.model.Item;
import pl.antpack.model.Knapsack;

import java.util.*;

/**
 * Represents an ant in the Ant Colony Optimization algorithm for solving the knapsack problem.
 * The ant selects items to maximize the total value of the knapsack while staying within the
 * knapsack's capacity. The selection process combines probabilistic influences of pheromone
 * levels and heuristic information (e.g., value-to-weight ratio).
 */
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

    /**
     * Builds a solution for the knapsack problem using a probabilistic approach influenced
     * by pheromone levels and heuristic information.
     *
     * The method iteratively selects items to add to the knapsack based on a combination of
     * pheromone levels and heuristic values (value-to-weight ratio of items). The selection
     * process continues until the knapsack reaches its capacity or no more suitable candidates
     * are available.
     *
     * @param capacity the capacity of the knapsack to be filled
     * @return a {@code Solution} containing the selected items and the total value
     */
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
