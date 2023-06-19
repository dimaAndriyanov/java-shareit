package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems();

    List<ItemDto> getAllItemsByOwnerId(Long ownerId);

    List<ItemDto> searchItems(String query);

    ItemDto getItemById(Long id);

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(ItemDto itemDto, Long id, Long ownerId);

    ItemDto deleteItemById(Long id, Long ownerId);

    void deleteAllItems();
}