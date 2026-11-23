package com.recipebook.repository;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Позиція у списку покупок для експорту у формат XML засобами JAXB.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ShoppingListItem {
    private String ingredientName;
    private double totalAmount;
    private String unit;

    // Порожній конструктор потрібен бібліотеці JAXB
    public ShoppingListItem() {
    }

    public ShoppingListItem(String ingredientName, double totalAmount, String unit) {
        this.ingredientName = ingredientName;
        this.totalAmount = Math.round(totalAmount * 10.0) / 10.0;
        this.unit = unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return ingredientName + ": " + totalAmount + " " + unit;
    }
}