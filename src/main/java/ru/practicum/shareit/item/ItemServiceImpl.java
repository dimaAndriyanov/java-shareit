package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.DataAccessException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    @Qualifier("itemRepositoryDbImpl")
    private final ItemRepository itemRepository;
    @Qualifier("userRepositoryDbImpl")
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        return toItemDto(itemRepository.getAll());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id, Long userId) {
        userRepository.checkForPresenceById(userId);
        itemRepository.checkForPresenceById(id);
        Item foundItem = itemRepository.getById(id);
        if (foundItem.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStart(id);
            LocalDateTime now = LocalDateTime.now();
            return toItemDto(foundItem, findLastBooking(now, bookings), findNextBooking(now, bookings));
        } else {
            return toItemDto(foundItem, null, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        List<Item> foundItems = itemRepository.getAllByOwnerId(ownerId);
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStart(
                foundItems.stream().map(Item::getId).collect(Collectors.toList())
        );
        Map<Long, List<Booking>> bookingsByItemIds = new HashMap<>();
        for (Item item : foundItems) {
            bookingsByItemIds.put(item.getId(), new ArrayList<>());
        }
        for (Booking booking : bookings) {
            bookingsByItemIds.get(booking.getItem().getId()).add(booking);
        }
        LocalDateTime now = LocalDateTime.now();
        return foundItems.stream()
                .map(item -> toItemDto(item,
                findLastBooking(now, bookingsByItemIds.get(item.getId())),
                findNextBooking(now, bookingsByItemIds.get(item.getId()))))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        return toItemDto(itemRepository.create(toItem(itemDto, userRepository.getById(ownerId))), null, null);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        itemRepository.checkForPresenceById(id);
        checkForDataAccessRights(id, ownerId, "Can not update someone else's item");
        return toItemDto(itemRepository.update(toItem(itemDto, userRepository.getById(ownerId)), id), null, null);
    }

    @Override
    @Transactional
    public ItemDto deleteItemById(Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        itemRepository.checkForPresenceById(id);
        checkForDataAccessRights(id, ownerId, "Can not delete someone else's item");
        return toItemDto(itemRepository.deleteById(id), null, null);
    }

    @Override
    @Transactional
    public void deleteAllItems() {
        itemRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String query) {
        return toItemDto(itemRepository.searchItems(query));
    }

    @Transactional(readOnly = true)
    private void checkForDataAccessRights(Long itemId, Long ownerId, String message) {
        if (!itemRepository.getById(itemId).getOwner().getId().equals(ownerId)) {
            throw new DataAccessException(message);
        }
    }

    private BookingInfo findLastBooking(LocalDateTime now, List<Booking> bookings) {
        Optional<Booking> optionalLastBooking = bookings.stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> booking.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getStart));
        return optionalLastBooking.map(BookingMapper::getBookingInfo).orElse(null);
    }

    private BookingInfo findNextBooking(LocalDateTime now, List<Booking> bookings) {
        Optional<Booking> optionalNextBooking = bookings.stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> now.isBefore(booking.getStart()))
                .findFirst();
        return optionalNextBooking.map(BookingMapper::getBookingInfo).orElse(null);
    }
}