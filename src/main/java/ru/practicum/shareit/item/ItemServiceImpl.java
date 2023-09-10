package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.DataAccessException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.PostingCommentWithoutCompletedBookingException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.*;
import static ru.practicum.shareit.item.CommentMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    @Qualifier("itemRepositoryDbImpl")
    private final ItemRepository itemRepository;
    @Qualifier("userRepositoryDbImpl")
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

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
        ItemDto result;
        if (foundItem.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStart(id);
            LocalDateTime now = LocalDateTime.now();
            result = toItemDto(foundItem, findLastBooking(now, bookings), findNextBooking(now, bookings));
        } else {
            result = toItemDto(foundItem, null, null);
        }
        addCommentsToItemDto(result, toCommentDto(commentRepository.findAllByItemId(id)));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId, Integer from, Integer size) {
        userRepository.checkForPresenceById(ownerId);
        List<Item> foundItems = itemRepository.getAllByOwnerId(ownerId, from, size);
        List<Long> foundItemsIds = foundItems.stream().map(Item::getId).collect(Collectors.toList());
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStart(foundItemsIds);
        Map<Long, List<Booking>> bookingsByItemIds = new HashMap<>();
        for (Item item : foundItems) {
            bookingsByItemIds.put(item.getId(), new ArrayList<>());
        }
        for (Booking booking : bookings) {
            bookingsByItemIds.get(booking.getItem().getId()).add(booking);
        }
        LocalDateTime now = LocalDateTime.now();
        List<ItemDto> result =  foundItems.stream()
                .map(item -> toItemDto(item,
                findLastBooking(now, bookingsByItemIds.get(item.getId())),
                findNextBooking(now, bookingsByItemIds.get(item.getId()))))
                .collect(Collectors.toList());
        List<Comment> comments = commentRepository.findAllByItemIdIn(foundItemsIds);
        Map<Long, List<Comment>> commentsByItemIds = new HashMap<>();
        for (Item item : foundItems) {
            commentsByItemIds.put(item.getId(), new ArrayList<>());
        }
        for (Comment comment : comments) {
            commentsByItemIds.get(comment.getItem().getId()).add(comment);
        }
        result.forEach(itemDto -> addCommentsToItemDto(itemDto, toCommentDto(commentsByItemIds.get(itemDto.getId()))));
        return result;
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        Item item = getItemWithItemRequest(itemDto, ownerId);
        return toItemDto(itemRepository.create(item), null, null);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long id, Long ownerId) {
        userRepository.checkForPresenceById(ownerId);
        itemRepository.checkForPresenceById(id);
        checkForDataAccessRights(id, ownerId, "Can not update someone else's item");
        Item item = getItemWithItemRequest(itemDto, ownerId);
        return toItemDto(itemRepository.update(item, id), null, null);
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
    public List<ItemDto> searchItems(String query, Integer from, Integer size) {
        return toItemDto(itemRepository.searchItems(query, from, size));
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long authorId, LocalDateTime created) {
        itemRepository.checkForPresenceById(itemId);
        userRepository.checkForPresenceById(authorId);
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerId(itemId, authorId);
        if (bookings.stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .noneMatch(booking -> booking.getEnd().isBefore(created))) {
            throw new PostingCommentWithoutCompletedBookingException(
                    "Can not comment on item that has never been used by user");
        }
        String authorName = userRepository.getById(authorId).getName();
        Item item = itemRepository.getById(itemId);
        Comment result = commentRepository.save(toComment(commentDto, item, authorName, created));
        log.info("Comment with id = {} on item with id = {} from user with id = {} has been created",
                result.getId(), itemId, authorId);
        return toCommentDto(result);
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
                .filter(booking -> booking.getStart().isBefore(now))
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

    private void addCommentsToItemDto(ItemDto itemDto, List<CommentDto> commentsDto) {
        commentsDto.forEach(itemDto::addCommentDto);
    }

    private Item getItemWithItemRequest(ItemDto itemDto, Long ownerId) {
        Item item;
        if (itemDto.getRequestId() == null) {
            item = toItem(itemDto, userRepository.getById(ownerId), null);
        } else {
            Optional<ItemRequest> possibleItemRequest = itemRequestRepository.findById(itemDto.getRequestId());
            if (possibleItemRequest.isPresent()) {
                item = toItem(itemDto, userRepository.getById(ownerId), possibleItemRequest.get());
            } else {
                throw new ObjectNotFoundException(
                        String.format("Item request with id = %s not found", itemDto.getRequestId()));
            }
        }
        return item;
    }
}