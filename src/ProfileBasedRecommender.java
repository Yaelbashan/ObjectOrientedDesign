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
        // TODO: implement

        Set<Integer> ratedItemIds = ratingsByUser.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(Rating::getItemId)
                .collect(toSet());
        List<User> matchingUsers = getMatchingProfileUsers(userId);
        return matchingUsers.stream()
                .flatMap(user -> ratingsByUser.getOrDefault(user.getId(), Collections.emptyList()).stream())
                .filter(r -> r.getRating() >= 4)
                .filter(r -> !ratedItemIds.contains(r.getItemId()))
                .collect(groupingBy(Rating::getItemId, counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
                        .thenComparing(entry -> items.get(entry.getKey()).getName()))
                .limit(NUM_OF_RECOMMENDATIONS)
                .map(entry -> items.get(entry.getKey()))
                .collect(toList());
    }




    public List<User> getMatchingProfileUsers(int userId) {
        // TODO: implement

        User currentUser = users.get(userId);
        if (currentUser == null) return Collections.emptyList();

        return users.values().stream()
                .filter(u -> u.getId() != userId)
                .filter(u -> u.getGender().equals(currentUser.getGender()))
                .filter(u -> u.getAge() == currentUser.getAge())
                .collect(toList());
    }
}
