import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/** Similarity-based recommender with bias correction. */
class SimilarityBasedRecommender<T extends Item> extends RecommenderSystem<T> {

    private final double globalBias;
    private final Map<Integer, Double> itemBiases;
    private final Map<Integer, Double> userBiases;
    private final Map<Integer, Map<Integer, Double>> biasFreeRatingsByUser;
    private final Map<Integer, Long> itemRatingsCount;

    public SimilarityBasedRecommender(Map<Integer, User> users,
                                      Map<Integer, T> items,
                                      List<Rating<T>> ratings) {
        super(users, items, ratings);

        // Step 1: Global bias calculation
        this.globalBias = ratings.stream()
                .mapToDouble(Rating::getRating)
                .average()
                .orElse(0.0);

        // Step 2: Item bias calculation
        this.itemBiases = ratings.stream()
                .collect(groupingBy(Rating::getItemId,
                        averagingDouble(r -> r.getRating() - globalBias)));

        // Step 3: User bias calculation
        this.userBiases = ratings.stream()
                .collect(groupingBy(Rating::getUserId,
                        averagingDouble(r -> r.getRating() - globalBias - itemBiases.getOrDefault(r.getItemId(), 0.0))));

        // Helper structure for bias-free ratings
        this.biasFreeRatingsByUser = ratings.stream()
                .collect(groupingBy(Rating::getUserId,
                        toMap(Rating::getItemId,
                                r -> r.getRating() - globalBias
                                        - itemBiases.getOrDefault(r.getItemId(), 0.0)
                                        - userBiases.getOrDefault(r.getUserId(), 0.0))));

        // Total rating count per item (used for tie-breaking)
        this.itemRatingsCount = ratings.stream()
                .collect(groupingBy(Rating::getItemId, counting()));
    }

    /** Dot-product similarity; returns 0 if fewer than 10 items are shared. */
    public double getSimilarity(int u1, int u2) {
        Map<Integer, Double> ratings1 = biasFreeRatingsByUser.getOrDefault(u1, Map.of());
        Map<Integer, Double> ratings2 = biasFreeRatingsByUser.getOrDefault(u2, Map.of());

        Set<Integer> sharedItems = ratings1.keySet().stream()
                .filter(ratings2::containsKey)
                .collect(toSet());

        if (sharedItems.size() < 10) return 0.0;

        return sharedItems.stream()
                .mapToDouble(itemId -> ratings1.get(itemId) * ratings2.get(itemId))
                .sum();
    }

    @Override
    public List<T> recommendTop10(int userId) {
        // Find similarity scores with all other users
        Map<Integer, Double> similarities = users.keySet().stream()
                .filter(id -> id != userId)
                .collect(toMap(Function.identity(), id -> getSimilarity(userId, id)));

        // Select the top 10 most similar users with positive similarity
        List<Integer> top10SimilarUsers = similarities.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(toList());

        Set<Integer> userRatedItems = biasFreeRatingsByUser.getOrDefault(userId, Map.of()).keySet();

        // Identify items rated by at least 5 similar users that the target user hasn't rated
        List<Integer> candidateItemIds = top10SimilarUsers.stream()
                .flatMap(simId -> biasFreeRatingsByUser.getOrDefault(simId, Map.of()).keySet().stream())
                .filter(itemId -> !userRatedItems.contains(itemId))
                .collect(groupingBy(Function.identity(), counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() >= 5)
                .map(Map.Entry::getKey)
                .collect(toList());

        // Predict ratings and
sort the top 10 recommendations
        return candidateItemIds.stream()
                .map(itemId -> Map.entry(items.get(itemId), predictRating(userId, itemId, top10SimilarUsers, similarities)))
                .sorted(Map.Entry.<T, Double>comparingByValue().reversed()
                        .thenComparing(e -> itemRatingsCount.getOrDefault(e.getKey().getId(), 0L), Comparator.reverseOrder())
                        .thenComparing(e -> e.getKey().getName()))
                .limit(NUM_OF_RECOMMENDATIONS)
                .map(Map.Entry::getKey)
                .collect(toList());
    }

    /** Predicts a rating using a weighted average of similar users' bias-free ratings. */
    private double predictRating(int userId, int itemId, List<Integer> similarUsers, Map<Integer, Double> similarities) {
        double weightedSum = similarUsers.stream()
                .filter(simId -> biasFreeRatingsByUser.getOrDefault(simId, Map.of()).containsKey(itemId))
                .mapToDouble(simId -> similarities.get(simId) * biasFreeRatingsByUser.get(simId).get(itemId))
                .sum();

        double simSum = similarUsers.stream()
                .filter(simId -> biasFreeRatingsByUser.getOrDefault(simId, Map.of()).containsKey(itemId))
                .mapToDouble(similarities::get)
                .sum();

        double predictedBiasFree = (simSum == 0) ? 0 : weightedSum / simSum;

        // Add biases back to get the final predicted rating
        return predictedBiasFree + globalBias + getItemBias(itemId) + getUserBias(userId);
    }

    public double getGlobalBias() { return globalBias; }
    public double getItemBias(int itemId) { return itemBiases.getOrDefault(itemId, 0.0); }
    public double getUserBias(int userId) { return userBiases.getOrDefault(userId, 0.0); }
}