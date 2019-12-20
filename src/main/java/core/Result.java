package core;


import java.util.ArrayList;
import java.util.List;

public class Result {

    private String query;
    private List<Item> items = new ArrayList<>();
    private boolean isSuccess;
    private int totalEntries;

    public Result(String query) {
        this.query = query;
        isSuccess = false;
    }

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

    public void addItem(Item item) {
        items.add(item);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }
}
