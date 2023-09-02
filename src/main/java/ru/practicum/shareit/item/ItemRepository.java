package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> getAll();

    Item getById(Long id);

    List<Item> getAllByOwnerId(Long ownerId);

    List<Item> getAllByOwnerId(Long ownerId, Integer from, Integer size);

    Item create(Item item);

    Item update(Item item, Long id);

    Item deleteById(Long id);

    void deleteAll();

    void deleteAllByOwnerId(Long ownerId);

    List<Item> searchItems(String query, Integer from, Integer size);

    void checkForPresenceById(Long id);

    List<Item> getAllItemsByRequestId(Long requestId);

    List<Item> getAllItemsByRequestIds(List<Long> requestIds);
}