package core;

import java.util.Objects;

public class Item {

    public Item(String itemId, double price, String sellingStatus, String itemUrl) {
        this.itemId = itemId;
        this.price = price;
        this.sellingStatus = sellingStatus;
        this.itemUrl = itemUrl;
    }

    private final String itemId;
    private final String itemUrl;
    private final double price;
    private final String sellingStatus;

    public double getPrice() {
        return price;
    }

    public String getItemId() {
        return itemId;
    }

    public String getSellingStatus() {
        return sellingStatus;
    }

    public boolean isSold() {
        return sellingStatus.equals("EndedWithSales");
    }

    public boolean isComplete() {
        return !sellingStatus.equals("Active");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return itemId.equals(item.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return String.format("%-25s%-20s%7.2f  %s", itemId, sellingStatus, price, itemUrl);
    }
}
