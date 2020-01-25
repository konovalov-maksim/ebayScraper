package core.entities;


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
    private String searchUrl;

    public Result(String query) {
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

    public double getSoldRatio() {
        if (items.size() == 0) return 0.0;
        return  round(getSoldItems() * 1.0 / items.size(), 2);
    }

    public String getSoldRatioString() {
        if (items.size() == 0) return "0.0%";
        return  round(getSoldItems() * 100.0 / items.size(), 2) + "%";
    }

    public double getCurValue() {
        if (getSoldRatio() > 0.3) return round(getAvgPriceListed() * (1 + getSoldRatio()), 2);
        else return round(getAvgPriceSold() * (1 + getSoldRatio()), 2);
    }

    public int getItemsCount() {
        return items.size();
    }

    public void addItem(Item item) {
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

    public void setIsSuccess(boolean success) {
        isSuccess = success;
    }

    public int getActiveItemsTotal() {
        return activeItemsTotal;
    }

    public void setActiveItemsTotal(int activeItemsTotal) {
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

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        if (this.searchUrl == null)
            this.searchUrl = searchUrl;
    }

    public String getSearchUrlActive() {
        return searchUrl == null ? null : searchUrl + "&_sop=16";
    }

    public String getSearchUrlSold() {
        return searchUrl == null ? null : searchUrl + "&_sop=16&rt=nc&LH_Sold=1";
    }
}

