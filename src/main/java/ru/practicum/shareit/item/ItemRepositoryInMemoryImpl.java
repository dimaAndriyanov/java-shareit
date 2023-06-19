package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemRepositoryInMemoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getAllByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new NullPointerException("Owner id must not be null");
        }
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getByIdList(List<Long> idList) {
        if (idList == null) {
            throw new NullPointerException("Id list must not be null");
        }
        return idList.stream().map(this::getById).collect(Collectors.toList());
    }

    @Override
    public Item getById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        checkForPresenceById(id);
        return items.get(id);
    }

    @Override
    public Item create(Item item) {
        if (item == null) {
            throw new NullPointerException("Can not create null item");
        }
        item.setId(getNextId());
        items.put(item.getId(), item);
        log.info("New item with id {} has been created", item.getId());
        return items.get(item.getId());
    }

    @Override
    public Item update(Item item, Long id) {
        if (item == null) {
            throw new NullPointerException("Can not update null item");
        }
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        checkForPresenceById(id);
        Item updatedItem = new Item(
                item.getName() != null ? item.getName() : items.get(id).getName(),
                item.getDescription() != null ? item.getDescription() : items.get(id).getDescription(),
                item.getAvailable() != null ? item.getAvailable() : items.get(id).getAvailable(),
                items.get(id).getOwner()
        );
        updatedItem.setId(id);
        items.put(updatedItem.getId(), updatedItem);
        log.info("Item with id {} has been updated", updatedItem.getId());
        return items.get(updatedItem.getId());
    }

    @Override
    public Item deleteById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        checkForPresenceById(id);
        Item deletedItem = items.remove(id);
        log.info("Item with id {} has been deleted", id);
        return deletedItem;
    }

    @Override
    public void deleteAll() {
        items.clear();
        log.info("All items has been deleted");
    }

    @Override
    public void checkForPresenceById(Long id) {
        if (!items.containsKey(id)) {
            throw new ObjectNotFoundException(String.format("Item with id = %s not found", id));
        }
    }

    @Override
    public void deleteAllByOwnerId(Long ownerId) {
        List<Long> idsToDelete = items.entrySet().stream()
                .filter(entry -> entry.getValue().getOwner().getId().equals(ownerId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        for (Long id : idsToDelete) {
            items.remove(id);
        }
    }

    private Long getNextId() {
        return nextId++;
    }
}