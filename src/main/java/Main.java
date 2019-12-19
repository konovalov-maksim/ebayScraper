import core.ItemsSeeker;

import java.util.Arrays;

public class Main {

    private static ItemsSeeker itemsSeeker;

    private static String[] testQueries = new String[]{"spinning reel shimano", "watches"};

    public static void main(String[] args) {
        itemsSeeker = new ItemsSeeker(Arrays.asList(testQueries), ItemsSeeker.Condition.NOT_SPEC);

    }
}
