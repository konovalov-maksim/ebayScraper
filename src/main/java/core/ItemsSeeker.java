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

    private final int MAX_ITEMS_PER_PAGE = 100; //limit from docs: https://developer.ebay.com/DevZone/finding/CallRef/findItemsByKeywords.html#Request.paginationInput
    private final int MAX_PAGE_NUMBER = 100; //limit from docs
    private int itemsLimit = MAX_ITEMS_PER_PAGE * MAX_PAGE_NUMBER; //default items limit: 10 000
    private int maxThreads = 5;
    private long timeout = 10000;
    private String categoryId = null;

    private LinkedHashMap<String, Result> results = new LinkedHashMap<>(); //Here stored all found results without duplicates
    private HashMap<String, Item> allItems = new HashMap<>(); //Here stored all found items and their IDs without duplicates

    public ItemsSeeker(List<String> queries, String appname, Condition condition, ResultsLoadingListener resultsLoadingListener) {
        unprocessed.addAll(queries.stream().distinct().collect(Collectors.toList()));
        this.APP_NAME = appname;
        this.condition = condition;
        this.resultsLoadingListener = resultsLoadingListener;
        initCallback();
    }

    public void start() {
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
        threads = 0;
        prepareUrl();
        isRunning = true;
        sendNewRequests();
    }

    public void stop() {
        isRunning = false;
        onFinish();
    }

    private void sendNewRequests() {
        while (isRunning && threads < maxThreads && !unprocessed.isEmpty()) {
            String query = unprocessed.pop();
            int page;
            int maxOnPage;
            Result result = results.get(query);
            if (result == null) {
                page = 1;
                maxOnPage = Math.min(itemsLimit, MAX_ITEMS_PER_PAGE);
            } else {
                page = result.getItemsCount() / MAX_ITEMS_PER_PAGE + 1;
                maxOnPage = Math.min(itemsLimit - result.getItemsCount(), MAX_ITEMS_PER_PAGE);
            }
            if (page > MAX_PAGE_NUMBER) {
                log(String.format("%-30s%s", query, " - all items found on " + MAX_PAGE_NUMBER + " pages"));
                return;
            }

            HttpUrl urlWithKeywords = preparedUrl.newBuilder()
                    .addQueryParameter("keywords", query)
                    .addQueryParameter("paginationInput.pageNumber", String.valueOf(page))
                    .addQueryParameter("paginationInput.entriesPerPage", String.valueOf(maxOnPage))
                    .build();
            Request request = new Request.Builder()
                    .url(urlWithKeywords)
                    .build();
            System.out.println(urlWithKeywords.url());
            threads++;
            client.newCall(request).enqueue(callback);
        }
    }

    private void initCallback() {
        callback = new Callback() {
            @Override
            public synchronized void onResponse(@NotNull Call call, @NotNull Response response) {
                if (!isRunning) return;
                threads--;
                //Adding results
                Result newResult = extractResult(response);
                Result oldResult = results.get(newResult.getQuery());
                Result result;
                log(String.format("%-30s%s", response.request().url().queryParameter("keywords"),
                        " - page " + response.request().url().queryParameter("paginationInput.pageNumber") + " loaded"));
                if (oldResult == null) {
                    results.put(newResult.getQuery(), newResult);
                    result = newResult;
                }
                else {
                    oldResult.getItems().addAll(newResult.getItems());
                    result = oldResult;
                }
                //Adding to queue again if needed to load remaining pagination pages
                if (result.getItemsCount() < result.getTotalEntries() && result.getItemsCount() < itemsLimit) {
                    unprocessed.add(result.getQuery());
                    result.setStatus(Result.Status.LOADING);
                } else {
                    result.setStatus(Result.Status.COMPLETED);
                    log(String.format("%-30s%s", result.getQuery(), " - all items found: " + result.getItemsCount()));
                }

                checkIsComplete();
                sendNewRequests();
                resultsLoadingListener.onResultReceived(result);
            }

            @Override
            public synchronized void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!isRunning) return;
                threads--;
                String query = call.request().url().queryParameter("keywords");
                Result result = new Result(query);
                result.setStatus(Result.Status.ERROR);
                results.putIfAbsent(result.getQuery(), result);
                log(String.format("%-30s%s", result.getQuery(),
                        " - page " + call.request().url().queryParameter("paginationInput.pageNumber") + ": loading error!"));
                checkIsComplete();
                sendNewRequests();
                resultsLoadingListener.onResultReceived(result);
            }
        };
    }

    private void checkIsComplete() {
        if (threads == 0 && unprocessed.isEmpty()) onFinish();
    }

    private void onFinish() {
        isRunning = false;
        client.connectionPool().evictAll();
        resultsLoadingListener.onAllResultsReceived();
    }

    //Extracting Result object from JSON response body
    private Result extractResult(Response response) {
        String query = response.request().url().queryParameter("keywords");
        Result result = new Result(query);
        try {
            String jsonData = response.body().string();
            JsonObject root = new Gson().fromJson(jsonData, JsonObject.class);
            //Status
            boolean isSuccess = root.getAsJsonArray("findItemsAdvancedResponse")
                    .get(0).getAsJsonObject()
                    .get("ack").getAsString()
                    .equals("Success");
            if (!isSuccess) {
                String errorMessage = root.getAsJsonArray("findItemsAdvancedResponse")
                        .get(0).getAsJsonObject()
                        .get("errorMessage").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("error").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("message").getAsJsonArray()
                        .get(0).getAsString();
                log("Query: " + query + " - error: " + errorMessage);
                return result;
            }
            //Total entries
            int totalEntries = root.getAsJsonArray("findItemsAdvancedResponse")
                    .get(0).getAsJsonObject()
                    .get("paginationOutput").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("totalEntries").getAsJsonArray()
                    .get(0).getAsInt();
            result.setTotalEntries(totalEntries);
            //Items
            JsonArray jsonItems = root.getAsJsonArray("findItemsAdvancedResponse")
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
//                    System.out.println(itemId);
                }
                allItems.put(itemId, item);
                result.addItem(item);
            }

            result.setIsSuccess(true);
        } catch (IOException | NullPointerException e) {
            log("Query: " + query + " - unable to get response body");
            e.printStackTrace();
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
//                .addQueryParameter("OPERATION-NAME", "findItemsByKeywords")
                .addQueryParameter("OPERATION-NAME", "findItemsAdvanced")
                .addQueryParameter("GLOBAL-ID", "EBAY-US")
                .addQueryParameter("SERVICE-VERSION", "1.13.0")
                .addQueryParameter("SECURITY-APPNAME", APP_NAME)
                .addQueryParameter("RESPONSE-DATA-FORMAT", "JSON")
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
        //Category filter
        if (categoryId != null && !categoryId.isEmpty()) urlBuilder.addQueryParameter("categoryId", categoryId);
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
        void onResultReceived(Result result);
        void onAllResultsReceived();
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
        if (itemsLimit > MAX_ITEMS_PER_PAGE * MAX_PAGE_NUMBER) this.itemsLimit = MAX_ITEMS_PER_PAGE * MAX_PAGE_NUMBER;
        else this.itemsLimit = itemsLimit;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public List<Result> getResults() {
        return new ArrayList<>(results.values());
    }

    public HashMap<String, Item> getAllItems() {
        return allItems;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
