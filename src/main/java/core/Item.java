package core;

public class Item {

    public Item(Long itemId, double price) {
        ItemId = itemId;
        this.price = price;
    }

    private Long ItemId;

    private boolean isInfoFull;

    private double price;

    private int soldCount;

    public double getPrice() {
        return price;
    }

    public Long getItemId() {
        return ItemId;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public boolean isInfoFull() {
        return isInfoFull;
    }

    public void setInfoFull(boolean infoFull) {
        isInfoFull = infoFull;
    }

    public void setItemId(Long itemId) {
        ItemId = itemId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
}
