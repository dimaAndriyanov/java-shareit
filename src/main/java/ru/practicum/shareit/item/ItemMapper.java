package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public ItemDto toItemDto(Item item, BookingInfo lastBooking, BookingInfo nextBooking) {
        ItemDto result = new ItemDto(
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking
        );
        result.setId(item.getId());
        return result;
    }

    public List<ItemDto> toItemDto(List<Item> items) {
        return items.stream().map(item -> toItemDto(item, null, null)).collect(Collectors.toList());
    }

    public Item toItem(ItemDto itemDto, User owner) {
        Item result = new Item(
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner
        );
        result.setId(itemDto.getId());
        return result;
    }

    public ItemDtoForBooking toItemDtoForBooking(Item item) {
        return new ItemDtoForBooking(item.getId(), item.getName());
    }
}