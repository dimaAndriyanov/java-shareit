package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @MockBean
    private final ItemClient itemClient;

    private final MockMvc mvc;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    void shouldReturnBadRequestAndErrorWhenGetAllItemsByOwnerIdWithWrongParametersFromOrSize() throws Exception {
        mvc.perform(get("/items?from={from}&size={size}", -1, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemsByOwnerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));

        mvc.perform(get("/items?from={from}&size={size}", 0, 0)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemsByOwnerId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenSearchItemsWithWrongParametersFromOrSize() throws Exception {
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
    }
}