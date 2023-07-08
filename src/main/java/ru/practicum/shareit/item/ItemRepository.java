package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> getAll();

    Item getById(Long id);

    List<Item> getAllByOwnerId(Long ownerId);

    Item create(Item item);

    Item update(Item item, Long id);

    Item deleteById(Long id);

    void deleteAll();

    void deleteAllByOwnerId(Long ownerId);

    List<Item> searchItems(String query);

    void checkForPresenceById(Long id);
}