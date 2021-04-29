package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class MovieRecommender {
    private String path;

    Hashtable<String, Integer> users = new Hashtable<String, Integer>();
    Hashtable<String, Integer> products = new Hashtable<String, Integer>();
    Hashtable<Integer, String> productsById = new Hashtable<Integer, String>();

    private Integer totalUsers = 0;
    private int totalReviews = 0;
    private Integer totalProducts = 0;



    public MovieRecommender(String path) throws IOException {
        this.path = path;
        processFile();
    }
    
    public int getTotalReviews(){
        return this.totalReviews;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTotalProducts() {
        return this.totalProducts;
    }

    public int getTotalUsers() {
        return this.totalUsers;
    }


    public List<String> getRecommendationsForUser(String userId) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<String> recommendations = new ArrayList<String>();

        for (RecommendedItem recommendation : recommender.recommend(users.get(userId), 3)) {
            recommendations.add(productsById.get((int)(recommendation.getItemID())));
        }

        return recommendations;
    }

    public void processFile() throws IOException {
        System.out.println(System.getProperty("user.dir"));
        FileInputStream file = new FileInputStream(this.path);
        GZIPInputStream gzipInput = new GZIPInputStream(file);
        Reader decoder = new InputStreamReader(gzipInput);
        BufferedReader reader = new BufferedReader(decoder);

        String usersSearch = "review/userId";
        String scoreSearch = "review/score";
        String productSearch = "product/productId";


        String currentLine = reader.readLine();

        String userId = "";
        String reviewId = "";
        String productId = "";

        while (currentLine != null) {
            System.out.println(currentLine);
            boolean isUserId = currentLine.contains(usersSearch);
            boolean isScore = currentLine.contains(scoreSearch);
            boolean isProductId = currentLine.contains(productSearch);

            if (isUserId) {
                userId = currentLine.split(" ")[1];
                if (users.get(userId) == null) {
                    this.totalUsers++;
                    users.put(userId, this.totalUsers);
                }
            } else if (isScore){
                reviewId = currentLine.split(" ")[1];
                this.totalReviews++;
            } else if (isProductId){
                productId = currentLine.split(" ")[1];

                if (products.get(productId) == null) {
                    this.totalProducts++;
                    products.put(productId, this.totalProducts);
                    productsById.put(this.totalProducts, productId);
                }
            }

            currentLine = reader.readLine();
        }

        reader.close();
    }
}
