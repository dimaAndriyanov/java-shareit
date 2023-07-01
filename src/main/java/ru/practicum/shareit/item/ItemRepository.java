package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> getAll();

    List<Item> getAllByOwnerId(Long ownerId);

    List<Item> getByIdList(List<Long> idList);

    Item getById(Long id);

    Item create(Item item);

    Item update(Item item, Long id);

    Item deleteById(Long id);

    void deleteByIdList(List<Long> idList);

    void deleteAll();

    void checkForPresenceById(Long id);

    List<Long> deleteAllByOwnerId(Long ownerId);
}