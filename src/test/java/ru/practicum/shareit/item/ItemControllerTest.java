package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    private final ObjectMapper mapper;

    @MockBean
    private final ItemService itemService;

    private final MockMvc mvc;

    private static final ItemDto item = new ItemDto(
            "itemName",
            "itemDescription",
            true,
            null,
            new BookingInfo(23L, 37L),
            17L
    );

    private static final ItemDto itemWithComment = new ItemDto(
            "itemWithCommentName",
            "itemWithCommentDescription",
            true,
            null,
            new BookingInfo(27L, 39L),
            17L
    );

    private static final CommentDto comment = new CommentDto("text", "authorName", "2020-01-01T00:00:00");

    static {
        item.setId(42L);
        itemWithComment.setId(49L);
        comment.setId(53L);
        itemWithComment.addCommentDto(comment);
    }

    @Test
    void getAllItems() throws Exception {
        when(itemService.getAllItems())
                .thenReturn(List.of(item));

        mvc.perform(get("/items/all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].nextBooking.id", is(item.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(item.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].comments", is(empty())));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItemById(any(), any()))
                .thenReturn(itemWithComment);

        mvc.perform(get("/items/{id}", 17)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithComment.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithComment.getName())))
                .andExpect(jsonPath("$.description", is(itemWithComment.getDescription())))
                .andExpect(jsonPath("$.nextBooking.id", is(itemWithComment.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(itemWithComment.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(itemWithComment.getRequestId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(itemWithComment.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName",
                        is(itemWithComment.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(itemWithComment.getComments().get(0).getCreated())));
    }

    @Test
    void getAllItemsByOwnerId() throws Exception {
        when(itemService.getAllItemsByOwnerId(any(), any(), any()))
                .thenReturn(List.of(itemWithComment));

        mvc.perform(get("/items?from={from}&size={size}", -1, 10)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemsByOwnerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));

        mvc.perform(get("/items?from={from}&size={size}", 0, 0)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemsByOwnerId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));

        mvc.perform(get("/items?from={from}&size={size}", 0, 10)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemWithComment.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemWithComment.getName())))
                .andExpect(jsonPath("$[0].description", is(itemWithComment.getDescription())))
                .andExpect(jsonPath("$[0].nextBooking.id", is(itemWithComment.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId",
                        is(itemWithComment.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(itemWithComment.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(itemWithComment.getComments().get(0).getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName",
                        is(itemWithComment.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$[0].comments[0].created", is(itemWithComment.getComments().get(0).getCreated())));
    }

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(any(), any()))
                .thenReturn(item);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.nextBooking.id", is(item.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(item.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$.comments", is(empty())));
    }

    @Test
    void updateItemById() throws Exception {
        when(itemService.updateItem(any(), any(), any()))
                .thenReturn(item);

        mvc.perform(patch("/items/{id}", 42)
                        .content(mapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.nextBooking.id", is(item.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(item.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$.comments", is(empty())));
    }

    @Test
    void deleteItemById() throws Exception {
        when(itemService.deleteItemById(any(), any()))
                .thenReturn(item);

        mvc.perform(delete("/items/{id}", 42)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.nextBooking.id", is(item.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId",
                        is(item.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$.comments", is(empty())));
    }

    @Test
    void deleteAll() throws Exception {
        mvc.perform(delete("/items"))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.searchItems(any(), any(), any()))
                .thenReturn(List.of(item));

        mvc.perform(get("/items/search?text={text}&from={from}&size={size}", "text", -1, 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("searchItems.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));

        mvc.perform(get("/items/search?text={text}&from={from}&size={size}", "text", 0, 0)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("searchItems.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));

        mvc.perform(get("/items/search?text={text}&from={from}&size={size}", "text", 0, 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].nextBooking.id", is(item.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(item.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(item.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].comments", is(empty())));
    }

    @Test
    void createComment() throws Exception {
        when(itemService.createComment(any(), any(), any(), any()))
                .thenReturn(comment);

        mvc.perform(post("/items/{id}/comment", 42)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.created", is(comment.getCreated())));
    }
}