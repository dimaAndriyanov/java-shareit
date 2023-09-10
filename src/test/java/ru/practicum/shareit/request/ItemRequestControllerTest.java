package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private final ObjectMapper mapper;

    @MockBean
    private final ItemRequestService itemRequestService;

    private final MockMvc mvc;

    private final static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final static ItemRequestDto itemRequest = new ItemRequestDto(
            "description",
            LocalDateTime.parse("2020-01-01T00:00:00", formatter)
    );

    private final static ItemDto item = new ItemDto(
            "name",
            "description",
            true,
            null,
            null,
            42L
    );

    static {
        itemRequest.setId(42L);
        item.setId(15L);
        itemRequest.addItemDto(item);
    }

    @Test
    void getAllItemRequestsByCreatorId() throws Exception {
        when(itemRequestService.getAllItemRequestsByCreatorId(any()))
                .thenReturn(List.of(itemRequest));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequest.getCreated().format(formatter))))
                .andExpect(jsonPath("$[0].items[0].id",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getName())))
                .andExpect(jsonPath("$[0].items[0].description",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getDescription())))
                .andExpect(jsonPath("$[0].items[0].available",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getAvailable())))
                .andExpect(jsonPath("$[0].items[0].requestId",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].comments", is(empty())));
    }

    @Test
    void getAllItemRequestsByUserId() throws Exception {
        when(itemRequestService.getAllItemRequestsByUserId(any(), any(), any()))
                .thenReturn(List.of(itemRequest));

        mvc.perform(get("/requests/all?from={from}&size={size}", -1, 10)
                        .header("X-Sharer-User-Id", 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemRequestsByUserId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 0)
                        .header("X-Sharer-User-Id", 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemRequestsByUserId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 10)
                        .header("X-Sharer-User-Id", 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequest.getCreated().format(formatter))))
                .andExpect(jsonPath("$[0].items[0].id",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getName())))
                .andExpect(jsonPath("$[0].items[0].description",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getDescription())))
                .andExpect(jsonPath("$[0].items[0].available",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getAvailable())))
                .andExpect(jsonPath("$[0].items[0].requestId",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].comments", is(empty())));
    }

    @Test
    void getItemRequestById() throws Exception {
        when(itemRequestService.getItemRequestById(any(), any()))
                .thenReturn(itemRequest);

        mvc.perform(get("/requests/{requestId}", 42)
                        .header("X-Sharer-User-Id", 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequest.getCreated().format(formatter))))
                .andExpect(jsonPath("$.items[0].id",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getName())))
                .andExpect(jsonPath("$.items[0].description",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getDescription())))
                .andExpect(jsonPath("$.items[0].available",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getAvailable())))
                .andExpect(jsonPath("$.items[0].requestId",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$.items[0].comments", is(empty())));
    }

    @Test
    void createItemRequest() throws Exception {
        when(itemRequestService.createItemRequest(any(), any(), any()))
                .thenReturn(itemRequest);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequest.getCreated().format(formatter))))
                .andExpect(jsonPath("$.items[0].id",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getId()), Long.class))
                .andExpect(jsonPath("$.items[0].name",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getName())))
                .andExpect(jsonPath("$.items[0].description",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getDescription())))
                .andExpect(jsonPath("$.items[0].available",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getAvailable())))
                .andExpect(jsonPath("$.items[0].requestId",
                        is(new ArrayList<>(itemRequest.getItems()).get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$.items[0].comments", is(empty())));
    }
}