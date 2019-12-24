package core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class Category {

    private static OkHttpClient client;
    private static Logger logger;
    private static Callback callback;
    private static HttpUrl preparedUrl;
    private static String APP_NAME;
    private final static String BASE_URL = "https://open.api.ebay.com/Shopping";

    public static Category findById(String caregoryId) {
        prepareUrl();
        client = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();

        HttpUrl urlWithCatId = preparedUrl.newBuilder()
                .addQueryParameter("CategoryID", caregoryId)
                .build();
        Request request = new Request.Builder()
                .url(urlWithCatId)
                .build();

        try {
            JsonObject root = new Gson().fromJson(client.newCall(request).execute().body().string(), JsonObject.class);
            //isSuccess
            boolean isSuccess = root.get("Ack").getAsString().equals("Success");
            if (!isSuccess) {
                String errorMessage = root.get("Errors").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("LongMessage").getAsString();
                log("Unable to read category info: " + errorMessage);
                return null;
            }
            JsonArray categories = root.get("CategoryArray").getAsJsonObject().get("Category").getAsJsonArray();
            String categoryName = categories.get(0).getAsJsonObject().get("CategoryName").getAsString();
            String parentId = categories.get(0).getAsJsonObject().get("CategoryParentID").getAsString();
            Category category = new Category(caregoryId, categoryName, parentId);
            for (int i = 1; i < categories.size(); i++) {
                String childName = categories.get(i).getAsJsonObject().get("CategoryName").getAsString();
                String childId = categories.get(i).getAsJsonObject().get("CategoryID").getAsString();
                category.addChild(childName, childId);
            }
            return category;
        } catch (IOException | NullPointerException e) {
            log("Unable to read category info: empty response body");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            log("Unable to read category info");
            e.printStackTrace();
            return null;
        } finally {
            client.connectionPool().evictAll();
        }
    }

    //Preparing URL with get parameters
    private static void prepareUrl() {
        HttpUrl httpUrl = HttpUrl.parse(BASE_URL);
        if (httpUrl == null) {
            log("Unable to detect base url");
            return;
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder()
                .addQueryParameter("callname", "GetCategoryInfo")
                .addQueryParameter("responseencoding", "JSON")
                .addQueryParameter("appid", APP_NAME)
                .addQueryParameter("siteid", "0")
                .addQueryParameter("version", "967")
                .addQueryParameter("IncludeSelector", "ChildCategories")
                ;
        preparedUrl = urlBuilder.build();
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

    private static void log(String message) {
        if (logger != null) logger.log(message);
    }

    private Category() {}

    private Category(String id, String name, String parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    //Instance fields and methods
    private String id;
    private String name;
    private String parentId;
    private LinkedHashMap<String, String> children = new LinkedHashMap<>();

    private void addChild(String name, String id) {
        children.put(name, id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LinkedHashMap<String, String> getChildren() {
        return children;
    }

    public String getParentId() {
        return parentId;
    }
}
