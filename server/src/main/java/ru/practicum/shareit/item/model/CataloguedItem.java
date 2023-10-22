package ru.practicum.shareit.item.model;

import lombok.Getter;

@Getter
public class CataloguedItem {
    private final String name;
    private final String description;

    public CataloguedItem(Item item) {
        this.name = item.getName().toLowerCase();
        this.description = item.getDescription().toLowerCase();
    }
}