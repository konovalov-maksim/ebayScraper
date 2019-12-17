package core;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Scraper {

    private Condition condition;
    private int maxThreads = 3;
    private int itemsLimit = 1000;

    private Deque<String> unprocessed = new ConcurrentLinkedDeque<>();

    private List<Item> items;
    private long itemsCnt;
    private long soldItemsCnt;
    private double avgPrice;
    private double avgSoldPrice;

    public Scraper(List<String> queries, Condition condition) {
        this.condition = condition;
        unprocessed.addAll(queries);
    }

    private int threads;





    public enum Condition {
        NEW, USED, NOT_SPEC
    }

}
