import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/** Similarity‑based recommender with bias correction. */
class SimilarityBasedRecommender<T extends Item> extends RecommenderSystem<T> {
    // TODO: add data structures to hold the global/item/user biases
    public SimilarityBasedRecommender(Map<Integer, User> users,
                                      Map<Integer, T> items,
                                      List<Rating<T>> ratings) {
        super(users, items, ratings);
        // TODO: initialize the data structures that hold the global/item/user biases
    }

    /** Dot‑product similarity; 0 if <10 shared items. */
    public double getSimilarity(int u1, int u2) {
        // TODO: implement
        return 0;
    }

    @Override public List<T> recommendTop10(int userId){
        // TODO: implement
        return null;
    }

    public void printGlobalBias() {
        // TODO: fix
        System.out.println("Global bias: " + String.format("%.2f", 0.0));
    }

    public void printItemBias(int itemId) {
        // TODO: fix
        System.out.println("Item bias for item " + itemId + ": " + String.format("%.2f", 0.0));
    }
    public void printUserBias(int userId) {
        // TODO: fix
        System.out.println("User bias for user " + userId + ": " + String.format("%.2f",0.0));
    }
}

