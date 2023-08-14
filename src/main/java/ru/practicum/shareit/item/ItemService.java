package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    List<ItemDto> getAllItems();

    ItemDto getItemById(Long id, Long userId);

    List<ItemDto> getAllItemsByOwnerId(Long ownerId);

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(ItemDto itemDto, Long id, Long ownerId);

    ItemDto deleteItemById(Long id, Long ownerId);

    void deleteAllItems();

    List<ItemDto> searchItems(String query);
}