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
import java.util.*;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    private int tr = 0;
    private int tp = 0;
    private int tu = 0;
    private String cs = "UTF-8";
    private Map<String, Integer> hashProducts = new HashMap<String, Integer>();
    private Map<String, Integer> hashUsers = new HashMap<String, Integer>();
    private List<String> products = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private List<String> scores = new ArrayList<>();

    public MovieRecommender(String sp) {
        try{
            if (!(new File("reviews.cvs").exists()) && !(new File("hashProducts.csv").exists())
                    && !(new File("hashUsers.csv").exists()) && !(new File("totalElements.csv").exists())) {
                ReadModel(sp);
                hashProducts = getMap(products);
                hashUsers = getMap(users);
                /**Write reviews datamodel*/
                FileWriter fw = new FileWriter("reviews.csv", false);
                    for (int i = 0; i<products.size(); i++){
                        fw.write(hashUsers.get(users.get(i)) + "," + hashProducts.get(products.get(i)) + "," + scores.get(i) + "\n");
                    }
                fw.close();
                /**Write hashProducts datamodel*/
                fw = new FileWriter("hashProducts.csv", false);
                for (String key : hashProducts.keySet()) {
                    fw.write(key + "," + hashProducts.get(key) + "\n");
                }
                fw.close();
                /**Write hashUsers datamodel*/
                fw = new FileWriter("hashUsers.csv", false);
                for (String key : hashUsers.keySet()) {
                    fw.write(key + "," + hashUsers.get(key) + "\n");
                }
                fw.close();
                /**Write totalElements datamodel*/
                fw = new FileWriter("totalElements.csv", false);
                fw.write(tr + "," + tp + "," + tu);
                fw.close();
            }else{
                /**Read hasProducts datamodel*/
                File file = new File("hashProducts.csv");
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, cs);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null){
                    String[] split = line.split(",");
                    hashProducts.put(split[0], Integer.valueOf(split[1]));
                }
                br.close();
                /**Read hashUers datamodel*/
                file = new File("hashUsers.csv");
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, cs);
                br = new BufferedReader(isr);
                while ((line = br.readLine()) != null){
                    hashUsers.put(line.split(",")[0], Integer.valueOf(line.split(",")[1]));
                }
                br.close();
                /**Read totalElements datamodel*/
                file = new File("totalElements.csv");
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, cs);
                br = new BufferedReader(isr);
                line = br.readLine();
                tr = Integer.parseInt(line.split(",")[0]);
                tp = Integer.parseInt(line.split(",")[1]);
                tu = Integer.parseInt(line.split(",")[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ReadModel(String sp) throws IOException {
        //String currentDir = System.getProperty("user.dir");
        //System.out.println("Current directory: " + currentDir);
        File file = new File(sp);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr;
        if (sp.contains(".gz")) {
            GZIPInputStream GZInp = new GZIPInputStream(fis);
            isr = new InputStreamReader(GZInp, cs);
        }else{
            isr = new InputStreamReader(fis, cs);
        }
        BufferedReader br = new BufferedReader(isr);
        System.out.println("Buffer created");

        String line;
        //Unique lists
        Set<String> uProducts = new HashSet<>();
        Set<String> uUsers = new HashSet<>();
        while ((line = br.readLine()) != null) {
            if (line.contains("product/productId")){
                String movie = line.substring(19);
                products.add(movie);
                //System.out.println("Review found: " + movie);
                tr ++;
                if (!uProducts.contains(movie)){
                    //System.out.println("New movie found: " + movie);
                    uProducts.add(movie);
                    tp ++;
                }
                String user = br.readLine().substring(15);
                users.add(user);
                if (!uUsers.contains(user)){
                    //System.out.println("New user found: " + user);
                    uUsers.add(user);
                    tu ++;
                }
                while (true){
                    if ((line = br.readLine()).contains("review/score")) break;
                }
                String score = line.substring(14);
                //System.out.println("Score found: " + score);
                if (score.matches("[\\d]\\.[\\d]")) {
                    scores.add(score);
                }else{
                    System.out.println("Score with wrong format: " + score);
                    score = "0";
                    scores.add(score);
                }
            }
        }
        br.close();
    }

    public int getTotalReviews() {
        System.out.println("Returning total reviews: " + tr);
        return tr;
    }

    public int getTotalProducts() {
        System.out.println("Returning total movies: " + tp);
        return tp;
    }

    public int getTotalUsers() {
        System.out.println("Returning total users: " + tu);
        return tu;
    }

    public List<String> getRecommendationsForUser(String user) {
        try {
            DataModel model = new FileDataModel(new File("reviews.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List <RecommendedItem> recommendations = recommender.recommend(hashUsers.get(user), 10);
            List <String> keys = new ArrayList<>(hashProducts.keySet());
            List <Integer> values = new ArrayList<>(hashProducts.values());
            List <String> transRec = new ArrayList<>();
            for (RecommendedItem recommendation : recommendations) {
                //System.out.println(recommendation);
                System.out.println("Product recommended: " + keys.get(values.indexOf((int) recommendation.getItemID())));
                transRec.add(keys.get(values.indexOf((int) recommendation.getItemID())));
            }
            return transRec;
        } catch (IOException | TasteException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map getMap (List<String> array){
        Map<String, Integer> map = new HashMap();
        int id = 0;
        for (String element : array) {
            map.put(element, id);
            id++;
        }
        return map;
    }

    /*public List<String> getRecommendationsForUser(String user) throws IOException {
        br = new BufferedReader(isr);
        float minScore = 4;
        List<String> orgRev;
        List<String> simUs = new ArrayList<>();
        Set<String> userRec = new HashSet<>();
        String line;
        orgRev = compareUserRevs(user, minScore);
        for (String movie : orgRev) {
            br.reset();
            while ((line = br.readLine()) != null){
                if (line.contains("product/productId: " +movie)){
                    String nUser = br.readLine().substring(15);
                    br.readLine();
                    br.readLine();
                    float score = Float.parseFloat(br.readLine().substring(14));
                    if (score >= minScore) {
                        System.out.println("Coincidence found:" + nUser + " scored " + score + "to " + movie);
                        simUs.add(nUser);
                    }
                }
            }
        }
        for (String similar : simUs) {
            br.reset();
            userRec.addAll(compareUserRevs(similar, minScore));
        }
        userRec.removeAll(orgRev);
        br.close();
        return (List<String>) userRec;
    }

    private List<String> compareUserRevs(String user, float minScore) throws IOException {
        String line;
        List<String> coincidence = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if (line.contains("product/productId") && br.readLine().contains(user)){
                br.readLine();
                br.readLine();
                float score = Float.parseFloat(br.readLine().substring(14));
                String movie = line.substring(19);
                if (score >= minScore) {
                    System.out.println("Coincidence found: " + movie + " scored " +score);
                    coincidence.add(movie);
                }
            }
        }
        return coincidence;
    }*/
}
