package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ItemDbInterface itemDbInterface;
    private final Map<Long, CataloguedItem> itemCatalogue = new HashMap<>();

    @Override
    public List<Item> getAll() {
        return itemDbInterface.findAll();
    }

    @Override
    public Item getById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        return itemDbInterface.findById(id).get();
    }

    @Override
    public List<Item> getAllByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new NullPointerException("Owner id must not be null");
        }
        return itemDbInterface.findAllByOwnerId(ownerId);
    }

    @Override
    public Item create(Item item) {
        if (item == null) {
            throw new NullPointerException("Can not create null item");
        }
        item.setNullId();
        Item result = itemDbInterface.save(item);
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
        Item oldItem = itemDbInterface.findByIdWithOwner(id).get();
        Item updatedItem = new Item(
                item.getName() != null ? item.getName() : oldItem.getName(),
                item.getDescription() != null ? item.getDescription() : oldItem.getDescription(),
                item.getAvailable() != null ? item.getAvailable() : oldItem.getAvailable(),
                oldItem.getOwner()
        );
        updatedItem.setId(id);
        Item result = itemDbInterface.saveAndFlush(updatedItem);
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
        Item deletedItem = itemDbInterface.findById(id).get();
        itemDbInterface.deleteById(id);
        log.info("Item with id {} has been deleted", id);
        if (deletedItem.getAvailable()) {
            itemCatalogue.remove(id);
            log.info("Item with id {} has been removed from Item Catalogue", id);
        }
        return deletedItem;
    }

    @Override
    public void deleteAll() {
        itemDbInterface.deleteAll();
        log.info("All items has been deleted");
        itemCatalogue.clear();
        log.info("All items has been removed from Item Catalogue");
    }

    @Override
    public void deleteAllByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new NullPointerException("Owner id must not be null");
        }
        List<Item> itemsToDelete = itemDbInterface.findAllByOwnerId(ownerId);
        if (!itemsToDelete.isEmpty()) {
            List<Long> idsOfItemsToDelete = itemsToDelete
                    .stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());
            itemDbInterface.deleteAllByOwnerId(ownerId);
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
    public List<Item> searchItems(String query) {
        if (query.isBlank()) {
            return List.of();
        }
        List<Long> foundItemsIds = itemCatalogue.entrySet().stream()
                .filter(entry -> entry.getValue().getName().contains(query) ||
                        entry.getValue().getDescription().contains(query))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return itemDbInterface.findAllById(foundItemsIds);
    }

    @Override
    public void checkForPresenceById(Long id) {
        if (id == null) {
            throw new NullPointerException("Id must not be null");
        }
        if (itemDbInterface.findById(id).isEmpty()) {
            throw new ObjectNotFoundException(String.format("Item with id = %s not found", id));
        }
    }

    private boolean updateItemCatalogue(Item item) {
        return itemCatalogue.compute(
                item.getId(), (k, v) -> item.getAvailable() ? new CataloguedItem(item) : null
        ) != null;
    }
}