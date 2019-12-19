package core;


import java.util.ArrayList;
import java.util.List;

public class Result {

    private String query;

    private List<Item> items = new ArrayList<>();

    public int getItemsCount() {
        return items.size();
    }

    public int getSoldCount() {
        return items.stream().filter(Item::isInfoFull).mapToInt(Item::getSoldCount).sum();
    }

    public double getAvgPrice() {
        return items.stream().mapToDouble(Item::getPrice).average().orElse(0);
    }

    public double getAvgPurchasePrice() {
        double purchasesSum = items.stream()
                .filter(Item::isInfoFull)
                .mapToDouble(i -> i.getSoldCount() * i.getPrice())
                .sum();
        return purchasesSum / getItemsCount();
    }
}
