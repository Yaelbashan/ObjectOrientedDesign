import java.util.*;

import static java.util.stream.Collectors.*;

/** Popularity‑based recommender implementation. */
class PopularityBasedRecommender<T extends Item> extends RecommenderSystem<T> {
    private static final int POPULARITY_THRESHOLD = 100;

    public PopularityBasedRecommender(Map<Integer, User> users,
                                      Map<Integer, T> items,
                                      List<Rating<T>> ratings) {
        super(users, items, ratings);
    }

    @Override
    public List<T> recommendTop10(int userId) {
        // 1. Get IDs of items already rated by this user
        Set<Integer> ratedItemIds = ratingsByUser.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(Rating::getItemId)
                .collect(toSet());

        return items.values().stream()
                // 2. Filter out items the user has already seen/rated [cite: 58]
                .filter(item -> !ratedItemIds.contains(item.getId()))
                // 3. Filter out items with fewer than 100 ratings [cite: 58]
                .filter(item -> getItemRatingsCount(item.getId()) >= POPULARITY_THRESHOLD)
                // 4. Sort by average rating in descending order [cite: 60]
                .sorted(Comparator.comparingDouble((T item) -> getItemAverageRating(item.getId()))
                        .reversed()
                        // Tie-break: Prefer items with a higher count of ratings [cite: 60]
                        .thenComparing(Comparator.comparingInt((T item) -> getItemRatingsCount(item.getId())).reversed())
                        // Further tie-break: Sort by item name alphabetically [cite: 61]
                        .thenComparing(Item::getName))
                // 5. Take the top 10 recommendations [cite: 52]
                .limit(NUM_OF_RECOMMENDATIONS)
                .collect(toList());
    }

    public double getItemAverageRating(int itemId) {
        List<Rating<T>> itemRatings = ratingsByItem.getOrDefault(itemId, Collections.emptyList());

        // Calculate the average of all ratings for this item [cite: 59, 63]
        return itemRatings.stream()
                .mapToDouble(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    public int getItemRatingsCount(int itemId) {
        // Return the total count of ratings for this item [cite: 64]
        return ratingsByItem.getOrDefault(itemId, Collections.emptyList()).size();
    }
}