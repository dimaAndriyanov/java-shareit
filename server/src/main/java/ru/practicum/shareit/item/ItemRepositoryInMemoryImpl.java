package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.CataloguedItem;
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
    private final Map<Long, CataloguedItem> itemCatalogue = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item getById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        return items.get(id);
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
    public List<Item> getAllByOwnerId(Long ownerId, Integer from, Integer size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item create(Item item) {
        if (item == null) {
            throw new NullPointerException("Can not create null item");
        }
        item.setId(getNextId());
        items.put(item.getId(), item);
        log.info("New item with id {} has been created", item.getId());
        Item result = items.get(item.getId());
        if (updateItemCatalogue(result)) {
            log.info("New item with id {} has been put to Item Catalogue", result.getId());
        }
        return result;
    }

    @Override
    public Item update(Item item, Long id) {
        if (item == null) {
            throw new NullPointerException("Can not update null item");
        }
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        Item updatedItem = new Item(
                item.getName() != null ? item.getName() : items.get(id).getName(),
                item.getDescription() != null ? item.getDescription() : items.get(id).getDescription(),
                item.getAvailable() != null ? item.getAvailable() : items.get(id).getAvailable(),
                items.get(id).getOwner(),
                item.getItemRequest() != null ? item.getItemRequest() : items.get(id).getItemRequest()
        );
        updatedItem.setId(id);
        items.put(updatedItem.getId(), updatedItem);
        log.info("Item with id {} has been updated", updatedItem.getId());
        Item result = items.get(updatedItem.getId());
        if (updateItemCatalogue(result)) {
            log.info("Item with id {} has been put (added or updated) to Item Catalogue", result.getId());
        } else {
            log.info("Item with id {} has been removed from Item Catalogue", result.getId());
        }
        return result;
    }

    @Override
    public Item deleteById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        Item deletedItem = items.remove(id);
        log.info("Item with id {} has been deleted", id);
        if (deletedItem.getAvailable()) {
            itemCatalogue.remove(id);
            log.info("Item with id {} has been removed from Item Catalogue", id);
        }
        return deletedItem;
    }

    @Override
    public void deleteAll() {
        items.clear();
        log.info("All items has been deleted");
        itemCatalogue.clear();
        log.info("All items has been removed from Item Catalogue");
    }

    @Override
    public void deleteAllByOwnerId(Long ownerId) {
        deleteByIdList(getAllByOwnerId(ownerId).stream().map(Item::getId).collect(Collectors.toList()));
    }

    @Override
    public List<Item> searchItems(String query, Integer from, Integer size) {
        if (query.isBlank()) {
            return List.of();
        }
        return itemCatalogue.entrySet().stream()
                .filter(entry -> entry.getValue().getName().contains(query) ||
                        entry.getValue().getDescription().contains(query))
                .map(Map.Entry::getKey)
                .map(id -> {
                    checkForPresenceById(id);
                    return getById(id);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void checkForPresenceById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        if (!items.containsKey(id)) {
            throw new ObjectNotFoundException(String.format("Item with id = %s not found", id));
        }
    }

    @Override
    public List<Item> getAllItemsByRequestId(Long requestId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Item> getAllItemsByRequestIds(List<Long> requestIds) {
        throw new UnsupportedOperationException();
    }

    private Long getNextId() {
        return nextId++;
    }

    private boolean updateItemCatalogue(Item item) {
        return itemCatalogue.compute(
                item.getId(), (k, v) -> item.getAvailable() ? new CataloguedItem(item) : null
        ) != null;
    }

    private void deleteByIdList(List<Long> idList) {
        if (idList == null) {
            throw new NullPointerException("Id list must not be null");
        }
        idList.forEach(this::deleteById);
    }
}