package ru.practicum.shareit.request;

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
@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @MockBean
    private final ItemRequestClient itemRequestClient;

    private final MockMvc mvc;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    void shouldReturnBadRequestAndErrorWhenGetAllItemRequestsByUserIdWithIncorrectPageParameters() throws Exception {
        mvc.perform(get("/requests/all?from={from}&size={size}", -1, 10)
                        .header(HEADER_USER_ID, 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemRequestsByUserId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));

        mvc.perform(get("/requests/all?from={from}&size={size}", 0, 0)
                        .header(HEADER_USER_ID, 17)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getAllItemRequestsByUserId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));
    }
}