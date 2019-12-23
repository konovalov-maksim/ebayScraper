package core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class ItemsLoader {

    private Logger logger;
    private OkHttpClient client;
    private Callback callback;
    private HttpUrl preparedUrl;
    private final String APP_NAME;

    private final String BASE_URL = "https://open.api.ebay.com/shopping";
    private boolean isRunning = false;
    private int threads;

    private ItemsLoadingListener itemsLoadingListener;
    private Deque<String> unprocessed = new ConcurrentLinkedDeque<>(); //IDs of unprocessed items
    private HashMap<String, Item> items; //all items, format <itemId, item>

    private final int ITEMS_PER_REQUEST = 20;
    private int maxThreads = 5;
    private long timeout = 10000;

    public ItemsLoader(HashMap<String, Item> items, String APP_NAME, ItemsLoadingListener itemsLoadingListener) {
        this.APP_NAME = APP_NAME;
        this.items = items;
        this.itemsLoadingListener = itemsLoadingListener;
        unprocessed.addAll(items.keySet());
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
        initCallback();
    }

    public void start() {
        threads = 0;
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
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
            HttpUrl urlItemsIds = preparedUrl.newBuilder()
                    .addQueryParameter("ItemID", pullItemsIds())
                    .build();
            Request request = new Request.Builder()
                    .url(urlItemsIds)
                    .build();
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
                extractDataIntoItems(response);
                checkIsComplete();
                sendNewRequests();
                itemsLoadingListener.onItemReceived();
            }

            @Override
            public synchronized void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!isRunning) return;
                threads--;
                Result result = new Result(call.request().header("keywords"));
                result.setIsSuccess(false);
                checkIsComplete();
                sendNewRequests();
                itemsLoadingListener.onItemReceived();
            }
        };
    }

    private void checkIsComplete() {
        if (threads == 0 && unprocessed.isEmpty())onFinish();
    }

    private void onFinish() {
        isRunning = false;
        client.connectionPool().evictAll();
        itemsLoadingListener.onAllItemsReceived();
    }

    //Extracting data from JSON into items
    private void extractDataIntoItems(Response response) {
        try {
            //Extracting Status
            JsonObject root = new Gson().fromJson(response.body().string(), JsonObject.class);
            boolean isSuccess = root.get("Ack").getAsString().equals("Success");
            if (!isSuccess) {
                String errorMessage = root.get("Errors").getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .get("LongMessage").getAsString();
                log("Unable to extract items data: " + errorMessage);
                return;
            }
            //Extracting items data
            JsonArray jsonItems = root.get("Item").getAsJsonArray();
            for (JsonElement jsonItem : jsonItems) {
                String itemId = jsonItem.getAsJsonObject().get("ItemID").getAsString();
                Item item = items.get(itemId);
                if (item == null) {
                    log("Item " + itemId + "not found in results");
                    continue;
                }
                int soldCount = jsonItem.getAsJsonObject().get("QuantitySold").getAsInt();
                item.setSoldCount(soldCount);
                item.setInfoFull(true);
            }
        } catch (IOException | NullPointerException e) {
            log("Unable to extract items data: empty response body");
        } catch (Exception e) {
            log("Unable to extract items data");
        }
    }

    //Preparing URL with get parameters
    private void prepareUrl() {
        HttpUrl httpUrl = HttpUrl.parse(BASE_URL);
        if (httpUrl == null) {
            log("Unable to detect base url");
            return;
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder()
                .addQueryParameter("callname", "GetMultipleItems")
                .addQueryParameter("responseencoding", "JSON")
                .addQueryParameter("appid", APP_NAME)
                .addQueryParameter("siteid", "0")
                .addQueryParameter("version", "967")
                .addQueryParameter("IncludeSelector", "Details")
                ;

        preparedUrl = urlBuilder.build();
    }

    //Obtaining unprocessed items IDs and them concatenation with comma separation
    private String pullItemsIds() {
        if (unprocessed.isEmpty()) return "";
        StringBuilder ids = new StringBuilder(unprocessed.pop());
        int idsCount = 1;
        while (!unprocessed.isEmpty() && idsCount < ITEMS_PER_REQUEST) {
            ids.append(",").append(unprocessed.pop());
            idsCount++;
        }
        return ids.toString();
    }

    private void log(String message) {
        if (logger != null) logger.log(message);
    }

    public interface ItemsLoadingListener {
        void onItemReceived();
        void onAllItemsReceived();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
