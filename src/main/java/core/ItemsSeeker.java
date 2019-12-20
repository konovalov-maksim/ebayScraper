package core;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ItemsSeeker {

    private Condition condition;
    private int maxThreads = 3;
    private int itemsLimit = 60;

    private Logger logger;

    private Deque<String> unprocessed = new ConcurrentLinkedDeque<>();

    private List<Result> results;
    private long itemsCnt;
    private long soldItemsCnt;
    private double avgPrice;
    private double avgSoldPrice;

    public ItemsSeeker(List<String> queries, Condition condition) {
        this.condition = condition;
        unprocessed.addAll(queries);
    }

    private int threads;



    private void log(String message) {
        if (logger != null) logger.log(message);
    }

    public enum Condition {
        NEW, USED, NOT_SPEC
    }

}
