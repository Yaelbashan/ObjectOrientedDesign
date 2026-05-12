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
        // TODO: implement
        Set<Integer> ratedItemIds = ratingsByUser.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(Rating::getItemId)
                .collect(toSet());

        return items.values().stream()
                // 2. מסננים פריטים שהמשתמש כבר ראה
                .filter(item -> !ratedItemIds.contains(item.getId()))
                // 3. מסננים פריטים שאין להם מספיק דירוגים (לפחות 100)
                .filter(item -> getItemRatingsCount(item.getId()) >= POPULARITY_THRESHOLD)
                // 4. ממיינים לפי הציון הממוצע מהגבוה לנמוך
                .sorted(Comparator.comparingDouble((T item) -> getItemAverageRating(item.getId()))
                        .reversed()
                        // במקרה של שוויון בציון, העדפה למי שיש לו יותר דירוגים
                        .thenComparing(Comparator.comparingInt((T item) -> getItemRatingsCount(item.getId())).reversed())
                        // שוויון נוסף? מיון לפי שם
                        .thenComparing(Item::getName))
                // 5. לוקחים את ה-10 הראשונים
                .limit(NUM_OF_RECOMMENDATIONS)
                .collect(toList());
    }

    public double getItemAverageRating(int itemId) {
        // TODO: implement
        List<Rating<T>> itemRatings = ratingsByItem.getOrDefault(itemId, Collections.emptyList());

        return itemRatings.stream()
                .mapToDouble(Rating::getRating)
                .average()
                .orElse(0.0);
    }
    public int getItemRatingsCount(int itemId) {
        // TODO: implement
        return ratingsByItem.getOrDefault(itemId, Collections.emptyList()).size();

    }

}

