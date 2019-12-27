package core;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Result {

    private String query;
    private List<Item> items = new ArrayList<>();
    private boolean isSuccess;
    private int activeItemsTotal;
    private int completeItemsTotal;
    private Status status;


    Result(String query) {
        this.query = query;
        this.status = Status.NEW;
        isSuccess = false;
    }

    public long getActiveItemsFound() {
        return items.stream()
                .filter(i -> !i.isComplete())
                .count();
    }

    public long getCompleteItemsFound() {
        return items.stream()
                .filter(Item::isComplete)
                .count();
    }

    public long getSoldItems() {
        return items.stream()
                .filter(Item::isSold)
                .count();
    }

    public double getAvgPriceListed() {
        return round(items.stream()
                .filter(i -> !i.isComplete())
                .mapToDouble(Item::getPrice)
                .average()
                .orElse(0d), 2);
    }

    public double getAvgPriceSold() {
        return round(items.stream()
                .filter(Item::isComplete)
                .mapToDouble(Item::getPrice)
                .average()
                .orElse(0d), 2);
    }

    public String getSoldRatio() {
        if (items.size() == 0) return "0.0%";
        return  round(getSoldItems() * 100.0 / items.size(), 1) + "%";
    }

    public int getItemsCount() {
        return items.size();
    }

    void addItem(Item item) {
        items.add(item);
    }

    public String getQuery() {
        return query;
    }

    void setQuery(String query) {
        this.query = query;
    }

    public List<Item> getItems() {
        return items;
    }

    void setItems(List<Item> items) {
        this.items = items;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    void setIsSuccess(boolean success) {
        isSuccess = success;
    }

    public int getActiveItemsTotal() {
        return activeItemsTotal;
    }

    void setActiveItemsTotal(int activeItemsTotal) {
        this.activeItemsTotal = activeItemsTotal;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getStatusString() {
        return status.getName();
    }

    public enum Status {
        NEW("New"),
        LOADING("Items searching"),
        ERROR("Error"),
        COMPLETED("Completed");

        String statusName;

        Status(String statusName) {
            this.statusName = statusName;
        }

        public String getName() {
            return statusName;
        }
    }

    public int getCompleteItemsTotal() {
        return completeItemsTotal;
    }

    public void setCompleteItemsTotal(int completeItemsTotal) {
        this.completeItemsTotal = completeItemsTotal;
    }
}

