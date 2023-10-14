package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.CataloguedItem;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ItemRepositoryDbImpl implements ItemRepository {
    private final ItemRepositoryDbInterface itemRepositoryDbInterface;
    private final Map<Long, CataloguedItem> itemCatalogue = new HashMap<>();

    @Override
    public List<Item> getAll() {
        return itemRepositoryDbInterface.findAll();
    }

    @Override
    public Item getById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        return itemRepositoryDbInterface.findById(id).get();
    }

    @Override
    public List<Item> getAllByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new NullPointerException("Owner id must not be null");
        }
        return itemRepositoryDbInterface.findAllByOwnerId(ownerId);
    }

    @Override
    public List<Item> getAllByOwnerId(Long ownerId, Integer from, Integer size) {
        if (ownerId == null) {
            throw new NullPointerException("Owner id must not be null");
        }
        PageRequest page = PageRequest.of(from / size, size);
        return itemRepositoryDbInterface.findAllByOwnerIdOrderById(ownerId, page).getContent();
    }

    @Override
    public Item create(Item item) {
        if (item == null) {
            throw new NullPointerException("Can not create null item");
        }
        item.setNullId();
        Item result = itemRepositoryDbInterface.save(item);
        log.info("New item with id {} has been created", result.getId());
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
        Item oldItem = itemRepositoryDbInterface.findByIdWithOwner(id).get();
        Item updatedItem = new Item(
                item.getName() != null ? item.getName() : oldItem.getName(),
                item.getDescription() != null ? item.getDescription() : oldItem.getDescription(),
                item.getAvailable() != null ? item.getAvailable() : oldItem.getAvailable(),
                oldItem.getOwner(),
                item.getItemRequest() != null ? item.getItemRequest() : oldItem.getItemRequest()
        );
        updatedItem.setId(id);
        Item result = itemRepositoryDbInterface.saveAndFlush(updatedItem);
        log.info("Item with id {} has been updated", updatedItem.getId());
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
        Item deletedItem = itemRepositoryDbInterface.findById(id).get();
        itemRepositoryDbInterface.deleteById(id);
        log.info("Item with id {} has been deleted", id);
        if (deletedItem.getAvailable()) {
            itemCatalogue.remove(id);
            log.info("Item with id {} has been removed from Item Catalogue", id);
        }
        return deletedItem;
    }

    @Override
    public void deleteAll() {
        itemRepositoryDbInterface.deleteAll();
        log.info("All items has been deleted");
        itemCatalogue.clear();
        log.info("All items has been removed from Item Catalogue");
    }

    @Override
    public void deleteAllByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new NullPointerException("Owner id must not be null");
        }
        List<Item> itemsToDelete = itemRepositoryDbInterface.findAllByOwnerId(ownerId);
        if (!itemsToDelete.isEmpty()) {
            List<Long> idsOfItemsToDelete = itemsToDelete
                    .stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());
            itemRepositoryDbInterface.deleteAllByOwnerId(ownerId);
            StringBuilder logMessageForItemsDeletion = new StringBuilder("Items with id: ");
            for (Long id : idsOfItemsToDelete) {
                logMessageForItemsDeletion.append(id).append(", ");
            }
            logMessageForItemsDeletion.delete(logMessageForItemsDeletion.length() - 2, logMessageForItemsDeletion.length());
            logMessageForItemsDeletion.append(" has been removed due to deletion of user with id {}");
            log.info(logMessageForItemsDeletion.toString(), ownerId);

            List<Long> idsOfItemsToDeleteFromCatalogue = itemsToDelete
                    .stream()
                    .filter(Item::getAvailable)
                    .map(Item::getId)
                    .collect(Collectors.toList());
            if (!idsOfItemsToDeleteFromCatalogue.isEmpty()) {
                StringBuilder logMessageForItemDeletionFromCatalogue = new StringBuilder("Items with id: ");
                for (Long id : idsOfItemsToDeleteFromCatalogue) {
                    itemCatalogue.remove(id);
                    logMessageForItemDeletionFromCatalogue.append(id).append(", ");
                }
                logMessageForItemDeletionFromCatalogue.delete(
                        logMessageForItemDeletionFromCatalogue.length() - 2,
                        logMessageForItemDeletionFromCatalogue.length()
                );
                logMessageForItemDeletionFromCatalogue
                        .append(" has been removed from Catalogue due to deletion of user with id {}");
                log.info(logMessageForItemDeletionFromCatalogue.toString(), ownerId);
            }
        }
    }

    @Override
    public List<Item> searchItems(String query, Integer from, Integer size) {
        if (query.isBlank()) {
            return List.of();
        }
        List<Long> foundItemsIds = itemCatalogue.entrySet().stream()
                .filter(entry -> entry.getValue().getName().contains(query) ||
                        entry.getValue().getDescription().contains(query))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        PageRequest page = PageRequest.of(from / size, size);
        return itemRepositoryDbInterface.findAllByIdIn(foundItemsIds, page).getContent();
    }

    @Override
    public void checkForPresenceById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        if (itemRepositoryDbInterface.findById(id).isEmpty()) {
            throw new ObjectNotFoundException(String.format("Item with id = %s not found", id));
        }
    }

    @Override
    public List<Item> getAllItemsByRequestId(Long requestId) {
        if (requestId == null) {
            throw new NullPointerException("Id must not be null");
        }
        return itemRepositoryDbInterface.findAllByItemRequestId(requestId);
    }

    @Override
    public List<Item> getAllItemsByRequestIds(List<Long> requestIds) {
        if (requestIds == null) {
            throw new NullPointerException("Id list must not be null");
        }
        return itemRepositoryDbInterface.findAllByItemRequestIdIn(requestIds);
    }

    private boolean updateItemCatalogue(Item item) {
        return itemCatalogue.compute(
                item.getId(), (k, v) -> item.getAvailable() ? new CataloguedItem(item) : null
        ) != null;
    }
}