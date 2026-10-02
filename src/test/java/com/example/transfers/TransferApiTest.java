package com.example.transfers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TransferApiTest {

    @Autowired
    MockMvc mvc;

    private String body(UUID from, UUID to, String amount) {
        return "{\"fromAccountId\":\"" + from + "\",\"toAccountId\":\"" + to
                + "\",\"amount\":" + amount + ",\"currency\":\"EUR\"}";
    }

    @Test
    void missingIdempotencyKeyIsRejected() throws Exception {
        mvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), UUID.randomUUID(), "10.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidAmountIsRejected() throws Exception {
        mvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", "api-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), UUID.randomUUID(), "-5")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        mvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", "api-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), UUID.randomUUID(), "10.00")))
                .andExpect(status().isNotFound());
    }
}
