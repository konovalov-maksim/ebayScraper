package core;

public class Item {

    public Item(String itemId, double price) {
        ItemId = itemId;
        this.price = price;
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

    public void setInfoFull(boolean infoFull) {
        isInfoFull = infoFull;
    }

    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
}
