package core;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ItemsSeeker implements Callback {

    private Logger logger;
    private OkHttpClient client;
    private HttpUrl preparedUrl;
    private final String BASE_URL = "https://svcs.ebay.com/services/search/FindingService/v1";
    private boolean isRunning = false;
    private int threads;

    private Deque<String> unprocessed = new ConcurrentLinkedDeque<>();
    private final String APP_NAME;
    private final Condition condition;

    private int maxThreads = 3;
    private int itemsLimit = 0;
    private long timeout = 8000;

    private List<Result> results = new ArrayList<>();

    public ItemsSeeker(List<String> queries, String appname, Condition condition) {
        unprocessed.addAll(queries.stream().distinct().collect(Collectors.toList()));
        this.APP_NAME = appname;
        this.condition = condition;
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
    }

    public void start() {
        isRunning = true;
        prepareUrl();
        callNewRequests();
    }

    private void callNewRequests() {
        while (isRunning && threads < maxThreads && !unprocessed.isEmpty()) {
            HttpUrl urlWithKeywords = preparedUrl.newBuilder()
                    .addQueryParameter("keywords", unprocessed.pop())
                    .build();
            Request request = new Request.Builder()
                    .url(urlWithKeywords)
                    .build();
            client.newCall(request).enqueue(this);
        }
    }

    @Override
    public synchronized void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        threads--;
        Result result = extractResult(response);

        results.add(result);
//        log(response.body().string());
    }

    @Override
    public synchronized void onFailure(@NotNull Call call, @NotNull IOException e) {
        threads--;
        Result result = new Result(call.request().header("keywords"));
        result.setSuccess(false);
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
//                .addQueryParameter("paginationInput.pageNumber", "1")
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

    private Result extractResult(Response response) {
        Result result = new Result(response.request().header("keywords"));
        result.setSuccess(true);
        return result;
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
}
