package com.evmoto.fee.controller;

import com.evmoto.fee.WaitingFeeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WaitingFeeApplication.class)
@AutoConfigureMockMvc
class FeePreviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void previewReturnsDetailedBreakdown() throws Exception {
        String body = """
                {
                  "arrivedAt": "2026-08-10T09:00:00+07:00",
                  "endedAt": "2026-08-10T09:21:40+07:00",
                  "endReason": "CANCELLED_BY_CUSTOMER",
                  "pickupPoint": {"lat": -6.21462, "lng": 106.84513},
                  "driverPings": [
                    {"at": "2026-08-10T09:00:00+07:00", "lat": -6.21462, "lng": 106.84513},
                    {"at": "2026-08-10T09:08:00+07:00", "lat": -6.21980, "lng": 106.85110},
                    {"at": "2026-08-10T09:14:00+07:00", "lat": -6.21470, "lng": 106.84520}
                  ]
                }
                """;

        mockMvc.perform(post("/v1/orders/ORD-88213/fee-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-88213"))
                .andExpect(jsonPath("$.breakdown.pausedSeconds").value(360))
                .andExpect(jsonPath("$.breakdown.billableMinutes").value(11))
                .andExpect(jsonPath("$.waitingFee").value(5500))
                .andExpect(jsonPath("$.cancellationFee").value(10500))
                .andExpect(jsonPath("$.totalFee").value(10500));
    }

    @Test
    void invalidEndTimeReturnsBadRequest() throws Exception {
        String body = """
                {
                  "arrivedAt": "2026-08-10T09:10:00+07:00",
                  "endedAt": "2026-08-10T09:00:00+07:00",
                  "endReason": "TRIP_STARTED",
                  "pickupPoint": {"lat": -6.21462, "lng": 106.84513},
                  "driverPings": []
                }
                """;

        mockMvc.perform(post("/v1/orders/ORD-1/fee-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endedAt must not be earlier than arrivedAt"));
    }
}
