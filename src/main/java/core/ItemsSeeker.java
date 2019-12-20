package core;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ItemsSeeker {

    private Logger logger;
    private OkHttpClient client;
    private final String BASE_URL = "https://svcs.ebay.com/services/search/FindingService/v1";
    private final String appName;

    private Condition condition = Condition.NOT_SPEC;

    private Deque<String> unprocessed = new ConcurrentLinkedDeque<>();

    private List<Result> results;
    private long itemsCnt;
    private long soldItemsCnt;
    private double avgPrice;
    private double avgSoldPrice;

    public ItemsSeeker(List<String> queries, Condition condition, String appname) {
        this.condition = condition;
        unprocessed.addAll(queries);
        this.appName = appname;
    }

    private int threads;



    private Headers.Builder baseHeadersBuilder() {
        return new Headers.Builder()
                .add("Host", "betslipapi.isppro.net")
                ;
    }

    private void log(String message) {
        if (logger != null) logger.log(message);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public enum Condition {
        NEW, USED, NOT_SPEC
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

}
