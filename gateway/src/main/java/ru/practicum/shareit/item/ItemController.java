package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.shareit.item.ItemValidator.*;
import static ru.practicum.shareit.item.CommentValidator.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItems() {
        log.info("Request on getting all items has been received");
        return itemClient.getAllItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@PathVariable Long id, @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Request on getting item with id = {} by user with id = {} has been received", id, userId);
        return itemClient.getItem(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwnerId(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                              @RequestParam(required = false) @PositiveOrZero Integer from,
                                              @RequestParam(required = false) @Positive Integer size) {
        log.info("Request on getting all items of user with id = {} " +
                "with page parameters from = {} and size = {} has been received", ownerId, from, size);
        return itemClient.getAllOwnersItems(ownerId, from == null ? 0 : from, size == null ? 10 : size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createItem(@RequestBody ItemDto itemDto, @RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("Request on posting item with\nname = {}\ndescription = {}\navailable = {}\nrequestId = {}" +
                "\nby user with id = {} has been received",
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                itemDto.getRequestId(),
                ownerId);
        validateItemForCreation(itemDto);
        return itemClient.postItem(itemDto, ownerId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItemById(@RequestBody ItemDto itemDto,
                                  @PathVariable Long id,
                                  @RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("Request on patching item with\nid = {}\nname = {}\ndescription = {}\navailable = {}\nrequestId = {}" +
                "\nby user with id = {} has been received",
                id,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                itemDto.getRequestId(),
                ownerId);
        validateItemForUpdating(itemDto);
        return itemClient.patchItem(itemDto, id, ownerId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItemById(@PathVariable Long id, @RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("Request on deleting item with id = {} by user with id = {} has been received", id, ownerId);
        return itemClient.deleteItem(id, ownerId);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteAll() {
        log.info("Request on deleting all items has been received");
        return itemClient.deleteAllItems();
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam("text") String query,
                                     @RequestParam(required = false) @PositiveOrZero Integer from,
                                     @RequestParam(required = false) @Positive Integer size) {
        log.info("Request on getting items by searchQuery = \"{}\" " +
                "with page parameters from = {} and size = {} has been received", query, from, size);
        return itemClient.searchItems(query.toLowerCase(), from == null ? 0 : from, size == null ? 10 : size);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> createComment(@RequestBody CommentDto commentDto,
                                    @PathVariable("id") Long itemId,
                                    @RequestHeader(HEADER_USER_ID) Long authorId) {
        log.info("Request on posting comment with text = {}" +
                "\non item with id = {}\nfrom user with id = {} has been received",
                commentDto.getText(),
                itemId,
                authorId);
        validateCommentDto(commentDto);
        commentDto.setCreated(LocalDateTime.now().format(formatter));
        return itemClient.postComment(commentDto, itemId, authorId);
    }
}