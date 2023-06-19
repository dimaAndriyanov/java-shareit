package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataAccessException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.CataloguedItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    @Qualifier("itemRepositoryInMemoryImpl")
    private final ItemRepository itemRepository;
    @Qualifier("userRepositoryInMemoryImpl")
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final Map<Long, CataloguedItem> itemCatalogue = new HashMap<>();

    @Override
    public List<ItemDto> getAllItems() {
        return itemMapper.toItemDto(itemRepository.getAll());
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        return itemMapper.toItemDto(itemRepository.getAllByOwnerId(ownerId));
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        if (query.isEmpty()) {
            return List.of();
        }
        List<Long> idList = itemCatalogue.entrySet().stream()
                .filter(entry -> entry.getValue().getName().contains(query) ||
                        entry.getValue().getDescription().contains(query))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return itemMapper.toItemDto(itemRepository.getByIdList(idList));
    }

    @Override
    public ItemDto getItemById(Long id) {
        return itemMapper.toItemDto(itemRepository.getById(id));
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        Item savedItem = itemRepository.create(itemMapper.toItem(itemDto, userRepository.getById(ownerId)));
        if (savedItem.getAvailable()) {
            itemCatalogue.put(savedItem.getId(), new CataloguedItem(savedItem));
        }
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        checkForDataAccessRights(id, ownerId, "Can not update someone else's item");
        Item updatedItem = itemRepository.update(itemMapper.toItem(itemDto, userRepository.getById(ownerId)), id);
        if (updatedItem.getAvailable()) {
            itemCatalogue.put(updatedItem.getId(), new CataloguedItem(updatedItem));
        } else {
            itemCatalogue.remove(updatedItem.getId());
        }
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto deleteItemById(Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        checkForDataAccessRights(id, ownerId, "Can not delete someone else's item");
        return itemMapper.toItemDto(itemRepository.deleteById(id));
    }

    @Override
    public void deleteAllItems() {
        itemRepository.deleteAll();
    }

    private void checkForDataAccessRights(Long itemId, Long ownerId, String message) {
        if (!itemRepository.getById(itemId).getOwner().getId().equals(ownerId)) {
            throw new DataAccessException(message);
        }
    }
}