import core.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Debugger implements Logger, ItemsSeeker.ResultsLoadingListener {

    private static Debugger debugger;
    private ItemsSeeker itemsSeeker;
    private String appName;

    private static String[] testQueries = new String[]{"spinning reel shimano", "watches vostok"};

    public static void main(String[] args) throws IOException {
        debugger = new Debugger();
        debugger.runItemsSearching();
    }

    private void runItemsSearching() throws IOException {
        appName = Files.readAllLines(Paths.get("key.txt")).get(0);
        itemsSeeker = new ItemsSeeker(Arrays.asList(testQueries), appName, ItemsSeeker.Condition.ALL, this);
        itemsSeeker.setLogger(this);
        itemsSeeker.setMaxThreads(4);
        itemsSeeker.setItemsLimit(10);
//        itemsSeeker.start();
        Category.setAppName(appName);
        Category category = Category.findById("20081");
        for (String name : category.getChildren().keySet())
            System.out.println(name);
    }

    @Override
    public void log(String message) {
        String curTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(curTime + ": " + message);
    }

    @Override
    public void onResultReceived(Result result) {
        log(result.getQuery() + ": result received");
    }

    @Override
    public void onAllResultsReceived() {
        for (Result result : itemsSeeker.getResults()) {
            log(result.getQuery());
        }
    }

}
