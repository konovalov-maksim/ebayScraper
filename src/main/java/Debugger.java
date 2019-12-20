import core.ItemsSeeker;
import core.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Debugger implements Logger, ItemsSeeker.ResultsLoadingListener {

    private static Debugger debugger;
    private ItemsSeeker itemsSeeker;

    private static String[] testQueries = new String[]{"spinning reel shimano", "watches"};

    public static void main(String[] args) throws IOException {
        debugger = new Debugger();
        debugger.run();
    }

    private void run() throws IOException {
        String key = Files.readAllLines(Paths.get("key.txt")).get(0);
        itemsSeeker = new ItemsSeeker(Arrays.asList(testQueries), key, ItemsSeeker.Condition.ALL, this);
        itemsSeeker.setLogger(this);
        itemsSeeker.setMaxThreads(4);
        itemsSeeker.setItemsLimit(10);
        itemsSeeker.start();
    }


    @Override
    public void log(String message) {
        String curTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(curTime + ": " + message);
    }

    @Override
    public void onResult() {

    }

    @Override
    public void onComplete() {

    }
}
