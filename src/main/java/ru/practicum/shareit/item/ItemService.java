package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
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

    CommentDto createComment(CommentDto commentDto, Long itemId, Long authorId, LocalDateTime created);
}