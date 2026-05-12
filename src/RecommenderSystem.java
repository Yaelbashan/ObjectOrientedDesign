import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/** Abstract generic recommender system. */
abstract class RecommenderSystem<T extends Item> {
    protected final Map<Integer, User> users;
    protected final Map<Integer, T> items;
    protected final List<Rating<T>> ratings;
    // TODO: add data structures to make the operation more efficient / simpler
    protected final Map<Integer, List<Rating<T>>> ratingsByUser; /// for profile-based and user-similarity-based recommenders
    protected final Map<Integer, List<Rating<T>>> ratingsByItem; /// for popularity-based recommender

    protected final int NUM_OF_RECOMMENDATIONS = 10;

    protected RecommenderSystem(Map<Integer, User> users,
                                Map<Integer, T> items,
                                List<Rating<T>> ratings) {
        this.users = users;
        this.items = items;
        this.ratings = ratings;
        // TODO: initialize additional data structures
        this.ratingsByUser = ratings.stream()
                .collect(groupingBy(Rating::getUserId));/// for profile-based and user-similarity-based recommenders

        this.ratingsByItem = ratings.stream()
                .collect(groupingBy(Rating::getItemId)); /// for popularity-based recommender

    }

    /** @return top‑10 recommended items for the given user, sorted best‑first. */
    public abstract List<T> recommendTop10(int userId);
}
