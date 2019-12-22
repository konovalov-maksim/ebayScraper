package core;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Result {

    private String query;
    private List<Item> items = new ArrayList<>();
    private boolean isSuccess;
    private int totalEntries;
    private Status status;

    public Result(String query) {
        this.query = query;
        this.status = Status.NEW;
        isSuccess = false;
    }

    public int getItemsCount() {
        return items.size();
    }

    public int getSoldCount() {
        return items.stream().filter(Item::isInfoFull).mapToInt(Item::getSoldCount).sum();
    }

    public double getAvgPrice() {
        return round(items.stream().mapToDouble(Item::getPrice).average().orElse(0), 2);
    }

    public double getAvgPurchasePrice() {
        double purchasesSum = items.stream()
                .filter(Item::isInfoFull)
                .mapToDouble(i -> i.getSoldCount() * i.getPrice())
                .sum();
        return round(getSoldCount() > 0 ? purchasesSum / getSoldCount() : 0, 2);
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

    public int getTotalEntries() {
        return totalEntries;
    }

    void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public String getIsSuccessString() {
        return isSuccess ? "Success": "Error";
    }

    public double getProgress() {
        if (items.isEmpty()) return 0d;
        long processed = items.stream().filter(Item::isInfoFull).count();
        return processed * 1.0 / items.size();
    }

    public String getProgressString() {
        return round(getProgress()*100, 1) + "%";
    }


    public static double round(double value, int places) {
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
}

