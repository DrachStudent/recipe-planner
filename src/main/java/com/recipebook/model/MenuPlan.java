package com.recipebook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Тижневий планувальник меню, що зберігає перелік запланованих страв.
 */
public class MenuPlan {
    private List<MenuEntry> entries = new ArrayList<>();

    public MenuPlan() {
    }

    public MenuPlan(List<MenuEntry> entries) {
        setEntries(entries);
    }

    public List<MenuEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MenuEntry> entries) {
        this.entries = (entries == null) ? new ArrayList<>() : new ArrayList<>(entries);
    }

    public void addEntry(MenuEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Запис меню не може бути null");
        }
        entries.add(entry);
    }

    public void removeEntry(MenuEntry entry) {
        entries.remove(entry);
    }
}