package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemValidator itemValidator;

    @GetMapping
    public List<ItemDto> getAllItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getAllItemsByOwnerId(ownerId);
    }

    @GetMapping("/all")
    public List<ItemDto> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemValidator.validateForCreation(itemDto);
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItemById(@RequestBody ItemDto itemDto,
                                  @PathVariable Long id,
                                  @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(itemDto, id, ownerId);
    }

    @DeleteMapping("/{id}")
    public ItemDto deleteItemById(@PathVariable Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.deleteItemById(id, ownerId);
    }

    @DeleteMapping
    public void deleteAll() {
        itemService.deleteAllItems();
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String query) {
        return itemService.searchItems(query.toLowerCase());
    }
}