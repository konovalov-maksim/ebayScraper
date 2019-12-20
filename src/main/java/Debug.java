import core.ItemsSeeker;
import core.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Debug implements Logger {

    private static ItemsSeeker itemsSeeker;

    private static String[] testQueries = new String[]{"spinning reel shimano", "watches"};

    public static void main(String[] args) throws IOException {
        String key = Files.readAllLines(Paths.get("key.txt")).get(0);
        itemsSeeker = new ItemsSeeker(Arrays.asList(testQueries), ItemsSeeker.Condition.NOT_SPEC, key);
    }


    @Override
    public void log(String message) {
        String curTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(curTime + ": " + message);
    }
}
