import java.util.*;
import static java.util.stream.Collectors.*;

/** Profile‑based recommender implementation. */
class ProfileBasedRecommender<T extends Item> extends RecommenderSystem<T> {

    public ProfileBasedRecommender(Map<Integer, User> users,
                                   Map<Integer, T> items,
                                   List<Rating<T>> ratings) {
        super(users, items, ratings);
    }

    @Override
    public List<T> recommendTop10(int userId) {
        User targetUser = users.get(userId);
        if (targetUser == null) return Collections.emptyList();

        // 1. Identify matching profile users
        Set<Integer> matchingUserIds = getMatchingProfileUsers(userId).stream()
                .map(User::getId)
                .collect(toSet());

        // 2. Identify items already rated by the user
        Set<Integer> userRatedItems = ratings.stream()
                .filter(r -> r.getUserId() == userId)
                .map(Rating::getItemId)
                .collect(toSet());

        // 3. Process ratings from the matching group only
        return ratings.stream()
                .filter(r -> matchingUserIds.contains(r.getUserId())) // Only from matching users
                .filter(r -> !userRatedItems.contains(r.getItemId())) // Only items not yet rated by the user
                .collect(groupingBy(Rating::getItemId))
                .entrySet().stream()
                .filter(e -> e.getValue().size() >= 5) // Requirement: at least 5 ratings in this group [cite: 74]
                .map(e -> {
                    double avg = e.getValue().stream().mapToDouble(Rating::getRating).average().orElse(0.0);
                    int count = e.getValue().size();
                    T item = items.get(e.getKey());
                    return new ProfileCandidate<>(item, avg, count);
                })
                .sorted(Comparator.<ProfileCandidate<T>>comparingDouble(c -> c.avgRating).reversed() // Descending average [cite: 76]
                        .thenComparing(Comparator.<ProfileCandidate<T>>comparingInt(c -> c.count).reversed()) // Descending count [cite: 77]
                        .thenComparing(c -> c.item.getName())) // Ascending name [cite: 77]
                .limit(NUM_OF_RECOMMENDATIONS)
                .map(c -> c.item)
                .collect(toList());
    }

    /** * Returns a list of users with the same gender and an age difference of up to 5 years.
     * Sorting by ID was removed to maintain the original file order.
     */
    public List<User> getMatchingProfileUsers(int userId) {
        User targetUser = users.get(userId);
        if (targetUser == null) return Collections.emptyList();

        return users.values().stream()
                .filter(u -> u.getId() != userId) // Not the user themselves
                .filter(u -> u.getGender().equals(targetUser.getGender())) // Same gender [cite: 70]
                .filter(u -> Math.abs(u.getAge() - targetUser.getAge()) <= 5) // Age difference up to 5 [cite: 71]
                .collect(toList());
    }

    /** Internal helper class for managing recommendation candidate data */
    private static class ProfileCandidate<T extends Item> {
        T item;
        double avgRating;
        int count;

        ProfileCandidate(T item, double avg, int c) {
            this.item = item;
            this.avgRating = avg;
            this.count = c;
        }
    }
}