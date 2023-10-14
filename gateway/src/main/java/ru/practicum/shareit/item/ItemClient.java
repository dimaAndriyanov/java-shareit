package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getAllItems() {
        return get("/all");
    }

    public ResponseEntity<Object> getItem(Long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllOwnersItems(long ownerId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", ownerId, parameters);
    }

    public ResponseEntity<Object> postItem(ItemDto itemDto, long ownerId) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> patchItem(ItemDto itemDto, Long itemId, long ownerId) {
        return patch("/" + itemId, ownerId, itemDto);
    }

    public ResponseEntity<Object> deleteItem(Long itemId, long ownerId) {
        return delete("/" + itemId, ownerId);
    }

    public ResponseEntity<Object> deleteAllItems() {
        return delete("");
    }

    public ResponseEntity<Object> searchItems(String query, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", query,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", parameters);
    }

    public ResponseEntity<Object> postComment(CommentDto commentDto, Long itemId, long userId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}