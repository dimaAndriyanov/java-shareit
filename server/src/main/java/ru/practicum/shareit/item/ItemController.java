package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @GetMapping("/all")
    public List<ItemDto> getAllItems() {
        log.info("Request on getting all items has been received");
        return itemService.getAllItems();
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id, @RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Request on getting item with id = {} by user with id = {} has been received", id, userId);
        return itemService.getItemById(id, userId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwnerId(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                              @RequestParam Integer from,
                                              @RequestParam Integer size) {
        log.info("Request on getting all items of user with id = {} " +
                "with page parameters from = {} and size = {} has been received", ownerId, from, size);
        return itemService.getAllItemsByOwnerId(ownerId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestBody ItemDto itemDto, @RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("Request on posting item with\nname = {}\ndescription = {}\navailable = {}\nrequestId = {}" +
                "\nby user with id = {} has been received",
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                itemDto.getRequestId(),
                ownerId);
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItemById(@RequestBody ItemDto itemDto,
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
        return itemService.updateItem(itemDto, id, ownerId);
    }

    @DeleteMapping("/{id}")
    public ItemDto deleteItemById(@PathVariable Long id, @RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("Request on deleting item with id = {} by user with id = {} has been received", id, ownerId);
        return itemService.deleteItemById(id, ownerId);
    }

    @DeleteMapping
    public void deleteAll() {
        log.info("Request on deleting all items has been received");
        itemService.deleteAllItems();
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String query,
                                     @RequestParam Integer from,
                                     @RequestParam Integer size) {
        log.info("Request on getting items by searchQuery = \"{}\" " +
                "with page parameters from = {} and size = {} has been received", query, from, size);
        return itemService.searchItems(query.toLowerCase(), from, size);
    }

    @PostMapping("/{id}/comment")
    public CommentDto createComment(@RequestBody CommentDto commentDto,
                                    @PathVariable("id") Long itemId,
                                    @RequestHeader(HEADER_USER_ID) Long authorId) {
        log.info("Request on posting comment with text = {}" +
                "\non item with id = {}\nfrom user with id = {} has been received",
                commentDto.getText(),
                itemId,
                authorId);
        return itemService.createComment(commentDto, itemId, authorId, LocalDateTime.parse(commentDto.getCreated()));
    }
}