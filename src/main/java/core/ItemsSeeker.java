package core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ItemsSeeker {

    private Logger logger;
    private OkHttpClient client;
    private Callback callback;
    private HttpUrl preparedUrl;
    private ResultsLoadingListener resultsLoadingListener;

    private final String BASE_URL = "https://svcs.ebay.com/services/search/FindingService/v1";
    private boolean isRunning = false;
    private int threads;

    private Deque<String> unprocessed = new ConcurrentLinkedDeque<>();
    private final String APP_NAME;
    private final Condition condition;

    private int maxThreads = 3;
    private int itemsLimit = 0;
    private long timeout = 8000;

    private List<Result> results = new ArrayList<>(); //Here stored all found results without duplicates
    private HashMap<String, Item> allItems = new HashMap<>(); //Here stored all found items and their IDs without duplicates

    public ItemsSeeker(List<String> queries, String appname, Condition condition, ResultsLoadingListener resultsLoadingListener) {
        unprocessed.addAll(queries.stream().distinct().collect(Collectors.toList()));
        this.APP_NAME = appname;
        this.condition = condition;
        this.resultsLoadingListener = resultsLoadingListener;
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
        initCallback();
    }

    public void start() {
        isRunning = true;
        prepareUrl();
        sendNewRequests();
    }

    public void stop() {
        isRunning = false;
        resultsLoadingListener.onComplete();
    }

    private void sendNewRequests() {
        while (isRunning && threads < maxThreads && !unprocessed.isEmpty()) {
            HttpUrl urlWithKeywords = preparedUrl.newBuilder()
                    .addQueryParameter("keywords", unprocessed.pop())
                    .build();
            Request request = new Request.Builder()
                    .url(urlWithKeywords)
                    .build();
            threads++;
            client.newCall(request).enqueue(callback);
        }
    }

    private void initCallback() {
        callback = new Callback() {
            @Override
            public synchronized void onResponse(@NotNull Call call, @NotNull Response response) {
                threads--;
                Result result = extractResult(response);
                results.add(result);
                checkIsComplete();
            }

            @Override
            public synchronized void onFailure(@NotNull Call call, @NotNull IOException e) {
                threads--;
                Result result = new Result(call.request().header("keywords"));
                result.setSuccess(false);
                checkIsComplete();
            }
        };
    }

    private void checkIsComplete() {
        if (threads == 0 && unprocessed.isEmpty()) resultsLoadingListener.onComplete();
    }

    //Extracting Result object from JSON response body
    private Result extractResult(Response response) {
        String query = response.request().url().queryParameter("keywords");
        Result result = new Result(query);
        try {
            String jsonData = response.body().string();
            JsonObject root = new Gson().fromJson(jsonData, JsonObject.class);
            //Status
            boolean isSuccess = root.getAsJsonArray("findItemsByKeywordsResponse")
                    .get(0).getAsJsonObject()
                    .get("ack").getAsString()
                    .equals("Success");
            if (!isSuccess) {
                log("Query: " + query + " - incorrect request");
                return result;
            }
            //Total entries
            int totalEntries = root.getAsJsonArray("findItemsByKeywordsResponse")
                    .get(0).getAsJsonObject()
                    .get("paginationOutput").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("totalEntries").getAsJsonArray()
                    .get(0).getAsInt();
            result.setTotalEntries(totalEntries);
            //Items
            JsonArray jsonItems = root.getAsJsonArray("findItemsByKeywordsResponse")
                    .get(0).getAsJsonObject()
                    .get("searchResult").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("item").getAsJsonArray();
            for (JsonElement jsonItem : jsonItems) {
                String itemId = jsonItem.getAsJsonObject().get("itemId").getAsString();
                //Checking if item already was found, it's need to have no duplicates
                Item item = allItems.get(itemId);
                if (item == null) {
                    double price = jsonItem.getAsJsonObject()
                            .get("sellingStatus").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("currentPrice").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("__value__").getAsDouble();
                    item = new Item(itemId, price);
                }
                allItems.put(itemId, item);
                result.addItem(item);
            }

            result.setSuccess(true);
        } catch (IOException | NullPointerException e) {
            log("Query: " + query + " - unable to get response body");
        } catch (Exception e) {
            log("Query: " + query + " - unable to process result");
            e.printStackTrace();
        }
        return result;
    }

    //Preparing URL with get parameters
    private void prepareUrl() {
        HttpUrl httpUrl = HttpUrl.parse(BASE_URL);
        if (httpUrl == null) {
            log("Unable to detect base url");
            return;
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder()
                .addQueryParameter("OPERATION-NAME", "findItemsByKeywords")
                .addQueryParameter("SERVICE-VERSION", "1.0.0")
                .addQueryParameter("SECURITY-APPNAME", APP_NAME)
                .addQueryParameter("RESPONSE-DATA-FORMAT", "JSON")
                .addQueryParameter("paginationInput.entriesPerPage", String.valueOf(itemsLimit))
                ;

        //Condition items filter. Docs - https://developer.ebay.com/DevZone/finding/CallRef/types/ItemFilterType.html
        if (condition.equals(Condition.NEW))  {
            urlBuilder.addQueryParameter("itemFilter(0).name", "Condition")
                    .addQueryParameter("itemFilter(0).value(0)", "1000") //New
                    .addQueryParameter("itemFilter(0).value(1)", "1500") //New other (see details)
                    .addQueryParameter("itemFilter(0).value(2)", "1750"); //New with defects
        } else if (condition.equals(Condition.USED)) {
            urlBuilder.addQueryParameter("itemFilter(0).name", "Condition")
                    .addQueryParameter("itemFilter(0).value(0)", "2000") //Manufacturer refurbished
                    .addQueryParameter("itemFilter(0).value(1)", "2500") //Seller refurbished
                    .addQueryParameter("itemFilter(0).value(2)", "3000") //Used
                    .addQueryParameter("itemFilter(0).value(3)", "4000") //Very Good
                    .addQueryParameter("itemFilter(0).value(4)", "5000") //Good
                    .addQueryParameter("itemFilter(0).value(5)", "6000") //Acceptable
                    .addQueryParameter("itemFilter(0).value(6)", "7000"); //For parts or not working
        }
       preparedUrl = urlBuilder.build();
    }

    private void log(String message) {
        if (logger != null) logger.log(message);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public enum Condition {
        NEW, USED, ALL
    }

    public interface ResultsLoadingListener {
        void onResult();
        void onComplete();
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getItemsLimit() {
        return itemsLimit;
    }

    public void setItemsLimit(int itemsLimit) {
        this.itemsLimit = itemsLimit;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public List<Result> getResults() {
        return results;
    }

    public HashMap<String, Item> getAllItems() {
        return allItems;
    }
}
