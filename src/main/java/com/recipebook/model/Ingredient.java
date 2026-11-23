package com.recipebook.model;

import java.util.Objects;

/**
 * Сутність інгредієнта у довіднику (назва, калорійність на 100 г/мл та одиниця виміру).
 */
public class Ingredient {
    private String name;
    private double caloriesPer100;
    private String unit = "г";

    // Порожній конструктор потрібен бібліотеці Jackson для читання JSON
    public Ingredient() {
    }

    public Ingredient(String name, double caloriesPer100, String unit) {
        setName(name);
        setCaloriesPer100(caloriesPer100);
        setUnit(unit);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва інгредієнта не може бути порожньою");
        }
        this.name = name.trim();
    }

    public double getCaloriesPer100() {
        return caloriesPer100;
    }

    public void setCaloriesPer100(double caloriesPer100) {
        if (caloriesPer100 < 0) {
            throw new IllegalArgumentException("Калорійність не може бути від'ємною");
        }
        this.caloriesPer100 = caloriesPer100;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            this.unit = "г";
        } else {
            this.unit = unit.trim();
        }
    }

    @Override
    public String toString() {
        return name + " (" + caloriesPer100 + " ккал / 100 " + unit + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient that)) return false;
        return name != null && name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name == null ? "" : name.toLowerCase());
    }
}