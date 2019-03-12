package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class MovieRecommenderTest {
    @Test
    public void testDataInfo() throws IOException, TasteException {
        //download movies.txt.gz from
        //    http://snap.stanford.edu/data/web-Movies.html
        MovieRecommender recommender = new MovieRecommender("/movies.txt.gz");
        long totalReviews = recommender.getTotalReviews();
        assertEquals(7911684, totalReviews);
        long totalProducts = recommender.getTotalProducts();
        assertEquals(253059, totalProducts);
        long totalUsers = recommender.getTotalUsers();
        assertEquals(889176, totalUsers);

        List<String> recommendations = recommender.getRecommendationsForUser("A141HP4LYPWMSR");
        assertThat(recommendations, hasItem("B0002O7Y8U"));
        assertThat(recommendations, hasItem("B00004CQTF"));
        assertThat(recommendations, hasItem("B000063W82"));

        System.out.println("\n\nTotal reviews: " +totalReviews);
        System.out.println("Total products: " +totalProducts);
        System.out.println("Total users: " +totalUsers);
        //System.out.println("Recommendations for user:\n" + recommendations);
    }

}
