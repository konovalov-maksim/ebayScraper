package core;

import java.util.Objects;

public class Item {

    public Item(String itemId, double price) {
        ItemId = itemId;
        this.price = price;
        isInfoFull = false;
    }

    private String ItemId;
    private boolean isInfoFull;
    private double price;
    private int soldCount;

    public double getPrice() {
        return price;
    }

    public String getItemId() {
        return ItemId;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public boolean isInfoFull() {
        return isInfoFull;
    }

    void setInfoFull(boolean infoFull) {
        isInfoFull = infoFull;
    }

    void setItemId(String itemId) {
        ItemId = itemId;
    }

    void setPrice(double price) {
        this.price = price;
    }

    void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return ItemId.equals(item.ItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemId);
    }
}
