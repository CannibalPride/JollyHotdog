package com.jolly_hotdogs.jollyhotdog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

public class Item {
    private StringProperty name;
    private int quantity;
    private ItemType type;
    private double price;
    private LocalDateTime lastTransaction;

    public Item(String name, int quantity, ItemType type, double price, LocalDateTime lastTransaction) {
        this.name = new SimpleStringProperty(name);
        this.quantity = quantity;
        this.type = type;
        this.price = price;
        this.lastTransaction = lastTransaction;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getLastTransaction() {
        return lastTransaction;
    }

    public void setLastTransaction(LocalDateTime lastTransaction) {
        this.lastTransaction = lastTransaction;
    }

    public String toCsvString() {
        return name + "," + quantity + "," + type.toString() + "," + price + "," + lastTransaction.toString();
    }
}
