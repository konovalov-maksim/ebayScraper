import core.Scraper;

import java.util.Arrays;

public class Main {

    private static Scraper scraper;

    private static String[] testQueris = new String[]{"spinning reel", "watches"};

    public static void main(String[] args) {
        scraper = new Scraper(Arrays.asList(testQueris), Scraper.Condition.NOT_SPEC);

    }
}
