package ru.practicum.shareit.booking;

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
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @MockBean
    private final BookingClient bookingClient;

    private final MockMvc mvc;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndBookerIdWithFromLessThanZero() throws Exception {
        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", -1, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndBookerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndBookerIdWithSizeLessThanOrEqualToZero() throws Exception {
        mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", 0, 0)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndBookerId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndOwnerIdWithFromLessThanZero() throws Exception {
        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", -1, 10)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndOwnerId.from")))
                .andExpect(jsonPath("$[0].message", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldReturnBadRequestAndErrorWhenGetBookingsByStateAndOwnerIdWithSizeLessThanOrEqualToZero() throws Exception {
        mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", 0, 0)
                        .header(HEADER_USER_ID, 23)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$[0].fieldName", is("getBookingsByStateAndOwnerId.size")))
                .andExpect(jsonPath("$[0].message", is("must be greater than 0")));
    }
}