package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataAccessException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static ru.practicum.shareit.item.ItemMapper.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    @Qualifier("itemRepositoryInMemoryImpl")
    private final ItemRepository itemRepository;
    @Qualifier("userRepositoryInMemoryImpl")
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getAllItems() {
        return toItemDto(itemRepository.getAll());
    }

    @Override
    public ItemDto getItemById(Long id) {
        itemRepository.checkForPresenceById(id);
        return toItemDto(itemRepository.getById(id));
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        return toItemDto(itemRepository.getAllByOwnerId(ownerId));
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        return toItemDto(itemRepository.create(toItem(itemDto, userRepository.getById(ownerId))));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        itemRepository.checkForPresenceById(id);
        checkForDataAccessRights(id, ownerId, "Can not update someone else's item");
        return toItemDto(itemRepository.update(toItem(itemDto, userRepository.getById(ownerId)), id));
    }

    @Override
    public ItemDto deleteItemById(Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        itemRepository.checkForPresenceById(id);
        checkForDataAccessRights(id, ownerId, "Can not delete someone else's item");
        return toItemDto(itemRepository.deleteById(id));
    }

    @Override
    public void deleteAllItems() {
        itemRepository.deleteAll();
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        return toItemDto(itemRepository.searchItems(query));
    }

    private void checkForDataAccessRights(Long itemId, Long ownerId, String message) {
        if (!itemRepository.getById(itemId).getOwner().getId().equals(ownerId)) {
            throw new DataAccessException(message);
        }
    }
}