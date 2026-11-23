package com.recipebook.repository;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Кореневий XML-елемент списку покупок на тиждень.
 */
@XmlRootElement(name = "shoppingList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShoppingListXml {
    private String generatedAt;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<ShoppingListItem> items = new ArrayList<>();

    public ShoppingListXml() {
    }

    public ShoppingListXml(List<ShoppingListItem> items) {
        this.generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.items = (items == null) ? new ArrayList<>() : new ArrayList<>(items);
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public List<ShoppingListItem> getItems() {
        return items;
    }
}